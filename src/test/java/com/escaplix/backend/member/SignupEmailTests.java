package com.escaplix.backend.member;

import com.escaplix.backend.common.mail.Mailer;
import com.escaplix.backend.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 가입 이메일 인증: 인증번호 받기 → 확인 → 가입.
 * 테스트에는 SMTP 설정이 없어서 Mailer 가 개발 모드 (메일 대신 기억) → 그 본문에서 인증번호를 꺼낸다.
 * 가입이 커밋된 뒤에 인증 기록을 지우는지도 봐야 해서 @Transactional 을 걸지 않고, 이메일·아이디는 tag 로 겹치지 않게 한다.
 */
@SpringBootTest
class SignupEmailTests {

    @Autowired WebApplicationContext context;
    @Autowired Mailer mailer;
    @Autowired MemberRepository memberRepository;

    MockMvc mvc;
    String tag;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(context).build();
        tag = String.valueOf(ThreadLocalRandom.current().nextInt(10000, 99999));
    }

    @Test
    void 인증번호를_받고_맞히면_가입되고_이메일은_소문자로_저장() throws Exception {
        String email = "mail" + tag + "@test.com";
        sendCode("  MAIL" + tag + "@Test.COM ").andExpect(status().isNoContent());   // 대소문자·공백은 정리해서 같은 주소
        String code = codeIn(email);
        assertThat(mailer.lastTextTo(email)).contains("5분 안에");

        verify(email, code).andExpect(status().isNoContent());
        verify(email, code).andExpect(status().isNoContent());   // 확인 버튼 두 번도 괜찮음

        signup("em" + tag, "메일" + tag, "Mail" + tag + "@test.com").andExpect(status().isCreated());
        assertThat(memberRepository.findByLoginId("em" + tag).orElseThrow().getEmail()).isEqualTo(email);

        // 같은 인증으로 또 가입 못 함 (이미 가입된 이메일 → 409. 인증 기록도 지워짐)
        signup("em2" + tag, "메일2" + tag, email).andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("이미 가입된 이메일입니다."));
        // 이미 가입된 이메일은 인증번호도 안 보냄
        sendCode(email).andExpect(status().isConflict());
    }

    @Test
    void 인증을_안_했으면_가입은_400() throws Exception {
        signup("nv" + tag, "미인증" + tag, "nv" + tag + "@test.com").andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("이메일 인증을 먼저 해 주세요."));
        // 인증번호만 받고 확인 안 한 경우도
        sendCode("nv" + tag + "@test.com").andExpect(status().isNoContent());
        signup("nv" + tag, "미인증" + tag, "nv" + tag + "@test.com").andExpect(status().isBadRequest());
        assertThat(memberRepository.existsByLoginId("nv" + tag)).isFalse();
    }

    @Test
    void 틀리면_횟수를_알려주고_5번_틀리면_다시_받아야() throws Exception {
        String email = "wr" + tag + "@test.com";
        sendCode(email).andExpect(status().isNoContent());
        String code = codeIn(email);
        String wrong = code.equals("000000") ? "111111" : "000000";

        verify(email, wrong).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("인증번호가 맞지 않아요. (1/5)"));
        for (int i = 2; i <= 4; i++) verify(email, wrong).andExpect(status().isBadRequest());
        verify(email, wrong).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("인증번호를 5번 틀렸어요. 인증번호를 다시 받아 주세요."));
        // 이제 맞는 번호도 안 됨
        verify(email, code).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("인증번호가 만료됐어요. 인증번호를 다시 받아 주세요."));
        // 무효가 됐으니 60초 안 기다리고 바로 다시 받을 수 있음
        sendCode(email).andExpect(status().isNoContent());
    }

    @Test
    void 바로_다시_받으면_429() throws Exception {
        String email = "rs" + tag + "@test.com";
        sendCode(email).andExpect(status().isNoContent());
        sendCode(email).andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.endsWith("초 뒤에 다시 받을 수 있어요.")));
    }

    @Test
    void 이메일_형식이_틀리면_400() throws Exception {
        sendCode("no-at-sign").andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("이메일 형식이 올바르지 않습니다."));
        sendCode("a@b").andExpect(status().isBadRequest())   // @Email 은 통과하지만 점 없는 도메인은 막음
                .andExpect(jsonPath("$.message").value("이메일 형식이 올바르지 않습니다."));
        signup("nf" + tag, "형식" + tag, "").andExpect(status().isBadRequest());
    }

    @Test
    void 사장_가입도_이메일_인증이_필요() throws Exception {
        String email = "ow" + tag + "@test.com";
        var owner = multipart("/api/members/owner")
                .file(new org.springframework.mock.web.MockMultipartFile("bizFile", "biz.png", "image/png", MemberProfileTests.PNG))
                .param("loginId", "ow" + tag).param("password", "1234").param("nickname", "사장" + tag)
                .param("bizNo", "123-45-" + tag).param("email", email);
        mvc.perform(owner).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("이메일 인증을 먼저 해 주세요."));

        sendCode(email).andExpect(status().isNoContent());
        verify(email, codeIn(email)).andExpect(status().isNoContent());
        mvc.perform(owner).andExpect(status().isCreated());
        var saved = memberRepository.findByLoginId("ow" + tag).orElseThrow();
        assertThat(saved.getEmail()).isEqualTo(email);
        // 테스트가 만든 등록증 파일 정리
        java.nio.file.Files.deleteIfExists(java.nio.file.Path.of("build/test-private").resolve(saved.getBizFilePath()));
    }

    // ---------- 도우미 ----------

    private ResultActions sendCode(String email) throws Exception {
        return mvc.perform(post("/api/members/email-code").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"" + email + "\"}"));
    }

    private ResultActions verify(String email, String code) throws Exception {
        return mvc.perform(post("/api/members/email-code/verify").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"" + email + "\",\"code\":\"" + code + "\"}"));
    }

    private ResultActions signup(String loginId, String nickname, String email) throws Exception {
        return mvc.perform(post("/api/members/user").contentType(MediaType.APPLICATION_JSON)
                .content("{\"loginId\":\"" + loginId + "\",\"password\":\"1234\",\"nickname\":\"" + nickname
                        + "\",\"email\":\"" + email + "\",\"genreIds\":[]}"));
    }

    /** 개발 모드 메일 본문에서 6자리 인증번호 꺼내기 */
    private String codeIn(String email) {
        String text = mailer.lastTextTo(email);
        assertThat(text).as("메일 본문").isNotNull();
        Matcher m = Pattern.compile("인증번호: (\\d{6})").matcher(text);
        assertThat(m.find()).isTrue();
        return m.group(1);
    }
}

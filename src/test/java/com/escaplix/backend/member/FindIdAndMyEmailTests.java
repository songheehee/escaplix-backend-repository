package com.escaplix.backend.member;

import com.escaplix.backend.common.auth.JwtProvider;
import com.escaplix.backend.common.mail.Mailer;
import com.escaplix.backend.entity.Member;
import com.escaplix.backend.entity.Role;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 아이디 찾기 (공개) · 내 이메일 등록·변경 (로그인).
 * 개발 모드 Mailer 가 본문을 기억해 두므로 거기서 아이디·인증번호를 꺼낸다. tag 로 겹치지 않게 (커밋 뒤 동작도 봐야 해서 @Transactional 없음)
 */
@SpringBootTest
class FindIdAndMyEmailTests {

    @Autowired WebApplicationContext context;
    @Autowired Mailer mailer;
    @Autowired MemberRepository memberRepository;
    @Autowired JwtProvider jwtProvider;

    MockMvc mvc;
    String tag;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(context).build();
        tag = String.valueOf(ThreadLocalRandom.current().nextInt(10000, 99999));
    }

    // ---------- 아이디 찾기 ----------

    @Test
    void 가입한_이메일이면_아이디를_메일로_보낸다() throws Exception {
        String email = "fi" + tag + "@test.com";
        member("fi" + tag, "찾기" + tag, email);

        findId("  FI" + tag + "@Test.com ").andExpect(status().isNoContent());   // 대소문자·공백 상관없음
        assertThat(mailer.lastTextTo(email)).contains("아이디: fi" + tag);

        // 60초 안에 또 → 429
        findId(email).andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.endsWith("초 뒤에 다시 시도해 주세요.")));
    }

    @Test
    void 가입하지_않은_이메일도_응답은_같고_메일은_없다() throws Exception {
        String email = "none" + tag + "@test.com";
        findId(email).andExpect(status().isNoContent());
        assertThat(mailer.lastTextTo(email)).isNull();
        findId(email).andExpect(status().isTooManyRequests());   // 60초 규칙도 똑같이 (429 로 회원 여부를 알 수 없게)
        findId("bad-email").andExpect(status().isBadRequest());
    }

    // ---------- 내 이메일 등록·변경 ----------

    @Test
    void 이메일이_없던_회원이_인증하고_등록한다() throws Exception {
        Member m = member("me" + tag, "등록" + tag, null);
        String token = token(m);
        String email = "me" + tag + "@test.com";

        sendMyCode(token, email).andExpect(status().isNoContent());
        String code = codeIn(mailer.lastTextTo(email));
        String wrong = code.equals("000000") ? "111111" : "000000";
        change(token, email, wrong).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("인증번호가 맞지 않아요. (1/5)"));
        change(token, email, code).andExpect(status().isNoContent());

        assertThat(memberRepository.findById(m.getMemberId()).orElseThrow().getEmail()).isEqualTo(email);
        mvc.perform(get("/api/members/me").header("Authorization", token))
                .andExpect(jsonPath("$.email").value(email));
        // 같은 인증으로 또 바꾸지 못함 (인증 기록 삭제)
        change(token, email, code).andExpect(status().isBadRequest());
    }

    @Test
    void 지금_이메일이거나_남이_쓰는_이메일이면_막는다() throws Exception {
        Member a = member("ma" + tag, "에이" + tag, "ma" + tag + "@test.com");
        member("mb" + tag, "비" + tag, "mb" + tag + "@test.com");
        String token = token(a);

        sendMyCode(token, "MA" + tag + "@test.com").andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("지금 쓰고 있는 이메일이에요."));
        sendMyCode(token, "mb" + tag + "@test.com").andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("이미 가입된 이메일입니다."));
    }

    @Test
    void 로그인_안_하면_401() throws Exception {
        mvc.perform(post("/api/members/me/email-code").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"x" + tag + "@test.com\"}")).andExpect(status().isUnauthorized());
    }

    // ---------- 도우미 ----------

    private Member member(String loginId, String nickname, String email) {
        Member m = new Member();
        m.setLoginId(loginId);
        m.setPassword("x");
        m.setNickname(nickname);
        m.setEmail(email);
        m.setRole(Role.USER);
        return memberRepository.save(m);
    }

    private String token(Member m) {
        return "Bearer " + jwtProvider.create(m.getMemberId(), m.getNickname(), m.getRole());
    }

    private ResultActions findId(String email) throws Exception {
        return mvc.perform(post("/api/auth/find-id").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"" + email + "\"}"));
    }

    private ResultActions sendMyCode(String token, String email) throws Exception {
        return mvc.perform(post("/api/members/me/email-code").header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON).content("{\"email\":\"" + email + "\"}"));
    }

    private ResultActions change(String token, String email, String code) throws Exception {
        return mvc.perform(post("/api/members/me/email").header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON).content("{\"email\":\"" + email + "\",\"code\":\"" + code + "\"}"));
    }

    private static String codeIn(String text) {
        assertThat(text).as("메일 본문").isNotNull();
        Matcher m = Pattern.compile("인증번호: (\\d{6})").matcher(text);
        assertThat(m.find()).isTrue();
        return m.group(1);
    }
}

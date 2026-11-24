package com.escaplix.backend.member;

import com.escaplix.backend.common.mail.Mailer;
import com.escaplix.backend.entity.Member;
import com.escaplix.backend.entity.Role;
import com.escaplix.backend.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 비밀번호 재설정: 아이디 + 이메일 → 인증번호 → 새 비밀번호.
 * 개발 모드 Mailer 가 본문을 기억해 두므로 거기서 인증번호를 꺼낸다.
 * 인증 기록이 커밋 뒤에 지워지는지도 봐야 해서 @Transactional 을 걸지 않고 tag 로 겹치지 않게 한다.
 */
@SpringBootTest
class PasswordResetTests {

    @Autowired WebApplicationContext context;
    @Autowired Mailer mailer;
    @Autowired MemberRepository memberRepository;
    @Autowired PasswordEncoder passwordEncoder;

    MockMvc mvc;
    String tag, loginId, email;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(context).build();
        tag = String.valueOf(ThreadLocalRandom.current().nextInt(10000, 99999));
        loginId = "pr" + tag;
        email = "pr" + tag + "@test.com";
        member(loginId, "재설정" + tag, email);
    }

    @Test
    void 아이디와_이메일이_맞으면_인증하고_새_비밀번호로_바꾼다() throws Exception {
        sendCode(loginId, "  PR" + tag + "@Test.com ").andExpect(status().isNoContent());   // 이메일 대소문자·공백은 상관없음
        String mail = mailer.lastTextTo(email);
        assertThat(mail).contains("아이디: " + loginId);
        String code = codeIn(mail);

        String wrong = code.equals("000000") ? "111111" : "000000";
        verify(loginId, email, wrong).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("인증번호가 맞지 않아요. (1/5)"));
        verify(loginId, email, code).andExpect(status().isNoContent());

        reset(loginId, email, "12").andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("비밀번호는 4~20자로 입력해 주세요."));
        reset(loginId, email, "5678").andExpect(status().isNoContent());

        login(loginId, "5678").andExpect(status().isOk());
        login(loginId, "1234").andExpect(status().isUnauthorized());

        // 같은 인증으로 또 바꾸지 못함
        reset(loginId, email, "9999").andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("이메일 인증을 먼저 해 주세요."));
    }

    @Test
    void 맞지_않아도_응답은_같고_메일은_안_보낸다() throws Exception {
        String other = "other" + tag + "@test.com";
        // 이메일이 다름 · 없는 아이디 → 둘 다 204 (회원이 맞는지 알려 주지 않음)
        sendCode(loginId, other).andExpect(status().isNoContent());
        sendCode("nobody" + tag, email).andExpect(status().isNoContent());
        assertThat(mailer.lastTextTo(other)).isNull();
        assertThat(mailer.lastTextTo(email)).as("맞는 쌍이 아니면 진짜 이메일로도 안 보냄").isNull();

        // 60초 규칙도 똑같이 걸림 (429 로 맞는지 알 수 없게)
        sendCode(loginId, other).andExpect(status().isTooManyRequests());
        sendCode(loginId, email).andExpect(status().isNoContent());
        sendCode(loginId, email).andExpect(status().isTooManyRequests());

        // 메일을 못 받았으니 인증번호는 그냥 "맞지 않아요"
        verify(loginId, other, "123456").andExpect(status().isBadRequest());
    }

    @Test
    void 인증_없이_바꾸려면_400() throws Exception {
        reset(loginId, email, "5678").andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("이메일 인증을 먼저 해 주세요."));
        login(loginId, "1234").andExpect(status().isOk());
    }

    @Test
    void 이메일을_안_넣은_예전_회원은_메일이_가지_않는다() throws Exception {
        String old = "old" + tag;
        member(old, "예전" + tag, null);
        sendCode(old, "old" + tag + "@test.com").andExpect(status().isNoContent());
        assertThat(mailer.lastTextTo("old" + tag + "@test.com")).isNull();
    }

    // ---------- 도우미 ----------

    private void member(String loginId, String nickname, String email) {
        Member m = new Member();
        m.setLoginId(loginId);
        m.setPassword(passwordEncoder.encode("1234"));
        m.setNickname(nickname);
        m.setEmail(email);
        m.setRole(Role.USER);
        memberRepository.save(m);
    }

    private ResultActions sendCode(String loginId, String email) throws Exception {
        return mvc.perform(post("/api/auth/password-reset/code").contentType(MediaType.APPLICATION_JSON)
                .content("{\"loginId\":\"" + loginId + "\",\"email\":\"" + email + "\"}"));
    }

    private ResultActions verify(String loginId, String email, String code) throws Exception {
        return mvc.perform(post("/api/auth/password-reset/verify").contentType(MediaType.APPLICATION_JSON)
                .content("{\"loginId\":\"" + loginId + "\",\"email\":\"" + email + "\",\"code\":\"" + code + "\"}"));
    }

    private ResultActions reset(String loginId, String email, String pw) throws Exception {
        return mvc.perform(post("/api/auth/password-reset").contentType(MediaType.APPLICATION_JSON)
                .content("{\"loginId\":\"" + loginId + "\",\"email\":\"" + email + "\",\"newPassword\":\"" + pw + "\"}"));
    }

    private ResultActions login(String loginId, String pw) throws Exception {
        return mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"loginId\":\"" + loginId + "\",\"password\":\"" + pw + "\"}"));
    }

    private static String codeIn(String text) {
        assertThat(text).as("메일 본문").isNotNull();
        Matcher m = Pattern.compile("인증번호: (\\d{6})").matcher(text);
        assertThat(m.find()).isTrue();
        return m.group(1);
    }
}

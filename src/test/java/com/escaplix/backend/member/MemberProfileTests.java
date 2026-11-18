package com.escaplix.backend.member;

import com.escaplix.backend.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** A-2 사장 가입 · A-7 내 정보 · A-8 닉네임 · A-9 비밀번호. 각 테스트는 끝나면 DB 가 되돌려진다 */
@SpringBootTest
@Transactional
class MemberProfileTests {

    /** PNG 파일 앞부분(시그니처). FileStorage 는 확장자가 아니라 이걸로 형식을 본다 */
    static final byte[] PNG = {(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A, 0, 0, 0, 0};

    @Autowired WebApplicationContext context;
    @Autowired MemberRepository memberRepository;
    @Autowired PasswordEncoder passwordEncoder;

    MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    // ---------- 도우미 ----------

    private void signupUser(String loginId, String nickname) throws Exception {
        mvc.perform(post("/api/members/user").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"loginId\":\"" + loginId + "\",\"password\":\"1234\",\"nickname\":\"" + nickname + "\"}"))
                .andExpect(status().isCreated());
    }

    private String login(String loginId, String password) throws Exception {
        String body = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"loginId\":\"" + loginId + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return "Bearer " + body.replaceAll(".*\"token\":\"([^\"]+)\".*", "$1");
    }

    private MockMultipartHttpServletRequestBuilder ownerSignup(String loginId, String nickname, String bizNo, byte[] file) {
        var req = multipart("/api/members/owner")
                .param("loginId", loginId).param("password", "1234")
                .param("nickname", nickname).param("bizNo", bizNo);
        if (file != null) {
            req.file(new MockMultipartFile("bizFile", "biz.png", "image/png", file));
        }
        return req;
    }

    // ---------- A-2 사장 가입 ----------

    @Test
    void 사장_가입하면_201_파일은_비공개_폴더에_저장() throws Exception {
        mvc.perform(ownerSignup("boss1", "사장님1", "777-77-77777", PNG))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.memberId").value(notNullValue()));

        var owner = memberRepository.findByLoginId("boss1").orElseThrow();
        assertThat(owner.getRole().name()).isEqualTo("OWNER");
        assertThat(owner.getBizNo()).isEqualTo("777-77-77777");
        assertThat(owner.getBizFilePath()).startsWith("biz/").endsWith(".png");
        Path saved = Path.of("build/test-private").resolve(owner.getBizFilePath());
        assertThat(Files.exists(saved)).isTrue();
        Files.deleteIfExists(saved);   // 테스트가 남긴 파일 정리 (DB 는 @Transactional 로 되돌려짐)
    }

    @Test
    void 사업자번호가_겹치면_409이고_파일도_만들지_않음() throws Exception {
        mvc.perform(ownerSignup("boss2", "사장님2", "888-88-88888", PNG)).andExpect(status().isCreated());
        Path dir = Path.of("build/test-private/biz");
        long before = Files.list(dir).count();

        mvc.perform(ownerSignup("boss3", "사장님3", "888-88-88888", PNG))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("이미 등록된 사업자등록번호입니다."));
        assertThat(Files.list(dir).count()).isEqualTo(before);

        Files.deleteIfExists(Path.of("build/test-private")
                .resolve(memberRepository.findByLoginId("boss2").orElseThrow().getBizFilePath()));
    }

    @Test
    void 사장_가입_형식_오류는_400() throws Exception {
        mvc.perform(ownerSignup("boss4", "사장님4", "1234567890", PNG))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("사업자등록번호 형식이 올바르지 않습니다."));
        mvc.perform(ownerSignup("boss5", "사장님5", "555-55-55555", null))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("파일을 첨부해 주세요."));
        mvc.perform(ownerSignup("boss6", "사장님6", "666-66-66666", "그냥 글자".getBytes()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("이미지 파일(jpg, png, webp)만 올릴 수 있습니다."));
        assertThat(memberRepository.existsByLoginId("boss5")).isFalse();
    }

    // ---------- A-7 내 정보 ----------

    @Test
    void 내_정보_손님은_bizNo_없음_사장은_있음() throws Exception {
        signupUser("guest1", "손님1");
        mvc.perform(get("/api/members/me").header("Authorization", login("guest1", "1234")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.loginId").value("guest1"))
                .andExpect(jsonPath("$.nickname").value("손님1"))
                .andExpect(jsonPath("$.role").value("USER"))
                .andExpect(jsonPath("$.bizNo").value(nullValue()))
                // createdAt 은 DB DEFAULT SYSDATE 로 채워짐. 테스트용 H2 테이블엔 DEFAULT 가 없어서 여기선 확인 안 함 (Oracle 에서 확인)
                .andExpect(jsonPath("$.password").doesNotExist());

        mvc.perform(ownerSignup("boss7", "사장님7", "999-99-99999", PNG)).andExpect(status().isCreated());
        mvc.perform(get("/api/members/me").header("Authorization", login("boss7", "1234")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("OWNER"))
                .andExpect(jsonPath("$.bizNo").value("999-99-99999"));
        Files.deleteIfExists(Path.of("build/test-private")
                .resolve(memberRepository.findByLoginId("boss7").orElseThrow().getBizFilePath()));
    }

    // ---------- A-8 닉네임 ----------

    @Test
    void 닉네임을_바꾸면_새_토큰이_나오고_그_토큰에도_새_닉네임() throws Exception {
        signupUser("nick1", "옛닉네임");
        String body = mvc.perform(put("/api/members/me").header("Authorization", login("nick1", "1234"))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"nickname\":\"새닉네임\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nickname").value("새닉네임"))
                .andExpect(jsonPath("$.token").value(notNullValue()))
                .andReturn().getResponse().getContentAsString();
        String newToken = "Bearer " + body.replaceAll(".*\"token\":\"([^\"]+)\".*", "$1");

        assertThat(memberRepository.findByLoginId("nick1").orElseThrow().getNickname()).isEqualTo("새닉네임");
        mvc.perform(get("/api/members/me").header("Authorization", newToken))
                .andExpect(jsonPath("$.nickname").value("새닉네임"));
    }

    @Test
    void 남이_쓰는_닉네임이면_409_내_닉네임_그대로면_200() throws Exception {
        signupUser("nick2", "먼저쓴닉");
        signupUser("nick3", "내닉네임");
        String token = login("nick3", "1234");
        mvc.perform(put("/api/members/me").header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"nickname\":\"먼저쓴닉\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("이미 사용 중인 닉네임입니다."));
        mvc.perform(put("/api/members/me").header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"nickname\":\"내닉네임\"}"))
                .andExpect(status().isOk());
        mvc.perform(put("/api/members/me").header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"nickname\":\"가\"}"))
                .andExpect(status().isBadRequest());
    }

    // ---------- A-9 비밀번호 ----------

    @Test
    void 비밀번호를_바꾸면_204_새_비밀번호로만_로그인() throws Exception {
        signupUser("pwuser1", "비번주인");
        mvc.perform(put("/api/members/me/password").header("Authorization", login("pwuser1", "1234"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"currentPassword\":\"1234\",\"newPassword\":\"5678\"}"))
                .andExpect(status().isNoContent());

        var member = memberRepository.findByLoginId("pwuser1").orElseThrow();
        assertThat(passwordEncoder.matches("5678", member.getPassword())).isTrue();
        login("pwuser1", "5678");
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"loginId\":\"pwuser1\",\"password\":\"1234\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void 현재_비밀번호가_틀리거나_같은_비밀번호면_401이_아니라_400() throws Exception {
        signupUser("pwuser2", "비번주인2");
        String token = login("pwuser2", "1234");
        mvc.perform(put("/api/members/me/password").header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"currentPassword\":\"0000\",\"newPassword\":\"5678\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("현재 비밀번호가 맞지 않습니다."));
        mvc.perform(put("/api/members/me/password").header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"currentPassword\":\"1234\",\"newPassword\":\"1234\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("지금 비밀번호와 다른 비밀번호를 입력해 주세요."));
    }

    @Test
    void 로그인_안_하면_내_정보_닉네임_비밀번호_모두_401() throws Exception {
        mvc.perform(get("/api/members/me")).andExpect(status().isUnauthorized());
        mvc.perform(put("/api/members/me").contentType(MediaType.APPLICATION_JSON)
                .content("{\"nickname\":\"아무개\"}")).andExpect(status().isUnauthorized());
        mvc.perform(put("/api/members/me/password").contentType(MediaType.APPLICATION_JSON)
                .content("{\"currentPassword\":\"1\",\"newPassword\":\"5678\"}")).andExpect(status().isUnauthorized());
    }
}

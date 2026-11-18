package com.escaplix.backend.member;

import com.escaplix.backend.entity.Genre;
import com.escaplix.backend.repository.GenreRepository;
import com.escaplix.backend.repository.MemberGenreRepository;
import com.escaplix.backend.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * A-1 손님 가입 · A-3~5 중복 확인 · A-6 로그인.
 * H2 메모리 DB. 각 테스트는 @Transactional 로 끝나면 되돌려진다.
 */
@SpringBootTest
@Transactional
class MemberSignupLoginTests {

    @Autowired WebApplicationContext context;
    @Autowired GenreRepository genreRepository;
    @Autowired MemberRepository memberRepository;
    @Autowired MemberGenreRepository memberGenreRepository;

    MockMvc mvc;
    Long horrorId;
    Long mysteryId;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(context).build();
        horrorId = saveGenre("공포");
        mysteryId = saveGenre("추리");
    }

    private Long saveGenre(String name) {
        Genre g = new Genre();
        g.setName(name);
        return genreRepository.save(g).getGenreId();
    }

    private void signup(String loginId, String nickname, String genreIdsJson) throws Exception {
        mvc.perform(post("/api/members/user").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"loginId\":\"" + loginId + "\",\"password\":\"1234\",\"nickname\":\"" + nickname
                                + "\",\"genreIds\":" + genreIdsJson + "}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.memberId").value(notNullValue()));
    }

    // ---------- A-1 ----------

    @Test
    void 가입하면_201과_회원번호_비밀번호는_BCrypt_장르도_저장() throws Exception {
        signup("newbie1", "새회원", "[" + horrorId + "," + mysteryId + "," + horrorId + "]");

        var member = memberRepository.findByLoginId("newbie1").orElseThrow();
        assertThat(member.getRole().name()).isEqualTo("USER");
        assertThat(member.getPassword()).startsWith("$2a$").isNotEqualTo("1234");
        // 같은 장르가 두 번 와도 한 줄만
        assertThat(memberGenreRepository.findByMember_MemberId(member.getMemberId())).hasSize(2);
    }

    @Test
    void 장르가_없거나_null이어도_가입된다() throws Exception {
        signup("newbie2", "장르없음", "[]");
        mvc.perform(post("/api/members/user").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"loginId\":\"newbie3\",\"password\":\"1234\",\"nickname\":\"장르널\"}"))
                .andExpect(status().isCreated());
    }

    @Test
    void 아이디_닉네임이_겹치면_409() throws Exception {
        signup("dupuser", "겹침", "[]");
        mvc.perform(post("/api/members/user").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"loginId\":\"dupuser\",\"password\":\"1234\",\"nickname\":\"다른닉\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("이미 사용 중인 아이디입니다."));
        mvc.perform(post("/api/members/user").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"loginId\":\"other1\",\"password\":\"1234\",\"nickname\":\"겹침\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("이미 사용 중인 닉네임입니다."));
    }

    @Test
    void 없는_장르면_400이고_회원도_저장되지_않음() throws Exception {
        mvc.perform(post("/api/members/user").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"loginId\":\"nogenre\",\"password\":\"1234\",\"nickname\":\"없는장르\",\"genreIds\":[999999]}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("없는 장르입니다."));
        assertThat(memberRepository.existsByLoginId("nogenre")).isFalse();
    }

    // ---------- A-3 ~ A-5 ----------

    @Test
    void 중복_확인은_있으면_false_없으면_true() throws Exception {
        signup("taken1", "있는닉", "[]");
        mvc.perform(get("/api/members/check-login-id").param("loginId", "taken1"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.available").value(false));
        mvc.perform(get("/api/members/check-login-id").param("loginId", "free1"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.available").value(true));
        mvc.perform(get("/api/members/check-nickname").param("nickname", "있는닉"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.available").value(false));
        mvc.perform(get("/api/members/check-biz-no").param("bizNo", "123-45-67890"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.available").value(true));
    }

    @Test
    void 중복_확인도_형식이_틀리면_400() throws Exception {
        mvc.perform(get("/api/members/check-login-id").param("loginId", "AB"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("아이디는 영문 소문자와 숫자 4~20자로 입력해 주세요."));
        mvc.perform(get("/api/members/check-nickname").param("nickname", "가"))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/api/members/check-biz-no").param("bizNo", "1234567890"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("사업자등록번호 형식이 올바르지 않습니다."));
    }

    // ---------- A-6 ----------

    @Test
    void 로그인하면_토큰이_나오고_그_토큰으로_로그인_API를_통과() throws Exception {
        signup("login1", "로그인왕", "[]");
        String body = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"loginId\":\"login1\",\"password\":\"1234\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nickname").value("로그인왕"))
                .andExpect(jsonPath("$.role").value("USER"))
                .andExpect(jsonPath("$.token").value(notNullValue()))
                .andReturn().getResponse().getContentAsString();
        String token = body.replaceAll(".*\"token\":\"([^\"]+)\".*", "$1");

        // A-7 은 아직 501 이지만, 401 이 아니라는 건 토큰이 통과했다는 뜻
        mvc.perform(get("/api/members/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isNotImplemented());
    }

    @Test
    void 아이디가_없어도_비밀번호가_틀려도_같은_401() throws Exception {
        signup("login2", "틀림테스트", "[]");
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"loginId\":\"login2\",\"password\":\"9999\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("아이디 또는 비밀번호가 틀렸습니다."));
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"loginId\":\"nobody\",\"password\":\"1234\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("아이디 또는 비밀번호가 틀렸습니다."));
    }
}

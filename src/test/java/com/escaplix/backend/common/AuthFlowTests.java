package com.escaplix.backend.common;

import com.escaplix.backend.common.auth.JwtProvider;
import com.escaplix.backend.entity.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 공통 뼈대(토큰·권한·에러 형식)가 약속대로 동작하는지 확인한다.
 * 각 API 를 구현하면 501 이 아니라 실제 결과가 나오므로, 그때는 이 테스트의 "501" 부분을 바꾼다.
 */
@SpringBootTest
class AuthFlowTests {

    @Autowired
    WebApplicationContext context;

    @Autowired
    JwtProvider jwtProvider;

    MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    private String bearer(Role role) {
        return "Bearer " + jwtProvider.create(1L, "테스트", role);
    }

    @Test
    void 로그인이_필요한_API에_토큰이_없으면_401() throws Exception {
        mvc.perform(get("/api/members/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("로그인이 필요합니다."));
    }

    @Test
    void 틀린_토큰이면_401() throws Exception {
        mvc.perform(get("/api/members/me").header("Authorization", "Bearer abc.def.ghi"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void 역할이_다르면_403() throws Exception {
        mvc.perform(get("/api/admin/summary").header("Authorization", bearer(Role.USER)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("권한이 없습니다."));
    }

    @Test
    void 권한이_맞으면_서비스까지_가고_아직은_501() throws Exception {
        mvc.perform(get("/api/admin/summary").header("Authorization", bearer(Role.ADMIN)))
                .andExpect(status().isNotImplemented())
                .andExpect(jsonPath("$.message").value("아직 구현 전입니다: C-12 관리자 홈 숫자 요약"));
    }

    @Test
    void 공개_API는_틀린_토큰이어도_비회원으로_통과() throws Exception {
        mvc.perform(get("/api/genres").header("Authorization", "Bearer 만료된토큰"))
                .andExpect(status().isNotImplemented());
    }

    @Test
    void 입력값이_틀리면_400과_한국어_메시지() throws Exception {
        mvc.perform(post("/api/members/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"loginId\":\"AB\",\"password\":\"1234\",\"nickname\":\"새회원\",\"genreIds\":[]}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("아이디는 영문 소문자와 숫자 4~20자로 입력해 주세요."));
    }
}

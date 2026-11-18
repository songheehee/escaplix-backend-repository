package com.escaplix.backend.common;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** 주소·방식이 틀린 요청은 500 이 아니라 404·405 + {"message"} */
@SpringBootTest
class NotFoundTests {

    @Autowired
    WebApplicationContext context;

    MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    void 없는_API_주소면_404() throws Exception {
        mvc.perform(get("/api/no-such-api"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("없는 주소입니다."));
    }

    @Test
    void 루트나_없는_파일도_404() throws Exception {
        mvc.perform(get("/"))
                .andExpect(status().isNotFound());
        mvc.perform(get("/uploads/posters/none.jpg"))
                .andExpect(status().isNotFound());
    }

    @Test
    void 주소는_있는데_방식이_틀리면_405() throws Exception {
        mvc.perform(delete("/api/auth/login"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.message").value("지원하지 않는 요청 방식입니다."));
    }
}

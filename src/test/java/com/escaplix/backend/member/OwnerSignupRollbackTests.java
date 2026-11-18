package com.escaplix.backend.member;

import com.escaplix.backend.repository.MemberRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * A-2: 파일은 저장됐는데 DB 저장이 실패하면(동시 가입 등) 파일도 지워지는지.
 * 진짜 커밋·롤백이 일어나야 해서 @Transactional 을 붙이지 않는다.
 */
@SpringBootTest
class OwnerSignupRollbackTests {

    @Autowired WebApplicationContext context;

    /** saveAndFlush 만 실패하게 만든다 (나머지는 진짜 동작) */
    @MockitoSpyBean MemberRepository memberRepository;

    @Test
    void DB_저장이_실패하면_저장했던_사업자등록증_파일도_지운다() throws Exception {
        MockMvc mvc = MockMvcBuilders.webAppContextSetup(context).build();
        Path dir = Path.of("build/test-private/biz");
        Files.createDirectories(dir);
        long before = Files.list(dir).count();

        doThrow(new DataIntegrityViolationException("동시 가입 흉내")).when(memberRepository).saveAndFlush(any());

        mvc.perform(multipart("/api/members/owner")
                        .file(new MockMultipartFile("bizFile", "biz.png", "image/png", MemberProfileTests.PNG))
                        .param("loginId", "raceboss").param("password", "1234")
                        .param("nickname", "동시가입").param("bizNo", "321-32-32132"))
                .andExpect(status().isConflict());

        assertThat(Files.list(dir).count()).isEqualTo(before);
    }
}

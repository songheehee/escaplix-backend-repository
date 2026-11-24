package com.escaplix.backend.member;

import com.escaplix.backend.common.TestEmails;
import com.escaplix.backend.common.mail.EmailCodeStore;
import com.escaplix.backend.common.auth.JwtProvider;
import com.escaplix.backend.entity.*;
import com.escaplix.backend.repository.MemberRepository;
import com.escaplix.backend.repository.StoreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ThreadLocalRandom;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** A-10 회원 목록 · A-11 사업자등록증 이미지 (관리자). 각 테스트는 끝나면 DB 가 되돌려진다 */
@SpringBootTest
@Transactional
class AdminMemberTests {

    static final byte[] PNG = {(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A, 0, 0, 0, 0};

    @Autowired WebApplicationContext context;
    @Autowired JwtProvider jwtProvider;
    @Autowired MemberRepository memberRepository;
    @Autowired StoreRepository storeRepository;
    @Autowired EmailCodeStore emailCodes;

    MockMvc mvc;
    String tag;
    String admin;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(context).build();
        tag = String.valueOf(ThreadLocalRandom.current().nextInt(10000, 99999));
        admin = "Bearer " + jwtProvider.create(9999L, "관리자", Role.ADMIN);
    }

    // ---------- A-10 회원 목록 ----------

    @Test
    void A10_검색어는_아이디_닉네임_일부_사장은_사업자번호와_매장_수() throws Exception {
        Member user = save("guest" + tag, "손님" + tag, Role.USER, null);
        Member boss = save("boss" + tag, "사장" + tag, Role.OWNER, "111-22-" + tag);
        Member newBoss = save("newboss" + tag, "새사장" + tag, Role.OWNER, "333-44-" + tag);
        saveStore(boss, StoreStatus.APPROVED, "1");
        saveStore(boss, StoreStatus.PENDING, "2");   // 상태와 상관없이 센다

        mvc.perform(get("/api/admin/members").param("keyword", " " + tag + " ").header("Authorization", admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(3))
                .andExpect(jsonPath("$.members[*].memberId",
                        contains(user.getMemberId().intValue(), boss.getMemberId().intValue(), newBoss.getMemberId().intValue())))
                .andExpect(jsonPath("$.members[0].role").value("USER"))
                .andExpect(jsonPath("$.members[0].bizNo").value(nullValue()))
                .andExpect(jsonPath("$.members[0].managedStoreCount").value(nullValue()))
                .andExpect(jsonPath("$.members[1].bizNo").value("111-22-" + tag))
                .andExpect(jsonPath("$.members[1].managedStoreCount").value(2))
                .andExpect(jsonPath("$.members[2].managedStoreCount").value(0))
                .andExpect(jsonPath("$.members[0].password").doesNotExist());

        // 역할 탭
        mvc.perform(get("/api/admin/members").param("keyword", tag).param("role", "OWNER").header("Authorization", admin))
                .andExpect(jsonPath("$.total").value(2))
                .andExpect(jsonPath("$.members[*].role", everyItem(is("OWNER"))));
        // 닉네임으로만 맞는 검색어
        mvc.perform(get("/api/admin/members").param("keyword", "새사장" + tag).header("Authorization", admin))
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.members[0].loginId").value("newboss" + tag));
        // % 는 "아무거나"가 아니라 글자 % 로 찾는다
        mvc.perform(get("/api/admin/members").param("keyword", "%").header("Authorization", admin))
                .andExpect(jsonPath("$.total").value(0));
    }

    @Test
    void A10_역할별_숫자는_검색_조건과_상관없이_전체() throws Exception {
        // 탈퇴한 회원은 세지 않는다 (다른 테스트가 탈퇴시킨 회원이 같은 H2 에 남아 있을 수 있음)
        save("guest" + tag, "손님" + tag, Role.USER, null);
        mvc.perform(get("/api/admin/members").param("keyword", "없는검색어" + tag).header("Authorization", admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(0))
                .andExpect(jsonPath("$.members", hasSize(0)))
                .andExpect(jsonPath("$.countByRole.USER").value((int) memberRepository.countByRoleAndWithdrawnAtIsNull(Role.USER)))
                .andExpect(jsonPath("$.countByRole.OWNER").value((int) memberRepository.countByRoleAndWithdrawnAtIsNull(Role.OWNER)))
                .andExpect(jsonPath("$.countByRole.ADMIN").value((int) memberRepository.countByRoleAndWithdrawnAtIsNull(Role.ADMIN)));
    }

    @Test
    void A10_A11_관리자가_아니면_403_로그인_안_하면_401() throws Exception {
        Member user = save("guest" + tag, "손님" + tag, Role.USER, null);
        String userToken = "Bearer " + jwtProvider.create(user.getMemberId(), user.getNickname(), Role.USER);
        String ownerToken = "Bearer " + jwtProvider.create(1L, "사장", Role.OWNER);
        for (String path : new String[]{"/api/admin/members", "/api/admin/members/1/biz-file"}) {
            mvc.perform(get(path).header("Authorization", userToken)).andExpect(status().isForbidden());
            mvc.perform(get(path).header("Authorization", ownerToken)).andExpect(status().isForbidden());
            mvc.perform(get(path)).andExpect(status().isUnauthorized());
        }
    }

    // ---------- A-11 사업자등록증 이미지 ----------

    @Test
    void A11_사장의_등록증은_이미지_그대로_내려준다() throws Exception {
        mvc.perform(multipart("/api/members/owner")
                        .file(new MockMultipartFile("bizFile", "biz.png", "image/png", PNG))
                        .param("loginId", "boss" + tag).param("password", "1234")
                        .param("nickname", "사장" + tag).param("bizNo", "555-66-" + tag)
                        .param("email", TestEmails.verified(emailCodes, "boss" + tag + "@test.com")))
                .andExpect(status().isCreated());
        Member boss = memberRepository.findByLoginId("boss" + tag).orElseThrow();
        Path saved = Path.of("build/test-private").resolve(boss.getBizFilePath());
        try {
            mvc.perform(get("/api/admin/members/" + boss.getMemberId() + "/biz-file").header("Authorization", admin))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType("image/png"))
                    .andExpect(content().bytes(PNG));
        } finally {
            Files.deleteIfExists(saved);   // 테스트가 남긴 파일 정리 (DB 는 @Transactional 로 되돌려짐)
        }
    }

    @Test
    void A11_사장이_아니거나_파일이_없으면_404() throws Exception {
        Member user = save("guest" + tag, "손님" + tag, Role.USER, null);
        Member noFile = save("boss" + tag, "사장" + tag, Role.OWNER, "111-22-" + tag);           // 경로 없음
        Member lost = save("lost" + tag, "분실" + tag, Role.OWNER, "222-33-" + tag);
        lost.setBizFilePath("biz/없는파일.png");                                                  // 경로는 있는데 파일 없음
        Member escape = save("esc" + tag, "탈출" + tag, Role.OWNER, "444-55-" + tag);
        escape.setBizFilePath("../../build.gradle");                                            // 비공개 폴더 밖

        for (Member m : new Member[]{user, noFile, lost, escape}) {
            mvc.perform(get("/api/admin/members/" + m.getMemberId() + "/biz-file").header("Authorization", admin))
                    .andExpect(status().isNotFound());
        }
        mvc.perform(get("/api/admin/members/999999/biz-file").header("Authorization", admin))
                .andExpect(status().isNotFound());
    }

    // ---------- 도우미 ----------

    private Member save(String loginId, String nickname, Role role, String bizNo) {
        Member m = new Member();
        m.setLoginId(loginId);
        m.setPassword("x");
        m.setNickname(nickname);
        m.setRole(role);
        m.setBizNo(bizNo);
        return memberRepository.save(m);
    }

    private void saveStore(Member owner, StoreStatus status, String seq) {
        Store s = new Store();
        s.setOwner(owner);
        s.setName("매장" + tag + seq);
        s.setArea("홍대");
        s.setAddress("서울");
        s.setPhone("02-000-0000");
        s.setWebsiteUrl("https://example.com");
        s.setHours("10:00 ~ 22:00");
        s.setIntro("소개");
        s.setBizPlaceNo("8" + seq + "0-88-" + tag);
        s.setStatus(status);
        storeRepository.save(s);
    }
}

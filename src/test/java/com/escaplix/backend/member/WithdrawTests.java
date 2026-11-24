package com.escaplix.backend.member;

import com.escaplix.backend.common.TestEmails;
import com.escaplix.backend.common.auth.JwtProvider;
import com.escaplix.backend.common.mail.EmailCodeStore;
import com.escaplix.backend.entity.*;
import com.escaplix.backend.repository.*;
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

import java.time.LocalDateTime;
import java.util.concurrent.ThreadLocalRandom;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 회원 탈퇴. 줄은 남기고 익명으로, 안 끝난 파티만 정리 (방장 → 삭제, 참여자 → 나가기), 찜·선호 장르 삭제.
 * 매장이 남은 사장·관리자는 못 함. tag 로 겹치지 않게 (@Transactional 없음: 커밋 뒤 토큰 거절까지 봄)
 */
@SpringBootTest
class WithdrawTests {

    @Autowired WebApplicationContext context;
    @Autowired JwtProvider jwtProvider;
    @Autowired PasswordEncoder passwordEncoder;
    @Autowired EmailCodeStore emailCodes;
    @Autowired MemberRepository memberRepository;
    @Autowired StoreRepository storeRepository;
    @Autowired ThemeRepository themeRepository;
    @Autowired PartyRepository partyRepository;
    @Autowired PartyMemberRepository partyMemberRepository;
    @Autowired PartyMessageRepository partyMessageRepository;
    @Autowired WishlistRepository wishlistRepository;
    @Autowired MemberGenreRepository memberGenreRepository;
    @Autowired GenreRepository genreRepository;

    MockMvc mvc;
    String tag;
    Theme theme;
    int seq;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(context).build();
        tag = String.valueOf(ThreadLocalRandom.current().nextInt(10000, 99999));
        theme = theme(null);
    }

    @Test
    void 손님이_탈퇴하면_익명이_되고_안_끝난_파티만_정리된다() throws Exception {
        Member me = member("wd" + tag, Role.USER, "wd" + tag + "@test.com");
        Member other = member("ot" + tag, Role.USER, null);
        Member third = member("th" + tag, Role.USER, null);
        String token = bearer(me);
        LocalDateTime future = LocalDateTime.now().plusDays(7), past = LocalDateTime.now().minusDays(3);

        Party myActive = party(me, future, PartyStatus.RECRUITING, 4, other);      // 내가 방장, 안 끝남 → 삭제
        Party myEnded = party(me, past, PartyStatus.CLOSED, 4, other);             // 내가 방장, 끝남 → 그대로
        Party joinedFull = party(other, future, PartyStatus.FULL, 2, me);          // 참여 중, 정원 마감 → 나가고 다시 모집 중
        Party joinedEnded = party(other, past, PartyStatus.CLOSED, 4, me, third);  // 참여했던 끝난 파티 → 기록 남김

        Wishlist w = new Wishlist();
        w.setMember(me);
        w.setTheme(theme);
        wishlistRepository.save(w);
        Genre g = new Genre();
        g.setName("탈퇴장르" + tag);
        MemberGenre mg = new MemberGenre();
        mg.setMember(me);
        mg.setGenre(genreRepository.save(g));
        memberGenreRepository.save(mg);

        withdraw(token, "틀림").andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("비밀번호가 맞지 않아요."));
        withdraw(token, "1234").andExpect(status().isNoContent());

        // 익명으로
        Member after = memberRepository.findById(me.getMemberId()).orElseThrow();
        assertThat(after.isWithdrawn()).isTrue();
        assertThat(after.getLoginId()).isEqualTo("withdrawn#" + me.getMemberId());
        assertThat(after.getNickname()).isEqualTo("탈퇴한 회원");
        assertThat(after.getEmail()).isNull();
        assertThat(passwordEncoder.matches("1234", after.getPassword())).isFalse();

        // 파티
        assertThat(partyStatus(myActive)).isEqualTo(PartyStatus.DELETED);
        assertThat(partyStatus(myEnded)).isEqualTo(PartyStatus.CLOSED);
        assertThat(partyMemberRepository.findByParty_PartyIdAndMember_MemberId(joinedFull.getPartyId(), me.getMemberId())).isEmpty();
        assertThat(partyStatus(joinedFull)).isEqualTo(PartyStatus.RECRUITING);
        assertThat(partyMessageRepository.findTopByParty_PartyIdOrderByMessageIdDesc(joinedFull.getPartyId()).orElseThrow().getMsgType())
                .isEqualTo(MessageType.LEAVE);
        assertThat(partyMemberRepository.findByParty_PartyIdAndMember_MemberId(joinedEnded.getPartyId(), me.getMemberId())).isPresent();

        // 찜·선호 장르
        assertThat(wishlistRepository.findByMember_MemberIdOrderByCreatedAtDesc(me.getMemberId())).isEmpty();
        assertThat(memberGenreRepository.findByMember_MemberId(me.getMemberId())).isEmpty();

        // 토큰: 로그인 API 는 401, 공개 API 는 비회원처럼 통과
        mvc.perform(get("/api/members/me").header("Authorization", token)).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/genres").header("Authorization", token)).andExpect(status().isOk());
        // 예전 아이디로 로그인 안 됨
        login("wd" + tag, "1234").andExpect(status().isUnauthorized());

        // 같은 아이디·이메일·닉네임으로 다시 가입 가능
        String email = TestEmails.verified(emailCodes, "wd" + tag + "@test.com");
        mvc.perform(post("/api/members/user").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"loginId\":\"wd" + tag + "\",\"password\":\"1234\",\"nickname\":\"wd" + tag
                                + "\",\"email\":\"" + email + "\",\"genreIds\":[]}"))
                .andExpect(status().isCreated());
    }

    @Test
    void 탈퇴한_회원_이름은_닉네임으로_못_쓴다() throws Exception {
        mvc.perform(get("/api/members/check-nickname").param("nickname", "탈퇴한 회원"))
                .andExpect(jsonPath("$.available").value(false));
    }

    @Test
    void 매장이_남은_사장은_막고_없으면_사업자_정보까지_지운다() throws Exception {
        Member withStore = member("os" + tag, Role.OWNER, null);
        theme(withStore);
        withdraw(bearer(withStore), "1234").andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("매장이 남아 있어서 탈퇴할 수 없어요. 매장을 먼저 정리해 주세요."));
        assertThat(memberRepository.findById(withStore.getMemberId()).orElseThrow().isWithdrawn()).isFalse();

        Member noStore = member("on" + tag, Role.OWNER, null);
        withdraw(bearer(noStore), "1234").andExpect(status().isNoContent());
        Member after = memberRepository.findById(noStore.getMemberId()).orElseThrow();
        assertThat(after.isWithdrawn()).isTrue();
        assertThat(after.getBizNo()).isNull();
        assertThat(after.getBizFilePath()).isNull();
    }

    @Test
    void 관리자는_탈퇴할_수_없다() throws Exception {
        Member admin = member("ad" + tag, Role.ADMIN, null);
        withdraw(bearer(admin), "1234").andExpect(status().isForbidden());
    }

    // ---------- 도우미 ----------

    private Member member(String loginId, Role role, String email) {
        Member m = new Member();
        m.setLoginId(loginId);
        m.setPassword(passwordEncoder.encode("1234"));
        m.setNickname("닉" + loginId);
        m.setEmail(email);
        m.setRole(role);
        if (role == Role.OWNER) {
            m.setBizNo(String.format("8%02d-88-%s", seq++, tag));
            m.setBizFilePath("biz/none-" + tag + ".png");   // 파일은 없음 (지우기는 조용히 넘어감)
        }
        return memberRepository.save(m);
    }

    private Theme theme(Member owner) {
        Store s = new Store();
        s.setOwner(owner);
        s.setName("탈퇴매장" + tag + seq);
        s.setArea("홍대");
        s.setAddress("서울");
        s.setPhone("02-000-0000");
        s.setWebsiteUrl("https://example.com");
        s.setHours("10:00 ~ 22:00");
        s.setIntro("소개");
        s.setBizPlaceNo(String.format("7%02d-77-%s", seq++, tag));
        s.setStatus(StoreStatus.APPROVED);
        storeRepository.save(s);
        Theme t = new Theme();
        t.setStore(s);
        t.setTitle("탈퇴테마" + tag + seq);
        t.setDifficulty(3);
        t.setPlayMinutes(60);
        t.setMinPeople(2);
        t.setMaxPeople(4);
        t.setPrice(20000);
        t.setBookingUrl("https://example.com/book");
        t.setSource(ThemeSource.OWNER);
        t.setStatus(ThemeStatus.OPEN);
        return themeRepository.save(t);
    }

    /** 파티 + 방장·참여자 줄 */
    private Party party(Member leader, LocalDateTime playAt, PartyStatus st, int max, Member... members) {
        Party p = new Party();
        p.setTheme(theme);
        p.setLeader(leader);
        p.setTitle("탈퇴 테스트");
        p.setIntro("한마디");
        p.setPlayAt(playAt);
        p.setDeadlineAt(playAt.minusDays(1));
        p.setMaxPeople(max);
        p.setStatus(st);
        partyRepository.save(p);
        joined(p, leader);
        for (Member m : members) joined(p, m);
        return p;
    }

    private void joined(Party p, Member m) {
        PartyMember pm = new PartyMember();
        pm.setParty(p);
        pm.setMember(m);
        pm.setStatus(PartyMemberStatus.JOINED);
        partyMemberRepository.save(pm);
    }

    private PartyStatus partyStatus(Party p) {
        return partyRepository.findById(p.getPartyId()).orElseThrow().getStatus();
    }

    private String bearer(Member m) {
        return "Bearer " + jwtProvider.create(m.getMemberId(), m.getNickname(), m.getRole());
    }

    private ResultActions withdraw(String token, String password) throws Exception {
        return mvc.perform(post("/api/members/me/withdraw").header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON).content("{\"password\":\"" + password + "\"}"));
    }

    private ResultActions login(String loginId, String password) throws Exception {
        return mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"loginId\":\"" + loginId + "\",\"password\":\"" + password + "\"}"));
    }
}

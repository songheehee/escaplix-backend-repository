package com.escaplix.backend.party;

import com.escaplix.backend.common.auth.JwtProvider;
import com.escaplix.backend.entity.*;
import com.escaplix.backend.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDateTime;
import java.util.concurrent.ThreadLocalRandom;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 테마 운영 종료 · 매장 폐업 뒤의 파티. 파티는 그대로 두고 요청마다 지금 상태로 막는다 (스케줄러 없음)
 *  - 목록에서 빠짐, 새로 참여 못 함, 방·내 채팅 목록에는 open=false 로 알려 줌
 *  - 운영을 다시 열면 그대로 돌아옴
 */
@SpringBootTest
class PartyThemeClosedTests {

    @Autowired WebApplicationContext context;
    @Autowired JwtProvider jwtProvider;
    @Autowired PasswordEncoder passwordEncoder;
    @Autowired MemberRepository memberRepository;
    @Autowired StoreRepository storeRepository;
    @Autowired ThemeRepository themeRepository;
    @Autowired PartyRepository partyRepository;
    @Autowired PartyMemberRepository partyMemberRepository;

    MockMvc mvc;
    String tag;
    Store store;
    Theme theme;
    Member leader, guest;
    Party party;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(context).build();
        tag = String.valueOf(ThreadLocalRandom.current().nextInt(10000, 99999));
        leader = member("tl" + tag);
        guest = member("tg" + tag);

        store = new Store();
        store.setName("종료매장" + tag);
        store.setArea("홍대");
        store.setAddress("서울");
        store.setPhone("02-000-0000");
        store.setWebsiteUrl("https://example.com");
        store.setHours("10:00 ~ 22:00");
        store.setIntro("소개");
        store.setBizPlaceNo("6" + tag.substring(0, 2) + "-66-" + tag);
        store.setStatus(StoreStatus.APPROVED);
        storeRepository.save(store);
        theme = new Theme();
        theme.setStore(store);
        theme.setTitle("종료테마" + tag);
        theme.setDifficulty(3);
        theme.setPlayMinutes(60);
        theme.setMinPeople(2);
        theme.setMaxPeople(4);
        theme.setPrice(20000);
        theme.setBookingUrl("https://example.com/book");
        theme.setSource(ThemeSource.OWNER);
        theme.setStatus(ThemeStatus.OPEN);
        themeRepository.save(theme);

        LocalDateTime playAt = LocalDateTime.now().plusDays(5);
        party = new Party();
        party.setTheme(theme);
        party.setLeader(leader);
        party.setTitle("종료 테스트 " + tag);
        party.setIntro("한마디");
        party.setPlayAt(playAt);
        party.setDeadlineAt(playAt.minusDays(1));
        party.setMaxPeople(4);
        party.setStatus(PartyStatus.RECRUITING);
        partyRepository.save(party);
        PartyMember pm = new PartyMember();
        pm.setParty(party);
        pm.setMember(leader);
        pm.setStatus(PartyMemberStatus.JOINED);
        partyMemberRepository.save(pm);
    }

    @Test
    void 운영_중이면_목록에_보이고_open_true() throws Exception {
        listed(true);
        mvc.perform(get("/api/parties/" + party.getPartyId()).header("Authorization", bearer(leader)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.theme.open").value(true));
        mvc.perform(get("/api/my/parties").header("Authorization", bearer(leader)))
                .andExpect(jsonPath("$[?(@.partyId == " + party.getPartyId() + ")].themeOpen").value(hasItem(true)));
    }

    @Test
    void 테마_운영_종료면_목록에서_빠지고_참여_못_하고_방에_알린다() throws Exception {
        setTheme(ThemeStatus.CLOSED);

        listed(false);
        mvc.perform(post("/api/parties/" + party.getPartyId() + "/join").header("Authorization", bearer(guest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("운영이 종료된 테마라 참여할 수 없습니다."));
        // 이미 들어온 사람: 방·채팅은 그대로, open=false
        mvc.perform(get("/api/parties/" + party.getPartyId()).header("Authorization", bearer(leader)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.theme.open").value(false));
        mvc.perform(get("/api/my/parties").header("Authorization", bearer(leader)))
                .andExpect(jsonPath("$[?(@.partyId == " + party.getPartyId() + ")].themeOpen").value(hasItem(false)));

        // 운영 재개 → 바로 원래대로
        setTheme(ThemeStatus.OPEN);
        listed(true);
        mvc.perform(post("/api/parties/" + party.getPartyId() + "/join").header("Authorization", bearer(guest)))
                .andExpect(status().isCreated());
    }

    @Test
    void 매장_폐업이어도_같다() throws Exception {
        store = storeRepository.findById(store.getStoreId()).orElseThrow();
        store.setStatus(StoreStatus.CLOSED);
        storeRepository.save(store);

        listed(false);
        mvc.perform(post("/api/parties/" + party.getPartyId() + "/join").header("Authorization", bearer(guest)))
                .andExpect(status().isConflict());
        mvc.perform(get("/api/parties/" + party.getPartyId()).header("Authorization", bearer(leader)))
                .andExpect(jsonPath("$.theme.open").value(false));
    }

    // ---------- 도우미 ----------

    /** 이 테마로 걸러 본 모집 목록에 이 파티가 있는지 */
    private void listed(boolean shown) throws Exception {
        mvc.perform(get("/api/parties").param("themeId", String.valueOf(theme.getThemeId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].partyId", shown
                        ? hasItem(party.getPartyId().intValue())
                        : not(hasItem(party.getPartyId().intValue()))));
    }

    private void setTheme(ThemeStatus st) {
        theme = themeRepository.findById(theme.getThemeId()).orElseThrow();
        theme.setStatus(st);
        themeRepository.save(theme);
    }

    private Member member(String loginId) {
        Member m = new Member();
        m.setLoginId(loginId);
        m.setPassword(passwordEncoder.encode("1234"));
        m.setNickname("닉" + loginId);
        m.setRole(Role.USER);
        return memberRepository.save(m);
    }

    private String bearer(Member m) {
        return "Bearer " + jwtProvider.create(m.getMemberId(), m.getNickname(), m.getRole());
    }
}

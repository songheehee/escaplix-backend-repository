package com.escaplix.backend.party;

import com.escaplix.backend.common.auth.JwtProvider;
import com.escaplix.backend.common.time.DayTypeResolver;
import com.escaplix.backend.entity.*;
import com.escaplix.backend.repository.*;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * A-12 ~ A-19 파티. H2 에서 돈다.
 * 동시 참여(잠금) 테스트 때문에 @Transactional 을 쓰지 않는다 → 이름에 tag 를 붙여 테스트끼리 안 겹치게 한다.
 */
@SpringBootTest
class PartyFlowTests {

    @Autowired WebApplicationContext context;
    @Autowired JwtProvider jwtProvider;
    @Autowired DayTypeResolver dayTypeResolver;
    @Autowired MemberRepository memberRepository;
    @Autowired StoreRepository storeRepository;
    @Autowired ThemeRepository themeRepository;
    @Autowired TimeSlotRepository timeSlotRepository;
    @Autowired GenreRepository genreRepository;
    @Autowired ThemeGenreRepository themeGenreRepository;
    @Autowired PartyRepository partyRepository;
    @Autowired RegionRepository regionRepository;
    @Autowired AreaRepository areaRepository;
    @Autowired com.escaplix.backend.scheduler.PartyDeadlineScheduler deadlineScheduler;
    @Autowired PartyMemberRepository partyMemberRepository;
    @Autowired PartyMessageRepository partyMessageRepository;

    MockMvc mvc;
    String tag;
    Theme theme;          // OPEN, 2~4명, 일주일 뒤 19:00 시간표 있음
    long genreId;
    LocalDateTime playAt; // 일주일 뒤 19:00
    LocalDateTime deadline;
    int storeSeq;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(context).build();
        tag = String.valueOf(ThreadLocalRandom.current().nextInt(10000, 99999));
        LocalDate day = LocalDate.now().plusDays(7);
        playAt = day.atTime(19, 0);
        deadline = day.minusDays(1).atTime(18, 0);

        Genre g = new Genre();
        g.setName("공포" + tag);
        genreId = genreRepository.save(g).getGenreId();
        theme = saveTheme("파티테마" + tag, ThemeStatus.OPEN, StoreStatus.APPROVED);
        ThemeGenre tg = new ThemeGenre();
        tg.setTheme(theme);
        tg.setGenre(g);
        themeGenreRepository.save(tg);
        TimeSlot slot = new TimeSlot();
        slot.setTheme(theme);
        slot.setDayType(dayTypeResolver.of(day));
        slot.setStartTime("19:00");
        timeSlotRepository.save(slot);
    }

    // ================================================================ A-14 파티 만들기

    @Test
    void A14_파티를_만들면_방장이_첫_참여자이고_안내_줄이_생긴다() throws Exception {
        Member leader = saveUser("leader");
        long partyId = create(bearer(leader), 4);

        Party p = partyRepository.findById(partyId).orElseThrow();
        assertThat(p.getStatus()).isEqualTo(PartyStatus.RECRUITING);
        assertThat(p.getLeader().getMemberId()).isEqualTo(leader.getMemberId());
        assertThat(partyMemberRepository.countByParty_PartyIdAndStatus(partyId, PartyMemberStatus.JOINED)).isEqualTo(1);
        PartyMessage msg = partyMessageRepository.findTopByParty_PartyIdOrderByMessageIdDesc(partyId).orElseThrow();
        assertThat(msg.getMsgType()).isEqualTo(MessageType.JOIN);
        assertThat(msg.getContent()).isNull();
    }

    @Test
    void A14_값이_틀리면_400_공개_안_된_테마는_404() throws Exception {
        String user = bearer(saveUser("maker"));

        // 모집 기한이 플레이 시간보다 늦음
        createReq(user, theme.getThemeId(), playAt, playAt.plusHours(1), 4)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("모집 기한은 플레이 시간 전이어야 합니다."));
        // 테마 인원 범위(2~4) 밖
        createReq(user, theme.getThemeId(), playAt, deadline, 5)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("모집 인원은 2~4명 사이로 정해 주세요."));
        // 시간표에 없는 시각
        createReq(user, theme.getThemeId(), playAt.withHour(20), deadline, 4)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("그 시간에는 이 테마 시간표가 없습니다."));
        // 지난 날짜
        createReq(user, theme.getThemeId(), LocalDateTime.now().minusDays(1), LocalDateTime.now().minusDays(2), 4)
                .andExpect(status().isBadRequest());
        // 승인 대기 테마, 승인 안 된 매장의 테마, 없는 테마
        Theme pending = saveTheme("대기테마" + tag, ThemeStatus.PENDING, StoreStatus.APPROVED);
        Theme hiddenStore = saveTheme("대기매장" + tag, ThemeStatus.OPEN, StoreStatus.PENDING);
        for (long id : new long[]{pending.getThemeId(), hiddenStore.getThemeId(), 999999L}) {
            createReq(user, id, playAt, deadline, 4).andExpect(status().isNotFound());
        }
        // 사장·관리자는 못 만든다 (손님 기능)
        Member owner = saveUser("owner");
        owner.setRole(Role.OWNER);
        memberRepository.save(owner);
        createReq(bearer(owner), theme.getThemeId(), playAt, deadline, 4).andExpect(status().isForbidden());
    }

    // ================================================================ A-13 파티 방 정보

    @Test
    void A13_참여자만_방을_본다_방장은_LEADER() throws Exception {
        Member leader = saveUser("leader");
        Member guest = saveUser("guest");
        long partyId = create(bearer(leader), 4);

        mvc.perform(get("/api/parties/" + partyId).header("Authorization", bearer(leader)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.myRole").value("LEADER"))
                .andExpect(jsonPath("$.currentPeople").value(1))
                .andExpect(jsonPath("$.theme.title").value("파티테마" + tag))
                .andExpect(jsonPath("$.theme.storeName").value("매장" + tag))
                .andExpect(jsonPath("$.members[0].nickname").value(leader.getNickname()))
                .andExpect(jsonPath("$.members[0].leader").value(true));

        mvc.perform(get("/api/parties/" + partyId).header("Authorization", bearer(guest)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("파티에 참여한 사람만 볼 수 있습니다."));
        mvc.perform(get("/api/parties/999999").header("Authorization", bearer(guest)))
                .andExpect(status().isNotFound());

        join(bearer(guest), partyId).andExpect(status().isCreated());
        mvc.perform(get("/api/parties/" + partyId).header("Authorization", bearer(guest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.myRole").value("MEMBER"))
                .andExpect(jsonPath("$.members", hasSize(2)));
    }

    // ================================================================ A-12 파티 목록

    @Test
    void A12_목록은_모집_중만_장르_검색어_테마로_거른다() throws Exception {
        Member leader = saveUser("leader");
        Member viewer = saveUser("viewer");
        long partyId = create(bearer(leader), 4);
        String path = "$[?(@.partyId == " + partyId + ")]";

        mvc.perform(get("/api/parties").param("themeId", String.valueOf(theme.getThemeId())).header("Authorization", bearer(viewer)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].partyId").value(partyId))
                .andExpect(jsonPath("$[0].currentPeople").value(1))
                .andExpect(jsonPath("$[0].maxPeople").value(4))
                .andExpect(jsonPath("$[0].genres[0]").value("공포" + tag))
                .andExpect(jsonPath("$[0].leaderNickname").value(leader.getNickname()))
                .andExpect(jsonPath("$[0].joined").value(false))
                .andExpect(jsonPath("$[0].kicked").value(false));

        // 방장 눈에는 joined=true
        mvc.perform(get("/api/parties").param("themeId", String.valueOf(theme.getThemeId())).header("Authorization", bearer(leader)))
                .andExpect(jsonPath("$[0].joined").value(true));
        // 검색어는 띄어쓰기 무시
        mvc.perform(get("/api/parties").param("keyword", "파티 테마 " + tag).header("Authorization", bearer(viewer)))
                .andExpect(jsonPath(path).isNotEmpty());
        mvc.perform(get("/api/parties").param("keyword", "없는이름" + tag).header("Authorization", bearer(viewer)))
                .andExpect(jsonPath(path).isEmpty());
        // 장르는 하나라도 맞으면
        mvc.perform(get("/api/parties").param("genreIds", "999999," + genreId).header("Authorization", bearer(viewer)))
                .andExpect(jsonPath(path).isNotEmpty());
        mvc.perform(get("/api/parties").param("genreIds", "999999").header("Authorization", bearer(viewer)))
                .andExpect(jsonPath(path).isEmpty());
        // 로그인 안 해도 목록은 보임 (참여·강퇴 표시는 false)
        mvc.perform(get("/api/parties")).andExpect(status().isOk())
                .andExpect(jsonPath(path).isNotEmpty())
                .andExpect(jsonPath(path + ".joined").value(org.hamcrest.Matchers.everyItem(org.hamcrest.Matchers.is(false))));

        // 기한 지난 파티는 안 보임 (스케줄러가 아직 안 돌았어도)
        Party p = partyRepository.findById(partyId).orElseThrow();
        p.setDeadlineAt(LocalDateTime.now().minusMinutes(1));
        partyRepository.save(p);
        mvc.perform(get("/api/parties").header("Authorization", bearer(viewer)))
                .andExpect(jsonPath(path).isEmpty());
    }

    @Test
    void A12_지역으로_거른다_세부_지역_또는_큰_지역() throws Exception {
        // 이 테스트만의 지역: 큰 지역(R) 안에 세부 지역(A), 다른 큰 지역(R2) 안에 세부 지역(B)
        Region r = new Region(); r.setName("큰" + tag); r.setSortOrder(1); regionRepository.save(r);
        Region r2 = new Region(); r2.setName("딴" + tag); r2.setSortOrder(2); regionRepository.save(r2);
        Area a = new Area(); a.setRegion(r); a.setName("동네" + tag); a.setSortOrder(1); areaRepository.save(a);
        Area b = new Area(); b.setRegion(r2); b.setName("옆동네" + tag); b.setSortOrder(1); areaRepository.save(b);
        Store store = theme.getStore();
        store.setArea(a.getName());
        storeRepository.save(store);

        long partyId = create(bearer(saveUser("leader")), 4);
        String path = "$[?(@.partyId == " + partyId + ")]";

        mvc.perform(get("/api/parties").param("area", a.getName())).andExpect(jsonPath(path).isNotEmpty());
        mvc.perform(get("/api/parties").param("area", b.getName())).andExpect(jsonPath(path).isEmpty());
        mvc.perform(get("/api/parties").param("region", r.getName())).andExpect(jsonPath(path).isNotEmpty());
        mvc.perform(get("/api/parties").param("region", r2.getName())).andExpect(jsonPath(path).isEmpty());
        // 둘 다 오면 세부 지역이 우선
        mvc.perform(get("/api/parties").param("area", a.getName()).param("region", r2.getName()))
                .andExpect(jsonPath(path).isNotEmpty());
        // 다른 조건과 같이: 장르가 안 맞으면 빠짐
        mvc.perform(get("/api/parties").param("region", r.getName()).param("genreIds", "999999"))
                .andExpect(jsonPath(path).isEmpty());
    }

    // ================================================================ A-16 참여

    @Test
    void A16_참여하면_인원이_늘고_정원이_차면_FULL() throws Exception {
        Member leader = saveUser("leader");
        long partyId = create(bearer(leader), 3);
        String a = bearer(saveUser("a"));
        String b = bearer(saveUser("b"));

        join(a, partyId).andExpect(status().isCreated())
                .andExpect(jsonPath("$.currentPeople").value(2))
                .andExpect(jsonPath("$.status").value("RECRUITING"));
        join(a, partyId).andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("이미 참여한 파티입니다."));
        join(b, partyId).andExpect(status().isCreated())
                .andExpect(jsonPath("$.currentPeople").value(3))
                .andExpect(jsonPath("$.status").value("FULL"));
        join(bearer(saveUser("c")), partyId).andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("정원이 다 찼습니다."));
        join(a, 999999L).andExpect(status().isNotFound());

        // 안내 줄: 방장 JOIN + a JOIN + b JOIN
        assertThat(partyMessageRepository.findTop50ByParty_PartyIdOrderByMessageIdDesc(partyId))
                .extracting(PartyMessage::getMsgType).containsOnly(MessageType.JOIN).hasSize(3);
        // 정원 마감(FULL)도 목록에 남는다 (모집 마감 전까지는 다 보임). 참여자는 joined 로 보임
        String path = "$[?(@.partyId == " + partyId + ")]";
        mvc.perform(get("/api/parties").param("themeId", String.valueOf(theme.getThemeId())).header("Authorization", a))
                .andExpect(jsonPath(path + ".status").value(org.hamcrest.Matchers.contains("FULL")))
                .andExpect(jsonPath(path + ".joined").value(org.hamcrest.Matchers.contains(true)));

        // 정원이 차도 모집 기한이 지나면 모집 마감: 목록에서 빠지고, 방·내 채팅에는 CLOSED 로 (스케줄러 전에도)
        Party p = partyRepository.findById(partyId).orElseThrow();
        p.setDeadlineAt(LocalDateTime.now().minusMinutes(1));
        partyRepository.save(p);
        mvc.perform(get("/api/parties").param("themeId", String.valueOf(theme.getThemeId())).header("Authorization", a))
                .andExpect(jsonPath(path).isEmpty());
        mvc.perform(get("/api/parties/" + partyId).header("Authorization", a))
                .andExpect(jsonPath("$.status").value("CLOSED"));
        mvc.perform(get("/api/my/parties").header("Authorization", a))
                .andExpect(jsonPath(path + ".status").value(org.hamcrest.Matchers.contains("CLOSED")));
        // 스케줄러는 DB 도 CLOSED 로
        assertThat(partyRepository.findById(partyId).orElseThrow().getStatus()).isEqualTo(PartyStatus.FULL);
        deadlineScheduler.closeExpired();
        assertThat(partyRepository.findById(partyId).orElseThrow().getStatus()).isEqualTo(PartyStatus.CLOSED);
    }

    @Test
    void A16_기한이_지난_파티는_409() throws Exception {
        long partyId = create(bearer(saveUser("leader")), 4);
        Party p = partyRepository.findById(partyId).orElseThrow();
        p.setDeadlineAt(LocalDateTime.now().minusMinutes(1));
        partyRepository.save(p);

        join(bearer(saveUser("late")), partyId).andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("모집이 끝난 파티입니다."));
    }

    @Test
    void A16_마지막_한_자리에_동시에_몰려도_한_명만_들어간다() throws Exception {
        long partyId = create(bearer(saveUser("leader")), 2);   // 남은 자리 1
        int n = 6;
        List<String> tokens = new ArrayList<>();
        for (int i = 0; i < n; i++) tokens.add(bearer(saveUser("rush" + i)));

        ExecutorService pool = Executors.newFixedThreadPool(n);
        CountDownLatch ready = new CountDownLatch(1);
        AtomicInteger created = new AtomicInteger();
        AtomicInteger conflict = new AtomicInteger();
        List<Future<?>> futures = new ArrayList<>();
        for (String t : tokens) {
            futures.add(pool.submit(() -> {
                ready.await();
                int s = join(t, partyId).andReturn().getResponse().getStatus();
                if (s == 201) created.incrementAndGet();
                if (s == 409) conflict.incrementAndGet();
                return null;
            }));
        }
        ready.countDown();
        for (Future<?> f : futures) f.get(30, TimeUnit.SECONDS);
        pool.shutdown();

        assertThat(created.get()).isEqualTo(1);
        assertThat(conflict.get()).isEqualTo(n - 1);
        assertThat(partyMemberRepository.countByParty_PartyIdAndStatus(partyId, PartyMemberStatus.JOINED)).isEqualTo(2);
        assertThat(partyRepository.findById(partyId).orElseThrow().getStatus()).isEqualTo(PartyStatus.FULL);
    }

    // ================================================================ A-17 나가기

    @Test
    void A17_나가면_자리가_나고_다시_들어올_수_있다_방장은_못_나감() throws Exception {
        String leader = bearer(saveUser("leader"));
        long partyId = create(leader, 2);
        String a = bearer(saveUser("a"));
        join(a, partyId).andExpect(jsonPath("$.status").value("FULL"));

        mvc.perform(delete("/api/parties/" + partyId + "/join").header("Authorization", leader))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("방장은 나갈 수 없습니다. 파티를 삭제해 주세요."));

        mvc.perform(delete("/api/parties/" + partyId + "/join").header("Authorization", a))
                .andExpect(status().isNoContent());
        assertThat(partyRepository.findById(partyId).orElseThrow().getStatus()).isEqualTo(PartyStatus.RECRUITING);
        assertThat(partyMessageRepository.findTopByParty_PartyIdOrderByMessageIdDesc(partyId).orElseThrow().getMsgType())
                .isEqualTo(MessageType.LEAVE);
        mvc.perform(get("/api/parties/" + partyId).header("Authorization", a)).andExpect(status().isForbidden());

        // 참여 안 한 상태로 또 나가기 → 404, 스스로 나간 사람은 다시 참여 가능
        mvc.perform(delete("/api/parties/" + partyId + "/join").header("Authorization", a))
                .andExpect(status().isNotFound());
        join(a, partyId).andExpect(status().isCreated());
    }

    // ================================================================ A-18 강퇴

    @Test
    void A18_강퇴하면_KICKED로_남아_다시_못_들어온다() throws Exception {
        Member leaderM = saveUser("leader");
        String leader = bearer(leaderM);
        long partyId = create(leader, 2);
        Member aM = saveUser("a");
        String a = bearer(aM);
        join(a, partyId).andExpect(jsonPath("$.status").value("FULL"));
        String kickA = "/api/parties/" + partyId + "/members/" + aM.getMemberId();

        mvc.perform(delete("/api/parties/" + partyId + "/members/" + leaderM.getMemberId()).header("Authorization", a))
                .andExpect(status().isForbidden());
        mvc.perform(delete("/api/parties/" + partyId + "/members/" + leaderM.getMemberId()).header("Authorization", leader))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("자기 자신은 강퇴할 수 없습니다."));

        mvc.perform(delete(kickA).header("Authorization", leader)).andExpect(status().isNoContent());
        assertThat(partyMemberRepository.findByParty_PartyIdAndMember_MemberId(partyId, aM.getMemberId()).orElseThrow().getStatus())
                .isEqualTo(PartyMemberStatus.KICKED);
        assertThat(partyRepository.findById(partyId).orElseThrow().getStatus()).isEqualTo(PartyStatus.RECRUITING);
        PartyMessage last = partyMessageRepository.findTopByParty_PartyIdOrderByMessageIdDesc(partyId).orElseThrow();
        assertThat(last.getMsgType()).isEqualTo(MessageType.KICK);

        // 강퇴된 사람: 방 403, 재참여 409, 나가기 404, 목록에선 kicked=true
        mvc.perform(get("/api/parties/" + partyId).header("Authorization", a)).andExpect(status().isForbidden());
        join(a, partyId).andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("강퇴된 파티에는 다시 참여할 수 없습니다."));
        mvc.perform(delete("/api/parties/" + partyId + "/join").header("Authorization", a)).andExpect(status().isNotFound());
        mvc.perform(get("/api/parties").param("themeId", String.valueOf(theme.getThemeId())).header("Authorization", a))
                .andExpect(jsonPath("$[0].kicked").value(true))
                .andExpect(jsonPath("$[0].currentPeople").value(1));
        // 이미 강퇴된 사람을 또 강퇴 → 404
        mvc.perform(delete(kickA).header("Authorization", leader)).andExpect(status().isNotFound());
    }

    // ================================================================ A-19 모집 마감

    @Test
    void A19_방장이_마감하면_CLOSED_목록에서_빠지고_참여_불가() throws Exception {
        String leader = bearer(saveUser("leader"));
        long partyId = create(leader, 4);
        String a = bearer(saveUser("a"));
        join(a, partyId).andExpect(status().isCreated());

        mvc.perform(put("/api/parties/" + partyId + "/close").header("Authorization", a))
                .andExpect(status().isForbidden());
        mvc.perform(put("/api/parties/" + partyId + "/close").header("Authorization", leader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CLOSED"));
        mvc.perform(put("/api/parties/" + partyId + "/close").header("Authorization", leader))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("이미 마감된 파티입니다."));

        join(bearer(saveUser("b")), partyId).andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("모집이 끝난 파티입니다."));
        mvc.perform(get("/api/parties").param("themeId", String.valueOf(theme.getThemeId())).header("Authorization", a))
                .andExpect(jsonPath("$[?(@.partyId == " + partyId + ")]").isEmpty());
        // 마감해도 방은 그대로 보임
        mvc.perform(get("/api/parties/" + partyId).header("Authorization", a))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CLOSED"));
    }

    // ================================================================ A-15 파티 삭제

    @Test
    void A15_삭제하면_DELETED_이후_모든_파티_API에서_404() throws Exception {
        String leader = bearer(saveUser("leader"));
        long partyId = create(leader, 4);
        String a = bearer(saveUser("a"));
        join(a, partyId).andExpect(status().isCreated());

        mvc.perform(delete("/api/parties/" + partyId).header("Authorization", a)).andExpect(status().isForbidden());
        mvc.perform(delete("/api/parties/" + partyId).header("Authorization", leader)).andExpect(status().isNoContent());

        // 실제로 지우지 않음 (줄과 채팅도 그대로)
        assertThat(partyRepository.findById(partyId).orElseThrow().getStatus()).isEqualTo(PartyStatus.DELETED);
        assertThat(partyMemberRepository.countByParty_PartyIdAndStatus(partyId, PartyMemberStatus.JOINED)).isEqualTo(2);

        mvc.perform(get("/api/parties/" + partyId).header("Authorization", a)).andExpect(status().isNotFound());
        join(bearer(saveUser("b")), partyId).andExpect(status().isNotFound());
        mvc.perform(delete("/api/parties/" + partyId + "/join").header("Authorization", a)).andExpect(status().isNotFound());
        mvc.perform(put("/api/parties/" + partyId + "/close").header("Authorization", leader)).andExpect(status().isNotFound());
        mvc.perform(delete("/api/parties/" + partyId).header("Authorization", leader)).andExpect(status().isNotFound());
        mvc.perform(get("/api/parties").param("themeId", String.valueOf(theme.getThemeId())).header("Authorization", a))
                .andExpect(jsonPath("$[?(@.partyId == " + partyId + ")]").isEmpty());
    }

    // ================================================================ A-21 채팅 보내기

    @Test
    void A21_참여자는_채팅을_보내고_공백은_지워서_저장() throws Exception {
        Member leaderM = saveUser("leader");
        String leader = bearer(leaderM);
        long partyId = create(leader, 4);

        say(leader, partyId, "  무서운 건 제가 앞장설게요  ")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.messageId").value(notNullValue()))
                .andExpect(jsonPath("$.memberId").value(leaderM.getMemberId()))
                .andExpect(jsonPath("$.nickname").value(leaderM.getNickname()))
                .andExpect(jsonPath("$.msgType").value("CHAT"))
                .andExpect(jsonPath("$.content").value("무서운 건 제가 앞장설게요"))
                .andExpect(jsonPath("$.mine").value(true));
        // createdAt 은 DB DEFAULT SYSDATE. 테스트용 H2 에는 DEFAULT 가 없어서 여기선 확인 안 함 (Oracle 에서 확인)

        PartyMessage last = partyMessageRepository.findTopByParty_PartyIdOrderByMessageIdDesc(partyId).orElseThrow();
        assertThat(last.getMsgType()).isEqualTo(MessageType.CHAT);
        assertThat(last.getContent()).isEqualTo("무서운 건 제가 앞장설게요");

        // 공백만, 500자 넘음 → 400. 공백을 지우고 500자면 통과
        say(leader, partyId, "   ").andExpect(status().isBadRequest());
        say(leader, partyId, "가".repeat(501)).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("메시지는 500자 이내로 입력해 주세요."));
        say(leader, partyId, "가".repeat(500)).andExpect(status().isCreated());
        // 길이는 공백을 지운 뒤로 잰다: 앞뒤 공백 + 500자는 통과(500자로 저장), 앞뒤 공백 + 501자는 400
        say(leader, partyId, "  " + "나".repeat(500) + "  ").andExpect(status().isCreated());
        assertThat(partyMessageRepository.findTopByParty_PartyIdOrderByMessageIdDesc(partyId).orElseThrow().getContent())
                .isEqualTo("나".repeat(500));
        say(leader, partyId, "  " + "나".repeat(501) + "  ").andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("메시지는 500자 이내로 입력해 주세요."));

        // 마감된 파티도 채팅 가능 (일정 조율)
        mvc.perform(put("/api/parties/" + partyId + "/close").header("Authorization", leader)).andExpect(status().isOk());
        say(leader, partyId, "마감했어요. 당일에 봬요").andExpect(status().isCreated());
    }

    @Test
    void A21_끝난_파티는_읽기_전용_보내면_409_기록은_그대로() throws Exception {
        String leader = bearer(saveUser("leader"));
        long partyId = create(leader, 4);
        say(leader, partyId, "당일에 봬요").andExpect(status().isCreated());

        Party p = partyRepository.findById(partyId).orElseThrow();
        p.setDeadlineAt(LocalDateTime.now().minusHours(2));
        p.setPlayAt(LocalDateTime.now().minusMinutes(1));
        partyRepository.save(p);

        say(leader, partyId, "재밌었어요").andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("종료된 파티에는 메시지를 보낼 수 없습니다."));
        // 지난 메시지는 그대로 읽힌다
        mvc.perform(get("/api/parties/" + partyId + "/messages").header("Authorization", leader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.content == '당일에 봬요')]").exists());
    }

    @Test
    void A21_참여_안_했거나_강퇴되면_403_삭제된_파티는_404() throws Exception {
        String leader = bearer(saveUser("leader"));
        long partyId = create(leader, 4);
        Member aM = saveUser("a");
        String a = bearer(aM);

        say(a, partyId, "들어가도 돼요?").andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("파티에 참여한 사람만 채팅할 수 있습니다."));

        join(a, partyId).andExpect(status().isCreated());
        say(a, partyId, "안녕하세요").andExpect(status().isCreated())
                .andExpect(jsonPath("$.mine").value(true));

        mvc.perform(delete("/api/parties/" + partyId + "/members/" + aM.getMemberId()).header("Authorization", leader))
                .andExpect(status().isNoContent());
        say(a, partyId, "왜 강퇴해요").andExpect(status().isForbidden());   // 강퇴 직후 바로 막힘

        mvc.perform(delete("/api/parties/" + partyId).header("Authorization", leader)).andExpect(status().isNoContent());
        say(leader, partyId, "삭제된 방").andExpect(status().isNotFound());
        say(leader, 999999L, "없는 방").andExpect(status().isNotFound());
        mvc.perform(post("/api/parties/" + partyId + "/messages").contentType(MediaType.APPLICATION_JSON)
                .content("{\"content\":\"로그인 안 함\"}")).andExpect(status().isUnauthorized());
    }

    // ================================================================ A-20 새 채팅 받기

    @Test
    void A20_처음엔_전부_오름차순_after_뒤로는_새_것만() throws Exception {
        Member leaderM = saveUser("leader");
        String leader = bearer(leaderM);
        long partyId = create(leader, 4);
        Member aM = saveUser("a");
        String a = bearer(aM);
        join(a, partyId).andExpect(status().isCreated());
        say(leader, partyId, "안녕하세요").andExpect(status().isCreated());
        say(a, partyId, "반가워요").andExpect(status().isCreated());

        // after 없음: 안내 줄(JOIN 2) + 채팅 2, 오래된 것부터
        String body = messages(a, partyId, null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(4)))
                .andExpect(jsonPath("$[*].msgType", contains("JOIN", "JOIN", "CHAT", "CHAT")))
                .andExpect(jsonPath("$[0].nickname").value(leaderM.getNickname()))
                .andExpect(jsonPath("$[0].content").value(nullValue()))
                .andExpect(jsonPath("$[0].mine").value(false))
                .andExpect(jsonPath("$[1].mine").value(true))
                .andExpect(jsonPath("$[2].content").value("안녕하세요"))
                .andExpect(jsonPath("$[3].mine").value(true))
                .andReturn().getResponse().getContentAsString();
        List<Number> ids = JsonPath.read(body, "$[*].messageId");
        assertThat(ids).extracting(Number::longValue).isSorted();
        long lastId = ids.get(ids.size() - 1).longValue();

        // 3초마다: 새 것 없으면 빈 배열, 생기면 그것만
        messages(a, partyId, lastId).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(0)));
        say(leader, partyId, "내일 봬요").andExpect(status().isCreated());
        messages(a, partyId, lastId)
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].content").value("내일 봬요"));

        // 닉네임을 바꾸면 예전 메시지도 새 닉네임 (MEMBER JOIN)
        leaderM.setNickname("새닉" + tag);
        memberRepository.save(leaderM);
        messages(a, partyId, null).andExpect(jsonPath("$[0].nickname").value("새닉" + tag));

        // 마감된 파티도 읽기 가능
        mvc.perform(put("/api/parties/" + partyId + "/close").header("Authorization", leader)).andExpect(status().isOk());
        messages(a, partyId, null).andExpect(status().isOk());
    }

    @Test
    void A20_처음엔_최근_50개_after_뒤로는_최대_100개() throws Exception {
        String leader = bearer(saveUser("leader"));
        long partyId = create(leader, 4);                         // 안내 줄 1개
        for (int i = 1; i <= 120; i++) say(leader, partyId, "메시지" + i).andExpect(status().isCreated());

        // 처음: 가장 최근 50개 (71 ~ 120), 오래된 것부터
        messages(leader, partyId, null)
                .andExpect(jsonPath("$", hasSize(50)))
                .andExpect(jsonPath("$[0].content").value("메시지71"))
                .andExpect(jsonPath("$[49].content").value("메시지120"));
        // after=0: 처음부터 최대 100개 (안내 줄 + 메시지1 ~ 99)
        messages(leader, partyId, 0L)
                .andExpect(jsonPath("$", hasSize(100)))
                .andExpect(jsonPath("$[0].msgType").value("JOIN"))
                .andExpect(jsonPath("$[99].content").value("메시지99"));
    }

    @Test
    void A20_참여_안_했거나_강퇴되면_403_삭제된_파티는_404() throws Exception {
        String leader = bearer(saveUser("leader"));
        long partyId = create(leader, 4);
        Member aM = saveUser("a");
        String a = bearer(aM);

        messages(a, partyId, null).andExpect(status().isForbidden());
        join(a, partyId).andExpect(status().isCreated());
        messages(a, partyId, null).andExpect(status().isOk());

        mvc.perform(delete("/api/parties/" + partyId + "/members/" + aM.getMemberId()).header("Authorization", leader))
                .andExpect(status().isNoContent());
        messages(a, partyId, null).andExpect(status().isForbidden());   // 강퇴 직후
        // 방장 눈에는 강퇴 안내 줄이 마지막
        messages(leader, partyId, null).andExpect(jsonPath("$[-1:].msgType", contains("KICK")));

        mvc.perform(delete("/api/parties/" + partyId).header("Authorization", leader)).andExpect(status().isNoContent());
        messages(leader, partyId, null).andExpect(status().isNotFound());
        mvc.perform(get("/api/parties/" + partyId + "/messages")).andExpect(status().isUnauthorized());
    }

    // ================================================================ A-22 내 채팅 목록

    @Test
    void A22_내가_참여_중인_파티만_마지막_메시지_최신순() throws Exception {
        Member meM = saveUser("me");
        String me = bearer(meM);
        Member otherM = saveUser("other");
        String other = bearer(otherM);

        long mine = create(me, 4);                  // 내가 방장
        long joined = create(other, 4);             // 참여자
        long closed = create(other, 4);             // 참여 후 마감 → 그래도 보임
        long kicked = create(other, 4);             // 강퇴 → 안 보임
        long deleted = create(other, 4);            // 삭제 → 안 보임
        long left = create(other, 4);               // 스스로 나감 → 안 보임
        for (long id : new long[]{joined, closed, kicked, deleted, left}) join(me, id).andExpect(status().isCreated());
        mvc.perform(put("/api/parties/" + closed + "/close").header("Authorization", other)).andExpect(status().isOk());
        mvc.perform(delete("/api/parties/" + kicked + "/members/" + meM.getMemberId()).header("Authorization", other)).andExpect(status().isNoContent());
        mvc.perform(delete("/api/parties/" + deleted).header("Authorization", other)).andExpect(status().isNoContent());
        mvc.perform(delete("/api/parties/" + left + "/join").header("Authorization", me)).andExpect(status().isNoContent());

        // 메시지 순서: mine → closed → joined (joined 가 가장 최근)
        say(me, mine, "제 파티예요").andExpect(status().isCreated());
        say(other, closed, "마감했어요").andExpect(status().isCreated());
        say(other, joined, "곧 봬요").andExpect(status().isCreated());

        // mine 은 이미 끝난 파티로 만든다 (ended=true → 후기 쓰기 버튼)
        Party past = partyRepository.findById(mine).orElseThrow();
        past.setPlayAt(LocalDateTime.now().minusHours(2));
        past.setDeadlineAt(LocalDateTime.now().minusHours(3));
        partyRepository.save(past);

        mvc.perform(get("/api/my/parties").header("Authorization", me))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].partyId", contains((int) joined, (int) closed, (int) mine)))
                .andExpect(jsonPath("$[0].myRole").value("MEMBER"))
                .andExpect(jsonPath("$[0].status").value("RECRUITING"))
                .andExpect(jsonPath("$[0].currentPeople").value(2))
                .andExpect(jsonPath("$[0].maxPeople").value(4))
                .andExpect(jsonPath("$[0].themeId").value(theme.getThemeId()))
                .andExpect(jsonPath("$[0].themeTitle").value("파티테마" + tag))
                .andExpect(jsonPath("$[0].ended").value(false))
                .andExpect(jsonPath("$[0].lastMessage.nickname").value(otherM.getNickname()))
                .andExpect(jsonPath("$[0].lastMessage.msgType").value("CHAT"))
                .andExpect(jsonPath("$[0].lastMessage.content").value("곧 봬요"))
                .andExpect(jsonPath("$[1].status").value("CLOSED"))
                .andExpect(jsonPath("$[2].myRole").value("LEADER"))
                .andExpect(jsonPath("$[2].ended").value(true));

        // 강퇴 안내 줄이 마지막이어도 방장 쪽 목록엔 KICK 으로 보임
        mvc.perform(get("/api/my/parties").header("Authorization", other))
                .andExpect(jsonPath("$[?(@.partyId == " + kicked + ")].lastMessage.msgType", contains("KICK")))
                .andExpect(jsonPath("$[?(@.partyId == " + deleted + ")]").isEmpty());
    }

    @Test
    void A22_참여한_파티가_없으면_빈_배열_로그인_안_하면_401() throws Exception {
        mvc.perform(get("/api/my/parties").header("Authorization", bearer(saveUser("alone"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
        mvc.perform(get("/api/my/parties")).andExpect(status().isUnauthorized());
    }

    // ================================================================ 도우미

    private ResultActions messages(String token, long partyId, Long after) throws Exception {
        var req = get("/api/parties/" + partyId + "/messages").header("Authorization", token);
        if (after != null) req.param("after", String.valueOf(after));
        return mvc.perform(req);
    }

    private ResultActions say(String token, long partyId, String content) throws Exception {
        String json = "{\"content\":" + (content == null ? "null" : "\"" + content + "\"") + "}";
        return mvc.perform(post("/api/parties/" + partyId + "/messages").header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON).content(json));
    }

    private long create(String token, int maxPeople) throws Exception {
        String body = createReq(token, theme.getThemeId(), playAt, deadline, maxPeople)
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(body, "$.partyId")).longValue();
    }

    private ResultActions createReq(String token, long themeId, LocalDateTime play, LocalDateTime dl, int maxPeople) throws Exception {
        String json = "{\"themeId\":" + themeId + ",\"title\":\"같이 가요\",\"intro\":\"추리 좋아하는 분\","
                + "\"playAt\":\"" + play + "\",\"deadlineAt\":\"" + dl + "\",\"maxPeople\":" + maxPeople + "}";
        return mvc.perform(post("/api/parties").header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON).content(json));
    }

    private ResultActions join(String token, long partyId) throws Exception {
        return mvc.perform(post("/api/parties/" + partyId + "/join").header("Authorization", token));
    }

    private Member saveUser(String name) {
        Member m = new Member();
        m.setLoginId(name + tag);
        m.setPassword("x");
        m.setNickname(name + tag);
        m.setRole(Role.USER);
        return memberRepository.save(m);
    }

    private String bearer(Member m) {
        return "Bearer " + jwtProvider.create(m.getMemberId(), m.getNickname(), m.getRole());
    }

    private Theme saveTheme(String title, ThemeStatus themeStatus, StoreStatus storeStatus) {
        Store s = new Store();
        s.setName(themeStatus == ThemeStatus.OPEN && storeStatus == StoreStatus.APPROVED ? "매장" + tag : "다른매장" + tag);
        s.setArea("홍대");
        s.setAddress("서울");
        s.setPhone("02-000-0000");
        s.setWebsiteUrl("https://example.com");
        s.setHours("10:00 ~ 22:00");
        s.setIntro("소개");
        s.setBizPlaceNo(String.format("9%02d-99-%s", storeSeq++, tag));   // 사업장 번호는 겹치면 안 됨
        s.setStatus(storeStatus);
        storeRepository.save(s);

        Theme t = new Theme();
        t.setStore(s);
        t.setTitle(title);
        t.setDifficulty(3);
        t.setPlayMinutes(70);
        t.setMinPeople(2);
        t.setMaxPeople(4);
        t.setPrice(20000);
        t.setBookingUrl("https://example.com/book");
        t.setSource(ThemeSource.OWNER);
        t.setStatus(themeStatus);
        return themeRepository.save(t);
    }
}

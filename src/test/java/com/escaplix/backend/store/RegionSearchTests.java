package com.escaplix.backend.store;

import com.escaplix.backend.entity.Area;
import com.escaplix.backend.entity.Member;
import com.escaplix.backend.entity.Region;
import com.escaplix.backend.entity.Role;
import com.escaplix.backend.entity.Store;
import com.escaplix.backend.entity.StoreStatus;
import com.escaplix.backend.entity.Theme;
import com.escaplix.backend.entity.ThemeSource;
import com.escaplix.backend.entity.ThemeStatus;
import com.escaplix.backend.repository.ThemeBrowseRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * B-1·B-9 검색의 큰 지역(region) 조건. H2 에서 돈다.
 * 서울(홍대·강남) / 경기(수원) 매장을 하나씩 두고, region·area 조건별로 어느 매장·테마가 나오는지 본다.
 */
@SpringBootTest
@Transactional   // 끝나면 되돌림
class RegionSearchTests {

    @Autowired EntityManager em;
    @Autowired ThemeBrowseRepository repo;

    String tag, seoul, gyeonggi, hongdae, gangnam, suwon, kw;

    @BeforeEach
    void setUp() {
        tag = String.valueOf(ThreadLocalRandom.current().nextInt(10000, 99999));
        seoul = "서울" + tag;
        gyeonggi = "경기" + tag;
        hongdae = "홍대" + tag;
        gangnam = "강남" + tag;
        suwon = "수원" + tag;
        kw = ThemeBrowseRepository.likePattern("지역테스트" + tag);   // 다른 테스트가 넣은 매장은 빼고 보려고

        Region s = region(seoul, 1), g = region(gyeonggi, 2);
        area(s, hongdae); area(s, gangnam); area(g, suwon);

        Member owner = new Member();
        owner.setLoginId("rg" + tag);
        owner.setPassword("x");
        owner.setNickname("지역사장" + tag);
        owner.setRole(Role.OWNER);
        em.persist(owner);

        for (String a : List.of(hongdae, gangnam, suwon)) theme(store(owner, a));
        em.flush();
    }

    @Test
    void 매장_큰_지역이면_그_안의_세부_지역_매장_전부() {
        assertThat(storeAreas(null, seoul)).containsExactlyInAnyOrder(hongdae, gangnam);
        assertThat(storeAreas(null, gyeonggi)).containsExactly(suwon);
        assertThat(storeAreas(null, "없는지역" + tag)).isEmpty();
        assertThat(storeAreas(null, null)).hasSize(3);
    }

    @Test
    void 매장_세부_지역이_있으면_큰_지역보다_우선() {
        assertThat(storeAreas(hongdae, null)).containsExactly(hongdae);
        assertThat(storeAreas(hongdae, gyeonggi)).containsExactly(hongdae);
    }

    @Test
    void 테마_큰_지역과_세부_지역() {
        assertThat(themeAreas(null, seoul)).containsExactlyInAnyOrder(hongdae, gangnam);
        assertThat(themeAreas(null, gyeonggi)).containsExactly(suwon);
        assertThat(themeAreas(gangnam, gyeonggi)).containsExactly(gangnam);
        assertThat(themeAreas(null, null)).hasSize(3);
    }

    private List<String> storeAreas(String area, String region) {
        return repo.findApprovedStores(area, region, kw).stream().map(Store::getArea).toList();
    }

    private List<String> themeAreas(String area, String region) {
        return repo.findPublicThemes(area, region, null, kw, null, null).stream().map(t -> t.getStore().getArea()).toList();
    }

    private Region region(String name, int order) {
        Region r = new Region();
        r.setName(name);
        r.setSortOrder(order);
        em.persist(r);
        return r;
    }

    private void area(Region r, String name) {
        Area a = new Area();
        a.setRegion(r);
        a.setName(name);
        a.setSortOrder(1);
        em.persist(a);
    }

    private Store store(Member owner, String area) {
        Store s = new Store();
        s.setOwner(owner);
        s.setName("지역테스트" + tag + area);
        s.setArea(area);
        s.setAddress("주소");
        s.setPhone("02-000-0000");
        s.setWebsiteUrl("https://example.com");
        s.setHours("10-22");
        s.setIntro("소개");
        s.setBizPlaceNo("1234567890");
        s.setStatus(StoreStatus.APPROVED);
        s.setApprovedAt(java.time.LocalDateTime.now());
        em.persist(s);
        return s;
    }

    private void theme(Store store) {
        Theme t = new Theme();
        t.setStore(store);
        t.setTitle("지역테스트" + tag + store.getArea() + "테마");
        t.setDescription("설명");
        t.setDifficulty(3);
        t.setPlayMinutes(60);
        t.setMinPeople(2);
        t.setMaxPeople(4);
        t.setPrice(20000);
        t.setBookingUrl("https://example.com");
        t.setSource(ThemeSource.OWNER);
        t.setStatus(ThemeStatus.OPEN);
        em.persist(t);
    }
}

package com.escaplix.backend.repository;

import com.escaplix.backend.entity.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.*;

/**
 * B · 손님 화면(테마·매장 탐색)용 조회 모음 (B-1 ~ B-11).
 * 조건이 많아서 JPQL 을 직접 쓴다. 값은 모두 setParameter 로 바인딩한다 (문자열 이어 붙이기 금지, 명세 1-7).
 * "공개된 테마" = THEME.status = OPEN 이면서 STORE.status = APPROVED.
 */
@Repository
public class ThemeBrowseRepository {

    /** Oracle IN 목록은 1000개까지라서 나눠서 묻는다 */
    private static final int IN_CHUNK = 900;

    @PersistenceContext
    private EntityManager em;

    // ---------------------------------------------------------------- 테마

    /**
     * B-1 공개 테마 검색. null 인 조건은 빼고 묻는다. 정렬은 서비스에서 (평균 별점은 계산 값이라)
     * @param keywordLike 띄어쓰기를 지운 검색어로 만든 LIKE 패턴 (likePattern 으로 만든다)
     * @param dayType     그날 유형 시간표가 하나라도 있는 테마만
     */
    public List<Theme> findPublicThemes(String area, Integer people, String keywordLike, Long genreId, DayType dayType) {
        StringBuilder jpql = new StringBuilder(
                "select t from Theme t join fetch t.store s" +
                " where t.status = :open and s.status = :approved");
        Map<String, Object> params = new HashMap<>();
        params.put("open", ThemeStatus.OPEN);
        params.put("approved", StoreStatus.APPROVED);

        if (area != null) {
            jpql.append(" and s.area = :area");
            params.put("area", area);
        }
        if (people != null) {
            jpql.append(" and t.minPeople <= :people and t.maxPeople >= :people");
            params.put("people", people);
        }
        if (keywordLike != null) {
            // 테마 이름 또는 매장 이름. 저장된 값의 띄어쓰기도 지우고 비교
            jpql.append(" and (replace(t.title, ' ', '') like :kw escape '\\'" +
                        " or replace(s.name, ' ', '') like :kw escape '\\')");
            params.put("kw", keywordLike);
        }
        if (genreId != null) {
            jpql.append(" and exists (select 1 from ThemeGenre tg where tg.theme = t and tg.genre.genreId = :genreId)");
            params.put("genreId", genreId);
        }
        if (dayType != null) {
            jpql.append(" and exists (select 1 from TimeSlot ts where ts.theme = t and ts.dayType = :dayType)");
            params.put("dayType", dayType);
        }

        TypedQuery<Theme> q = em.createQuery(jpql.toString(), Theme.class);
        params.forEach(q::setParameter);
        return q.getResultList();
    }

    /** B-2·B-3·B-4 공개 테마 하나 (매장 함께). 공개 안 됐으면 비어 있음 */
    public Optional<Theme> findPublicTheme(Long themeId) {
        return em.createQuery(
                        "select t from Theme t join fetch t.store s" +
                        " where t.themeId = :id and t.status = :open and s.status = :approved", Theme.class)
                .setParameter("id", themeId)
                .setParameter("open", ThemeStatus.OPEN)
                .setParameter("approved", StoreStatus.APPROVED)
                .getResultStream().findFirst();
    }

    /** B-9·B-10·B-11 여러 매장의 OPEN 테마 (매장 승인 여부는 부르는 쪽에서 이미 거름) */
    public List<Theme> findOpenThemesOfStores(Collection<Long> storeIds) {
        return inChunks(storeIds, ids -> em.createQuery(
                        "select t from Theme t join fetch t.store s" +
                        " where s.storeId in :ids and t.status = :open", Theme.class)
                .setParameter("ids", ids)
                .setParameter("open", ThemeStatus.OPEN)
                .getResultList());
    }

    /** 테마별 장르 (장르 번호 순). 테마마다 따로 묻지 않고 한 번에 */
    public Map<Long, List<Genre>> findGenresByThemeIds(Collection<Long> themeIds) {
        Map<Long, List<Genre>> map = new HashMap<>();
        List<ThemeGenre> rows = inChunks(themeIds, ids -> em.createQuery(
                        "select tg from ThemeGenre tg join fetch tg.genre g" +
                        " where tg.theme.themeId in :ids order by g.genreId", ThemeGenre.class)
                .setParameter("ids", ids)
                .getResultList());
        for (ThemeGenre tg : rows) {
            map.computeIfAbsent(tg.getTheme().getThemeId(), k -> new ArrayList<>()).add(tg.getGenre());
        }
        return map;
    }

    /** 테마별 모집 중 파티 수: RECRUITING 이면서 기한이 남은 것 (명세 B-1 recruitingPartyCount) */
    public Map<Long, Integer> countRecruitingParties(Collection<Long> themeIds, LocalDateTime now) {
        Map<Long, Integer> map = new HashMap<>();
        List<Object[]> rows = inChunks(themeIds, ids -> em.createQuery(
                        "select p.theme.themeId, count(p) from Party p" +
                        " where p.theme.themeId in :ids and p.status = :recruiting and p.deadlineAt > :now" +
                        " group by p.theme.themeId", Object[].class)
                .setParameter("ids", ids)
                .setParameter("recruiting", PartyStatus.RECRUITING)
                .setParameter("now", now)
                .getResultList());
        for (Object[] r : rows) {
            map.put((Long) r[0], ((Number) r[1]).intValue());
        }
        return map;
    }

    // ---------------------------------------------------------------- 이번 주 인기 (B-1 sort=popular, D-9 대신 목록)

    /**
     * 이번 주 인기 점수: 최근 7일 동안 새로 생긴 활동으로 매긴다 (결정 C2)
     *   점수 = 새 파티 × 3 + 새 찜 × 2 + 새 후기 × 1   (같으면 평점순은 부르는 쪽에서)
     * 누적이 아니라 최근 7일만 세서, 주마다 순위가 바뀐다
     */
    public static final int POPULAR_DAYS = 7;
    public static final int PARTY_POINT = 3, WISH_POINT = 2, REVIEW_POINT = 1;

    /** 테마별 이번 주 인기 점수 */
    public Map<Long, Integer> weeklyScores(Collection<Long> themeIds, LocalDateTime now) {
        LocalDateTime since = now.minusDays(POPULAR_DAYS);
        Map<Long, Integer> newParties = countPartiesSince(themeIds, since);
        Map<Long, Integer> newWishes = countWishesSince(themeIds, since);
        Map<Long, Integer> newReviews = countReviewsSince(themeIds, since);
        Map<Long, Integer> score = new HashMap<>();
        for (Long id : themeIds) {
            score.put(id, newParties.getOrDefault(id, 0) * PARTY_POINT
                    + newWishes.getOrDefault(id, 0) * WISH_POINT
                    + newReviews.getOrDefault(id, 0) * REVIEW_POINT);
        }
        return score;
    }

    /** 테마별 since 이후 새로 생긴 찜 수 */
    public Map<Long, Integer> countWishesSince(Collection<Long> themeIds, LocalDateTime since) {
        return countByTheme(themeIds, ids -> em.createQuery(
                        "select w.theme.themeId, count(w) from Wishlist w" +
                        " where w.theme.themeId in :ids and w.createdAt >= :since group by w.theme.themeId", Object[].class)
                .setParameter("ids", ids)
                .setParameter("since", since)
                .getResultList());
    }

    /** 테마별 since 이후 새로 만들어진 파티 수 (삭제된 파티는 빼고, 마감·정원 찬 파티는 셈) */
    public Map<Long, Integer> countPartiesSince(Collection<Long> themeIds, LocalDateTime since) {
        return countByTheme(themeIds, ids -> em.createQuery(
                        "select p.theme.themeId, count(p) from Party p" +
                        " where p.theme.themeId in :ids and p.createdAt >= :since and p.status <> :deleted" +
                        " group by p.theme.themeId", Object[].class)
                .setParameter("ids", ids)
                .setParameter("since", since)
                .setParameter("deleted", PartyStatus.DELETED)
                .getResultList());
    }

    /** 테마별 since 이후 새로 쓴 후기 수 (숨긴 후기는 빼고) */
    public Map<Long, Integer> countReviewsSince(Collection<Long> themeIds, LocalDateTime since) {
        return countByTheme(themeIds, ids -> em.createQuery(
                        "select r.theme.themeId, count(r) from Review r" +
                        " where r.theme.themeId in :ids and r.createdAt >= :since and r.status = :visible" +
                        " group by r.theme.themeId", Object[].class)
                .setParameter("ids", ids)
                .setParameter("since", since)
                .setParameter("visible", ReviewStatus.VISIBLE)
                .getResultList());
    }

    private Map<Long, Integer> countByTheme(Collection<Long> themeIds,
                                            java.util.function.Function<List<Long>, List<Object[]>> query) {
        Map<Long, Integer> map = new HashMap<>();
        for (Object[] r : inChunks(themeIds, query)) {
            map.put((Long) r[0], ((Number) r[1]).intValue());
        }
        return map;
    }

    // ---------------------------------------------------------------- 찜

    /** 내가 찜한 테마 번호들 (wished 채우기) */
    public Set<Long> findWishedThemeIds(Long memberId) {
        return new HashSet<>(em.createQuery(
                        "select w.theme.themeId from Wishlist w where w.member.memberId = :mid", Long.class)
                .setParameter("mid", memberId)
                .getResultList());
    }

    /** B-6 찜 목록 (테마·매장 함께), 최근 찜한 순 */
    public List<Wishlist> findWishesWithTheme(Long memberId) {
        return em.createQuery(
                        "select w from Wishlist w join fetch w.theme t join fetch t.store" +
                        " where w.member.memberId = :mid order by w.createdAt desc, w.wishId desc", Wishlist.class)
                .setParameter("mid", memberId)
                .getResultList();
    }

    // ---------------------------------------------------------------- 매장

    /** B-8 승인된 매장의 지역 (중복 없이) */
    public List<String> findApprovedAreas() {
        return em.createQuery(
                        "select distinct s.area from Store s where s.status = :approved", String.class)
                .setParameter("approved", StoreStatus.APPROVED)
                .getResultList();
    }

    /** B-9 승인된 매장 검색 (이름 순) */
    public List<Store> findApprovedStores(String area, String keywordLike) {
        StringBuilder jpql = new StringBuilder("select s from Store s where s.status = :approved");
        if (area != null) jpql.append(" and s.area = :area");
        if (keywordLike != null) jpql.append(" and replace(s.name, ' ', '') like :kw escape '\\'");
        jpql.append(" order by s.name, s.storeId");

        TypedQuery<Store> q = em.createQuery(jpql.toString(), Store.class)
                .setParameter("approved", StoreStatus.APPROVED);
        if (area != null) q.setParameter("area", area);
        if (keywordLike != null) q.setParameter("kw", keywordLike);
        return q.getResultList();
    }

    /** B-10 승인된 매장 하나 */
    public Optional<Store> findApprovedStore(Long storeId) {
        return em.createQuery("select s from Store s where s.storeId = :id and s.status = :approved", Store.class)
                .setParameter("id", storeId)
                .setParameter("approved", StoreStatus.APPROVED)
                .getResultStream().findFirst();
    }

    /** B-11 승인되었고 위치(lat·lng)가 둘 다 있는 매장 */
    public List<Store> findApprovedStoresWithLocation() {
        return em.createQuery(
                        "select s from Store s where s.status = :approved" +
                        " and s.lat is not null and s.lng is not null order by s.name, s.storeId", Store.class)
                .setParameter("approved", StoreStatus.APPROVED)
                .getResultList();
    }

    // ---------------------------------------------------------------- 도우미

    /**
     * 검색어 → LIKE 패턴. 띄어쓰기를 모두 지우고, %·_ 는 글자 그대로 찾도록 \ 로 막는다.
     * 비어 있으면 null (조건 없음)
     */
    public static String likePattern(String keyword) {
        if (keyword == null) return null;
        String k = keyword.replaceAll("\\s+", "");
        if (k.isEmpty()) return null;
        k = k.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
        return "%" + k + "%";
    }

    private <T> List<T> inChunks(Collection<Long> ids, java.util.function.Function<List<Long>, List<T>> query) {
        if (ids == null || ids.isEmpty()) return new ArrayList<>();
        List<Long> all = new ArrayList<>(ids);
        List<T> out = new ArrayList<>();
        for (int i = 0; i < all.size(); i += IN_CHUNK) {
            out.addAll(query.apply(all.subList(i, Math.min(i + IN_CHUNK, all.size()))));
        }
        return out;
    }
}

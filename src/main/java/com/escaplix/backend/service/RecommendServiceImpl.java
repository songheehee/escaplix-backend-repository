package com.escaplix.backend.service;

import com.escaplix.backend.common.*;
import com.escaplix.backend.common.auth.*;
import com.escaplix.backend.dto.review.*;
import com.escaplix.backend.dto.theme.*;
import com.escaplix.backend.entity.*;
import com.escaplix.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * RecommendService 구현 (D 담당): 선호 장르(D-7, D-8), 맞춤 추천(D-9), 내 통계(D-10).
 * 지역 표(AREA·REGION)는 B 의 AreaRepository·RegionRepository 를 읽기만 한다.
 * 공개 테마·장르·모집 파티 조회는 B 의 ThemeBrowseRepository 를 그대로 쓴다 (같은 "공개" 기준).
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecommendServiceImpl implements RecommendService {

    /** 추천 개수 (D-9) */
    private static final int RECOMMEND_SIZE = 5;
    /** "새로 문을 연" 기준 일수 (D-9) */
    private static final int NEW_THEME_DAYS = 14;

    /** 평점순: B-1 sort=rate 와 같은 기준을 그대로 씀 (평균 별점 → 후기 많은 순 → 최신순) */
    private static final Comparator<Theme> BY_RATING = ThemeServiceImpl.BY_RATING;

    private final MemberGenreRepository memberGenreRepository;
    private final GenreRepository genreRepository;
    private final MemberRepository memberRepository;
    private final ThemeRepository themeRepository;
    private final ReviewRepository reviewRepository;
    private final ThemeBrowseRepository themeBrowseRepository;   // B 의 공개 테마 조회 (읽기만)
    private final AreaRepository areaRepository;                 // 세부 지역 → 큰 지역 (D-10, 읽기만)
    private final RegionRepository regionRepository;             // 큰 지역 목록 (D-10, 읽기만)

    // ======================================================== D-7, D-8 선호 장르

    /** D-7 내 선호 장르. 장르 번호순 */
    @Override
    public List<GenreResponse> getMyGenres(LoginMember login) {
        List<MemberGenre> rows = memberGenreRepository.findByMember_MemberId(login.memberId());
        return rows.stream()
                .map(mg -> new GenreResponse(mg.getGenre().getGenreId(), mg.getGenre().getName()))
                .sorted(Comparator.comparing(GenreResponse::genreId))
                .toList();
    }

    /** D-8 선호 장르 통째 교체. 한 트랜잭션에서 지우고 다시 넣기 */
    @Override
    @Transactional
    public List<GenreResponse> saveMyGenres(LoginMember login, GenreIdsRequest req) {
        Long myId = login.memberId();

        // 1. 같은 번호가 두 번 오면 하나로 (UNIQUE(member_id, genre_id) 위반 방지)
        List<Long> genreIds = new LinkedHashSet<>(req.genreIds()).stream().toList();

        // 2. 보낸 장르가 전부 실제로 있는지 확인. 하나라도 없으면 400
        List<Genre> genres = genreRepository.findAllById(genreIds);
        if (genres.size() != genreIds.size()) {
            throw ApiException.badRequest("없는 장르가 포함되어 있습니다.");
        }

        // 3. 내 기존 선호 장르 전부 지우기
        memberGenreRepository.deleteByMember_MemberId(myId);
        memberGenreRepository.flush();   // DELETE 를 INSERT 보다 먼저 DB에 보냄 (UNIQUE(member_id, genre_id) 충돌 방지)

        // 4. 새로 고른 장르 넣기 (빈 배열이면 아무것도 안 넣음 = 전부 해제)
        Member me = memberRepository.getReferenceById(myId);
        for (Genre genre : genres) {
            MemberGenre mg = new MemberGenre();
            mg.setMember(me);
            mg.setGenre(genre);
            memberGenreRepository.save(mg);
        }

        // 5. 저장된 결과를 D-7 모양으로 돌려주기
        return genres.stream()
                .map(g -> new GenreResponse(g.getGenreId(), g.getName()))
                .sorted(Comparator.comparing(GenreResponse::genreId))
                .toList();
    }

    // ======================================================== D-9 맞춤 추천

    /** D-9 맞춤 추천 (메인 배너): 선호 장르 겹치는 공개 테마, 후기 쓴 테마 제외, 최대 5개 */
    @Override
    public List<RecommendItem> getRecommendations(LoginMember login) {
        Long myId = login.memberId();
        LocalDateTime now = LocalDateTime.now();

        // 1. 내 선호 장르 번호들
        Set<Long> myGenreIds = memberGenreRepository.findByMember_MemberId(myId).stream()
                .map(mg -> mg.getGenre().getGenreId())
                .collect(Collectors.toSet());

        // 2. 공개 테마 전부 (테마 OPEN + 매장 APPROVED, 매장 함께) 와 테마별 장르 (한 번에)
        List<Theme> publicThemes = themeBrowseRepository.findPublicThemes(null, null, null, null, null, null);
        Map<Long, List<Genre>> genresByTheme = themeBrowseRepository.findGenresByThemeIds(
                publicThemes.stream().map(Theme::getThemeId).toList());

        // 3. 이미 후기를 쓴 테마 번호들
        Set<Long> reviewedThemeIds = reviewRepository
                .findByMember_MemberIdOrderByPlayedDateDescReviewIdDesc(myId).stream()
                .map(r -> r.getTheme().getThemeId())
                .collect(Collectors.toSet());

        // 4. 후보: 후기 안 쓴 테마 중 선호 장르가 하나라도 겹치는 것. 겹치는 장르를 같이 기억
        Map<Long, List<Genre>> overlapByTheme = new HashMap<>();
        List<Theme> matched = new ArrayList<>();
        for (Theme t : publicThemes) {
            if (reviewedThemeIds.contains(t.getThemeId())) continue;
            List<Genre> overlap = genresOf(genresByTheme, t).stream()
                    .filter(g -> myGenreIds.contains(g.getGenreId()))
                    .toList();
            if (!overlap.isEmpty()) {
                matched.add(t);
                overlapByTheme.put(t.getThemeId(), overlap);
            }
        }

        // 5. 선호 장르가 없거나 맞는 테마가 없으면: 이번 주 인기 상위 5개 (비회원 배너와 같은 기준, 결정 C2)
        //    여기서도 이미 후기를 쓴 테마는 뺀다
        if (matched.isEmpty()) {
            List<Theme> notReviewed = publicThemes.stream()
                    .filter(t -> !reviewedThemeIds.contains(t.getThemeId()))
                    .toList();
            Map<Long, Integer> weekly = themeBrowseRepository.weeklyScores(
                    notReviewed.stream().map(Theme::getThemeId).toList(), now);
            List<Theme> top = notReviewed.stream()
                    .sorted(ThemeServiceImpl.byPopular(weekly))
                    .limit(RECOMMEND_SIZE)
                    .toList();
            return toRecommendItems(top, genresByTheme, now, t -> "이번 주 인기 테마");
        }

        // 6. 정렬: 겹치는 장르 수 많은 순 → 평점순(평균 별점 → 후기 많은 순 → 최신), 최대 5개
        List<Theme> picked = matched.stream()
                .sorted(Comparator.<Theme>comparingInt(t -> overlapByTheme.get(t.getThemeId()).size())
                        .reversed()
                        .thenComparing(BY_RATING))
                .limit(RECOMMEND_SIZE)
                .toList();

        // 7. reason 문장 재료: 닉네임(DB 최신 값), 장르별 평점 1위 테마, "새로 문을 연" 기준 시각
        String nickname = memberRepository.findById(myId).map(Member::getNickname).orElse("회원");
        Map<Long, Long> topThemeByGenre = topRatedThemeByGenre(publicThemes, genresByTheme);
        LocalDateTime newSince = now.minusDays(NEW_THEME_DAYS);

        return toRecommendItems(picked, genresByTheme, now,
                t -> reasonFor(t, overlapByTheme.get(t.getThemeId()), nickname, topThemeByGenre, newSince));
    }

    /**
     * 추천 문장 (API.md D-9 표, 위에서부터 먼저 맞는 것):
     * 장르 2개 이상 겹침 → 그 장르 평점 1위 → 최근 14일 등록 → 그 밖
     */
    private String reasonFor(Theme t, List<Genre> overlap, String nickname,
                             Map<Long, Long> topThemeByGenre, LocalDateTime newSince) {
        if (overlap.size() >= 2) {
            String names = overlap.stream().map(Genre::getName).collect(Collectors.joining(" · "));
            String all = (overlap.size() == 2) ? " 둘 다" : " 모두";
            return names + all + " 좋아하는 " + nickname + " 님께";
        }
        Genre g = overlap.get(0);
        if (t.getThemeId().equals(topThemeByGenre.get(g.getGenreId()))) {
            return g.getName() + " 장르 평점 1위";
        }
        if (t.getCreatedAt() != null && t.getCreatedAt().isAfter(newSince)) {
            return "새로 문을 연 " + g.getName() + " 테마";
        }
        return g.getName() + " 좋아하시면 꼭";
    }

    /** 장르 번호 → 그 장르의 공개 테마 중 평점 1위 테마 번호 (후기 없는 테마는 1위 후보에서 뺌) */
    private Map<Long, Long> topRatedThemeByGenre(List<Theme> themes, Map<Long, List<Genre>> genresByTheme) {
        Map<Long, Theme> best = new HashMap<>();
        for (Theme t : themes) {
            if (t.getAvgRating() == null) continue;
            for (Genre g : genresOf(genresByTheme, t)) {
                best.merge(g.getGenreId(), t, (a, b) -> BY_RATING.compare(a, b) <= 0 ? a : b);
            }
        }
        Map<Long, Long> result = new HashMap<>();
        best.forEach((genreId, t) -> result.put(genreId, t.getThemeId()));
        return result;
    }

    /** 테마들 → RecommendItem 목록. 모집 중 파티 수는 한 번에 센다 */
    private List<RecommendItem> toRecommendItems(List<Theme> themes, Map<Long, List<Genre>> genresByTheme,
                                                 LocalDateTime now, Function<Theme, String> reasonOf) {
        Map<Long, Integer> recruiting = themeBrowseRepository.countRecruitingParties(
                themes.stream().map(Theme::getThemeId).toList(), now);

        return themes.stream()
                .map(t -> new RecommendItem(
                        t.getThemeId(),
                        t.getTitle(),
                        t.getStore().getName(),
                        genresOf(genresByTheme, t).stream().map(Genre::getName).toList(),
                        t.getDifficulty(),
                        t.getPlayMinutes(),
                        t.getMinPeople(),
                        t.getMaxPeople(),
                        t.getPrice(),
                        t.getDescription(),
                        t.getPosterUrl(),
                        t.getAvgRating(),
                        recruiting.getOrDefault(t.getThemeId(), 0),
                        reasonOf.apply(t)
                ))
                .toList();
    }

    // ======================================================== D-10 내 통계

    /** D-10 내 통계. 내 후기 전부 기준 (숨김 포함) */
    @Override
    public StatsResponse getStats(LoginMember login) {
        List<Review> reviews = reviewRepository
                .findByMember_MemberIdOrderByPlayedDateDescReviewIdDesc(login.memberId());

        // 1. 플레이 수, 탈출 성공 수, 노힌트 탈출 수 (힌트 0개로 성공)
        int playCount = reviews.size();
        int escapeCount = (int) reviews.stream().filter(r -> Boolean.TRUE.equals(r.getEscaped())).count();
        int noHintCount = (int) reviews.stream()
                .filter(r -> Boolean.TRUE.equals(r.getEscaped()) && r.getHintCount() != null && r.getHintCount() == 0)
                .count();

        // 2. 평균 남은 시간: 탈출 성공한 판만 (실패는 남은 시간이 0 이라 빼고 계산)
        //    초 평균은 반올림, 비율은 판마다 남은 시간 / 제한 시간 을 구해서 평균
        List<Review> escapedReviews = reviews.stream()
                .filter(r -> Boolean.TRUE.equals(r.getEscaped()))
                .toList();
        Integer avgRemainSeconds = null;
        int avgRemainRate = 0;
        if (!escapedReviews.isEmpty()) {
            long remainSum = 0;
            double rateSum = 0;
            for (Review r : escapedReviews) {
                int remain = nz(r.getRemainMinutes()) * 60 + nz(r.getRemainSeconds());
                int limit = nz(r.getTheme().getPlayMinutes()) * 60;
                remainSum += remain;
                rateSum += (limit == 0) ? 0 : Math.min(1.0, (double) remain / limit);
            }
            avgRemainSeconds = (int) Math.round((double) remainSum / escapedReviews.size());
            avgRemainRate = (int) Math.round(rateSum * 100 / escapedReviews.size());
        }

        // 3. 전국 제패: 지금 공개된 테마를 큰 지역별로 묶고, 그중 내가 후기를 쓴(=플레이한) 테마를 센다
        //    매장의 세부 지역 이름(store.area) → AREA → 큰 지역(REGION)
        Set<Long> playedThemeIds = reviews.stream()
                .map(r -> r.getTheme().getThemeId())
                .collect(Collectors.toSet());
        Map<String, Long> regionIdByArea = new HashMap<>();
        for (Area a : areaRepository.findAllOrdered()) {
            regionIdByArea.put(a.getName(), a.getRegion().getRegionId());
        }
        Map<Long, Integer> themeCountByRegion = new HashMap<>();
        Map<Long, Integer> playedCountByRegion = new HashMap<>();
        int themeCount = 0;
        int conquerCount = 0;
        for (Theme t : themeBrowseRepository.findPublicThemes(null, null, null, null, null, null)) {
            boolean played = playedThemeIds.contains(t.getThemeId());
            themeCount++;
            if (played) conquerCount++;
            Long regionId = regionIdByArea.get(t.getStore().getArea());
            if (regionId == null) continue;   // 지역 표에 없는 매장 (DB FK 가 막지만 혹시 몰라서)
            themeCountByRegion.merge(regionId, 1, Integer::sum);
            if (played) playedCountByRegion.merge(regionId, 1, Integer::sum);
        }

        // 4. 큰 지역 8개 전부 화면 순서대로 (테마가 없는 지역도 0 / 0 으로)
        List<StatsResponse.RegionStat> byRegion = regionRepository.findAll().stream()
                .sorted(Comparator.comparing(Region::getSortOrder).thenComparing(Region::getRegionId))
                .map(rg -> {
                    int played = playedCountByRegion.getOrDefault(rg.getRegionId(), 0);
                    int total = themeCountByRegion.getOrDefault(rg.getRegionId(), 0);
                    return new StatsResponse.RegionStat(rg.getRegionId(), rg.getName(), played, total, percent(played, total));
                })
                .toList();

        return new StatsResponse(
                playCount, escapeCount, percent(escapeCount, playCount),
                noHintCount, percent(noHintCount, playCount),
                avgRemainSeconds, avgRemainRate,
                conquerCount, themeCount, percent(conquerCount, themeCount),
                byRegion);
    }

    private static int nz(Integer n) { return n == null ? 0 : n; }

    /** part / whole 을 % 로 반올림. whole 이 0 이면 0 */
    private static int percent(int part, int whole) {
        return (whole == 0) ? 0 : (int) Math.round(part * 100.0 / whole);
    }

    // ======================================================== 공용 도우미

    /** 테마의 장르 목록 (없으면 빈 목록) */
    private List<Genre> genresOf(Map<Long, List<Genre>> genresByTheme, Theme t) {
        return genresByTheme.getOrDefault(t.getThemeId(), List.of());
    }
}
package com.escaplix.backend.service;

import com.escaplix.backend.common.*;
import com.escaplix.backend.common.auth.*;
import com.escaplix.backend.dto.theme.*;
import com.escaplix.backend.entity.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import com.escaplix.backend.common.time.DayTypeResolver;
import com.escaplix.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * ThemeService 구현 (B 담당). 명세: docs/API.md B-1 ~ B-7
 * 손님 화면에 보이는 테마 = THEME.status OPEN 이면서 STORE.status APPROVED (ThemeBrowseRepository).
 * 조회는 클래스의 readOnly 트랜잭션, 저장·삭제 메서드에는 @Transactional.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ThemeServiceImpl implements ThemeService {

    private static final DateTimeFormatter HH_MM = DateTimeFormatter.ofPattern("HH:mm");

    /** 평점순: 평균 별점 높은 순, 후기 없는 테마는 뒤로. 같으면 후기 많은 순 → 최신순 */
    static final Comparator<Theme> BY_RATING = Comparator
            .comparing(Theme::getAvgRating, Comparator.nullsLast(Comparator.reverseOrder()))
            .thenComparing(t -> nz(t.getReviewCount()), Comparator.reverseOrder())
            .thenComparing(Theme::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder()));

    /** 최신순: 등록일 최신 → 번호 큰 순 */
    static final Comparator<Theme> BY_NEW = Comparator
            .comparing(Theme::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder()))
            .thenComparing(Theme::getThemeId, Comparator.reverseOrder());

    private final ThemeBrowseRepository browseRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final WishlistRepository wishlistRepository;
    private final GenreRepository genreRepository;
    private final MemberRepository memberRepository;
    private final DayTypeResolver dayTypeResolver;

    @Override
    public List<ThemeListItem> findThemes(LoginMember login, ThemeSearchCondition cond) {
        // 1) 조건 검사
        String sort = blankToNull(cond.sort()) == null ? "rate" : cond.sort().trim();
        if (!sort.equals("rate") && !sort.equals("new")) {
            throw ApiException.badRequest("정렬은 rate(평점순) 또는 new(최신순)만 고를 수 있습니다.");
        }
        if (cond.people() != null && cond.people() < 1) {
            throw ApiException.badRequest("인원은 1명 이상으로 골라 주세요.");
        }
        DayType dayType = null;
        if (cond.date() != null) {
            checkNotPast(cond.date());
            dayType = dayTypeResolver.of(cond.date());
        }

        // 2) 찾기 (keyword: 테마 이름 또는 매장 이름, 띄어쓰기 무시)
        List<Theme> themes = new ArrayList<>(browseRepository.findPublicThemes(
                blankToNull(cond.area()), cond.people(),
                ThemeBrowseRepository.likePattern(cond.keyword()), cond.genreId(), dayType));
        themes.sort(sort.equals("new") ? BY_NEW : BY_RATING);

        // 3) 장르·파티 수·찜 여부는 테마마다 묻지 않고 한 번에
        List<Long> ids = themes.stream().map(Theme::getThemeId).toList();
        Map<Long, List<Genre>> genres = browseRepository.findGenresByThemeIds(ids);
        Map<Long, Integer> parties = browseRepository.countRecruitingParties(ids, LocalDateTime.now());
        Set<Long> wished = login == null ? Set.of() : browseRepository.findWishedThemeIds(login.memberId());

        return themes.stream().map(t -> new ThemeListItem(
                t.getThemeId(), t.getTitle(),
                t.getStore().getStoreId(), t.getStore().getName(), t.getStore().getArea(),
                genreNames(genres.get(t.getThemeId())),
                t.getDifficulty(), t.getPlayMinutes(), t.getMinPeople(), t.getMaxPeople(), t.getPrice(),
                t.getPosterUrl(), t.getAvgRating(), nz(t.getReviewCount()),
                parties.getOrDefault(t.getThemeId(), 0), t.getCreatedAt(),
                wished.contains(t.getThemeId())
        )).toList();
    }

    @Override
    public ThemeDetailResponse getTheme(LoginMember login, Long themeId) {
        Theme t = findPublicTheme(themeId);
        Store s = t.getStore();
        List<Long> id = List.of(themeId);

        List<GenreResponse> genres = browseRepository.findGenresByThemeIds(id)
                .getOrDefault(themeId, List.of()).stream()
                .map(g -> new GenreResponse(g.getGenreId(), g.getName()))
                .toList();
        int parties = browseRepository.countRecruitingParties(id, LocalDateTime.now()).getOrDefault(themeId, 0);
        boolean wished = login != null
                && wishlistRepository.existsByMember_MemberIdAndTheme_ThemeId(login.memberId(), themeId);

        return new ThemeDetailResponse(
                t.getThemeId(), t.getTitle(), t.getDescription(), genres,
                t.getDifficulty(), t.getPlayMinutes(), t.getMinPeople(), t.getMaxPeople(), t.getPrice(),
                t.getPosterUrl(), t.getBookingUrl(),
                new ThemeDetailResponse.StoreBrief(s.getStoreId(), s.getName(), s.getArea(), s.getAddress(), s.getPhone()),
                t.getAvgRating(), nz(t.getReviewCount()), parties, wished);
    }

    @Override
    public DaySlotsResponse getSlots(Long themeId, LocalDate date) {
        checkNotPast(date);
        Theme t = findPublicTheme(themeId);

        // 토·일·공휴일 → WEEKEND (결정 T1). 규칙은 DayTypeResolver 한 곳에서만
        DayType dayType = dayTypeResolver.of(date);
        String holidayName = dayTypeResolver.holidayName(date).orElse(null);

        // 오늘이면 이미 지난 회차는 뺀다
        LocalTime nowTime = date.equals(LocalDate.now()) ? LocalTime.now() : null;
        List<SlotItem> slots = timeSlotRepository.findByTheme_ThemeIdAndDayTypeOrderByStartTimeAsc(themeId, dayType)
                .stream()
                .filter(s -> nowTime == null || LocalTime.parse(s.getStartTime(), HH_MM).isAfter(nowTime))
                .map(s -> new SlotItem(s.getSlotId(), s.getStartTime(),
                        LocalTime.parse(s.getStartTime(), HH_MM).plusMinutes(t.getPlayMinutes()).format(HH_MM)))
                .toList();

        return new DaySlotsResponse(date, dayType, holidayName, slots);
    }

    @Override
    @Transactional
    public WishIdResponse addWish(LoginMember login, Long themeId) {
        Theme t = findPublicTheme(themeId);
        if (wishlistRepository.existsByMember_MemberIdAndTheme_ThemeId(login.memberId(), themeId)) {
            throw ApiException.conflict("이미 찜한 테마입니다.");
        }
        Wishlist w = new Wishlist();
        w.setMember(memberRepository.getReferenceById(login.memberId()));
        w.setTheme(t);
        try {
            // 같은 사람이 동시에 두 번 눌러도 UNIQUE(member_id, theme_id) 가 막아 준다
            wishlistRepository.saveAndFlush(w);
        } catch (DataIntegrityViolationException e) {
            throw ApiException.conflict("이미 찜한 테마입니다.");
        }
        return new WishIdResponse(w.getWishId());
    }

    @Override
    @Transactional
    public void removeWish(LoginMember login, Long themeId) {
        // 운영 종료(CLOSED) 테마도 찜 목록에 남아 있으니, 공개 여부와 상관없이 지울 수 있게 한다
        Wishlist w = wishlistRepository.findByMember_MemberIdAndTheme_ThemeId(login.memberId(), themeId)
                .orElseThrow(() -> ApiException.notFound("찜하지 않은 테마입니다."));
        wishlistRepository.delete(w);
    }

    @Override
    public List<WishItem> getMyWishes(LoginMember login) {
        // 최근 찜한 순. CLOSED 테마도 status 와 함께 남김
        return browseRepository.findWishesWithTheme(login.memberId()).stream()
                .map(w -> {
                    Theme t = w.getTheme();
                    return new WishItem(w.getWishId(), t.getThemeId(), t.getTitle(), t.getStore().getName(),
                            t.getStatus(), t.getPosterUrl(), w.getCreatedAt());
                })
                .toList();
    }

    @Override
    public List<GenreResponse> getGenres() {
        return genreRepository.findAll(Sort.by("genreId")).stream()
                .map(g -> new GenreResponse(g.getGenreId(), g.getName()))
                .toList();
    }

    // ---------------------------------------------------------------- 도우미

    /** 공개된 테마만. 승인 전·반려·운영 종료이거나 매장이 승인 전이면 404 (명세 B-2) */
    private Theme findPublicTheme(Long themeId) {
        return browseRepository.findPublicTheme(themeId)
                .orElseThrow(() -> ApiException.notFound("공개되지 않았거나 없는 테마입니다."));
    }

    private static void checkNotPast(LocalDate date) {
        if (date.isBefore(LocalDate.now())) {
            throw ApiException.badRequest("오늘 이후 날짜만 고를 수 있습니다.");
        }
    }

    static List<String> genreNames(List<Genre> genres) {
        return genres == null ? List.of() : genres.stream().map(Genre::getName).toList();
    }

    static int nz(Integer n) {
        return n == null ? 0 : n;
    }

    private static String blankToNull(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }
}

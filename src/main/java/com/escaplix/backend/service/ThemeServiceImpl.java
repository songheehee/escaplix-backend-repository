package com.escaplix.backend.service;

import com.escaplix.backend.common.*;
import com.escaplix.backend.common.auth.*;
import com.escaplix.backend.dto.theme.*;

import java.time.LocalDate;
import java.util.List;

import com.escaplix.backend.common.time.DayTypeResolver;
import com.escaplix.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * ThemeService 구현 (B 담당). 지금은 전부 501 "아직 구현 전". TODO 를 채우고 throw 줄을 지운다.
 * 조회는 클래스의 readOnly 트랜잭션, 저장·수정·삭제 메서드에는 @Transactional 이 붙어 있다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ThemeServiceImpl implements ThemeService {

    private final ThemeRepository themeRepository;
    private final ThemeGenreRepository themeGenreRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final WishlistRepository wishlistRepository;
    private final GenreRepository genreRepository;
    private final MemberRepository memberRepository;
    private final DayTypeResolver dayTypeResolver;

    @Override
    public List<ThemeListItem> findThemes(LoginMember login, ThemeSearchCondition cond) {
        // TODO(B) THEME.status=OPEN AND STORE.status=APPROVED 만
        // TODO(B) keyword: 테마 이름 또는 매장 이름 (띄어쓰기 무시), date: dayTypeResolver 로 그날 유형 시간표 있는 테마
        // TODO(B) people: min ≤ people ≤ max, sort: rate(기본, 후기 없는 건 뒤) / new
        // TODO(B) recruitingPartyCount = RECRUITING + 기한 남은 파티 수, wished 는 login 있을 때만
        throw ApiException.notImplemented("B-1 테마 목록·검색·필터");
    }

    @Override
    public ThemeDetailResponse getTheme(LoginMember login, Long themeId) {
        // TODO(B) 공개 안 된 테마(OPEN 아님 또는 매장 APPROVED 아님)면 404
        throw ApiException.notImplemented("B-2 테마 상세");
    }

    @Override
    public DaySlotsResponse getSlots(Long themeId, LocalDate date) {
        // TODO(B) 오늘 이전 날짜면 400
        // TODO(B) dayType = dayTypeResolver.of(date), holidayName = dayTypeResolver.holidayName(date)
        // TODO(B) endTime = startTime + playMinutes, 오늘이면 지난 시각은 빼기
        throw ApiException.notImplemented("B-3 그날 시간표");
    }

    @Override
    @Transactional
    public WishIdResponse addWish(LoginMember login, Long themeId) {
        // TODO(B) 공개 안 된 테마면 404, 이미 찜했으면 409 "이미 찜한 테마입니다."
        throw ApiException.notImplemented("B-4 찜");
    }

    @Override
    @Transactional
    public void removeWish(LoginMember login, Long themeId) {
        // TODO(B) 찜하지 않았으면 404
        throw ApiException.notImplemented("B-5 찜 취소");
    }

    @Override
    public List<WishItem> getMyWishes(LoginMember login) {
        // TODO(B) 최근 찜한 순. CLOSED 테마도 status 와 함께 남김
        throw ApiException.notImplemented("B-6 찜 목록");
    }

    @Override
    public List<GenreResponse> getGenres() {
        throw ApiException.notImplemented("B-7 장르 목록");
    }
}

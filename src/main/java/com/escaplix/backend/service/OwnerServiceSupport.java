package com.escaplix.backend.service;

import com.escaplix.backend.common.ApiException;
import com.escaplix.backend.common.auth.LoginMember;
import com.escaplix.backend.dto.owner.OwnerThemeDetailResponse;
import com.escaplix.backend.entity.*;
import com.escaplix.backend.repository.ThemeGenreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.Comparator;
import java.util.List;

/**
 * C 담당 서비스 세 곳(OwnerStore·OwnerTheme·Admin)이 같이 쓰는 도우미.
 * - 주인 확인 (API.md 1-4 OWNER*)
 * - 테마 장르 이름, C-6·C-18 테마 상세 응답 만들기
 */
@Component
@RequiredArgsConstructor
public class OwnerServiceSupport {

    private final ThemeGenreRepository themeGenreRepository;

    /**
     * 사장: 내 매장이 아니면 403.
     * 관리자: 모든 매장 (매장 관리 개편 11/23. 사장 매장도 보고 고칠 수 있음, 고쳐도 승인 상태 그대로 = 결정 S3 확장)
     */
    public void checkStoreAccess(LoginMember login, Store store) {
        if (login.is(Role.ADMIN)) {
            return;
        }
        if (store.getOwner() == null || !store.getOwner().getMemberId().equals(login.memberId())) {
            throw ApiException.forbidden("내 매장이 아닙니다.");
        }
    }

    /** 폐업한 매장(과 그 테마)은 고칠 수 없다. 조회만 됨 */
    public void checkNotClosed(Store store) {
        if (store.getStatus() == StoreStatus.CLOSED) {
            throw ApiException.conflict("폐업한 매장은 고칠 수 없습니다.");
        }
    }

    /** 테마의 매장으로 주인 확인 */
    public void checkThemeAccess(LoginMember login, Theme theme) {
        checkStoreAccess(login, theme.getStore());
    }

    /** 장르 번호 순 */
    public List<Genre> genresOf(Long themeId) {
        return themeGenreRepository.findByTheme_ThemeId(themeId).stream()
                .map(ThemeGenre::getGenre)
                .sorted(Comparator.comparing(Genre::getGenreId))
                .toList();
    }

    public List<String> genreNames(Long themeId) {
        return genresOf(themeId).stream().map(Genre::getName).toList();
    }

    /** C-6 테마 수정용 상세 (C-18 관리자 상세에도 씀). 상태와 상관없이 만든다 */
    public OwnerThemeDetailResponse toThemeDetail(Theme t) {
        List<Genre> genres = genresOf(t.getThemeId());
        return new OwnerThemeDetailResponse(
                t.getThemeId(),
                t.getStore().getStoreId(),
                t.getTitle(),
                t.getDescription(),
                genres.stream().map(Genre::getGenreId).toList(),
                genres.stream().map(Genre::getName).toList(),
                t.getDifficulty(),
                t.getPlayMinutes(),
                t.getMinPeople(),
                t.getMaxPeople(),
                t.getPrice(),
                t.getPosterUrl(),
                t.getBookingUrl(),
                t.getStatus(),
                t.getSource(),
                t.getRejectReason(),
                t.getAvgRating(),
                orZero(t.getReviewCount()),
                t.getCreatedAt(),
                t.getUpdatedAt());
    }

    public static int orZero(Integer n) {
        return n == null ? 0 : n;
    }

    public static boolean hasFile(MultipartFile file) {
        return file != null && !file.isEmpty();
    }
}

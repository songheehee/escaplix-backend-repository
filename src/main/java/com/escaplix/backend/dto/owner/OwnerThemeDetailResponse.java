package com.escaplix.backend.dto.owner;

import com.escaplix.backend.entity.*;
import java.util.List;
import java.time.LocalDateTime;

/** C-6 테마 수정용 상세 (상태 상관없이). C-18 관리자 상세에도 같이 씀 */
public record OwnerThemeDetailResponse(
        Long themeId,
        Long storeId,
        String title,
        String description,
        List<Long> genreIds,
        List<String> genres,
        int difficulty,
        int playMinutes,
        int minPeople,
        int maxPeople,
        int price,
        String posterUrl,
        String bookingUrl,
        ThemeStatus status,
        ThemeSource source,
        String rejectReason,
        Double avgRating,
        int reviewCount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}

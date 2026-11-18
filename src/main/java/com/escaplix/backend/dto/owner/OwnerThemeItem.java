package com.escaplix.backend.dto.owner;

import com.escaplix.backend.entity.*;
import java.util.List;
import java.time.LocalDateTime;

/** C-5 매장의 테마 한 줄 (모든 상태) */
public record OwnerThemeItem(
        Long themeId,
        String title,
        List<String> genres,
        int playMinutes,
        ThemeStatus status,
        String rejectReason,
        Double avgRating,
        int reviewCount,
        LocalDateTime updatedAt
) {}

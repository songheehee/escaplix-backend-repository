package com.escaplix.backend.dto.review;

import com.escaplix.backend.entity.*;
import java.time.LocalDate;

/** D-5 내 기록 한 줄 (숨김 포함), D-2 응답 */
public record MyReviewItem(
        Long reviewId,
        Long themeId,
        String themeTitle,
        String storeName,
        int playMinutes,
        int rating,
        boolean escaped,
        int hintCount,
        int playerCount,
        int remainMinutes,
        int remainSeconds,
        LocalDate playedDate,
        String content,
        ReviewStatus status
) {}

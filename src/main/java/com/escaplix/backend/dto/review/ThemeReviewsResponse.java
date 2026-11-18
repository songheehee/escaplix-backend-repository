package com.escaplix.backend.dto.review;

import java.util.List;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** D-4 테마 후기 + 평균 (VISIBLE 만, 최신순) */
public record ThemeReviewsResponse(
        Double avgRating,
        int reviewCount,
        int playMinutes,
        List<Item> reviews
) {

    /** nickname 은 MEMBER JOIN (닉네임 바꾸면 바로 반영) */
    public record Item(Long reviewId, String nickname, int rating, boolean escaped, int hintCount, int playerCount,
                       int remainMinutes, int remainSeconds, LocalDate playedDate, String content, LocalDateTime createdAt,
                       boolean mine, boolean reportedByMe) {
    }
}

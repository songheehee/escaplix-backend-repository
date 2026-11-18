package com.escaplix.backend.dto.review;

import java.util.List;

/** D-10 내 통계 (선택 기능). 숨김 후기도 포함 */
public record StatsResponse(
        int playCount,
        int escapeCount,
        int escapeRate,
        GenreCount topGenre,
        List<GenreCount> byGenre
) {

    public record GenreCount(String name, int count) {
    }
}

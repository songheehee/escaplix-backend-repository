package com.escaplix.backend.dto.theme;

import java.util.List;

/** B-2 테마 상세. 공개 안 된 테마면 404 */
public record ThemeDetailResponse(
        Long themeId,
        String title,
        String description,
        List<GenreResponse> genres,
        int difficulty,
        int playMinutes,
        int minPeople,
        int maxPeople,
        int price,
        String posterUrl,
        String bookingUrl,
        StoreBrief store,
        Double avgRating,
        int reviewCount,
        int recruitingPartyCount,
        boolean wished
) {

    public record StoreBrief(Long storeId, String name, String area, String address, String phone) {
    }
}

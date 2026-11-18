package com.escaplix.backend.dto.store;

import java.util.List;
import java.math.BigDecimal;

/** B-10 매장 상세. "매장 웹사이트" 버튼 = websiteUrl. 승인 안 된 매장은 404 */
public record StoreDetailResponse(
        Long storeId,
        String name,
        String area,
        String address,
        String phone,
        String websiteUrl,
        String hours,
        String intro,
        /** 매장 사진 "/uploads/stores/...". 없으면 null */
        String photoUrl,
        BigDecimal lat,
        BigDecimal lng,
        Double avgRating,
        List<ThemeItem> themes
) {

    /** OPEN 테마만 */
    public record ThemeItem(Long themeId, String title, List<String> genres, int difficulty, int playMinutes,
                            int minPeople, int maxPeople, int price, String posterUrl, Double avgRating,
                            int reviewCount, int recruitingPartyCount) {
    }
}

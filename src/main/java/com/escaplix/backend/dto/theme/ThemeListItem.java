package com.escaplix.backend.dto.theme;

import java.util.List;
import java.time.LocalDateTime;

/** B-1 테마 카드. OPEN 테마 + APPROVED 매장만 */
public record ThemeListItem(
        Long themeId,
        String title,
        Long storeId,
        String storeName,
        String area,
        List<String> genres,
        int difficulty,
        int playMinutes,
        int minPeople,
        int maxPeople,
        int price,
        String posterUrl,
        Double avgRating,
        int reviewCount,
        int recruitingPartyCount,
        LocalDateTime createdAt,
        boolean wished
) {}

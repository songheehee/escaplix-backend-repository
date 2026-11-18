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
        /** 이번 주 인기 점수: 최근 7일 새 파티×3 + 새 찜×2 + 새 후기×1 (sort=popular 의 기준) */
        int weeklyScore,
        LocalDateTime createdAt,
        boolean wished
) {}

package com.escaplix.backend.dto.review;

import java.util.List;

/** D-9 맞춤 추천 (메인 배너). reason 문장은 서버가 만든다 (API.md D-9 표) */
public record RecommendItem(
        Long themeId,
        String title,
        String storeName,
        List<String> genres,
        int difficulty,
        int playMinutes,
        int minPeople,
        int maxPeople,
        int price,
        String description,
        String posterUrl,
        Double avgRating,
        int recruitingPartyCount,
        String reason
) {}

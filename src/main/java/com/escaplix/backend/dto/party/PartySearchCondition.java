package com.escaplix.backend.dto.party;

import java.util.List;

/** A-12 파티 목록 조건. genreIds=1,2 처럼 여러 개 */
public record PartySearchCondition(
        List<Long> genreIds,
        String keyword,
        Long themeId
) {}

package com.escaplix.backend.dto.party;

import java.util.List;

/** A-12 파티 목록 조건. genreIds=1,2 처럼 여러 개 */
public record PartySearchCondition(
        List<Long> genreIds,
        String keyword,
        String area,        // 세부 지역 이름 (홍대·강남…). 매장 지역과 같은 값
        String region,      // 큰 지역 이름 (서울·경기…): 그 안의 모든 세부 지역. area 가 있으면 area 가 우선
        Long themeId
) {}

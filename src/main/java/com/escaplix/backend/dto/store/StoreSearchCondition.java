package com.escaplix.backend.dto.store;


/** B-9 매장 목록 조건 */
public record StoreSearchCondition(
        String area,
        String region,      // 큰 지역 이름 (서울·경기…): 그 안의 모든 세부 지역. area 가 있으면 area 가 우선
        String keyword,
        String sort         // rate(평점순, 기본) / new(최신순 = 처음 승인된 때)
) {}

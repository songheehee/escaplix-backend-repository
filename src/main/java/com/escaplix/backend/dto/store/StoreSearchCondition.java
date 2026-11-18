package com.escaplix.backend.dto.store;


/** B-9 매장 목록 조건 */
public record StoreSearchCondition(
        String area,
        String keyword
) {}

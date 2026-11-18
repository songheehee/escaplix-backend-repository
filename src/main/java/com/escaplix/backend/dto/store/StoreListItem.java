package com.escaplix.backend.dto.store;

import java.util.List;

/** B-9 매장 카드 (APPROVED 매장만). 숫자들은 그 매장 OPEN 테마로 계산 */
public record StoreListItem(
        Long storeId,
        String name,
        String area,
        String address,
        String hours,
        List<String> genres,
        int themeCount,
        Double avgRating,
        int recruitingPartyCount
) {}

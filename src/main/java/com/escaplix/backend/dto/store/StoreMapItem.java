package com.escaplix.backend.dto.store;

import java.math.BigDecimal;

/** B-11 지도용 매장 (lat·lng 있는 APPROVED 매장만) */
public record StoreMapItem(
        Long storeId,
        String name,
        String area,
        BigDecimal lat,
        BigDecimal lng,
        int themeCount
) {}

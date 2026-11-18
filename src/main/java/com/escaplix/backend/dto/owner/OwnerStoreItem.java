package com.escaplix.backend.dto.owner;

import com.escaplix.backend.entity.*;
import java.time.LocalDateTime;
import java.math.BigDecimal;

/** C-1 내 매장 한 줄. rejectReason 은 REJECTED 일 때만 */
public record OwnerStoreItem(
        Long storeId,
        String name,
        String area,
        String address,
        String phone,
        String websiteUrl,
        String hours,
        String intro,
        BigDecimal lat,
        BigDecimal lng,
        String bizPlaceNo,
        StoreStatus status,
        String rejectReason,
        long themeCount,
        LocalDateTime createdAt
) {}

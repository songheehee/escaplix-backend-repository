package com.escaplix.backend.dto.admin;

import java.time.LocalDateTime;

/** C-13 매장 승인 목록 한 줄 (오래된 순) */
public record AdminStoreItem(
        Long storeId,
        String name,
        String bizPlaceNo,
        OwnerBrief owner,
        LocalDateTime createdAt
) {}

package com.escaplix.backend.dto.owner;

import com.escaplix.backend.entity.*;

/** C-3, C-4 응답 (사장 PENDING / 관리자 APPROVED) */
public record StoreSaveResponse(
        Long storeId,
        StoreStatus status
) {}

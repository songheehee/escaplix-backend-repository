package com.escaplix.backend.dto.admin;

import com.escaplix.backend.entity.*;

/** C-16 응답 */
public record StoreApprovalResponse(
        Long storeId,
        StoreStatus status,
        String rejectReason
) {}

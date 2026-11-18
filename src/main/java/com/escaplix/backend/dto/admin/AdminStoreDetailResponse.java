package com.escaplix.backend.dto.admin;

import com.escaplix.backend.entity.*;
import java.time.LocalDateTime;

/** C-14 매장 승인 상세. 서류: 사업장등록증 C-15, 사장 사업자등록증 A-11 */
public record AdminStoreDetailResponse(
        Long storeId,
        String name,
        String area,
        String address,
        String phone,
        String websiteUrl,
        String hours,
        String intro,
        /** 매장 사진 "/uploads/stores/...". 없으면 null */
        String photoUrl,
        String bizPlaceNo,
        boolean hasBizPlaceFile,
        OwnerBrief owner,
        StoreStatus status,
        String rejectReason,
        LocalDateTime createdAt
) {}

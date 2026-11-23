package com.escaplix.backend.dto.admin;

import com.escaplix.backend.entity.*;
import com.escaplix.backend.dto.owner.OwnerThemeDetailResponse;

/** C-18 테마 승인 상세 = C-6 모양 + 매장 정보 */
public record AdminThemeDetailResponse(
        OwnerThemeDetailResponse theme,
        StoreInfo store
) {

    public record StoreInfo(Long storeId, String name, String bizPlaceNo, String phone, String websiteUrl,
                            StoreStatus status, String area, String address, String hours,
                            /** 매장 사장 아이디. 관리자 직접 등록 매장이면 null */
                            String ownerLoginId) {
    }
}

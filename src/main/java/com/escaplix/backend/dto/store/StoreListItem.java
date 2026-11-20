package com.escaplix.backend.dto.store;

import java.time.LocalDateTime;
import java.util.List;

/** B-9 매장 카드 (APPROVED 매장만). 숫자들은 그 매장 OPEN 테마로 계산 */
public record StoreListItem(
        Long storeId,
        String name,
        String area,
        String address,
        String hours,
        /** 매장 사진 "/uploads/stores/...". 없으면 null */
        String photoUrl,
        List<String> genres,
        int themeCount,
        Double avgRating,
        int recruitingPartyCount,
        /** 처음 승인된 때. 손님 화면은 30일 안이면 NEW */
        LocalDateTime approvedAt
) {}

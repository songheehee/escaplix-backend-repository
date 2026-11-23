package com.escaplix.backend.dto.member;

import com.escaplix.backend.entity.*;
import java.time.LocalDateTime;

/** A-10 회원 한 줄. 사장이 아니면 bizNo·managedStoreCount·stores 는 null */
public record AdminMemberItem(
        Long memberId,
        String loginId,
        String nickname,
        Role role,
        String bizNo,
        Long managedStoreCount,
        LocalDateTime createdAt,
        /** 사장의 매장 (등록 순). 회원 관리에서 "매장 N곳" 을 펼치면 보임 */
        java.util.List<StoreBrief> stores
) {
    public record StoreBrief(Long storeId, String name, StoreStatus status) {}
}

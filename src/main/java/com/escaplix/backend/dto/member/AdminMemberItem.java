package com.escaplix.backend.dto.member;

import com.escaplix.backend.entity.*;
import java.time.LocalDateTime;

/** A-10 회원 한 줄. 사장이 아니면 bizNo·managedStoreCount 는 null */
public record AdminMemberItem(
        Long memberId,
        String loginId,
        String nickname,
        Role role,
        String bizNo,
        Long managedStoreCount,
        LocalDateTime createdAt
) {}

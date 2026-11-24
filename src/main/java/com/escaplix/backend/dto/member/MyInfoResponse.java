package com.escaplix.backend.dto.member;

import com.escaplix.backend.entity.*;
import java.time.LocalDateTime;

/** A-7 내 정보. bizNo 는 사장만 (아니면 null). email 은 이메일 칸 생기기 전 가입한 회원이면 null */
public record MyInfoResponse(
        Long memberId,
        String loginId,
        String nickname,
        String email,
        Role role,
        String bizNo,
        LocalDateTime createdAt
) {}

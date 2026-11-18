package com.escaplix.backend.dto.member;

import com.escaplix.backend.entity.*;

/** A-6 로그인 응답. 프런트는 role 로 첫 화면을 정한다 (USER 메인 / OWNER 사장 홈 / ADMIN 관리자 홈) */
public record LoginResponse(
        String token,
        Long memberId,
        String nickname,
        Role role
) {}

package com.escaplix.backend.common.auth;

import com.escaplix.backend.entity.Role;

/**
 * 토큰에서 꺼낸 "지금 요청한 사람". 컨트롤러 메서드 파라미터에 LoginMember 를 적으면 들어온다.
 * 공개 API 에서 로그인 안 했으면 null. 회원번호는 반드시 여기서 꺼낸다 (요청 본문의 번호를 믿지 않음).
 */
public record LoginMember(Long memberId, String nickname, Role role) {

    public boolean is(Role r) {
        return role == r;
    }
}

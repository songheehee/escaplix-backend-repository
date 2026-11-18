package com.escaplix.backend.dto.admin;


/** 승인 화면의 "신청한 사장". 관리자 직접 등록 매장이면 owner 자체가 null. bizNo 는 상세(C-14)에서만 */
public record OwnerBrief(
        Long memberId,
        String loginId,
        String nickname,
        String bizNo
) {}

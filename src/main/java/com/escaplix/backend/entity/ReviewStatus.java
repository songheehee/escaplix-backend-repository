package com.escaplix.backend.entity;

/** REVIEW.status 후기 상태. DB 에는 이 이름이 대문자 문자열로 그대로 저장된다 (@Enumerated(EnumType.STRING)) */
public enum ReviewStatus {
    /** 보임 */
    VISIBLE,
    /** 숨김 (신고 처리, 나중 기능) */
    HIDDEN
}

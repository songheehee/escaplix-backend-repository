package com.escaplix.backend.entity;

/** THEME.status 테마 상태. DB 에는 이 이름이 대문자 문자열로 그대로 저장된다 (@Enumerated(EnumType.STRING)) */
public enum ThemeStatus {
    /** 승인 대기 */
    PENDING,
    /** 공개 */
    OPEN,
    /** 반려 (reject_reason 필수) */
    REJECTED,
    /** 운영 종료 */
    CLOSED
}

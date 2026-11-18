package com.escaplix.backend.entity;

/** STORE.status 매장 승인. DB 에는 이 이름이 대문자 문자열로 그대로 저장된다 (@Enumerated(EnumType.STRING)) */
public enum StoreStatus {
    /** 승인 대기 */
    PENDING,
    /** 승인됨 (손님에게 보임) */
    APPROVED,
    /** 반려 (reject_reason 필수) */
    REJECTED
}

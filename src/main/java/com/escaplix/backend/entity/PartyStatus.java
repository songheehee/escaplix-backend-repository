package com.escaplix.backend.entity;

/** PARTY.status 파티 상태. DB 에는 이 이름이 대문자 문자열로 그대로 저장된다 (@Enumerated(EnumType.STRING)) */
public enum PartyStatus {
    /** 모집 중 */
    RECRUITING,
    /** 정원 참 */
    FULL,
    /** 마감 (방장 또는 기한 지남) */
    CLOSED,
    /** 삭제됨 (모든 화면에서 숨김) */
    DELETED
}

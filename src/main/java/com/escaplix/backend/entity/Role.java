package com.escaplix.backend.entity;

/** MEMBER.role 회원 유형. DB 에는 이 이름이 대문자 문자열로 그대로 저장된다 (@Enumerated(EnumType.STRING)) */
public enum Role {
    /** 손님 */
    USER,
    /** 사장 */
    OWNER,
    /** 관리자 */
    ADMIN
}

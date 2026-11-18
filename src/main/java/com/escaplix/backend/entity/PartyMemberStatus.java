package com.escaplix.backend.entity;

/** PARTY_MEMBER.status 참여 상태. DB 에는 이 이름이 대문자 문자열로 그대로 저장된다 (@Enumerated(EnumType.STRING)) */
public enum PartyMemberStatus {
    /** 참여 중 */
    JOINED,
    /** 강퇴됨 (재참여 불가) */
    KICKED
}

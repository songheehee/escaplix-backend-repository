package com.escaplix.backend.entity;

/** PARTY_MESSAGE.msg_type 채팅 종류. DB 에는 이 이름이 대문자 문자열로 그대로 저장된다 (@Enumerated(EnumType.STRING)) */
public enum MessageType {
    /** 일반 채팅 */
    CHAT,
    /** ○○님이 들어왔어요 */
    JOIN,
    /** ○○님이 나갔어요 */
    LEAVE,
    /** ○○님이 강퇴됐어요 */
    KICK
}

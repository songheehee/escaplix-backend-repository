package com.escaplix.backend.dto.party;

import com.escaplix.backend.entity.*;
import java.time.LocalDateTime;

/** A-22 내 채팅 목록 한 줄 (JOINED + DELETED 아님). ended = play_at 지남 → 후기 쓰기 버튼 */
public record MyPartyItem(
        Long partyId,
        String title,
        Long themeId,
        String themeTitle,
        boolean themeOpen,      // 테마 운영 중 + 매장 승인. false = 운영 종료·폐업
        PartyStatus status,
        PartyRole myRole,
        int currentPeople,
        int maxPeople,
        LocalDateTime playAt,
        boolean ended,
        LastMessage lastMessage
) {

    /** 메시지가 하나도 없으면 lastMessage 는 null */
    public record LastMessage(String nickname, MessageType msgType, String content, LocalDateTime createdAt) {
    }
}

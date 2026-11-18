package com.escaplix.backend.dto.party;

import com.escaplix.backend.entity.*;
import java.util.List;
import java.time.LocalDateTime;

/** A-13 파티 방 정보 (JOINED 참여자만 볼 수 있음) */
public record PartyDetailResponse(
        Long partyId,
        String title,
        String intro,
        PartyStatus status,
        ThemeBrief theme,
        LocalDateTime playAt,
        LocalDateTime deadlineAt,
        int maxPeople,
        int currentPeople,
        List<Participant> members,
        PartyRole myRole
) {

    public record ThemeBrief(Long themeId, String title, String storeName, int playMinutes) {
    }

    /** JOINED 인 사람만 */
    public record Participant(Long memberId, String nickname, boolean leader, LocalDateTime joinedAt) {
    }
}

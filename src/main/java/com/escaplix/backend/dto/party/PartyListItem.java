package com.escaplix.backend.dto.party;

import com.escaplix.backend.entity.*;
import java.util.List;
import java.time.LocalDateTime;

/** A-12 파티 배너 한 장. 항상 RECRUITING + 기한 남은 것만, play_at 오름차순 */
public record PartyListItem(
        Long partyId,
        String title,
        String intro,
        Long themeId,
        String themeTitle,
        String storeName,
        List<String> genres,
        LocalDateTime playAt,
        LocalDateTime deadlineAt,
        int currentPeople,
        int maxPeople,
        String leaderNickname,
        PartyStatus status,
        /** 내가 JOINED → 팝업 없이 바로 방으로 */
        boolean joined,
        /** 내가 KICKED → 배너 흐리게, 참여 불가 */
        boolean kicked
) {}

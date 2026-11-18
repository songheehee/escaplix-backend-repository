package com.escaplix.backend.dto.review;

import java.time.LocalDateTime;

/** D-6 후기 쓸 수 있는 테마: 내가 JOINED 였던 파티 중 play_at 지남 + 내 후기 없음 */
public record ReviewCandidateItem(
        Long themeId,
        String title,
        String storeName,
        int playMinutes,
        Long partyId,
        LocalDateTime playAt,
        int memberCount
) {}

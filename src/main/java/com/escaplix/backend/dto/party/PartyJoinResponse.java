package com.escaplix.backend.dto.party;

import com.escaplix.backend.entity.*;

/** A-16 참여 응답 (마지막 자리면 status FULL) */
public record PartyJoinResponse(
        Long partyId,
        int currentPeople,
        int maxPeople,
        PartyStatus status
) {}

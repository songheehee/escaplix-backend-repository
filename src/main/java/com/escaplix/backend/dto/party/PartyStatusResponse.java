package com.escaplix.backend.dto.party;

import com.escaplix.backend.entity.*;

/** A-19 모집 마감 응답 */
public record PartyStatusResponse(
        Long partyId,
        PartyStatus status
) {}

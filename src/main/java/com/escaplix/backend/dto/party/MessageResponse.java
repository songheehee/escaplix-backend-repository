package com.escaplix.backend.dto.party;

import com.escaplix.backend.entity.*;
import java.time.LocalDateTime;

/** A-20 채팅 한 줄, A-21 응답. 안내 줄(JOIN/LEAVE/KICK)은 content null. nickname 은 MEMBER JOIN */
public record MessageResponse(
        Long messageId,
        Long memberId,
        String nickname,
        MessageType msgType,
        String content,
        LocalDateTime createdAt,
        boolean mine
) {}

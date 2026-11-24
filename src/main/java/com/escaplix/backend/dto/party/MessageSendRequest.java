package com.escaplix.backend.dto.party;

import jakarta.validation.constraints.*;

/**
 * A-21 채팅 보내기 (앞뒤 공백 지운 뒤 1~500자).
 * 길이(500자)는 여기서 @Size 로 막지 않는다: @Size 는 공백을 지우기 전 길이를 재서 "공백 + 500자"를 잘못 거절함.
 * 공백을 지운 뒤 길이는 PartyServiceImpl.sendMessage 가 검사한다
 */
public record MessageSendRequest(
        @NotBlank(message = "메시지를 입력해 주세요.")
        String content
) {}

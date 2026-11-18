package com.escaplix.backend.dto.party;

import jakarta.validation.constraints.*;

/** A-21 채팅 보내기 (앞뒤 공백 지운 뒤 1~500자) */
public record MessageSendRequest(
        @NotBlank(message = "메시지를 입력해 주세요.")
        @Size(max = 500, message = "메시지는 500자 이내로 입력해 주세요.")
        String content
) {}

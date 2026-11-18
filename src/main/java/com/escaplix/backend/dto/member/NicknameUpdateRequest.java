package com.escaplix.backend.dto.member;

import jakarta.validation.constraints.*;

/** A-8 닉네임 바꾸기 */
public record NicknameUpdateRequest(
        @NotBlank(message = "닉네임을 입력해 주세요.")
        @Size(min = 2, max = 20, message = "닉네임은 2~20자로 입력해 주세요.")
        String nickname
) {}

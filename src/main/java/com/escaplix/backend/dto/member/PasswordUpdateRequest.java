package com.escaplix.backend.dto.member;

import jakarta.validation.constraints.*;

/** A-9 비밀번호 바꾸기. 현재 비밀번호가 틀리면 400 (401 아님) */
public record PasswordUpdateRequest(
        @NotBlank(message = "현재 비밀번호를 입력해 주세요.") String currentPassword,
        @NotBlank(message = "새 비밀번호를 입력해 주세요.")
        @Size(min = 4, max = 20, message = "비밀번호는 4~20자로 입력해 주세요.")
        String newPassword
) {}

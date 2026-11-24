package com.escaplix.backend.dto.member;

import jakarta.validation.constraints.*;

/** 비밀번호 재설정 2단계: 인증번호 확인 */
public record PasswordResetVerifyRequest(
        @NotBlank(message = "아이디를 입력해 주세요.")
        String loginId,
        @NotBlank(message = "이메일을 입력해 주세요.")
        String email,
        @NotBlank(message = "인증번호를 입력해 주세요.")
        String code
) {}

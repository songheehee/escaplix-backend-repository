package com.escaplix.backend.dto.member;

import jakarta.validation.constraints.*;

/** 비밀번호 재설정 3단계: 인증을 마친 아이디·이메일로 새 비밀번호 (규칙은 가입과 같음: 4~20자) */
public record PasswordResetRequest(
        @NotBlank(message = "아이디를 입력해 주세요.")
        String loginId,
        @NotBlank(message = "이메일을 입력해 주세요.")
        String email,
        @NotBlank(message = "새 비밀번호를 입력해 주세요.")
        @Size(min = 4, max = 20, message = "비밀번호는 4~20자로 입력해 주세요.")
        String newPassword
) {}

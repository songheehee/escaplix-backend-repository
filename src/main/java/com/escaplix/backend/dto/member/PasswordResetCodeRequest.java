package com.escaplix.backend.dto.member;

import jakarta.validation.constraints.*;

/** 비밀번호 재설정 1단계: 아이디 + 가입한 이메일 → 둘이 맞으면 인증번호 메일 */
public record PasswordResetCodeRequest(
        @NotBlank(message = "아이디를 입력해 주세요.")
        String loginId,
        @NotBlank(message = "이메일을 입력해 주세요.")
        // 형식·길이는 앞뒤 공백을 지운 뒤 서비스(MemberServiceImpl.normalizeEmail)가 본다
        String email
) {}

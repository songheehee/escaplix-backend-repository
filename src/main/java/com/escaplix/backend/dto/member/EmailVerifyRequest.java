package com.escaplix.backend.dto.member;

import jakarta.validation.constraints.*;

/** 가입 이메일 인증번호 확인 */
public record EmailVerifyRequest(
        @NotBlank(message = "이메일을 입력해 주세요.")
        // 형식·길이(100자)는 앞뒤 공백을 지운 뒤 서비스(MemberServiceImpl.normalizeEmail)가 본다
        String email,
        @NotBlank(message = "인증번호를 입력해 주세요.")
        String code
) {}

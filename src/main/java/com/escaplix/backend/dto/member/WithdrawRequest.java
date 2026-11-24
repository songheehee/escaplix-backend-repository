package com.escaplix.backend.dto.member;

import jakarta.validation.constraints.*;

/** 회원 탈퇴: 본인 확인용 비밀번호 */
public record WithdrawRequest(
        @NotBlank(message = "비밀번호를 입력해 주세요.")
        String password
) {}

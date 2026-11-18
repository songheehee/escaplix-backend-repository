package com.escaplix.backend.dto.admin;

import jakarta.validation.constraints.*;

/** C-16, C-19 승인·반려. 반려(approved=false)면 reason 필수 1~300자 (서비스에서 검사) */
public record ApprovalRequest(
        boolean approved,
        @Size(max = 300, message = "반려 사유는 300자 이내로 입력해 주세요.")
        String reason
) {}

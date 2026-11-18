package com.escaplix.backend.dto.review;

import jakarta.validation.constraints.*;

/** D-11 후기 신고. 자유 글 사유만 (결정 R3) */
public record ReportRequest(
        @NotBlank(message = "신고 사유를 입력해 주세요.")
        @Size(max = 300, message = "신고 사유는 300자 이내로 입력해 주세요.")
        String reason
) {}

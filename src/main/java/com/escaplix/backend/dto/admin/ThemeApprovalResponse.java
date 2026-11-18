package com.escaplix.backend.dto.admin;

import com.escaplix.backend.entity.*;

/** C-19 응답 */
public record ThemeApprovalResponse(
        Long themeId,
        ThemeStatus status,
        String rejectReason
) {}

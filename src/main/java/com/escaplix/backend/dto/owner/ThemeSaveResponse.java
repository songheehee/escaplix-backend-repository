package com.escaplix.backend.dto.owner;

import com.escaplix.backend.entity.*;

/** C-7, C-8 응답 (사장 PENDING / 관리자 OPEN) */
public record ThemeSaveResponse(
        Long themeId,
        ThemeStatus status
) {}

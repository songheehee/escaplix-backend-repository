package com.escaplix.backend.dto.owner;

import com.escaplix.backend.entity.*;

/** C-9 응답 */
public record ThemeStatusResponse(
        Long themeId,
        ThemeStatus status
) {}

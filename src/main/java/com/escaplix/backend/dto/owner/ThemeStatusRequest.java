package com.escaplix.backend.dto.owner;

import com.escaplix.backend.entity.*;
import jakarta.validation.constraints.*;

/** C-9 운영 종료·재개: OPEN ↔ CLOSED 만 */
public record ThemeStatusRequest(
        @NotNull(message = "상태를 골라 주세요.") ThemeStatus status
) {}

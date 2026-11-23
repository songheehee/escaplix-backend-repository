package com.escaplix.backend.dto.admin;

import com.escaplix.backend.entity.*;
import java.time.LocalDateTime;
import java.util.Map;

/** C-12 관리자 홈 숫자 요약 */
public record AdminSummaryResponse(
        long pendingStores,
        long pendingThemes,
        LocalDateTime oldestPendingStoreAt,
        LocalDateTime oldestPendingThemeAt,
        long approvedStores,
        long openThemes,
        Map<Role, Long> memberCountByRole
) {}

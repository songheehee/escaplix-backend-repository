package com.escaplix.backend.dto.theme;

import com.escaplix.backend.entity.*;
import java.time.LocalDateTime;

/** B-6 찜 목록 한 줄. 운영 종료(CLOSED) 테마도 남는다 */
public record WishItem(
        Long wishId,
        Long themeId,
        String title,
        String storeName,
        ThemeStatus status,
        String posterUrl,
        LocalDateTime createdAt
) {}

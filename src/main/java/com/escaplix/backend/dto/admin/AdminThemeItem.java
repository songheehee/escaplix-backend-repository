package com.escaplix.backend.dto.admin;

import java.util.List;
import java.time.LocalDateTime;

/** C-17 테마 승인 목록 한 줄 (오래된 순) */
public record AdminThemeItem(
        Long themeId,
        String title,
        Long storeId,          // 매장 관리 검색: 테마 이름으로 그 매장 찾기
        String storeName,
        List<String> genres,
        String posterUrl,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}

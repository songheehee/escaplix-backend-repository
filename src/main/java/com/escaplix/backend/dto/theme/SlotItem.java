package com.escaplix.backend.dto.theme;


/** 시간표 한 칸 (B-3, C-10). endTime = startTime + playMinutes */
public record SlotItem(
        Long slotId,
        String startTime,
        String endTime
) {}

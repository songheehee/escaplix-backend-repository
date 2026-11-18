package com.escaplix.backend.dto.owner;

import java.util.List;
import com.escaplix.backend.dto.theme.SlotItem;

/** C-10 주중/주말 시간표 (C-11 응답도 같은 모양) */
public record WeekSlotsResponse(
        Long themeId,
        int playMinutes,
        List<SlotItem> weekday,
        List<SlotItem> weekend
) {}

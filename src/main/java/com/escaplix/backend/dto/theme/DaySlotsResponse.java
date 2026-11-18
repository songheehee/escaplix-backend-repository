package com.escaplix.backend.dto.theme;

import com.escaplix.backend.entity.*;
import java.util.List;
import java.time.LocalDate;

/** B-3 그날 시간표. 토·일·공휴일이면 WEEKEND (DayTypeResolver). 오늘이면 지난 시각은 뺀다 */
public record DaySlotsResponse(
        LocalDate date,
        DayType dayType,
        /** 공휴일이면 이름, 아니면 null */
        String holidayName,
        List<SlotItem> slots
) {}

package com.escaplix.backend.dto.owner;

import jakarta.validation.constraints.*;
import java.util.List;

/** C-11 시간표 통째 저장. "HH:mm" 문자열 목록. 중복·겹침·20개 초과는 서비스에서 400 */
public record WeekSlotsRequest(
        @NotNull(message = "주중 시간을 보내 주세요 (없으면 빈 배열).") List<String> weekday,
        @NotNull(message = "주말 시간을 보내 주세요 (없으면 빈 배열).") List<String> weekend
) {}

package com.escaplix.backend.dto.review;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

/** D-1 후기 쓰기 */
public record ReviewCreateRequest(
        @NotNull(message = "테마를 골라 주세요.") Long themeId,
        @NotNull(message = "별점을 골라 주세요.") @Min(value = 1, message = "별점은 1~5점입니다.") @Max(value = 5, message = "별점은 1~5점입니다.")
        Integer rating,
        @NotNull(message = "탈출 성공 여부를 골라 주세요.")
        Boolean escaped,
        @NotNull(message = "사용한 힌트 수를 입력해 주세요.") @Min(value = 0, message = "힌트 수는 0~99입니다.") @Max(value = 99, message = "힌트 수는 0~99입니다.")
        Integer hintCount,
        @NotNull(message = "플레이 인원을 입력해 주세요.") @Min(value = 1, message = "플레이 인원은 1~20명입니다.") @Max(value = 20, message = "플레이 인원은 1~20명입니다.")
        Integer playerCount,
        /** 남은 시간 분. 실패면 0. 분*60+초 <= playMinutes*60 (상한은 서비스에서) */
        @NotNull(message = "남은 시간(분)을 입력해 주세요.") @Min(value = 0, message = "남은 시간(분)은 0 이상입니다.")
        Integer remainMinutes,
        /** 남은 시간 초 0~59. 실패면 0 */
        @NotNull(message = "남은 시간(초)을 입력해 주세요.") @Min(value = 0, message = "초는 0~59입니다.") @Max(value = 59, message = "초는 0~59입니다.")
        Integer remainSeconds,
        /** 비우면 작성일 (결정 R1) */
        @PastOrPresent(message = "플레이한 날은 오늘이거나 그 이전이어야 합니다.")
        LocalDate playedDate,
        @Size(max = 200, message = "한 줄 후기는 200자 이내로 입력해 주세요.")
        String content
) {}

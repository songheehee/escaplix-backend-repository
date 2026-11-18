package com.escaplix.backend.dto.party;

import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

/** A-14 파티 만들기. playAt 은 그날 유형(주중/주말·공휴일) 시간표에 있는 시각이어야 함 (서비스에서 검사) */
public record PartyCreateRequest(
        @NotNull(message = "테마를 골라 주세요.") Long themeId,
        @NotBlank(message = "제목을 입력해 주세요.")
        @Size(max = 100, message = "제목은 100자 이내로 입력해 주세요.")
        String title,
        @NotBlank(message = "한마디를 입력해 주세요.")
        @Size(max = 100, message = "한마디는 100자 이내로 입력해 주세요.")
        String intro,
        @NotNull(message = "플레이 날짜와 시간을 골라 주세요.")
        @Future(message = "오늘 이후 날짜만 고를 수 있습니다.")
        LocalDateTime playAt,
        @NotNull(message = "모집 기한을 정해 주세요.")
        @Future(message = "모집 기한은 지금 이후여야 합니다.")
        LocalDateTime deadlineAt,
        @NotNull(message = "모집 인원을 정해 주세요.")
        @Min(value = 2, message = "모집 인원은 2명 이상이어야 합니다.")
        Integer maxPeople
) {}

package com.escaplix.backend.dto.owner;

import jakarta.validation.constraints.*;
import java.util.List;

/** C-7, C-8 테마 등록·수정 (multipart 폼 칸). 사진은 poster 로 따로. 수정 때 storeId 는 무시 */
public record ThemeForm(
        @NotNull(message = "매장을 골라 주세요.") Long storeId,
        @NotBlank(message = "테마 이름을 입력해 주세요.") @Size(max = 50, message = "테마 이름은 50자 이내로 입력해 주세요.")
        String title,
        @Size(max = 1300, message = "소개는 1300자 이내로 입력해 주세요.")
        String description,
        @NotNull(message = "난이도를 골라 주세요.") @Min(value = 1, message = "난이도는 1~5입니다.") @Max(value = 5, message = "난이도는 1~5입니다.")
        Integer difficulty,
        @NotNull(message = "제한 시간을 입력해 주세요.") @Min(value = 1, message = "제한 시간은 1~720분입니다.") @Max(value = 720, message = "제한 시간은 1~720분입니다.")
        Integer playMinutes,
        @NotNull(message = "최소 인원을 입력해 주세요.") @Min(value = 1, message = "인원은 1~20명입니다.") @Max(value = 20, message = "인원은 1~20명입니다.")
        Integer minPeople,
        @NotNull(message = "최대 인원을 입력해 주세요.") @Min(value = 1, message = "인원은 1~20명입니다.") @Max(value = 20, message = "인원은 1~20명입니다.")
        Integer maxPeople,
        @NotNull(message = "가격을 입력해 주세요.") @Min(value = 0, message = "가격은 0원 이상입니다.")
        Integer price,
        @NotBlank(message = "예약 링크를 입력해 주세요.")
        @Size(max = 500, message = "주소는 500자 이내로 입력해 주세요.")
        @Pattern(regexp = "^https?://.+", message = "예약 링크는 http:// 또는 https:// 로 시작해야 합니다.")
        String bookingUrl,
        @NotEmpty(message = "장르를 하나 이상 골라 주세요.")
        List<Long> genreIds
) {}

package com.escaplix.backend.dto.owner;

import jakarta.validation.constraints.*;

/** C-3, C-4 매장 등록·수정 (multipart 폼 칸). 사업장등록증 파일은 bizPlaceFile 로 따로 */
public record StoreForm(
        @NotBlank(message = "매장 이름을 입력해 주세요.") @Size(max = 50, message = "매장 이름은 50자 이내로 입력해 주세요.")
        String name,
        @NotBlank(message = "지역을 입력해 주세요.") @Size(max = 10, message = "지역은 10자 이내로 입력해 주세요.")
        String area,
        @NotBlank(message = "주소를 입력해 주세요.") @Size(max = 100, message = "주소는 100자 이내로 입력해 주세요.")
        String address,
        @NotBlank(message = "전화번호를 입력해 주세요.")
        @Pattern(regexp = "^[0-9-]{9,13}$", message = "전화번호는 숫자와 - 로 입력해 주세요.")
        String phone,
        @NotBlank(message = "매장 웹사이트를 입력해 주세요.")
        @Size(max = 500, message = "주소는 500자 이내로 입력해 주세요.")
        @Pattern(regexp = "^https?://.+", message = "웹사이트 주소는 http:// 또는 https:// 로 시작해야 합니다.")
        String websiteUrl,
        @NotBlank(message = "영업시간을 입력해 주세요.") @Size(max = 50, message = "영업시간은 50자 이내로 입력해 주세요.")
        String hours,
        @NotBlank(message = "매장 소개를 입력해 주세요.") @Size(max = 200, message = "매장 소개는 200자 이내로 입력해 주세요.")
        String intro,
        @NotBlank(message = "사업장 등록번호를 입력해 주세요.")
        @Pattern(regexp = "^\\d{3}-\\d{2}-\\d{5}$", message = "사업장 등록번호 형식이 올바르지 않습니다.")
        String bizPlaceNo
) {}

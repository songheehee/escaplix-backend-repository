package com.escaplix.backend.dto.owner;

import jakarta.validation.constraints.*;

/** C-3, C-4 매장 등록·수정 (multipart 폼 칸). 사업장 증빙 서류 파일은 bizPlaceFile 로 따로 */
public record StoreForm(
        @NotBlank(message = "매장 이름을 입력해 주세요.") @Size(max = 50, message = "매장 이름은 50자 이내로 입력해 주세요.")
        String name,
        @NotBlank(message = "지역을 입력해 주세요.") @Size(max = 10, message = "지역은 10자 이내로 입력해 주세요.")
        String area,
        @NotBlank(message = "주소를 입력해 주세요.") @Size(max = 100, message = "주소는 100자 이내로 입력해 주세요.")
        String address,
        @NotBlank(message = "전화번호를 입력해 주세요.")
        // 02-123-4567 · 031-123-4567 · 010-1234-5678 · 0507-1234-5678 · 1588-1234 (화면 store-form.js PHONE_RE 와 같음)
        @Pattern(regexp = "^(02-\\d{3,4}-\\d{4}|050\\d-\\d{4}-\\d{4}|0[13-9]\\d-\\d{3,4}-\\d{4}|1[5-9]\\d{2}-\\d{4})$",
                message = "전화번호 형식이 올바르지 않습니다. (예: 02-123-4567)")
        String phone,
        @NotBlank(message = "매장 웹사이트를 입력해 주세요.")
        @Size(max = 500, message = "주소는 500자 이내로 입력해 주세요.")
        @Pattern(regexp = "^https?://.+", message = "웹사이트 주소는 http:// 또는 https:// 로 시작해야 합니다.")
        String websiteUrl,
        @NotBlank(message = "영업시간을 입력해 주세요.") @Size(max = 50, message = "영업시간은 50자 이내로 입력해 주세요.")
        // 자유 입력이지만 시간이 하나는 있어야 함 (10:00 / 10시). 화면 store-form.js 와 같은 규칙
        @Pattern(regexp = "(?s).*(\\d{1,2}\\s*:\\s*\\d{2}|\\d{1,2}\\s*시).*", message = "영업시간에 시간을 넣어 주세요. 예) 10:00 ~ 23:00 (화 휴무)")
        String hours,
        @Size(max = 200, message = "매장 소개는 200자 이내로 입력해 주세요.")   // 선택. 비우면 NULL
        String intro,
        // 사업장 관리번호 11자리 (000-00-00000-0). 10자리는 예전 데이터를 바꾸지 않고 다시 저장할 때만 통과 (OwnerStoreServiceImpl.checkBizPlaceNoForm)
        @NotBlank(message = "사업장 관리번호를 입력해 주세요.")
        @Pattern(regexp = "^\\d{3}-\\d{2}-\\d{5}(-\\d)?$", message = "사업장 관리번호 형식이 올바르지 않습니다. (000-00-00000-0)")
        String bizPlaceNo
) {}

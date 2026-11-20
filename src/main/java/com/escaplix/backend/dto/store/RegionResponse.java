package com.escaplix.backend.dto.store;

import java.util.List;

/** B-12 지역 전체 (큰 지역별로 묶음). 매장 등록 화면의 지역 select */
public record RegionResponse(
        Long regionId,
        String name,
        List<AreaItem> areas
) {
    public record AreaItem(Long areaId, String name) { }
}

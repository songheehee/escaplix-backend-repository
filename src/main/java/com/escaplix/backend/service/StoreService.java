package com.escaplix.backend.service;

import com.escaplix.backend.common.*;
import com.escaplix.backend.common.auth.*;
import com.escaplix.backend.common.file.FileDownload;
import com.escaplix.backend.dto.admin.*;
import com.escaplix.backend.dto.member.*;
import com.escaplix.backend.dto.owner.*;
import com.escaplix.backend.dto.party.*;
import com.escaplix.backend.dto.review.*;
import com.escaplix.backend.dto.store.*;
import com.escaplix.backend.dto.theme.*;
import com.escaplix.backend.entity.*;
import java.util.List;

/** B 담당 · 매장 탐색 (B-8 ~ B-10, B-12). 명세: docs/API.md (목업 레포) */
public interface StoreService {

    /** B-8 지역 목록 (검색용: 승인된 매장이 있는 세부 지역만) */
    List<String> getAreas();

    /** B-12 지역 전체 (매장 등록용: 큰 지역별로 묶은 모든 세부 지역) */
    List<RegionResponse> getRegions();

    /** B-9 매장 목록 */
    List<StoreListItem> findStores(StoreSearchCondition cond);

    /** B-10 매장 상세 */
    StoreDetailResponse getStore(Long storeId);
}

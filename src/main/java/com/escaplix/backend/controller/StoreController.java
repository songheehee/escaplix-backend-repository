package com.escaplix.backend.controller;

import com.escaplix.backend.dto.store.*;
import com.escaplix.backend.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * B · 매장 (B-8 ~ B-12)
 * 컨트롤러는 받고 넘기기만 한다: 검사·권한 확인·트랜잭션은 서비스에서. 응답은 DTO 만 (엔티티 금지).
 */
@RestController
@RequiredArgsConstructor
public class StoreController {

    private final StoreService storeService;

    /** B-8 지역 목록 (검색용: 매장이 있는 세부 지역만) · 공개 · 200 */
    @GetMapping("/api/areas")
    public List<String> getAreas() {
        return storeService.getAreas();
    }

    /** B-12 지역 전체 (매장 등록용: 큰 지역별로 묶음) · 공개 · 200 */
    @GetMapping("/api/regions")
    public List<RegionResponse> getRegions() {
        return storeService.getRegions();
    }

    /** B-9 매장 목록 · 공개 · 200 */
    @GetMapping("/api/stores")
    public List<StoreListItem> findStores(@ModelAttribute StoreSearchCondition cond) {
        return storeService.findStores(cond);
    }

    /** B-10 매장 상세 · 공개 · 200 */
    @GetMapping("/api/stores/{storeId}")
    public StoreDetailResponse getStore(@PathVariable Long storeId) {
        return storeService.getStore(storeId);
    }

    /** B-11 지도용 매장 (선택 기능) · 공개 · 200 */
    @GetMapping("/api/stores/map")
    public List<StoreMapItem> getMapStores() {
        return storeService.getMapStores();
    }
}

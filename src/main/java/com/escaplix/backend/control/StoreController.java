package com.escaplix.backend.control;

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
import com.escaplix.backend.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

/**
 * B · 매장 (B-8 ~ B-11)
 * 컨트롤러는 받고 넘기기만 한다: 검사·권한 확인·트랜잭션은 서비스에서. 응답은 DTO 만 (엔티티 금지).
 */
@RestController
@RequiredArgsConstructor
public class StoreController {

    private final StoreService storeService;

    /** B-8 지역 목록 · 공개 · 200 */
    @GetMapping("/api/areas")
    public List<String> getAreas() {
        return storeService.getAreas();
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

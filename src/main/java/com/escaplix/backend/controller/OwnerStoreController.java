package com.escaplix.backend.controller;

import com.escaplix.backend.common.*;
import com.escaplix.backend.common.auth.*;
import com.escaplix.backend.dto.owner.*;
import com.escaplix.backend.entity.*;
import com.escaplix.backend.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * C · 사장 매장 (C-1 ~ C-5)
 * 컨트롤러는 받고 넘기기만 한다: 검사·권한 확인·트랜잭션은 서비스에서. 응답은 DTO 만 (엔티티 금지).
 */
@RestController
@RequiredArgsConstructor
public class OwnerStoreController {

    private final OwnerStoreService ownerStoreService;

    /** C-1 내 매장 목록 · OWNER* · 200 */
    @GetMapping("/api/owner/stores")
    @Auth({Role.OWNER, Role.ADMIN})
    public List<OwnerStoreItem> getMyStores(LoginMember login) {
        return ownerStoreService.getMyStores(login);
    }

    /** C-2 사업장 등록번호 중복 확인 · OWNER* · 200 */
    @GetMapping("/api/owner/stores/check-biz-place-no")
    @Auth({Role.OWNER, Role.ADMIN})
    public AvailableResponse checkBizPlaceNo(@RequestParam String bizPlaceNo) {
        return ownerStoreService.checkBizPlaceNo(bizPlaceNo);
    }

    /** C-3 매장 등록 · OWNER* · 201 */
    @PostMapping(value = "/api/owner/stores", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @Auth({Role.OWNER, Role.ADMIN})
    public StoreSaveResponse createStore(LoginMember login, @Valid @ModelAttribute StoreForm form, @RequestPart(value = "bizPlaceFile", required = false) MultipartFile bizPlaceFile,
                                         @RequestPart(value = "storePhoto", required = false) MultipartFile storePhoto) {
        return ownerStoreService.createStore(login, form, bizPlaceFile, storePhoto);
    }

    /** C-4 매장 수정 · OWNER* · 200 */
    @PutMapping(value = "/api/owner/stores/{storeId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Auth({Role.OWNER, Role.ADMIN})
    public StoreSaveResponse updateStore(LoginMember login, @PathVariable Long storeId, @Valid @ModelAttribute StoreForm form, @RequestPart(value = "bizPlaceFile", required = false) MultipartFile bizPlaceFile,
                                         @RequestPart(value = "storePhoto", required = false) MultipartFile storePhoto) {
        return ownerStoreService.updateStore(login, storeId, form, bizPlaceFile, storePhoto);
    }

    /** 매장 폐업 · OWNER* (관리자도) · 200. 승인된 매장만, 되돌릴 수 없음 */
    @PutMapping("/api/owner/stores/{storeId}/close")
    @Auth({Role.OWNER, Role.ADMIN})
    public StoreSaveResponse closeStore(LoginMember login, @PathVariable Long storeId) {
        return ownerStoreService.closeStore(login, storeId);
    }

    /** C-5 매장의 테마 목록 · OWNER* · 200 */
    @GetMapping("/api/owner/stores/{storeId}/themes")
    @Auth({Role.OWNER, Role.ADMIN})
    public List<OwnerThemeItem> getStoreThemes(LoginMember login, @PathVariable Long storeId) {
        return ownerStoreService.getStoreThemes(login, storeId);
    }
}

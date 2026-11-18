package com.escaplix.backend.controller;

import com.escaplix.backend.common.auth.*;
import com.escaplix.backend.common.file.FileDownload;
import com.escaplix.backend.dto.admin.*;
import com.escaplix.backend.entity.*;
import com.escaplix.backend.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * C · 관리자 승인 (C-12 ~ C-19)
 * 컨트롤러는 받고 넘기기만 한다: 검사·권한 확인·트랜잭션은 서비스에서. 응답은 DTO 만 (엔티티 금지).
 */
@RestController
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    /** C-12 관리자 홈 숫자 요약 · ADMIN · 200 */
    @GetMapping("/api/admin/summary")
    @Auth(Role.ADMIN)
    public AdminSummaryResponse getSummary() {
        return adminService.getSummary();
    }

    /** C-13 매장 승인 목록 · ADMIN · 200 */
    @GetMapping("/api/admin/stores")
    @Auth(Role.ADMIN)
    public List<AdminStoreItem> getStores(@RequestParam(defaultValue = "PENDING") StoreStatus status) {
        return adminService.getStores(status);
    }

    /** C-14 매장 승인 상세 · ADMIN · 200 */
    @GetMapping("/api/admin/stores/{storeId}")
    @Auth(Role.ADMIN)
    public AdminStoreDetailResponse getStore(@PathVariable Long storeId) {
        return adminService.getStore(storeId);
    }

    /** C-15 사업장등록증 이미지 · ADMIN · 200 */
    @GetMapping("/api/admin/stores/{storeId}/biz-place-file")
    @Auth(Role.ADMIN)
    public ResponseEntity<Resource> getBizPlaceFile(@PathVariable Long storeId) {
        FileDownload file = adminService.getBizPlaceFile(storeId);
        return ResponseEntity.ok().contentType(MediaType.parseMediaType(file.contentType())).body(file.resource());
    }

    /** C-16 매장 승인·반려 · ADMIN · 200 */
    @PutMapping("/api/admin/stores/{storeId}/approval")
    @Auth(Role.ADMIN)
    public StoreApprovalResponse approveStore(@PathVariable Long storeId, @Valid @RequestBody ApprovalRequest req) {
        return adminService.approveStore(storeId, req);
    }

    /** C-17 테마 승인 목록 · ADMIN · 200 */
    @GetMapping("/api/admin/themes")
    @Auth(Role.ADMIN)
    public List<AdminThemeItem> getThemes(@RequestParam(defaultValue = "PENDING") ThemeStatus status) {
        return adminService.getThemes(status);
    }

    /** C-18 테마 승인 상세 · ADMIN · 200 */
    @GetMapping("/api/admin/themes/{themeId}")
    @Auth(Role.ADMIN)
    public AdminThemeDetailResponse getTheme(@PathVariable Long themeId) {
        return adminService.getTheme(themeId);
    }

    /** C-19 테마 승인·반려 · ADMIN · 200 */
    @PutMapping("/api/admin/themes/{themeId}/approval")
    @Auth(Role.ADMIN)
    public ThemeApprovalResponse approveTheme(@PathVariable Long themeId, @Valid @RequestBody ApprovalRequest req) {
        return adminService.approveTheme(themeId, req);
    }
}

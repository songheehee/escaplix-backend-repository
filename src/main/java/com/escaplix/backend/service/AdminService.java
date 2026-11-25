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

/** C 담당 · 관리자 승인 (C-12 ~ C-19). 명세: docs/API.md (목업 레포) */
public interface AdminService {

    /** C-12 관리자 홈 숫자 요약 */
    AdminSummaryResponse getSummary();

    /** C-13 매장 승인 목록 */
    List<AdminStoreItem> getStores(StoreStatus status);

    /** C-14 매장 승인 상세 */
    AdminStoreDetailResponse getStore(Long storeId);

    /** C-15 사업장 증빙 서류 이미지 */
    FileDownload getBizPlaceFile(Long storeId);

    /** C-16 매장 승인·반려 */
    StoreApprovalResponse approveStore(Long storeId, ApprovalRequest req);

    /** C-17 테마 승인 목록 */
    List<AdminThemeItem> getThemes(ThemeStatus status);

    /** C-18 테마 승인 상세 */
    AdminThemeDetailResponse getTheme(Long themeId);

    /** C-19 테마 승인·반려 */
    ThemeApprovalResponse approveTheme(Long themeId, ApprovalRequest req);
}

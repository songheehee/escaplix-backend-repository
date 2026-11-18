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
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

/** C 담당 · 사장 매장 (C-1 ~ C-5). 명세: docs/API.md (목업 레포) */
public interface OwnerStoreService {

    /** C-1 내 매장 목록 */
    List<OwnerStoreItem> getMyStores(LoginMember login);

    /** C-2 사업장 등록번호 중복 확인 */
    AvailableResponse checkBizPlaceNo(String bizPlaceNo);

    /** C-3 매장 등록 */
    StoreSaveResponse createStore(LoginMember login, StoreForm form, MultipartFile bizPlaceFile, MultipartFile storePhoto);

    /** C-4 매장 수정 */
    StoreSaveResponse updateStore(LoginMember login, Long storeId, StoreForm form, MultipartFile bizPlaceFile, MultipartFile storePhoto);

    /** C-5 매장의 테마 목록 */
    List<OwnerThemeItem> getStoreThemes(LoginMember login, Long storeId);
}

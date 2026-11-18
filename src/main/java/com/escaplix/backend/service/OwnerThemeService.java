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

/** C 담당 · 사장 테마·시간표 (C-6 ~ C-11). 명세: docs/API.md (목업 레포) */
public interface OwnerThemeService {

    /** C-6 테마 수정용 상세 */
    OwnerThemeDetailResponse getTheme(LoginMember login, Long themeId);

    /** C-7 테마 등록 */
    ThemeSaveResponse createTheme(LoginMember login, ThemeForm form, MultipartFile poster);

    /** C-8 테마 수정 */
    ThemeSaveResponse updateTheme(LoginMember login, Long themeId, ThemeForm form, MultipartFile poster);

    /** C-9 운영 종료·재개 */
    ThemeStatusResponse changeStatus(LoginMember login, Long themeId, ThemeStatusRequest req);

    /** C-10 주중/주말 시간표 보기 */
    WeekSlotsResponse getSlots(LoginMember login, Long themeId);

    /** C-11 주중/주말 시간표 저장 (통째 교체) */
    WeekSlotsResponse saveSlots(LoginMember login, Long themeId, WeekSlotsRequest req);
}

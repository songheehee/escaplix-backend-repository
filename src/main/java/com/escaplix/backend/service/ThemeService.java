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
import java.time.LocalDate;
import java.util.List;

/** B 담당 · 테마 탐색·찜·장르 (B-1 ~ B-7). 명세: docs/API.md (목업 레포) */
public interface ThemeService {

    /** B-1 테마 목록·검색·필터 */
    List<ThemeListItem> findThemes(LoginMember login, ThemeSearchCondition cond);

    /** B-2 테마 상세 */
    ThemeDetailResponse getTheme(LoginMember login, Long themeId);

    /** B-3 그날 시간표 */
    DaySlotsResponse getSlots(Long themeId, LocalDate date);

    /** B-4 찜 */
    WishIdResponse addWish(LoginMember login, Long themeId);

    /** B-5 찜 취소 */
    void removeWish(LoginMember login, Long themeId);

    /** B-6 찜 목록 */
    List<WishItem> getMyWishes(LoginMember login);

    /** B-7 장르 목록 */
    List<GenreResponse> getGenres();
}

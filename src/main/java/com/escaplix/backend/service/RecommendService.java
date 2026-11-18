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

/** D 담당 · 선호 장르·추천·통계 (D-7 ~ D-10). 명세: docs/API.md (목업 레포) */
public interface RecommendService {

    /** D-7 내 선호 장르 */
    List<GenreResponse> getMyGenres(LoginMember login);

    /** D-8 선호 장르 저장 (통째 교체) */
    List<GenreResponse> saveMyGenres(LoginMember login, GenreIdsRequest req);

    /** D-9 맞춤 추천 (메인 배너) */
    List<RecommendItem> getRecommendations(LoginMember login);

    /** D-10 내 통계 (선택 기능) */
    StatsResponse getStats(LoginMember login);
}

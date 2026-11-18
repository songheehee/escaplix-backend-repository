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

/** D 담당 · 후기·신고 (D-1 ~ D-6, D-11). 명세: docs/API.md (목업 레포) */
public interface ReviewService {

    /** D-1 후기 쓰기 */
    ReviewIdResponse createReview(LoginMember login, ReviewCreateRequest req);

    /** D-2 후기 수정 */
    MyReviewItem updateReview(LoginMember login, Long reviewId, ReviewUpdateRequest req);

    /** D-3 후기 삭제 */
    void deleteReview(LoginMember login, Long reviewId);

    /** D-4 테마 후기 + 평균 */
    ThemeReviewsResponse getThemeReviews(LoginMember login, Long themeId);

    /** D-11 후기 신고 */
    ReportIdResponse report(LoginMember login, Long reviewId, ReportRequest req);

    /** D-5 내 기록 */
    List<MyReviewItem> getMyReviews(LoginMember login);

    /** D-6 후기 쓸 수 있는 테마 */
    List<ReviewCandidateItem> getReviewCandidates(LoginMember login);
}

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
 * D · 후기·신고 (D-1 ~ D-4, D-11)
 * 컨트롤러는 받고 넘기기만 한다: 검사·권한 확인·트랜잭션은 서비스에서. 응답은 DTO 만 (엔티티 금지).
 */
@RestController
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    /** D-1 후기 쓰기 · USER · 201 */
    @PostMapping("/api/reviews")
    @ResponseStatus(HttpStatus.CREATED)
    @Auth(Role.USER)
    public ReviewIdResponse createReview(LoginMember login, @Valid @RequestBody ReviewCreateRequest req) {
        return reviewService.createReview(login, req);
    }

    /** D-2 후기 수정 · USER · 200 */
    @PutMapping("/api/reviews/{reviewId}")
    @Auth(Role.USER)
    public MyReviewItem updateReview(LoginMember login, @PathVariable Long reviewId, @Valid @RequestBody ReviewUpdateRequest req) {
        return reviewService.updateReview(login, reviewId, req);
    }

    /** D-3 후기 삭제 · USER · 204 */
    @DeleteMapping("/api/reviews/{reviewId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Auth(Role.USER)
    public void deleteReview(LoginMember login, @PathVariable Long reviewId) {
        reviewService.deleteReview(login, reviewId);
    }

    /** D-4 테마 후기 + 평균 · 공개 · 200 */
    @GetMapping("/api/themes/{themeId}/reviews")
    public ThemeReviewsResponse getThemeReviews(LoginMember login, @PathVariable Long themeId) {
        return reviewService.getThemeReviews(login, themeId);
    }

    /** D-11 후기 신고 · USER·OWNER · 201 */
    @PostMapping("/api/reviews/{reviewId}/report")
    @ResponseStatus(HttpStatus.CREATED)
    @Auth({Role.USER, Role.OWNER})
    public ReportIdResponse report(LoginMember login, @PathVariable Long reviewId, @Valid @RequestBody ReportRequest req) {
        return reviewService.report(login, reviewId, req);
    }
}

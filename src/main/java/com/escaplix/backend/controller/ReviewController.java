package com.escaplix.backend.controller;

import com.escaplix.backend.common.auth.*;
import com.escaplix.backend.dto.review.*;
import com.escaplix.backend.entity.*;
import com.escaplix.backend.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * D · 후기 (D-1 ~ D-4)
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

}

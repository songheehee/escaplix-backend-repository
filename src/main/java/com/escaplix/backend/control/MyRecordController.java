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
 * D · 내 기록·선호 장르·추천·통계 (D-5 ~ D-10)
 * 컨트롤러는 받고 넘기기만 한다: 검사·권한 확인·트랜잭션은 서비스에서. 응답은 DTO 만 (엔티티 금지).
 */
@RestController
@RequiredArgsConstructor
public class MyRecordController {

    private final RecommendService recommendService;
    private final ReviewService reviewService;

    /** D-5 내 기록 · USER · 200 */
    @GetMapping("/api/my/reviews")
    @Auth(Role.USER)
    public List<MyReviewItem> getMyReviews(LoginMember login) {
        return reviewService.getMyReviews(login);
    }

    /** D-6 후기 쓸 수 있는 테마 · USER · 200 */
    @GetMapping("/api/my/review-candidates")
    @Auth(Role.USER)
    public List<ReviewCandidateItem> getReviewCandidates(LoginMember login) {
        return reviewService.getReviewCandidates(login);
    }

    /** D-7 내 선호 장르 · USER · 200 */
    @GetMapping("/api/my/genres")
    @Auth(Role.USER)
    public List<GenreResponse> getMyGenres(LoginMember login) {
        return recommendService.getMyGenres(login);
    }

    /** D-8 선호 장르 저장 (통째 교체) · USER · 200 */
    @PutMapping("/api/my/genres")
    @Auth(Role.USER)
    public List<GenreResponse> saveMyGenres(LoginMember login, @Valid @RequestBody GenreIdsRequest req) {
        return recommendService.saveMyGenres(login, req);
    }

    /** D-9 맞춤 추천 (메인 배너) · USER · 200 */
    @GetMapping("/api/my/recommendations")
    @Auth(Role.USER)
    public List<RecommendItem> getRecommendations(LoginMember login) {
        return recommendService.getRecommendations(login);
    }

    /** D-10 내 통계 (선택 기능) · USER · 200 */
    @GetMapping("/api/my/stats")
    @Auth(Role.USER)
    public StatsResponse getStats(LoginMember login) {
        return recommendService.getStats(login);
    }
}

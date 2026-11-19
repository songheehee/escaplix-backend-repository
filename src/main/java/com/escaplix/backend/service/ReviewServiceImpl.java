package com.escaplix.backend.service;

import com.escaplix.backend.common.*;
import com.escaplix.backend.common.auth.*;
import com.escaplix.backend.dto.review.*;

import java.util.List;

import com.escaplix.backend.entity.Review;
import com.escaplix.backend.entity.ReviewStatus;
import com.escaplix.backend.entity.Theme;
import com.escaplix.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * ReviewService 구현 (D 담당). 지금은 전부 501 "아직 구현 전". TODO 를 채우고 throw 줄을 지운다.
 * 조회는 클래스의 readOnly 트랜잭션, 저장·수정·삭제 메서드에는 @Transactional 이 붙어 있다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final ReportRepository reportRepository;
    private final ThemeRepository themeRepository;
    private final MemberRepository memberRepository;
    private final PartyMemberRepository partyMemberRepository;

    @Override
    @Transactional
    public ReviewIdResponse createReview(LoginMember login, ReviewCreateRequest req) {
        // TODO(D) 테마가 OPEN 또는 CLOSED 아니면 404, 이미 썼으면 409
        // TODO(D) 실패(escaped=false)인데 remainSeconds ≠ 0 → 400, 성공이면 ≤ playMinutes*60 아니면 400
        // TODO(D) playedDate 비면 null 로 두기 (DB 기본값 = 작성일). 별점 집계는 트리거가 함
        throw ApiException.notImplemented("D-1 후기 쓰기");
    }

    @Override
    @Transactional
    public MyReviewItem updateReview(LoginMember login, Long reviewId, ReviewUpdateRequest req) {
        // TODO(D) 내 후기 아니면 403. 테마는 바꾸지 않음. 검사는 D-1 과 같음
        throw ApiException.notImplemented("D-2 후기 수정");
    }

    @Override
    @Transactional
    public void deleteReview(LoginMember login, Long reviewId) {
        // TODO(D) 내 후기 아니면 403
        // TODO(D) REPORT 먼저 삭제 → REVIEW 삭제 (한 트랜잭션)
        throw ApiException.notImplemented("D-3 후기 삭제");
    }

    /** D-4 테마 후기 + 평균 (공개). VISIBLE 만 최신순, 평균은 THEME 의 트리거 값 */
    @Override
    public ThemeReviewsResponse getThemeReviews(LoginMember login, Long themeId) {
        // 1. 주소의 번호로 테마 한 개 가져오기 (별점 집계·제한 시간이 THEME 에 있음). 없는 번호면 404
        Theme theme = themeRepository.findById(themeId)
                .orElseThrow(() -> ApiException.notFound("없는 테마입니다."));

        // 2. 평균 별점: 트리거가 채운 review_count, rating_sum 으로 계산 (후기 0개면 null)
        int count = theme.getReviewCount();
        Double avg = (count == 0) ? null
                : Math.round(theme.getRatingSum() * 10.0 / count) / 10.0;

        // 3. 요청한 사람의 회원번호. 토큰 없이 온 비회원이면 null
        Long myId = (login == null) ? null : login.memberId();

        // 4. 이 테마의 VISIBLE 후기만 최신순 (작성 시각이 같으면 번호 큰 것 먼저)
        List<Review> reviews = reviewRepository
                .findByTheme_ThemeIdAndStatusOrderByCreatedAtDescReviewIdDesc(themeId, ReviewStatus.VISIBLE);

        // 5. 엔티티 하나하나를 응답용 Item(DTO)으로 바꾸기
        List<ThemeReviewsResponse.Item> items = reviews.stream()
                .map(r -> new ThemeReviewsResponse.Item(
                        r.getReviewId(),
                        r.getMember().getNickname(),          // 닉네임은 MEMBER에서
                        r.getRating(),
                        r.getEscaped(),
                        r.getHintCount(),
                        r.getPlayerCount(),
                        r.getRemainMinutes(),                 // 남은 시간 분
                        r.getRemainSeconds(),                 // 남은 시간 초 (0~59)
                        r.getPlayedDate(),
                        r.getContent(),
                        r.getCreatedAt(),
                        myId != null && r.getMember().getMemberId().equals(myId),              // mine
                        myId != null && reportRepository
                                .existsByReview_ReviewIdAndReporter_MemberId(r.getReviewId(), myId)  // reportedByMe
                ))
                .toList();

        return new ThemeReviewsResponse(avg, count, theme.getPlayMinutes(), items);
    }

    @Override
    @Transactional
    public ReportIdResponse report(LoginMember login, Long reviewId, ReportRequest req) {
        // TODO(D) 없거나 HIDDEN 이면 404, 내 후기면 400, 이미 신고했으면 409
        // TODO(D) REPORT(WAIT) 저장 (처리는 나중 기능)
        throw ApiException.notImplemented("D-11 후기 신고");
    }

    @Override
    public List<MyReviewItem> getMyReviews(LoginMember login) {
        // TODO(D) 플레이한 날 최신순, HIDDEN 도 status 와 함께
        throw ApiException.notImplemented("D-5 내 기록");
    }

    @Override
    public List<ReviewCandidateItem> getReviewCandidates(LoginMember login) {
        // TODO(D) 내가 JOINED 였던 파티 중 play_at 지난 것의 테마, 내 후기 없는 것만
        // TODO(D) 같은 테마는 가장 최근 파티 하나
        throw ApiException.notImplemented("D-6 후기 쓸 수 있는 테마");
    }
}

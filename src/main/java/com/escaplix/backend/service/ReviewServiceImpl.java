package com.escaplix.backend.service;

import com.escaplix.backend.common.*;
import com.escaplix.backend.common.auth.*;
import com.escaplix.backend.dto.review.*;

import java.util.List;

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

    @Override
    public ThemeReviewsResponse getThemeReviews(LoginMember login, Long themeId) {
        // TODO(D) VISIBLE 만 최신순. avgRating·reviewCount 는 THEME 의 트리거 값
        // TODO(D) mine·reportedByMe 는 login 있을 때만
        throw ApiException.notImplemented("D-4 테마 후기 + 평균");
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

package com.escaplix.backend.service;

import com.escaplix.backend.common.*;
import com.escaplix.backend.common.auth.*;
import com.escaplix.backend.dto.review.*;

import java.util.List;

import com.escaplix.backend.entity.Review;
import com.escaplix.backend.entity.ReviewStatus;
import com.escaplix.backend.entity.Theme;
import com.escaplix.backend.entity.ThemeStatus;
import com.escaplix.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
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

    /** D-1 후기 쓰기 */
    @Override
    @Transactional
    public ReviewIdResponse createReview(LoginMember login, ReviewCreateRequest req) {
        Long myId = login.memberId();

        // 1. 테마 확인: OPEN 또는 CLOSED 만 후기 가능 (승인 전·반려 테마는 없는 것처럼 404)
        Theme theme = themeRepository.findById(req.themeId())
                .filter(t -> t.getStatus() == ThemeStatus.OPEN || t.getStatus() == ThemeStatus.CLOSED)
                .orElseThrow(() -> ApiException.notFound("없는 테마입니다."));

        // 2. 한 테마에 후기는 하나만
        if (reviewRepository.existsByMember_MemberIdAndTheme_ThemeId(myId, theme.getThemeId())) {
            throw ApiException.conflict("이미 이 테마에 후기를 남겼습니다. 내 기록에서 수정해 주세요.");
        }

        // 3. 남은 시간 규칙 (실패면 0, 제한 시간 이하)
        checkRemainTime(req.escaped(), req.remainMinutes(), req.remainSeconds(), theme);

        // 4. 새 후기 만들기. status·playedDate 를 비워 두면 DB 기본값(VISIBLE, 오늘)이 들어감 (@DynamicInsert)
        Review r = new Review();
        r.setMember(memberRepository.getReferenceById(myId));
        r.setTheme(theme);
        r.setRating(req.rating());
        r.setEscaped(req.escaped());
        r.setHintCount(req.hintCount());
        r.setPlayerCount(req.playerCount());
        r.setRemainMinutes(req.remainMinutes());
        r.setRemainSeconds(req.remainSeconds());
        r.setPlayedDate(req.playedDate());
        r.setContent(cleanContent(req.content()));

        // 5. 저장. 버튼을 두 번 눌러 동시에 들어와도 UNIQUE 가 막아 줌 → 409
        try {
            reviewRepository.saveAndFlush(r);
        } catch (DataIntegrityViolationException e) {
            throw ApiException.conflict("이미 이 테마에 후기를 남겼습니다. 내 기록에서 수정해 주세요.");
        }
        // 별점 집계는 트리거가 함
        return new ReviewIdResponse(r.getReviewId());
    }

    /** D-2 후기 수정. 테마는 바꾸지 않음 */
    @Override
    @Transactional
    public MyReviewItem updateReview(LoginMember login, Long reviewId, ReviewUpdateRequest req) {
        // 1. 내 후기인지 확인 (없으면 404, 남의 것이면 403)
        Review r = findMyReview(login, reviewId, "수정");

        // 2. 남은 시간 규칙은 D-1 과 같음 (제한 시간은 그 후기의 테마 기준)
        checkRemainTime(req.escaped(), req.remainMinutes(), req.remainSeconds(), r.getTheme());

        // 3. 값 바꾸기. save 를 부르지 않아도 트랜잭션이 끝날 때 바뀐 값이 UPDATE 됨
        r.setRating(req.rating());
        r.setEscaped(req.escaped());
        r.setHintCount(req.hintCount());
        r.setPlayerCount(req.playerCount());
        r.setRemainMinutes(req.remainMinutes());
        r.setRemainSeconds(req.remainSeconds());
        // 플레이한 날을 비우면 작성일 (결정 R1)
        r.setPlayedDate(req.playedDate() != null ? req.playedDate() : r.getCreatedAt().toLocalDate());
        r.setContent(cleanContent(req.content()));

        // 별점이 바뀌면 트리거가 THEME 집계를 고침
        return toMyReviewItem(r);
    }

    /** D-3 후기 삭제. 신고 먼저 지우고 후기 삭제 (FK) */
    @Override
    @Transactional
    public void deleteReview(LoginMember login, Long reviewId) {
        // 1. 내 후기인지 확인
        Review r = findMyReview(login, reviewId, "삭제");

        // 2. 이 후기를 가리키는 신고 먼저 지우기
        reportRepository.deleteByReview_ReviewId(reviewId);
        reportRepository.flush();

        // 3. 후기 지우기 (VISIBLE 이었다면 트리거가 THEME 집계에서 뺌)
        reviewRepository.delete(r);
    }

    /** D-4 테마 후기 + 평균 (공개). VISIBLE 만 최신순, 평균은 THEME 의 트리거 값 */
    @Override
    public ThemeReviewsResponse getThemeReviews(LoginMember login, Long themeId) {
        // 1. 주소의 번호로 테마 한 개 가져오기 (별점 집계·제한 시간이 THEME 에 있음). 없는 번호면 404
        Theme theme = themeRepository.findById(themeId)
                .orElseThrow(() -> ApiException.notFound("없는 테마입니다."));

        // 2. 평균 별점: Theme 의 공용 계산 메서드 사용 (후기 0개면 null)
        int count = theme.getReviewCount();
        Double avg = theme.getAvgRating();

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

    /** D-5 내 기록. 플레이한 날 최신순, 숨김 후기도 status 와 함께 */
    @Override
    public List<MyReviewItem> getMyReviews(LoginMember login) {
        // 1. 토큰의 회원번호로 내 후기 전부 (숨김 포함)
        List<Review> reviews = reviewRepository
                .findByMember_MemberIdOrderByPlayedDateDescReviewIdDesc(login.memberId());

        // 2. 한 줄씩 MyReviewItem(DTO)으로
        return reviews.stream()
                .map(this::toMyReviewItem)
                .toList();
    }

    @Override
    public List<ReviewCandidateItem> getReviewCandidates(LoginMember login) {
        // TODO(D) 내가 JOINED 였던 파티 중 play_at 지난 것의 테마, 내 후기 없는 것만
        // TODO(D) 같은 테마는 가장 최근 파티 하나
        throw ApiException.notImplemented("D-6 후기 쓸 수 있는 테마");
    }

    /** Review 엔티티 → MyReviewItem (D-5 목록 한 줄, D-2 수정 응답에서 같이 씀) */
    private MyReviewItem toMyReviewItem(Review r) {
        Theme theme = r.getTheme();
        return new MyReviewItem(
                r.getReviewId(),
                theme.getThemeId(),
                theme.getTitle(),
                theme.getStore().getName(),   // 매장 이름: 후기 → 테마 → 매장
                theme.getPlayMinutes(),       // 화면 "12:34 남기고 (70:00 중)" 표시용
                r.getRating(),
                r.getEscaped(),
                r.getHintCount(),
                r.getPlayerCount(),
                r.getRemainMinutes(),
                r.getRemainSeconds(),
                r.getPlayedDate(),
                r.getContent(),
                r.getStatus()
        );
    }

    /** 남은 시간 규칙 (D-1, D-2). 실패면 분·초 둘 다 0, 성공이면 제한 시간 이하 */
    private void checkRemainTime(Boolean escaped, int minutes, int seconds, Theme theme) {
        if (!escaped && (minutes != 0 || seconds != 0)) {
            throw ApiException.badRequest("탈출에 실패했다면 남은 시간은 0이어야 합니다.");
        }
        int limit = theme.getPlayMinutes();
        if (minutes * 60 + seconds > limit * 60) {
            throw ApiException.badRequest("남은 시간은 제한 시간(" + limit + "분)보다 길 수 없습니다.");
        }
    }

    /** 후기를 찾고 작성자 본인인지 확인 (D-2, D-3). 없으면 404, 남의 것이면 403 */
    private Review findMyReview(LoginMember login, Long reviewId, String action) {
        Review r = reviewRepository.findById(reviewId)
                .orElseThrow(() -> ApiException.notFound("없는 후기입니다."));
        if (!r.getMember().getMemberId().equals(login.memberId())) {
            throw ApiException.forbidden("내 후기만 " + action + "할 수 있습니다.");
        }
        return r;
    }

    /** 한 줄 후기 정리: 앞뒤 공백 제거, 빈 글이면 null (별점만 남긴 기록) */
    private String cleanContent(String content) {
        return (content == null || content.isBlank()) ? null : content.strip();
    }
}

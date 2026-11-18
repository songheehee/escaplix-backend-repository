package com.escaplix.backend.service;

import com.escaplix.backend.common.*;
import com.escaplix.backend.common.auth.*;
import com.escaplix.backend.dto.review.*;
import com.escaplix.backend.dto.theme.*;

import java.util.List;

import com.escaplix.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * RecommendService 구현 (D 담당). 지금은 전부 501 "아직 구현 전". TODO 를 채우고 throw 줄을 지운다.
 * 조회는 클래스의 readOnly 트랜잭션, 저장·수정·삭제 메서드에는 @Transactional 이 붙어 있다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecommendServiceImpl implements RecommendService {

    private final MemberGenreRepository memberGenreRepository;
    private final GenreRepository genreRepository;
    private final MemberRepository memberRepository;
    private final ThemeRepository themeRepository;
    private final ReviewRepository reviewRepository;

    @Override
    public List<GenreResponse> getMyGenres(LoginMember login) {
        throw ApiException.notImplemented("D-7 내 선호 장르");
    }

    @Override
    @Transactional
    public List<GenreResponse> saveMyGenres(LoginMember login, GenreIdsRequest req) {
        // TODO(D) 지우고 다시 넣기 (한 트랜잭션). 없는 장르면 400
        // TODO(D) 저장 뒤 목록 반환
        throw ApiException.notImplemented("D-8 선호 장르 저장 (통째 교체)");
    }

    @Override
    public List<RecommendItem> getRecommendations(LoginMember login) {
        // TODO(D) 공개 테마 중 선호 장르가 하나라도 겹치는 것, 내가 후기 쓴 테마 제외
        // TODO(D) 정렬: 겹치는 장르 수 → 평균 별점 → 최신, 최대 5개
        // TODO(D) reason 문장은 API.md D-9 표대로. 맞는 게 없으면 평점 상위 5개 + "평점 높은 인기 테마"
        throw ApiException.notImplemented("D-9 맞춤 추천 (메인 배너)");
    }

    @Override
    public StatsResponse getStats(LoginMember login) {
        // TODO(D) 내 후기 기준 (숨김 포함)
        throw ApiException.notImplemented("D-10 내 통계 (선택 기능)");
    }
}

package com.escaplix.backend.service;

import com.escaplix.backend.common.*;
import com.escaplix.backend.common.auth.*;
import com.escaplix.backend.dto.review.*;
import com.escaplix.backend.dto.theme.*;

import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;

import com.escaplix.backend.entity.Genre;
import com.escaplix.backend.entity.Member;
import com.escaplix.backend.entity.MemberGenre;
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

    /** D-7 내 선호 장르. 장르 번호순 */
    @Override
    public List<GenreResponse> getMyGenres(LoginMember login) {
        // 1. 토큰의 회원번호로 내 MEMBER_GENRE 줄들 가져오기
        List<MemberGenre> rows = memberGenreRepository.findByMember_MemberId(login.memberId());

        // 2. 각 줄의 장르를 GenreResponse(DTO)로 바꾸고, 장르 번호순 정렬
        return rows.stream()
                .map(mg -> new GenreResponse(mg.getGenre().getGenreId(), mg.getGenre().getName()))
                .sorted(Comparator.comparing(GenreResponse::genreId))
                .toList();
    }

    /** D-8 선호 장르 통째 교체. 한 트랜잭션에서 지우고 다시 넣기 */
    @Override
    @Transactional
    public List<GenreResponse> saveMyGenres(LoginMember login, GenreIdsRequest req) {
        Long myId = login.memberId();

        // 1. 같은 번호가 두 번 오면 하나로 (UNIQUE(member_id, genre_id) 위반 방지)
        List<Long> genreIds = new LinkedHashSet<>(req.genreIds()).stream().toList();

        // 2. 보낸 장르가 전부 실제로 있는지 확인. 하나라도 없으면 400
        List<Genre> genres = genreRepository.findAllById(genreIds);
        if (genres.size() != genreIds.size()) {
            throw ApiException.badRequest("없는 장르가 포함되어 있습니다.");
        }

        // 3. 내 기존 선호 장르 전부 지우기
        memberGenreRepository.deleteByMember_MemberId(myId);
        memberGenreRepository.flush();   // DELETE 를 INSERT 보다 먼저 DB에 보냄 (UNIQUE(member_id, genre_id) 충돌 방지)
        
        // 4. 새로 고른 장르 넣기 (빈 배열이면 아무것도 안 넣음 = 전부 해제)
        Member me = memberRepository.getReferenceById(myId);
        for (Genre genre : genres) {
            MemberGenre mg = new MemberGenre();
            mg.setMember(me);
            mg.setGenre(genre);
            memberGenreRepository.save(mg);
        }

        // 5. 저장된 결과를 D-7 모양으로 돌려주기
        return genres.stream()
                .map(g -> new GenreResponse(g.getGenreId(), g.getName()))
                .sorted(Comparator.comparing(GenreResponse::genreId))
                .toList();
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

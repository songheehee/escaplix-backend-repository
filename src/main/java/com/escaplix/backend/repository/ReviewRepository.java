package com.escaplix.backend.repository;

import com.escaplix.backend.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

/** REVIEW (D) */
public interface ReviewRepository extends JpaRepository<Review, Long> {

    boolean existsByMember_MemberIdAndTheme_ThemeId(Long memberId, Long themeId);

    /** 테마 후기 (D-4): 보이는 것만 최신순 (같은 시각이면 번호 큰 것 먼저) */
    List<Review> findByTheme_ThemeIdAndStatusOrderByCreatedAtDescReviewIdDesc(Long themeId, ReviewStatus status);

    /** 내 기록 (D-5) */
    List<Review> findByMember_MemberIdOrderByPlayedDateDesc(Long memberId);
}

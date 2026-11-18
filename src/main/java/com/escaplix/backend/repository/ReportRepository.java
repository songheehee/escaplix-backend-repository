package com.escaplix.backend.repository;

import com.escaplix.backend.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;

/** REPORT (D) */
public interface ReportRepository extends JpaRepository<Report, Long> {

    boolean existsByReview_ReviewIdAndReporter_MemberId(Long reviewId, Long reporterId);

    /** 후기 삭제(D-3) 때 신고 먼저 지움 */
    void deleteByReview_ReviewId(Long reviewId);

    long countByStatus(ReportStatus status);
}

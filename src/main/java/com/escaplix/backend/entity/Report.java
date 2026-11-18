package com.escaplix.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.DynamicInsert;

import java.time.LocalDateTime;

/** REPORT 후기 신고 (D). 이번 범위는 접수(WAIT)까지 */
@Entity
@Table(name = "report")
@DynamicInsert   // null 인 컬럼은 INSERT 에서 빼서 DB DEFAULT 가 들어가게
@Getter
@Setter
@NoArgsConstructor
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sq_report")
    @SequenceGenerator(name = "sq_report", sequenceName = "sq_report", allocationSize = 1)
    @Column(name = "report_id")
    private Long reportId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id", nullable = false)
    private Review review;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id", nullable = false)
    private Member reporter;

    /** 자유 글 사유 (결정 R3) */
    @Column(nullable = false, length = 900)
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private ReportStatus status;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;
}

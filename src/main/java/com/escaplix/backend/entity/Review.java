package com.escaplix.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.DynamicInsert;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** REVIEW 후기 (D). 닉네임은 저장하지 않고 member 에서. 별점 집계는 트리거 */
@Entity
@Table(name = "review")
@DynamicInsert   // null 인 컬럼은 INSERT 에서 빼서 DB DEFAULT 가 들어가게
@Getter
@Setter
@NoArgsConstructor
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sq_review")
    @SequenceGenerator(name = "sq_review", sequenceName = "sq_review", allocationSize = 1)
    @Column(name = "review_id")
    private Long reviewId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    /** 수정 API 에서 바꾸지 않는다 (트리거 전제) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "theme_id", nullable = false)
    private Theme theme;

    @Column(nullable = false)
    private Integer rating;

    /** DB 는 'Y'/'N' */
    @Convert(converter = BooleanToYnConverter.class)
    @Column(nullable = false, length = 1)
    private Boolean escaped;

    @Column(name = "hint_count")
    private Integer hintCount;

    @Column(name = "player_count", nullable = false)
    private Integer playerCount;

    /** 남은 시간 분. 실패면 0 */
    @Column(name = "remain_minutes")
    private Integer remainMinutes;

    /** 남은 시간 초 0~59. 실패면 0 (CHECK review_sec_ck, review_fail_ck) */
    @Column(name = "remain_seconds")
    private Integer remainSeconds;

    /** 비우면 DB 기본값 TRUNC(SYSDATE) */
    @Column(name = "played_date")
    private LocalDate playedDate;

    @Column(length = 600)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private ReviewStatus status;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;
}

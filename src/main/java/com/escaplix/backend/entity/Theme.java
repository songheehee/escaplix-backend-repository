package com.escaplix.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.DynamicInsert;

import java.time.LocalDateTime;

/** THEME 테마 (C). 손님 화면에는 OPEN 이면서 매장 APPROVED 인 것만 */
@Entity
@Table(name = "theme")
@DynamicInsert   // null 인 컬럼은 INSERT 에서 빼서 DB DEFAULT 가 들어가게
@Getter
@Setter
@NoArgsConstructor
public class Theme {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sq_theme")
    @SequenceGenerator(name = "sq_theme", sequenceName = "sq_theme", allocationSize = 1)
    @Column(name = "theme_id")
    private Long themeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(length = 4000)
    private String description;

    @Column(nullable = false)
    private Integer difficulty;

    /** 제한 시간(분). 종료 시각·남은 시간 상한 계산에 쓴다 */
    @Column(name = "play_minutes", nullable = false)
    private Integer playMinutes;

    @Column(name = "min_people", nullable = false)
    private Integer minPeople;

    @Column(name = "max_people", nullable = false)
    private Integer maxPeople;

    @Column(nullable = false)
    private Integer price;

    /** 공개 주소 "/uploads/posters/uuid.jpg" */
    @Column(name = "poster_url", length = 500)
    private String posterUrl;

    @Column(name = "booking_url", nullable = false, length = 500)
    private String bookingUrl;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private ThemeSource source;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private ThemeStatus status;

    @Column(name = "reject_reason", length = 900)
    private String rejectReason;

    /** 트리거 trg_review_theme_stats 가 관리. 자바에서 쓰지 않는다 */
    @Column(name = "review_count", insertable = false, updatable = false)
    private Integer reviewCount;

    /** 트리거가 관리. 평균 = ratingSum / reviewCount */
    @Column(name = "rating_sum", insertable = false, updatable = false)
    private Integer ratingSum;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    /** 트리거 trg_theme_updated_at 가 관리 */
    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    /** 평균 별점 (소수 첫째 자리). 후기 없으면 null */
    public Double getAvgRating() {
        if (reviewCount == null || reviewCount == 0) return null;
        return Math.round(ratingSum * 10.0 / reviewCount) / 10.0;
    }
}

package com.escaplix.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.DynamicInsert;

import java.time.LocalDateTime;

/** TIME_SLOT 주중/주말 반복 시간표 (C). 종료 시각은 저장하지 않고 startTime + playMinutes */
@Entity
@Table(name = "time_slot")
@DynamicInsert   // null 인 컬럼은 INSERT 에서 빼서 DB DEFAULT 가 들어가게
@Getter
@Setter
@NoArgsConstructor
public class TimeSlot {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sq_time_slot")
    @SequenceGenerator(name = "sq_time_slot", sequenceName = "sq_time_slot", allocationSize = 1)
    @Column(name = "slot_id")
    private Long slotId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "theme_id", nullable = false)
    private Theme theme;

    @Enumerated(EnumType.STRING)
    @Column(name = "day_type", nullable = false, length = 10)
    private DayType dayType;

    /** "19:00" */
    @Column(name = "start_time", nullable = false, length = 5)
    private String startTime;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;
}

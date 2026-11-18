package com.escaplix.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.DynamicInsert;

import java.time.LocalDateTime;

/** PARTY 파티 모집 글 (A). 현재 인원 = PARTY_MEMBER 중 JOINED 수. 삭제는 status=DELETED */
@Entity
@Table(name = "party")
@DynamicInsert   // null 인 컬럼은 INSERT 에서 빼서 DB DEFAULT 가 들어가게
@Getter
@Setter
@NoArgsConstructor
public class Party {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sq_party")
    @SequenceGenerator(name = "sq_party", sequenceName = "sq_party", allocationSize = 1)
    @Column(name = "party_id")
    private Long partyId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "theme_id", nullable = false)
    private Theme theme;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "leader_id", nullable = false)
    private Member leader;

    @Column(nullable = false, length = 300)
    private String title;

    /** 방장 한마디 (100자) */
    @Column(nullable = false, length = 300)
    private String intro;

    @Column(name = "play_at", nullable = false)
    private LocalDateTime playAt;

    @Column(name = "deadline_at", nullable = false)
    private LocalDateTime deadlineAt;

    @Column(name = "max_people", nullable = false)
    private Integer maxPeople;

    @Enumerated(EnumType.STRING)
    @Column(length = 12)
    private PartyStatus status;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;
}

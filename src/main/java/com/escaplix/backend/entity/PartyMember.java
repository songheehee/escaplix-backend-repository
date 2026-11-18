package com.escaplix.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.DynamicInsert;

import java.time.LocalDateTime;

/** PARTY_MEMBER 파티 참여자 (A). 나가기 = 줄 삭제, 강퇴 = status KICKED 로 남김(재참여 불가) */
@Entity
@Table(name = "party_member")
@DynamicInsert   // null 인 컬럼은 INSERT 에서 빼서 DB DEFAULT 가 들어가게
@Getter
@Setter
@NoArgsConstructor
public class PartyMember {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sq_party_member")
    @SequenceGenerator(name = "sq_party_member", sequenceName = "sq_party_member", allocationSize = 1)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "party_id", nullable = false)
    private Party party;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private PartyMemberStatus status;

    @Column(name = "joined_at", insertable = false, updatable = false)
    private LocalDateTime joinedAt;
}

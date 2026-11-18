package com.escaplix.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.DynamicInsert;

import java.time.LocalDateTime;

/** PARTY_MESSAGE 파티 채팅 (A). 안내 줄(JOIN/LEAVE/KICK)은 content 없이 저장 */
@Entity
@Table(name = "party_message")
@DynamicInsert   // null 인 컬럼은 INSERT 에서 빼서 DB DEFAULT 가 들어가게
@Getter
@Setter
@NoArgsConstructor
public class PartyMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sq_party_message")
    @SequenceGenerator(name = "sq_party_message", sequenceName = "sq_party_message", allocationSize = 1)
    @Column(name = "message_id")
    private Long messageId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "party_id", nullable = false)
    private Party party;

    /** 보낸 사람, 안내 줄이면 그 대상 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(name = "msg_type", length = 10)
    private MessageType msgType;

    @Column(length = 1500)
    private String content;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;
}

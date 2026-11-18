package com.escaplix.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.DynamicInsert;

/** MEMBER_GENRE 선호 장르 N:M (D). 손님 가입(A-1) 때 처음 들어감 */
@Entity
@Table(name = "member_genre")
@Getter
@Setter
@NoArgsConstructor
public class MemberGenre {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sq_member_genre")
    @SequenceGenerator(name = "sq_member_genre", sequenceName = "sq_member_genre", allocationSize = 1)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "genre_id", nullable = false)
    private Genre genre;
}

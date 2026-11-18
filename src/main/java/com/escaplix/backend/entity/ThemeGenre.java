package com.escaplix.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.DynamicInsert;

/** THEME_GENRE 테마-장르 N:M (C). 테마 저장과 같은 트랜잭션에서 지우고 다시 넣는다 */
@Entity
@Table(name = "theme_genre")
@Getter
@Setter
@NoArgsConstructor
public class ThemeGenre {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sq_theme_genre")
    @SequenceGenerator(name = "sq_theme_genre", sequenceName = "sq_theme_genre", allocationSize = 1)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "theme_id", nullable = false)
    private Theme theme;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "genre_id", nullable = false)
    private Genre genre;
}

package com.escaplix.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.DynamicInsert;

/** GENRE 장르 코드 (C). 더미 데이터로만 넣는다 */
@Entity
@Table(name = "genre")
@Getter
@Setter
@NoArgsConstructor
public class Genre {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sq_genre")
    @SequenceGenerator(name = "sq_genre", sequenceName = "sq_genre", allocationSize = 1)
    @Column(name = "genre_id")
    private Long genreId;

    @Column(nullable = false, length = 30)
    private String name;
}

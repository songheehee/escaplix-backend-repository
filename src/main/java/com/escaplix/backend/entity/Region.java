package com.escaplix.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** REGION 큰 지역 코드 (서울 / 경기 / 인천 ...). 더미 데이터로만 넣는다 (결정 L1) */
@Entity
@Table(name = "region")
@Getter
@Setter
@NoArgsConstructor
public class Region {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sq_region")
    @SequenceGenerator(name = "sq_region", sequenceName = "sq_region", allocationSize = 1)
    @Column(name = "region_id")
    private Long regionId;

    @Column(nullable = false, length = 30)
    private String name;

    /** 화면에 보일 순서 (작을수록 앞) */
    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;
}

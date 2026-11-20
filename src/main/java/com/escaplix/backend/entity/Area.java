package com.escaplix.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * AREA 세부 지역 코드 (홍대 / 강남 / 건대 ...). 더미 데이터로만 넣는다 (결정 L1)
 * STORE.area 에 이 name 이 그대로 들어간다 (DB FK store_area_fk). 그래서 name 은 전체에서 겹치지 않는다
 */
@Entity
@Table(name = "area")
@Getter
@Setter
@NoArgsConstructor
public class Area {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sq_area")
    @SequenceGenerator(name = "sq_area", sequenceName = "sq_area", allocationSize = 1)
    @Column(name = "area_id")
    private Long areaId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "region_id", nullable = false)
    private Region region;

    @Column(nullable = false, length = 30)
    private String name;

    /** 큰 지역 안에서의 순서 */
    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;
}

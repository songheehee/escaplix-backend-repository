package com.escaplix.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.DynamicInsert;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** STORE 매장 (C). 수정·등록하면 PENDING, 관리자가 APPROVED/REJECTED. 삭제하지 않음 */
@Entity
@Table(name = "store")
@DynamicInsert   // null 인 컬럼은 INSERT 에서 빼서 DB DEFAULT 가 들어가게
@Getter
@Setter
@NoArgsConstructor
public class Store {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sq_store")
    @SequenceGenerator(name = "sq_store", sequenceName = "sq_store", allocationSize = 1)
    @Column(name = "store_id")
    private Long storeId;

    /** 등록한 사장. 관리자가 직접 등록하면 null */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    private Member owner;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false, length = 30)
    private String area;

    @Column(nullable = false, length = 300)
    private String address;

    @Column(nullable = false, length = 20)
    private String phone;

    @Column(name = "website_url", nullable = false, length = 500)
    private String websiteUrl;

    @Column(nullable = false, length = 150)
    private String hours;

    @Column(nullable = false, length = 600)
    private String intro;

    /** 매장 사진 공개 주소 "/uploads/stores/uuid.jpg" (FileStorage.saveStorePhoto). 없으면 null */
    @Column(name = "photo_url", length = 500)
    private String photoUrl;

    @Column(precision = 9, scale = 6)
    private BigDecimal lat;

    @Column(precision = 9, scale = 6)
    private BigDecimal lng;

    @Column(name = "biz_place_no", nullable = false, length = 12)
    private String bizPlaceNo;

    @Column(name = "biz_place_file_path", length = 500)
    private String bizPlaceFilePath;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private StoreStatus status;

    /** 반려 사유. REJECTED 일 때 필수 (CHECK store_reject_ck), PENDING 으로 돌아가면 비움 */
    @Column(name = "reject_reason", length = 900)
    private String rejectReason;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;
}

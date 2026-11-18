package com.escaplix.backend.repository;

import com.escaplix.backend.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

/** STORE (C) */
public interface StoreRepository extends JpaRepository<Store, Long> {

    boolean existsByBizPlaceNo(String bizPlaceNo);

    /** 매장 수정(C-4) 때 내 매장을 뺀 나머지에서 번호 중복 확인 */
    boolean existsByBizPlaceNoAndStoreIdNot(String bizPlaceNo, Long storeId);

    /** 관리자 홈(C-12): 가장 오래된 승인 대기 매장 */
    Optional<Store> findFirstByStatusOrderByCreatedAtAsc(StoreStatus status);

    /** 사장의 매장 (C-1) */
    List<Store> findByOwner_MemberIdOrderByCreatedAtDesc(Long ownerId);

    /** 관리자가 직접 등록한 매장 (C-1, 관리자) */
    List<Store> findByOwnerIsNullOrderByCreatedAtDesc();

    /** 승인 목록 (C-13): 오래된 순 */
    List<Store> findByStatusOrderByCreatedAtAsc(StoreStatus status);

    List<Store> findByStatus(StoreStatus status);

    long countByStatus(StoreStatus status);

    long countByOwner_MemberId(Long ownerId);
}

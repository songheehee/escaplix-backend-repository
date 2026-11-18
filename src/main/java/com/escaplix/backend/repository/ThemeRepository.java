package com.escaplix.backend.repository;

import com.escaplix.backend.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

/** THEME (C). 손님 화면 목록(B-1)은 조건이 많아서 @Query 나 Specification 으로 직접 작성 */
public interface ThemeRepository extends JpaRepository<Theme, Long> {

    List<Theme> findByStore_StoreIdOrderByCreatedAtDesc(Long storeId);

    /** 승인 목록 (C-17): 오래된 순 */
    List<Theme> findByStatusOrderByCreatedAtAsc(ThemeStatus status);

    long countByStatus(ThemeStatus status);
}

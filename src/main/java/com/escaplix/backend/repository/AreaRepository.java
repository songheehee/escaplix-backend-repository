package com.escaplix.backend.repository;

import com.escaplix.backend.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/** AREA 세부 지역 (B: 지역 목록 B-8·B-12, C: 매장 등록·수정 때 지역 검사) */
public interface AreaRepository extends JpaRepository<Area, Long> {

    /** 매장 등록·수정 (C-3, C-4): 목록에 있는 지역인지 */
    boolean existsByName(String name);

    /** 전체 세부 지역 (큰 지역 함께), 큰 지역 순서 → 세부 지역 순서 */
    @Query("select a from Area a join fetch a.region r order by r.sortOrder, a.sortOrder, a.areaId")
    List<Area> findAllOrdered();

    /** B-8: 승인된 매장이 하나라도 있는 세부 지역 이름, 큰 지역 순서 → 세부 지역 순서 */
    @Query("select a.name from Area a join a.region r" +
           " where exists (select 1 from Store s where s.area = a.name" +
           "               and s.status = com.escaplix.backend.entity.StoreStatus.APPROVED)" +
           " order by r.sortOrder, a.sortOrder, a.areaId")
    List<String> findNamesWithApprovedStores();
}

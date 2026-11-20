package com.escaplix.backend.repository;

import com.escaplix.backend.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;

/** REGION 큰 지역 */
public interface RegionRepository extends JpaRepository<Region, Long> {
}

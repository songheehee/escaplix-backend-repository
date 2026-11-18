package com.escaplix.backend.repository;

import com.escaplix.backend.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;

/** HOLIDAY (C). findById(날짜) 로 공휴일인지 본다 */
public interface HolidayRepository extends JpaRepository<Holiday, LocalDate> {
}

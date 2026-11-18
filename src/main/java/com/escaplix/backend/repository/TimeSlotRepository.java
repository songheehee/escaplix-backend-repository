package com.escaplix.backend.repository;

import com.escaplix.backend.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

/** TIME_SLOT (C) */
public interface TimeSlotRepository extends JpaRepository<TimeSlot, Long> {

    List<TimeSlot> findByTheme_ThemeIdAndDayTypeOrderByStartTimeAsc(Long themeId, DayType dayType);

    List<TimeSlot> findByTheme_ThemeIdOrderByStartTimeAsc(Long themeId);

    boolean existsByTheme_ThemeIdAndDayTypeAndStartTime(Long themeId, DayType dayType, String startTime);

    void deleteByTheme_ThemeId(Long themeId);
}

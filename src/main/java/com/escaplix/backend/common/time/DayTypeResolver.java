package com.escaplix.backend.common.time;

import com.escaplix.backend.entity.DayType;
import com.escaplix.backend.entity.Holiday;
import com.escaplix.backend.repository.HolidayRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Optional;

/**
 * 날짜 → 주중/주말 (ERD 3-7). 토·일, 또는 HOLIDAY 에 있는 날짜면 WEEKEND (결정 T1).
 * B-1 날짜 필터, B-3 시간표, A-14 파티 만들기가 같이 쓴다. 규칙을 각자 다시 만들지 말 것.
 */
@Component
@RequiredArgsConstructor
public class DayTypeResolver {

    private final HolidayRepository holidayRepository;

    public DayType of(LocalDate date) {
        DayOfWeek d = date.getDayOfWeek();
        if (d == DayOfWeek.SATURDAY || d == DayOfWeek.SUNDAY || holidayRepository.existsById(date)) {
            return DayType.WEEKEND;
        }
        return DayType.WEEKDAY;
    }

    /** 공휴일이면 이름 (B-3 holidayName), 아니면 빈 값 */
    public Optional<String> holidayName(LocalDate date) {
        return holidayRepository.findById(date).map(Holiday::getName);
    }
}

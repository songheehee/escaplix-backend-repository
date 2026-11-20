package com.escaplix.backend.dto.theme;

import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;

/** B-1 조건. 모두 선택. date 는 오늘 이전이면 400. sort: rate(기본) / new */
public record ThemeSearchCondition(
        String keyword,
        String area,
        String region,      // 큰 지역 이름 (서울·경기…): 그 안의 모든 세부 지역. area 가 있으면 area 가 우선
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
        Integer people,
        Long genreId,
        String sort
) {}

package com.escaplix.backend.dto.theme;

import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;

/** B-1 조건. 모두 선택. date 는 오늘 이전이면 400. sort: rate(기본) / new */
public record ThemeSearchCondition(
        String keyword,
        String area,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
        Integer people,
        Long genreId,
        String sort
) {}

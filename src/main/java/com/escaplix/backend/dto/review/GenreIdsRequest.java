package com.escaplix.backend.dto.review;

import jakarta.validation.constraints.*;
import java.util.List;

/** D-8 선호 장르 통째 저장 (빈 배열이면 모두 지움) */
public record GenreIdsRequest(
        @NotNull(message = "장르 목록을 보내 주세요 (없으면 빈 배열).") List<Long> genreIds
) {}

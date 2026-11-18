package com.escaplix.backend.dto.theme;


/** B-7 장르 (D-7 선호 장르에도 씀) */
public record GenreResponse(
        Long genreId,
        String name
) {}

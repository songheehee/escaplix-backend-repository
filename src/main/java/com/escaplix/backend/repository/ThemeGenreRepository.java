package com.escaplix.backend.repository;

import com.escaplix.backend.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

/** THEME_GENRE (C) */
public interface ThemeGenreRepository extends JpaRepository<ThemeGenre, Long> {

    List<ThemeGenre> findByTheme_ThemeId(Long themeId);

    void deleteByTheme_ThemeId(Long themeId);
}

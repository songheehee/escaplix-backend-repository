package com.escaplix.backend.control;

import com.escaplix.backend.common.*;
import com.escaplix.backend.common.auth.*;
import com.escaplix.backend.common.file.FileDownload;
import com.escaplix.backend.dto.admin.*;
import com.escaplix.backend.dto.member.*;
import com.escaplix.backend.dto.owner.*;
import com.escaplix.backend.dto.party.*;
import com.escaplix.backend.dto.review.*;
import com.escaplix.backend.dto.store.*;
import com.escaplix.backend.dto.theme.*;
import com.escaplix.backend.entity.*;
import com.escaplix.backend.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

/**
 * B · 테마·찜·장르 (B-1 ~ B-7)
 * 컨트롤러는 받고 넘기기만 한다: 검사·권한 확인·트랜잭션은 서비스에서. 응답은 DTO 만 (엔티티 금지).
 */
@RestController
@RequiredArgsConstructor
public class ThemeController {

    private final ThemeService themeService;

    /** B-1 테마 목록·검색·필터 · 공개 · 200 */
    @GetMapping("/api/themes")
    public List<ThemeListItem> findThemes(LoginMember login, @ModelAttribute ThemeSearchCondition cond) {
        return themeService.findThemes(login, cond);
    }

    /** B-2 테마 상세 · 공개 · 200 */
    @GetMapping("/api/themes/{themeId}")
    public ThemeDetailResponse getTheme(LoginMember login, @PathVariable Long themeId) {
        return themeService.getTheme(login, themeId);
    }

    /** B-3 그날 시간표 · 공개 · 200 */
    @GetMapping("/api/themes/{themeId}/slots")
    public DaySlotsResponse getSlots(@PathVariable Long themeId, @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @RequestParam LocalDate date) {
        return themeService.getSlots(themeId, date);
    }

    /** B-4 찜 · USER · 201 */
    @PostMapping("/api/themes/{themeId}/wish")
    @ResponseStatus(HttpStatus.CREATED)
    @Auth(Role.USER)
    public WishIdResponse addWish(LoginMember login, @PathVariable Long themeId) {
        return themeService.addWish(login, themeId);
    }

    /** B-5 찜 취소 · USER · 204 */
    @DeleteMapping("/api/themes/{themeId}/wish")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Auth(Role.USER)
    public void removeWish(LoginMember login, @PathVariable Long themeId) {
        themeService.removeWish(login, themeId);
    }

    /** B-6 찜 목록 · USER · 200 */
    @GetMapping("/api/my/wishes")
    @Auth(Role.USER)
    public List<WishItem> getMyWishes(LoginMember login) {
        return themeService.getMyWishes(login);
    }

    /** B-7 장르 목록 · 공개 · 200 */
    @GetMapping("/api/genres")
    public List<GenreResponse> getGenres() {
        return themeService.getGenres();
    }
}

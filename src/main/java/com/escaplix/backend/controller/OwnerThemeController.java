package com.escaplix.backend.controller;

import com.escaplix.backend.common.auth.*;
import com.escaplix.backend.dto.owner.*;
import com.escaplix.backend.entity.*;
import com.escaplix.backend.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * C · 사장 테마·시간표 (C-6 ~ C-11)
 * 컨트롤러는 받고 넘기기만 한다: 검사·권한 확인·트랜잭션은 서비스에서. 응답은 DTO 만 (엔티티 금지).
 */
@RestController
@RequiredArgsConstructor
public class OwnerThemeController {

    private final OwnerThemeService ownerThemeService;

    /** C-6 테마 수정용 상세 · OWNER* · 200 */
    @GetMapping("/api/owner/themes/{themeId}")
    @Auth({Role.OWNER, Role.ADMIN})
    public OwnerThemeDetailResponse getTheme(LoginMember login, @PathVariable Long themeId) {
        return ownerThemeService.getTheme(login, themeId);
    }

    /** C-7 테마 등록 · OWNER* · 201 */
    @PostMapping(value = "/api/owner/themes", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @Auth({Role.OWNER, Role.ADMIN})
    public ThemeSaveResponse createTheme(LoginMember login, @Valid @ModelAttribute ThemeForm form, @RequestPart(value = "poster", required = false) MultipartFile poster) {
        return ownerThemeService.createTheme(login, form, poster);
    }

    /** C-8 테마 수정 · OWNER* · 200 */
    @PutMapping(value = "/api/owner/themes/{themeId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Auth({Role.OWNER, Role.ADMIN})
    public ThemeSaveResponse updateTheme(LoginMember login, @PathVariable Long themeId, @Valid @ModelAttribute ThemeForm form, @RequestPart(value = "poster", required = false) MultipartFile poster) {
        return ownerThemeService.updateTheme(login, themeId, form, poster);
    }

    /** C-9 운영 종료·재개 · OWNER* · 200 */
    @PutMapping("/api/owner/themes/{themeId}/status")
    @Auth({Role.OWNER, Role.ADMIN})
    public ThemeStatusResponse changeStatus(LoginMember login, @PathVariable Long themeId, @Valid @RequestBody ThemeStatusRequest req) {
        return ownerThemeService.changeStatus(login, themeId, req);
    }

    /** C-10 주중/주말 시간표 보기 · OWNER* · 200 */
    @GetMapping("/api/owner/themes/{themeId}/slots")
    @Auth({Role.OWNER, Role.ADMIN})
    public WeekSlotsResponse getSlots(LoginMember login, @PathVariable Long themeId) {
        return ownerThemeService.getSlots(login, themeId);
    }

    /** C-11 주중/주말 시간표 저장 (통째 교체) · OWNER* · 200 */
    @PutMapping("/api/owner/themes/{themeId}/slots")
    @Auth({Role.OWNER, Role.ADMIN})
    public WeekSlotsResponse saveSlots(LoginMember login, @PathVariable Long themeId, @Valid @RequestBody WeekSlotsRequest req) {
        return ownerThemeService.saveSlots(login, themeId, req);
    }
}

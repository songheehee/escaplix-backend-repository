package com.escaplix.backend.service;

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
import org.springframework.web.multipart.MultipartFile;
import com.escaplix.backend.common.file.FileStorage;
import com.escaplix.backend.common.time.DayTypeResolver;
import com.escaplix.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * OwnerThemeService 구현 (C 담당). 지금은 전부 501 "아직 구현 전". TODO 를 채우고 throw 줄을 지운다.
 * 조회는 클래스의 readOnly 트랜잭션, 저장·수정·삭제 메서드에는 @Transactional 이 붙어 있다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OwnerThemeServiceImp implements OwnerThemeService {

    private final ThemeRepository themeRepository;
    private final ThemeGenreRepository themeGenreRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final StoreRepository storeRepository;
    private final GenreRepository genreRepository;
    private final FileStorage fileStorage;

    @Override
    public OwnerThemeDetailResponse getTheme(LoginMember login, Long themeId) {
        // TODO(C) 사장이면 theme.store.owner 가 나인지 확인 아니면 403
        // TODO(C) 상태와 상관없이
        throw ApiException.notImplemented("C-6 테마 수정용 상세");
    }

    @Override
    @Transactional
    public ThemeSaveResponse createTheme(LoginMember login, ThemeForm form, MultipartFile poster) {
        // TODO(C) 매장이 내 매장인지(403), APPROVED 인지(409 "승인된 매장에만 테마를 등록할 수 있습니다.")
        // TODO(C) minPeople > maxPeople 이면 400
        // TODO(C) poster 있으면 fileStorage.savePoster
        // TODO(C) THEME + THEME_GENRE 한 트랜잭션. 사장 PENDING·OWNER / 관리자 OPEN·ADMIN
        throw ApiException.notImplemented("C-7 테마 등록");
    }

    @Override
    @Transactional
    public ThemeSaveResponse updateTheme(LoginMember login, Long themeId, ThemeForm form, MultipartFile poster) {
        // TODO(C) 사장이면 theme.store.owner 가 나인지 확인 아니면 403
        // TODO(C) storeId 는 바꾸지 않음, poster 비우면 기존 유지
        // TODO(C) THEME_GENRE 지우고 다시 넣기
        // TODO(C) 사장이면 PENDING + rejectReason 비움, 관리자면 OPEN 유지
        throw ApiException.notImplemented("C-8 테마 수정");
    }

    @Override
    @Transactional
    public ThemeStatusResponse changeStatus(LoginMember login, Long themeId, ThemeStatusRequest req) {
        // TODO(C) 사장이면 theme.store.owner 가 나인지 확인 아니면 403
        // TODO(C) OPEN ↔ CLOSED 만. 그 밖이면 409
        throw ApiException.notImplemented("C-9 운영 종료·재개");
    }

    @Override
    public WeekSlotsResponse getSlots(LoginMember login, Long themeId) {
        // TODO(C) 사장이면 theme.store.owner 가 나인지 확인 아니면 403
        throw ApiException.notImplemented("C-10 주중/주말 시간표 보기");
    }

    @Override
    @Transactional
    public WeekSlotsResponse saveSlots(LoginMember login, Long themeId, WeekSlotsRequest req) {
        // TODO(C) 사장이면 theme.store.owner 가 나인지 확인 아니면 403
        // TODO(C) "HH:mm" 형식, 같은 유형 안 중복, 앞뒤 회차 간격 < playMinutes, 유형당 20개 초과 → 400 (메시지는 API.md C-11)
        // TODO(C) TIME_SLOT 지우고 다시 넣기 (한 트랜잭션)
        throw ApiException.notImplemented("C-11 주중/주말 시간표 저장 (통째 교체)");
    }
}

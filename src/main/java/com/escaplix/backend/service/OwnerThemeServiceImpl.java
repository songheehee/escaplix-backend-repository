package com.escaplix.backend.service;

import com.escaplix.backend.common.*;
import com.escaplix.backend.common.auth.*;
import com.escaplix.backend.dto.owner.*;
import com.escaplix.backend.dto.theme.SlotItem;
import com.escaplix.backend.entity.*;
import org.springframework.web.multipart.MultipartFile;
import com.escaplix.backend.common.file.FileStorage;
import com.escaplix.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static com.escaplix.backend.service.OwnerServiceSupport.hasFile;

/**
 * OwnerThemeService 구현 (C 담당).
 * 조회는 클래스의 readOnly 트랜잭션, 저장·수정·삭제 메서드에는 @Transactional 이 붙어 있다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OwnerThemeServiceImpl implements OwnerThemeService {

    /** 한 유형(주중/주말)에 넣을 수 있는 회차 수 */
    private static final int MAX_SLOTS_PER_DAY_TYPE = 20;
    private static final Pattern HH_MM = Pattern.compile("^([01]\\d|2[0-3]):[0-5]\\d$");
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    private final ThemeRepository themeRepository;
    private final ThemeGenreRepository themeGenreRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final StoreRepository storeRepository;
    private final GenreRepository genreRepository;
    private final FileStorage fileStorage;
    private final OwnerServiceSupport support;

    @Override
    public OwnerThemeDetailResponse getTheme(LoginMember login, Long themeId) {
        Theme theme = findTheme(themeId);
        support.checkThemeAccess(login, theme);
        // 상태와 상관없이 (승인 전이어도) 볼 수 있다
        return support.toThemeDetail(theme);
    }

    @Override
    @Transactional
    public ThemeSaveResponse createTheme(LoginMember login, ThemeForm form, MultipartFile poster) {
        Store store = storeRepository.findById(form.storeId())
                .orElseThrow(() -> ApiException.notFound("매장을 찾을 수 없습니다."));
        support.checkStoreAccess(login, store);
        if (store.getStatus() != StoreStatus.APPROVED) {
            throw ApiException.conflict("승인된 매장에만 테마를 등록할 수 있습니다.");
        }
        validatePeople(form);
        List<Genre> genres = loadGenres(form.genreIds());

        boolean admin = login.is(Role.ADMIN);
        Theme theme = new Theme();
        theme.setStore(store);
        applyForm(theme, form);
        // 사장 PENDING·OWNER / 관리자 OPEN·ADMIN (결정 S3)
        theme.setSource(admin ? ThemeSource.ADMIN : ThemeSource.OWNER);
        theme.setStatus(admin ? ThemeStatus.OPEN : ThemeStatus.PENDING);

        String posterUrl = hasFile(poster) ? fileStorage.savePoster(poster) : null;
        theme.setPosterUrl(posterUrl);
        try {
            // THEME + THEME_GENRE 한 트랜잭션
            themeRepository.saveAndFlush(theme);
            saveGenres(theme, genres);
        } catch (RuntimeException e) {
            fileStorage.deleteQuietly(posterUrl);
            throw e;
        }
        return new ThemeSaveResponse(theme.getThemeId(), theme.getStatus());
    }

    @Override
    @Transactional
    public ThemeSaveResponse updateTheme(LoginMember login, Long themeId, ThemeForm form, MultipartFile poster) {
        Theme theme = findTheme(themeId);
        support.checkThemeAccess(login, theme);
        support.checkNotClosed(theme.getStore());
        validatePeople(form);
        List<Genre> genres = loadGenres(form.genreIds());

        // storeId 는 바꾸지 않는다 (form.storeId 무시)
        applyForm(theme, form);
        if (!login.is(Role.ADMIN)) {
            // 사장이 수정하면 다시 승인 대기. 관리자는 지금 상태 그대로 (결정 S3)
            theme.setStatus(ThemeStatus.PENDING);
            theme.setRejectReason(null);
        }

        // poster 를 비우면 기존 사진 그대로
        String newPosterUrl = null;
        if (hasFile(poster)) {
            newPosterUrl = fileStorage.savePoster(poster);
            theme.setPosterUrl(newPosterUrl);
        }
        try {
            // THEME_GENRE 지우고 다시 넣기. 지우기를 먼저 flush 해야 UNIQUE(theme_id, genre_id)에 안 걸린다
            themeGenreRepository.deleteByTheme_ThemeId(themeId);
            themeGenreRepository.flush();
            saveGenres(theme, genres);
            themeRepository.flush();
        } catch (RuntimeException e) {
            fileStorage.deleteQuietly(newPosterUrl);
            throw e;
        }
        return new ThemeSaveResponse(theme.getThemeId(), theme.getStatus());
    }

    @Override
    @Transactional
    public ThemeStatusResponse changeStatus(LoginMember login, Long themeId, ThemeStatusRequest req) {
        Theme theme = findTheme(themeId);
        support.checkThemeAccess(login, theme);
        support.checkNotClosed(theme.getStore());

        ThemeStatus target = req.status();
        if (target != ThemeStatus.OPEN && target != ThemeStatus.CLOSED) {
            throw ApiException.badRequest("운영 재개(OPEN) 또는 운영 종료(CLOSED)만 고를 수 있습니다.");
        }
        ThemeStatus current = theme.getStatus();
        if (current != ThemeStatus.OPEN && current != ThemeStatus.CLOSED) {
            throw ApiException.conflict("승인 전이거나 반려된 테마는 운영 상태를 바꿀 수 없습니다.");
        }
        if (current == target) {
            throw ApiException.conflict(target == ThemeStatus.OPEN ? "이미 운영 중인 테마입니다." : "이미 운영을 종료한 테마입니다.");
        }
        theme.setStatus(target);
        return new ThemeStatusResponse(theme.getThemeId(), theme.getStatus());
    }

    @Override
    public WeekSlotsResponse getSlots(LoginMember login, Long themeId) {
        Theme theme = findTheme(themeId);
        support.checkThemeAccess(login, theme);
        return toWeekSlots(theme);
    }

    @Override
    @Transactional
    public WeekSlotsResponse saveSlots(LoginMember login, Long themeId, WeekSlotsRequest req) {
        Theme theme = findTheme(themeId);
        support.checkThemeAccess(login, theme);
        support.checkNotClosed(theme.getStore());

        int playMinutes = theme.getPlayMinutes();
        List<LocalTime> weekday = validateSlots("주중", req.weekday(), playMinutes);
        List<LocalTime> weekend = validateSlots("주말", req.weekend(), playMinutes);

        // 한 트랜잭션에서 지우고 다시 넣기. 지우기를 먼저 flush 해야 UNIQUE(theme_id, day_type, start_time)에 안 걸린다
        timeSlotRepository.deleteByTheme_ThemeId(themeId);
        timeSlotRepository.flush();

        List<TimeSlot> slots = new ArrayList<>();
        weekday.forEach(t -> slots.add(newSlot(theme, DayType.WEEKDAY, t)));
        weekend.forEach(t -> slots.add(newSlot(theme, DayType.WEEKEND, t)));
        timeSlotRepository.saveAllAndFlush(slots);

        return toWeekSlots(theme);
    }

    // ------------------------------------------------------------------

    private Theme findTheme(Long themeId) {
        return themeRepository.findById(themeId)
                .orElseThrow(() -> ApiException.notFound("테마를 찾을 수 없습니다."));
    }

    private void validatePeople(ThemeForm form) {
        if (form.minPeople() > form.maxPeople()) {
            throw ApiException.badRequest("최소 인원이 최대 인원보다 클 수 없습니다.");
        }
    }

    /** 같은 번호가 여러 번 와도 한 번만. 없는 장르가 섞여 있으면 400 */
    private List<Genre> loadGenres(List<Long> genreIds) {
        List<Long> ids = genreIds.stream().distinct().toList();
        if (ids.contains(null)) {
            throw ApiException.badRequest("장르를 다시 골라 주세요.");
        }
        List<Genre> genres = genreRepository.findAllById(ids);
        if (genres.size() != ids.size()) {
            throw ApiException.badRequest("없는 장르가 들어 있습니다.");
        }
        return genres;
    }

    private void saveGenres(Theme theme, List<Genre> genres) {
        List<ThemeGenre> rows = genres.stream().map(g -> {
            ThemeGenre tg = new ThemeGenre();
            tg.setTheme(theme);
            tg.setGenre(g);
            return tg;
        }).toList();
        themeGenreRepository.saveAllAndFlush(rows);
    }

    private void applyForm(Theme theme, ThemeForm form) {
        theme.setTitle(form.title());
        theme.setDescription(form.description() == null || form.description().isBlank() ? null : form.description());
        theme.setDifficulty(form.difficulty());
        theme.setPlayMinutes(form.playMinutes());
        theme.setMinPeople(form.minPeople());
        theme.setMaxPeople(form.maxPeople());
        theme.setPrice(form.price());
        theme.setBookingUrl(form.bookingUrl());
    }

    /**
     * C-11 검사 (메시지는 API.md C-11)
     * HH:mm 형식 / 유형당 20개까지 / 같은 시각 두 번 금지.
     * 회차가 겹치는 건 허용 (밀어내기 테마처럼 일부러 겹쳐 운영하는 경우가 있음, 11/23)
     */
    private List<LocalTime> validateSlots(String label, List<String> values, int playMinutes) {
        if (values.size() > MAX_SLOTS_PER_DAY_TYPE) {
            throw ApiException.badRequest(label + " 회차는 " + MAX_SLOTS_PER_DAY_TYPE + "개까지 넣을 수 있습니다.");
        }
        List<LocalTime> times = new ArrayList<>();
        for (String v : values) {
            if (v == null || !HH_MM.matcher(v.trim()).matches()) {
                throw ApiException.badRequest("시간은 19:00처럼 입력해 주세요.");
            }
            times.add(LocalTime.parse(v.trim(), TIME_FORMAT));
        }
        times.sort(null);
        for (int i = 1; i < times.size(); i++) {
            LocalTime prev = times.get(i - 1);
            LocalTime cur = times.get(i);
            if (prev.equals(cur)) {
                throw ApiException.badRequest(label + " " + fmt(cur) + "이 두 번 들어 있습니다.");
            }
        }
        return times;
    }

    private TimeSlot newSlot(Theme theme, DayType dayType, LocalTime start) {
        TimeSlot slot = new TimeSlot();
        slot.setTheme(theme);
        slot.setDayType(dayType);
        slot.setStartTime(fmt(start));
        return slot;
    }

    /** C-10 응답 (C-11 응답도 같은 모양). endTime = startTime + playMinutes */
    private WeekSlotsResponse toWeekSlots(Theme theme) {
        int playMinutes = theme.getPlayMinutes();
        List<TimeSlot> all = timeSlotRepository.findByTheme_ThemeIdOrderByStartTimeAsc(theme.getThemeId());
        return new WeekSlotsResponse(
                theme.getThemeId(),
                playMinutes,
                toSlotItems(all, DayType.WEEKDAY, playMinutes),
                toSlotItems(all, DayType.WEEKEND, playMinutes));
    }

    private List<SlotItem> toSlotItems(List<TimeSlot> all, DayType dayType, int playMinutes) {
        return all.stream()
                .filter(s -> s.getDayType() == dayType)
                .map(s -> new SlotItem(s.getSlotId(), s.getStartTime(),
                        fmt(LocalTime.parse(s.getStartTime(), TIME_FORMAT).plusMinutes(playMinutes))))
                .toList();
    }

    private static String fmt(LocalTime t) {
        return t.format(TIME_FORMAT);
    }
}

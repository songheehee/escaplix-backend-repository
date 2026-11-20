package com.escaplix.backend.service;

import com.escaplix.backend.common.*;
import com.escaplix.backend.dto.store.*;
import com.escaplix.backend.entity.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import com.escaplix.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * StoreService 구현 (B 담당). 명세: docs/API.md B-8 ~ B-11
 * 손님에게 보이는 매장은 APPROVED 만. 매장의 장르·테마 수·평점·파티 수는 그 매장의 OPEN 테마로 계산한다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StoreServiceImpl implements StoreService {

    private final ThemeBrowseRepository browseRepository;
    private final AreaRepository areaRepository;

    @Override
    public List<String> getAreas() {
        // 승인된 매장이 있는 세부 지역만. 순서는 AREA 표 순서 (서울 → 경기 → ..., 그 안에서 sort_order)
        return areaRepository.findNamesWithApprovedStores();
    }

    @Override
    public List<RegionResponse> getRegions() {
        // 큰 지역별로 묶기. 순서를 지키려고 LinkedHashMap
        Map<Long, RegionResponse> byRegion = new LinkedHashMap<>();
        for (Area a : areaRepository.findAllOrdered()) {
            Region r = a.getRegion();
            byRegion.computeIfAbsent(r.getRegionId(), id -> new RegionResponse(id, r.getName(), new ArrayList<>()))
                    .areas().add(new RegionResponse.AreaItem(a.getAreaId(), a.getName()));
        }
        return new ArrayList<>(byRegion.values());
    }

    @Override
    public List<StoreListItem> findStores(StoreSearchCondition cond) {
        String area = cond.area() == null || cond.area().isBlank() ? null : cond.area().trim();
        List<Store> stores = browseRepository.findApprovedStores(area, ThemeBrowseRepository.likePattern(cond.keyword()));

        Map<Long, List<Theme>> themesByStore = openThemesByStore(stores);
        List<Long> themeIds = themesByStore.values().stream().flatMap(List::stream).map(Theme::getThemeId).toList();
        Map<Long, List<Genre>> genres = browseRepository.findGenresByThemeIds(themeIds);
        Map<Long, Integer> parties = browseRepository.countRecruitingParties(themeIds, LocalDateTime.now());

        return stores.stream().map(s -> {
            List<Theme> themes = themesByStore.getOrDefault(s.getStoreId(), List.of());
            // 매장 장르 = 테마 장르를 중복 없이, 장르 번호 순
            List<String> storeGenres = themes.stream()
                    .flatMap(t -> genres.getOrDefault(t.getThemeId(), List.of()).stream())
                    .collect(Collectors.toMap(Genre::getGenreId, Genre::getName, (a, b) -> a, TreeMap::new))
                    .values().stream().toList();
            int partyCount = themes.stream().mapToInt(t -> parties.getOrDefault(t.getThemeId(), 0)).sum();
            return new StoreListItem(s.getStoreId(), s.getName(), s.getArea(), s.getAddress(), s.getHours(),
                    s.getPhotoUrl(), storeGenres, themes.size(), storeRating(themes), partyCount);
        }).toList();
    }

    @Override
    public StoreDetailResponse getStore(Long storeId) {
        // 승인 전(PENDING)·반려 매장은 404. 사장이 수정해서 다시 PENDING 이 된 동안도 404 (결정 S1)
        Store s = browseRepository.findApprovedStore(storeId)
                .orElseThrow(() -> ApiException.notFound("승인되지 않았거나 없는 매장입니다."));

        List<Theme> themes = new ArrayList<>(browseRepository.findOpenThemesOfStores(List.of(storeId)));
        themes.sort(ThemeServiceImpl.BY_RATING);
        List<Long> themeIds = themes.stream().map(Theme::getThemeId).toList();
        Map<Long, List<Genre>> genres = browseRepository.findGenresByThemeIds(themeIds);
        Map<Long, Integer> parties = browseRepository.countRecruitingParties(themeIds, LocalDateTime.now());

        List<StoreDetailResponse.ThemeItem> items = themes.stream().map(t -> new StoreDetailResponse.ThemeItem(
                t.getThemeId(), t.getTitle(), ThemeServiceImpl.genreNames(genres.get(t.getThemeId())),
                t.getDifficulty(), t.getPlayMinutes(), t.getMinPeople(), t.getMaxPeople(), t.getPrice(),
                t.getPosterUrl(), t.getAvgRating(), ThemeServiceImpl.nz(t.getReviewCount()),
                parties.getOrDefault(t.getThemeId(), 0)
        )).toList();

        return new StoreDetailResponse(s.getStoreId(), s.getName(), s.getArea(), s.getAddress(), s.getPhone(),
                s.getWebsiteUrl(), s.getHours(), s.getIntro(), s.getPhotoUrl(), s.getLat(), s.getLng(), storeRating(themes), items);
    }

    @Override
    public List<StoreMapItem> getMapStores() {
        List<Store> stores = browseRepository.findApprovedStoresWithLocation();
        Map<Long, List<Theme>> themesByStore = openThemesByStore(stores);
        return stores.stream()
                .map(s -> new StoreMapItem(s.getStoreId(), s.getName(), s.getArea(), s.getLat(), s.getLng(),
                        themesByStore.getOrDefault(s.getStoreId(), List.of()).size()))
                .toList();
    }

    // ---------------------------------------------------------------- 도우미

    /** 매장들의 OPEN 테마를 한 번에 가져와서 매장별로 나눔 */
    private Map<Long, List<Theme>> openThemesByStore(List<Store> stores) {
        List<Long> storeIds = stores.stream().map(Store::getStoreId).toList();
        return browseRepository.findOpenThemesOfStores(storeIds).stream()
                .collect(Collectors.groupingBy(t -> t.getStore().getStoreId()));
    }

    /** 매장 평점 = 후기 있는 테마들의 평균 별점의 평균 (소수 첫째 자리). 없으면 null (명세 B-9) */
    private static Double storeRating(List<Theme> themes) {
        OptionalDouble avg = themes.stream()
                .map(Theme::getAvgRating)
                .filter(Objects::nonNull)
                .mapToDouble(Double::doubleValue)
                .average();
        return avg.isPresent() ? Math.round(avg.getAsDouble() * 10) / 10.0 : null;
    }
}

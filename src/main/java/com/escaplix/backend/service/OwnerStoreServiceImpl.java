package com.escaplix.backend.service;

import com.escaplix.backend.common.*;
import com.escaplix.backend.common.auth.*;
import com.escaplix.backend.dto.owner.*;
import com.escaplix.backend.entity.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import com.escaplix.backend.common.file.FileStorage;
import com.escaplix.backend.common.geo.KakaoGeocoder;
import com.escaplix.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.escaplix.backend.service.OwnerServiceSupport.hasFile;
import static com.escaplix.backend.service.OwnerServiceSupport.orZero;

/**
 * OwnerStoreService 구현 (C 담당).
 * 조회는 클래스의 readOnly 트랜잭션, 저장·수정·삭제 메서드에는 @Transactional 이 붙어 있다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OwnerStoreServiceImpl implements OwnerStoreService {

    private static final String BIZ_PLACE_FOLDER = "biz-place";

    private final StoreRepository storeRepository;
    private final ThemeRepository themeRepository;
    private final MemberRepository memberRepository;
    private final AreaRepository areaRepository;
    private final FileStorage fileStorage;
    private final OwnerServiceSupport support;
    private final KakaoGeocoder geocoder;

    @Override
    public List<OwnerStoreItem> getMyStores(LoginMember login) {
        // 사장: owner = 나, 관리자: 승인된 매장 + 폐업 매장 (매장 관리. 대기·반려는 승인 요청 화면에서)
        List<Store> stores = login.is(Role.ADMIN)
                ? storeRepository.findByStatusInOrderByCreatedAtDesc(List.of(StoreStatus.APPROVED, StoreStatus.CLOSED))
                : storeRepository.findByOwner_MemberIdOrderByCreatedAtDesc(login.memberId());
        return stores.stream().map(this::toItem).toList();
    }

    /** 새 번호는 11자리만. 예전 10자리 번호(더미 데이터)는 바꾸지 않고 다시 저장할 때만 통과 */
    private void checkBizPlaceNoForm(String bizPlaceNo, String current) {
        if (bizPlaceNo.equals(current)) return;
        if (!bizPlaceNo.matches("^\\d{3}-\\d{2}-\\d{5}-\\d$")) {
            throw ApiException.badRequest("사업장 관리번호는 숫자 11자리입니다. (000-00-00000-0)");
        }
    }

    @Override
    public AvailableResponse checkBizPlaceNo(String bizPlaceNo) {
        return new AvailableResponse(!storeRepository.existsByBizPlaceNoAndStatusNot(bizPlaceNo.trim(), StoreStatus.CLOSED));
    }

    @Override
    @Transactional
    public StoreSaveResponse createStore(LoginMember login, StoreForm form, MultipartFile bizPlaceFile, MultipartFile storePhoto) {
        validateArea(form);
        checkBizPlaceNoForm(form.bizPlaceNo(), null);
        if (storeRepository.existsByBizPlaceNoAndStatusNot(form.bizPlaceNo(), StoreStatus.CLOSED)) {
            throw ApiException.conflict("이미 등록된 사업장 관리번호입니다.");
        }

        boolean admin = login.is(Role.ADMIN);
        if (!admin && !hasFile(bizPlaceFile)) {
            throw ApiException.badRequest("사업장등록증 이미지를 첨부해 주세요.");
        }

        Store store = new Store();
        applyForm(store, form);
        fillLatLng(store);
        if (admin) {
            // 관리자 직접 등록: 바로 공개 (결정 S3)
            store.setOwner(null);
            store.setStatus(StoreStatus.APPROVED);
            store.setApprovedAt(LocalDateTime.now());   // NEW 기준
        } else {
            store.setOwner(memberRepository.getReferenceById(login.memberId()));
            store.setStatus(StoreStatus.PENDING);
        }

        // 검사가 다 끝난 뒤에 파일을 저장한다. 사진 저장이나 DB 저장이 실패하면 이미 저장한 파일을 지운다
        String savedPath = null;
        String photoUrl = null;
        try {
            savedPath = hasFile(bizPlaceFile) ? fileStorage.savePrivate(bizPlaceFile, BIZ_PLACE_FOLDER) : null;
            photoUrl = hasFile(storePhoto) ? fileStorage.saveStorePhoto(storePhoto) : null;   // 매장 사진은 선택
            store.setBizPlaceFilePath(savedPath);
            store.setPhotoUrl(photoUrl);
            storeRepository.saveAndFlush(store);
        } catch (RuntimeException e) {
            fileStorage.deleteQuietly(savedPath);
            fileStorage.deleteQuietly(photoUrl);
            throw e;
        }
        return new StoreSaveResponse(store.getStoreId(), store.getStatus());
    }

    @Override
    @Transactional
    public StoreSaveResponse updateStore(LoginMember login, Long storeId, StoreForm form, MultipartFile bizPlaceFile, MultipartFile storePhoto) {
        Store store = findStore(storeId);
        support.checkStoreAccess(login, store);
        support.checkNotClosed(store);
        validateArea(form);
        checkBizPlaceNoForm(form.bizPlaceNo(), store.getBizPlaceNo());
        if (storeRepository.existsByBizPlaceNoAndStatusNotAndStoreIdNot(form.bizPlaceNo(), StoreStatus.CLOSED, storeId)) {
            throw ApiException.conflict("이미 등록된 사업장 관리번호입니다.");
        }

        String oldAddress = store.getAddress();
        applyForm(store, form);
        if (!form.address().equals(oldAddress) || store.getLat() == null) {
            fillLatLng(store);   // 주소가 바뀌었거나 좌표가 없을 때만 다시 찾음
        }
        if (!login.is(Role.ADMIN)) {
            // 사장이 수정하면 어떤 칸이든 다시 승인 대기 (결정 S1).
            // 관리자는 사장 매장이든 직접 등록 매장이든 지금 상태 그대로 (결정 S3, 매장 관리 개편으로 사장 매장까지)
            store.setStatus(StoreStatus.PENDING);
            store.setRejectReason(null);
        }

        // 파일을 비우면 기존 파일 그대로. 새 파일이면 바꾼다 (등록증·사진 둘 다. 옛 파일은 남겨 둠)
        String newPath = null;
        String newPhotoUrl = null;
        try {
            if (hasFile(bizPlaceFile)) {
                newPath = fileStorage.savePrivate(bizPlaceFile, BIZ_PLACE_FOLDER);
                store.setBizPlaceFilePath(newPath);
            }
            if (hasFile(storePhoto)) {
                newPhotoUrl = fileStorage.saveStorePhoto(storePhoto);
                store.setPhotoUrl(newPhotoUrl);
            }
            storeRepository.flush();
        } catch (RuntimeException e) {
            fileStorage.deleteQuietly(newPath);
            fileStorage.deleteQuietly(newPhotoUrl);
            throw e;
        }
        return new StoreSaveResponse(store.getStoreId(), store.getStatus());
    }

    @Override
    public List<OwnerThemeItem> getStoreThemes(LoginMember login, Long storeId) {
        Store store = findStore(storeId);
        support.checkStoreAccess(login, store);
        // 모든 상태의 테마 (PENDING, OPEN, REJECTED, CLOSED)
        return themeRepository.findByStore_StoreIdOrderByCreatedAtDesc(storeId).stream()
                .map(t -> new OwnerThemeItem(
                        t.getThemeId(),
                        t.getTitle(),
                        support.genreNames(t.getThemeId()),
                        t.getPlayMinutes(),
                        t.getStatus(),
                        t.getStatus() == ThemeStatus.REJECTED ? t.getRejectReason() : null,
                        t.getAvgRating(),
                        orZero(t.getReviewCount()),
                        t.getUpdatedAt()))
                .toList();
    }

    @Override
    @Transactional
    public StoreSaveResponse closeStore(LoginMember login, Long storeId) {
        Store store = findStore(storeId);
        support.checkStoreAccess(login, store);
        support.checkNotClosed(store);
        if (store.getStatus() != StoreStatus.APPROVED) {
            throw ApiException.conflict("승인된 매장만 폐업할 수 있습니다.");
        }
        store.setStatus(StoreStatus.CLOSED);
        // 그 매장 테마는 전부 운영 종료 (승인 대기·반려 테마도. 매장이 없으니 다시 열 일이 없음)
        themeRepository.findByStore_StoreIdOrderByCreatedAtDesc(storeId)
                .forEach(t -> t.setStatus(ThemeStatus.CLOSED));
        return new StoreSaveResponse(store.getStoreId(), store.getStatus());
    }

    // ------------------------------------------------------------------

    private Store findStore(Long storeId) {
        return storeRepository.findById(storeId)
                .orElseThrow(() -> ApiException.notFound("매장을 찾을 수 없습니다."));
    }

    private OwnerStoreItem toItem(Store s) {
        return new OwnerStoreItem(
                s.getStoreId(),
                s.getName(),
                s.getArea(),
                s.getAddress(),
                s.getPhone(),
                s.getWebsiteUrl(),
                s.getHours(),
                s.getIntro(),
                s.getPhotoUrl(),
                s.getLat(),
                s.getLng(),
                s.getBizPlaceNo(),
                s.getStatus(),
                s.getStatus() == StoreStatus.REJECTED ? s.getRejectReason() : null,   // 결정 S4
                themeRepository.countByStore_StoreId(s.getStoreId()),
                s.getCreatedAt(),
                s.getApprovedAt(),
                s.getOwner() == null ? null : s.getOwner().getLoginId(),
                s.getOwner() == null ? null : s.getOwner().getNickname());
    }

    /** 지역은 AREA 표에 있는 세부 지역만 (결정 L1). 화면은 select 로 고르지만 서버도 다시 확인 */
    private void validateArea(StoreForm form) {
        if (!areaRepository.existsByName(form.area())) {
            throw ApiException.badRequest("지역은 목록에서 골라 주세요.");
        }
    }

    /** 주소 → 좌표 (카카오). 못 찾으면 둘 다 null → 매장 상세 지도만 안 보임 */
    private void fillLatLng(Store store) {
        KakaoGeocoder.LatLng p = geocoder.find(store.getAddress()).orElse(null);
        store.setLat(p == null ? null : p.lat());
        store.setLng(p == null ? null : p.lng());
    }

    private void applyForm(Store store, StoreForm form) {
        store.setName(form.name());
        store.setArea(form.area());
        store.setAddress(form.address());
        store.setPhone(form.phone());
        store.setWebsiteUrl(form.websiteUrl());
        store.setHours(form.hours());
        store.setIntro(form.intro() == null || form.intro().isBlank() ? null : form.intro().strip());   // 소개는 선택
        store.setBizPlaceNo(form.bizPlaceNo());
    }
}

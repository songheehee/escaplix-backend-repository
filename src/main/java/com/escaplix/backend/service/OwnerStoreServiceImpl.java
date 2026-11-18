package com.escaplix.backend.service;

import com.escaplix.backend.common.*;
import com.escaplix.backend.common.auth.*;
import com.escaplix.backend.dto.owner.*;
import com.escaplix.backend.entity.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;
import com.escaplix.backend.common.file.FileStorage;
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
    private final FileStorage fileStorage;
    private final OwnerServiceSupport support;

    @Override
    public List<OwnerStoreItem> getMyStores(LoginMember login) {
        // 사장: owner = 나, 관리자: owner IS NULL (직접 등록한 매장)
        List<Store> stores = login.is(Role.ADMIN)
                ? storeRepository.findByOwnerIsNullOrderByCreatedAtDesc()
                : storeRepository.findByOwner_MemberIdOrderByCreatedAtDesc(login.memberId());
        return stores.stream().map(this::toItem).toList();
    }

    @Override
    public AvailableResponse checkBizPlaceNo(String bizPlaceNo) {
        return new AvailableResponse(!storeRepository.existsByBizPlaceNo(bizPlaceNo.trim()));
    }

    @Override
    @Transactional
    public StoreSaveResponse createStore(LoginMember login, StoreForm form, MultipartFile bizPlaceFile, MultipartFile storePhoto) {
        validateLatLng(form);
        if (storeRepository.existsByBizPlaceNo(form.bizPlaceNo())) {
            throw ApiException.conflict("이미 등록된 사업장 등록번호입니다.");
        }

        boolean admin = login.is(Role.ADMIN);
        if (!admin && !hasFile(bizPlaceFile)) {
            throw ApiException.badRequest("사업장등록증 이미지를 첨부해 주세요.");
        }

        Store store = new Store();
        applyForm(store, form);
        if (admin) {
            // 관리자 직접 등록: 바로 공개 (결정 S3)
            store.setOwner(null);
            store.setStatus(StoreStatus.APPROVED);
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
        validateLatLng(form);
        if (storeRepository.existsByBizPlaceNoAndStoreIdNot(form.bizPlaceNo(), storeId)) {
            throw ApiException.conflict("이미 등록된 사업장 등록번호입니다.");
        }

        applyForm(store, form);
        if (!login.is(Role.ADMIN)) {
            // 사장이 수정하면 어떤 칸이든 다시 승인 대기 (결정 S1). 관리자는 APPROVED 유지 (결정 S3)
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
                s.getCreatedAt());
    }

    /** lat·lng 는 둘 다 있거나 둘 다 없음 */
    private void validateLatLng(StoreForm form) {
        BigDecimal lat = form.lat();
        BigDecimal lng = form.lng();
        if ((lat == null) != (lng == null)) {
            throw ApiException.badRequest("위도와 경도는 둘 다 입력하거나 둘 다 비워 주세요.");
        }
        if (lat != null && (lat.abs().compareTo(BigDecimal.valueOf(90)) > 0
                || lng.abs().compareTo(BigDecimal.valueOf(180)) > 0)) {
            throw ApiException.badRequest("위도·경도 값이 올바르지 않습니다.");
        }
    }

    private void applyForm(Store store, StoreForm form) {
        store.setName(form.name());
        store.setArea(form.area());
        store.setAddress(form.address());
        store.setPhone(form.phone());
        store.setWebsiteUrl(form.websiteUrl());
        store.setHours(form.hours());
        store.setIntro(form.intro());
        store.setLat(form.lat());
        store.setLng(form.lng());
        store.setBizPlaceNo(form.bizPlaceNo());
    }
}

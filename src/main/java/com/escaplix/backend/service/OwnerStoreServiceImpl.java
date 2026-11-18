package com.escaplix.backend.service;

import com.escaplix.backend.common.*;
import com.escaplix.backend.common.auth.*;
import com.escaplix.backend.dto.owner.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import com.escaplix.backend.common.file.FileStorage;
import com.escaplix.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * OwnerStoreService 구현 (C 담당). 지금은 전부 501 "아직 구현 전". TODO 를 채우고 throw 줄을 지운다.
 * 조회는 클래스의 readOnly 트랜잭션, 저장·수정·삭제 메서드에는 @Transactional 이 붙어 있다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OwnerStoreServiceImpl implements OwnerStoreService {

    private final StoreRepository storeRepository;
    private final ThemeRepository themeRepository;
    private final MemberRepository memberRepository;
    private final FileStorage fileStorage;

    @Override
    public List<OwnerStoreItem> getMyStores(LoginMember login) {
        // TODO(C) 사장: owner = 나, 관리자: owner IS NULL (직접 등록한 매장)
        // TODO(C) rejectReason 은 REJECTED 일 때만
        throw ApiException.notImplemented("C-1 내 매장 목록");
    }

    @Override
    public AvailableResponse checkBizPlaceNo(String bizPlaceNo) {
        throw ApiException.notImplemented("C-2 사업장 등록번호 중복 확인");
    }

    @Override
    @Transactional
    public StoreSaveResponse createStore(LoginMember login, StoreForm form, MultipartFile bizPlaceFile) {
        // TODO(C) 사업장 번호 중복 409, lat·lng 한쪽만 있으면 400
        // TODO(C) 사장: bizPlaceFile 필수 → savePrivate(.., "biz-place"), owner=나, PENDING
        // TODO(C) 관리자: 파일 생략 가능, owner=null, APPROVED (결정 S3)
        throw ApiException.notImplemented("C-3 매장 등록");
    }

    @Override
    @Transactional
    public StoreSaveResponse updateStore(LoginMember login, Long storeId, StoreForm form, MultipartFile bizPlaceFile) {
        // TODO(C) 사장이면 store.owner.memberId == login.memberId() 아니면 403 (관리자는 owner 가 null 인 매장만)
        // TODO(C) 파일 비우면 기존 파일 유지
        // TODO(C) 사장이 수정하면 무조건 PENDING + rejectReason 비움 (결정 S1). 관리자는 APPROVED 유지
        throw ApiException.notImplemented("C-4 매장 수정");
    }

    @Override
    public List<OwnerThemeItem> getStoreThemes(LoginMember login, Long storeId) {
        // TODO(C) 사장이면 store.owner.memberId == login.memberId() 아니면 403 (관리자는 owner 가 null 인 매장만)
        // TODO(C) 모든 상태의 테마
        throw ApiException.notImplemented("C-5 매장의 테마 목록");
    }
}

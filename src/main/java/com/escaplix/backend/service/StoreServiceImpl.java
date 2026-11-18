package com.escaplix.backend.service;

import com.escaplix.backend.common.*;
import com.escaplix.backend.dto.store.*;

import java.util.List;

import com.escaplix.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * StoreService 구현 (B 담당). 지금은 전부 501 "아직 구현 전". TODO 를 채우고 throw 줄을 지운다.
 * 조회는 클래스의 readOnly 트랜잭션, 저장·수정·삭제 메서드에는 @Transactional 이 붙어 있다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StoreServiceImpl implements StoreService {

    private final StoreRepository storeRepository;
    private final ThemeRepository themeRepository;

    @Override
    public List<String> getAreas() {
        // TODO(B) APPROVED 매장의 area 를 중복 없이 가나다순
        throw ApiException.notImplemented("B-8 지역 목록");
    }

    @Override
    public List<StoreListItem> findStores(StoreSearchCondition cond) {
        // TODO(B) APPROVED 매장만. keyword 는 매장 이름 일부(띄어쓰기 무시)
        // TODO(B) genres·themeCount·avgRating·recruitingPartyCount 는 그 매장 OPEN 테마로
        throw ApiException.notImplemented("B-9 매장 목록");
    }

    @Override
    public StoreDetailResponse getStore(Long storeId) {
        // TODO(B) APPROVED 아니면 404
        // TODO(B) themes 는 OPEN 테마만
        throw ApiException.notImplemented("B-10 매장 상세");
    }

    @Override
    public List<StoreMapItem> getMapStores() {
        // TODO(B) APPROVED 이면서 lat·lng 둘 다 있는 매장
        throw ApiException.notImplemented("B-11 지도용 매장 (선택 기능)");
    }
}

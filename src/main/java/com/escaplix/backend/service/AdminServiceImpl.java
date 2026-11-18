package com.escaplix.backend.service;

import com.escaplix.backend.common.*;
import com.escaplix.backend.common.file.FileDownload;
import com.escaplix.backend.dto.admin.*;
import com.escaplix.backend.entity.*;
import java.util.List;
import com.escaplix.backend.common.file.FileStorage;
import com.escaplix.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * AdminService 구현 (C 담당). 지금은 전부 501 "아직 구현 전". TODO 를 채우고 throw 줄을 지운다.
 * 조회는 클래스의 readOnly 트랜잭션, 저장·수정·삭제 메서드에는 @Transactional 이 붙어 있다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminServiceImpl implements AdminService {

    private final StoreRepository storeRepository;
    private final ThemeRepository themeRepository;
    private final ThemeGenreRepository themeGenreRepository;
    private final ReportRepository reportRepository;
    private final MemberRepository memberRepository;
    private final FileStorage fileStorage;

    @Override
    public AdminSummaryResponse getSummary() {
        // TODO(C) 대기 매장·테마 수와 가장 오래된 신청일, 승인 매장·공개 테마 수, WAIT 신고 수, 역할별 회원 수
        throw ApiException.notImplemented("C-12 관리자 홈 숫자 요약");
    }

    @Override
    public List<AdminStoreItem> getStores(StoreStatus status) {
        // TODO(C) 오래된 순. owner 는 MEMBER JOIN (관리자 등록 매장이면 null, bizNo 는 목록에선 null)
        throw ApiException.notImplemented("C-13 매장 승인 목록");
    }

    @Override
    public AdminStoreDetailResponse getStore(Long storeId) {
        // TODO(C) owner 에 bizNo 까지 넣기, hasBizPlaceFile
        throw ApiException.notImplemented("C-14 매장 승인 상세");
    }

    @Override
    public FileDownload getBizPlaceFile(Long storeId) {
        // TODO(C) 파일 없으면 404 (관리자 직접 등록 매장)
        throw ApiException.notImplemented("C-15 사업장등록증 이미지");
    }

    @Override
    @Transactional
    public StoreApprovalResponse approveStore(Long storeId, ApprovalRequest req) {
        // TODO(C) PENDING 아니면 409 "이미 처리된 매장입니다."
        // TODO(C) 반려인데 reason 비면 400 → REJECTED + rejectReason
        // TODO(C) 승인 → APPROVED + rejectReason 비움
        throw ApiException.notImplemented("C-16 매장 승인·반려");
    }

    @Override
    public List<AdminThemeItem> getThemes(ThemeStatus status) {
        // TODO(C) 오래된 순
        throw ApiException.notImplemented("C-17 테마 승인 목록");
    }

    @Override
    public AdminThemeDetailResponse getTheme(Long themeId) {
        throw ApiException.notImplemented("C-18 테마 승인 상세");
    }

    @Override
    @Transactional
    public ThemeApprovalResponse approveTheme(Long themeId, ApprovalRequest req) {
        // TODO(C) PENDING 아니면 409, 매장이 APPROVED 아니면 409 "매장이 승인되지 않았습니다."
        // TODO(C) 반려 reason 필수(400) → REJECTED / 승인 → OPEN + rejectReason 비움
        throw ApiException.notImplemented("C-19 테마 승인·반려");
    }
}

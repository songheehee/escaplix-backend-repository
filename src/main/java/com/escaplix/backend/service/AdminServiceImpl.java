package com.escaplix.backend.service;

import com.escaplix.backend.common.*;
import com.escaplix.backend.common.file.FileDownload;
import com.escaplix.backend.dto.admin.*;
import com.escaplix.backend.entity.*;

import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import com.escaplix.backend.common.file.FileStorage;
import com.escaplix.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * AdminService 구현 (C 담당).
 * 조회는 클래스의 readOnly 트랜잭션, 저장·수정·삭제 메서드에는 @Transactional 이 붙어 있다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminServiceImpl implements AdminService {

    private final StoreRepository storeRepository;
    private final ThemeRepository themeRepository;
    private final MemberRepository memberRepository;
    private final FileStorage fileStorage;
    private final OwnerServiceSupport support;

    @Override
    public AdminSummaryResponse getSummary() {
        LocalDateTime oldestStoreAt = storeRepository.findFirstByStatusOrderByCreatedAtAsc(StoreStatus.PENDING)
                .map(Store::getCreatedAt).orElse(null);
        LocalDateTime oldestThemeAt = themeRepository.findFirstByStatusOrderByCreatedAtAsc(ThemeStatus.PENDING)
                .map(Theme::getCreatedAt).orElse(null);

        Map<Role, Long> memberCountByRole = new EnumMap<>(Role.class);
        for (Role role : Role.values()) {
            memberCountByRole.put(role, memberRepository.countByRoleAndWithdrawnAtIsNull(role));   // 탈퇴 회원 빼고
        }

        return new AdminSummaryResponse(
                storeRepository.countByStatus(StoreStatus.PENDING),
                themeRepository.countByStatus(ThemeStatus.PENDING),
                oldestStoreAt,
                oldestThemeAt,
                storeRepository.countByStatus(StoreStatus.APPROVED),
                themeRepository.countByStatus(ThemeStatus.OPEN),
                memberCountByRole);
    }

    @Override
    public List<AdminStoreItem> getStores(StoreStatus status) {
        // 오래된 순. 관리자 등록 매장이면 owner = null, 목록에서는 bizNo 를 넣지 않는다
        return storeRepository.findByStatusOrderByCreatedAtAsc(status).stream()
                .map(s -> new AdminStoreItem(s.getStoreId(), s.getName(), s.getBizPlaceNo(),
                        ownerBrief(s.getOwner(), false), s.getCreatedAt()))
                .toList();
    }

    @Override
    public AdminStoreDetailResponse getStore(Long storeId) {
        Store s = findStore(storeId);
        return new AdminStoreDetailResponse(
                s.getStoreId(),
                s.getName(),
                s.getArea(),
                s.getAddress(),
                s.getPhone(),
                s.getWebsiteUrl(),
                s.getHours(),
                s.getIntro(),
                s.getPhotoUrl(),
                s.getBizPlaceNo(),
                s.getBizPlaceFilePath() != null,
                ownerBrief(s.getOwner(), true),
                s.getStatus(),
                s.getRejectReason(),
                s.getCreatedAt(),
                s.getApprovedAt());
    }

    @Override
    public FileDownload getBizPlaceFile(Long storeId) {
        Store s = findStore(storeId);
        String path = s.getBizPlaceFilePath();
        if (path == null) {
            // 관리자 직접 등록 매장은 파일이 없을 수 있다
            throw ApiException.notFound("사업장 증빙 서류 파일이 없습니다.");
        }
        return new FileDownload(fileStorage.loadPrivate(path), FileStorage.contentType(path));
    }

    @Override
    @Transactional
    public StoreApprovalResponse approveStore(Long storeId, ApprovalRequest req) {
        Store s = findStore(storeId);
        if (s.getStatus() != StoreStatus.PENDING) {
            throw ApiException.conflict("이미 처리된 매장입니다.");
        }
        if (req.approved()) {
            s.setStatus(StoreStatus.APPROVED);
            s.setRejectReason(null);
            if (s.getApprovedAt() == null) s.setApprovedAt(LocalDateTime.now());   // 처음 승인 때만 (NEW 기준)
        } else {
            s.setStatus(StoreStatus.REJECTED);
            s.setRejectReason(requireReason(req));   // 사장 홈(C-1)에 보인다 (결정 S4)
        }
        return new StoreApprovalResponse(s.getStoreId(), s.getStatus(), s.getRejectReason());
    }

    @Override
    public List<AdminThemeItem> getThemes(ThemeStatus status) {
        // 오래된 순
        return themeRepository.findByStatusOrderByCreatedAtAsc(status).stream()
                .map(t -> new AdminThemeItem(
                        t.getThemeId(),
                        t.getTitle(),
                        t.getStore().getStoreId(),
                        t.getStore().getName(),
                        support.genreNames(t.getThemeId()),
                        t.getPosterUrl(),
                        t.getCreatedAt(),
                        t.getUpdatedAt()))
                .toList();
    }

    @Override
    public AdminThemeDetailResponse getTheme(Long themeId) {
        Theme t = findTheme(themeId);
        Store s = t.getStore();
        return new AdminThemeDetailResponse(
                support.toThemeDetail(t),
                new AdminThemeDetailResponse.StoreInfo(s.getStoreId(), s.getName(), s.getBizPlaceNo(),
                        s.getPhone(), s.getWebsiteUrl(), s.getStatus(), s.getArea(), s.getAddress(), s.getHours(),
                        s.getOwner() == null ? null : s.getOwner().getLoginId()));
    }

    @Override
    @Transactional
    public ThemeApprovalResponse approveTheme(Long themeId, ApprovalRequest req) {
        Theme t = findTheme(themeId);
        if (t.getStatus() != ThemeStatus.PENDING) {
            throw ApiException.conflict("이미 처리된 테마입니다.");
        }
        if (req.approved()) {
            if (t.getStore().getStatus() != StoreStatus.APPROVED) {
                throw ApiException.conflict("매장이 승인되지 않았습니다.");
            }
            t.setStatus(ThemeStatus.OPEN);
            t.setRejectReason(null);
        } else {
            t.setStatus(ThemeStatus.REJECTED);
            t.setRejectReason(requireReason(req));   // 사장 홈(C-5)에 보인다 (결정 S4)
        }
        return new ThemeApprovalResponse(t.getThemeId(), t.getStatus(), t.getRejectReason());
    }

    // ------------------------------------------------------------------

    private Store findStore(Long storeId) {
        return storeRepository.findById(storeId)
                .orElseThrow(() -> ApiException.notFound("매장을 찾을 수 없습니다."));
    }

    private Theme findTheme(Long themeId) {
        return themeRepository.findById(themeId)
                .orElseThrow(() -> ApiException.notFound("테마를 찾을 수 없습니다."));
    }

    /** 반려 사유 1~300자 (300자 초과는 DTO 에서 400) */
    private String requireReason(ApprovalRequest req) {
        if (req.reason() == null || req.reason().isBlank()) {
            throw ApiException.badRequest("반려 사유를 입력해 주세요.");
        }
        return req.reason().trim();
    }

    /** 신청한 사장 (MEMBER JOIN). 관리자 직접 등록이면 null. bizNo 는 상세(C-14)에서만 */
    private OwnerBrief ownerBrief(Member owner, boolean withBizNo) {
        if (owner == null) return null;
        return new OwnerBrief(owner.getMemberId(), owner.getLoginId(), owner.getNickname(),
                withBizNo ? owner.getBizNo() : null,
                withBizNo ? owner.getCreatedAt() : null);
    }
}

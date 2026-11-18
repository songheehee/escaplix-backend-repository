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
 * MemberService 구현 (A 담당). 지금은 전부 501 "아직 구현 전". TODO 를 채우고 throw 줄을 지운다.
 * 조회는 클래스의 readOnly 트랜잭션, 저장·수정·삭제 메서드에는 @Transactional 이 붙어 있다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberServiceImp implements MemberService {

    private final MemberRepository memberRepository;
    private final MemberGenreRepository memberGenreRepository;
    private final GenreRepository genreRepository;
    private final StoreRepository storeRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final FileStorage fileStorage;

    @Override
    @Transactional
    public MemberIdResponse signupUser(SignupUserRequest req) {
        // TODO(A) 아이디·닉네임 중복이면 409 (중복 확인 버튼을 눌렀어도 다시 검사)
        // TODO(A) 비밀번호 BCrypt(passwordEncoder.encode) → MEMBER(role=USER) 저장
        // TODO(A) 같은 트랜잭션에서 genreIds 만큼 MEMBER_GENRE 저장 (없는 장르면 400)
        throw ApiException.notImplemented("A-1 손님 회원가입");
    }

    @Override
    @Transactional
    public MemberIdResponse signupOwner(SignupOwnerRequest req, MultipartFile bizFile) {
        // TODO(A) 아이디·닉네임·사업자번호 중복이면 409
        // TODO(A) fileStorage.savePrivate(bizFile, "biz") → 경로를 biz_file_path 에
        // TODO(A) MEMBER(role=OWNER) 저장. DB 저장이 실패하면 fileStorage.deleteQuietly(경로)
        throw ApiException.notImplemented("A-2 사장 회원가입");
    }

    @Override
    public AvailableResponse checkLoginId(String loginId) {
        // TODO(A) 있으면 available=false (400 아님)
        throw ApiException.notImplemented("A-3 아이디 중복 확인");
    }

    @Override
    public AvailableResponse checkNickname(String nickname) {
        throw ApiException.notImplemented("A-4 닉네임 중복 확인");
    }

    @Override
    public AvailableResponse checkBizNo(String bizNo) {
        throw ApiException.notImplemented("A-5 사업자등록번호 중복 확인");
    }

    @Override
    public LoginResponse login(LoginRequest req) {
        // TODO(A) loginId 로 찾고 passwordEncoder.matches 로 비교
        // TODO(A) 없거나 틀리면 둘 다 401 "아이디 또는 비밀번호가 틀렸습니다."
        // TODO(A) jwtProvider.create(memberId, nickname, role) 로 토큰
        throw ApiException.notImplemented("A-6 로그인");
    }

    @Override
    public MyInfoResponse getMyInfo(LoginMember login) {
        // TODO(A) login.memberId() 로 조회. bizNo 는 사장만
        throw ApiException.notImplemented("A-7 내 정보");
    }

    @Override
    @Transactional
    public NicknameUpdateResponse updateNickname(LoginMember login, NicknameUpdateRequest req) {
        // TODO(A) 다른 사람이 쓰는 닉네임이면 409
        // TODO(A) 바꾸고 새 토큰 발급 (복사본 없음 → 기록은 JOIN 으로 자동 반영)
        throw ApiException.notImplemented("A-8 닉네임 바꾸기");
    }

    @Override
    @Transactional
    public void updatePassword(LoginMember login, PasswordUpdateRequest req) {
        // TODO(A) 현재 비밀번호 틀리면 400 (401 이면 프런트가 로그아웃시킴)
        // TODO(A) 새 비밀번호가 지금과 같으면 400
        // TODO(A) encode 해서 저장
        throw ApiException.notImplemented("A-9 비밀번호 바꾸기");
    }

    @Override
    public AdminMemberListResponse findMembers(String keyword, Role role) {
        // TODO(A) keyword: 아이디·닉네임 일부, role: USER/OWNER
        // TODO(A) countByRole 은 조건과 상관없이 전체 (memberRepository.countByRole)
        // TODO(A) 사장이면 managedStoreCount = storeRepository.countByOwner_MemberId
        throw ApiException.notImplemented("A-10 회원 목록 (관리자)");
    }

    @Override
    public FileDownload getBizFile(Long memberId) {
        // TODO(A) 사장이 아니거나 파일 없으면 404
        // TODO(A) new FileDownload(fileStorage.loadPrivate(경로), FileStorage.contentType(경로))
        throw ApiException.notImplemented("A-11 사업자등록증 이미지 (관리자)");
    }
}

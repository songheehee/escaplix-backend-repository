package com.escaplix.backend.service;

import com.escaplix.backend.common.*;
import com.escaplix.backend.common.auth.*;
import com.escaplix.backend.common.file.FileDownload;
import com.escaplix.backend.dto.member.*;
import com.escaplix.backend.entity.*;
import org.springframework.web.multipart.MultipartFile;
import com.escaplix.backend.common.file.FileStorage;
import com.escaplix.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * MemberService 구현 (A 담당). 지금은 전부 501 "아직 구현 전". TODO 를 채우고 throw 줄을 지운다.
 * 조회는 클래스의 readOnly 트랜잭션, 저장·수정·삭제 메서드에는 @Transactional 이 붙어 있다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;
    private final MemberGenreRepository memberGenreRepository;
    private final GenreRepository genreRepository;
    private final StoreRepository storeRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final FileStorage fileStorage;

    /** 형식 규칙: 가입 DTO(@Pattern·@Size)와 같은 규칙. 중복 확인(A-3~5)은 쿼리 파라미터라 여기서 검사 */
    private static final Pattern LOGIN_ID = Pattern.compile("^[a-z0-9]{4,20}$");
    private static final Pattern BIZ_NO = Pattern.compile("^\\d{3}-\\d{2}-\\d{5}$");
    private static final String LOGIN_FAILED = "아이디 또는 비밀번호가 틀렸습니다.";

    @Override
    @Transactional
    public MemberIdResponse signupUser(SignupUserRequest req) {
        // 중복 확인 버튼을 눌렀어도 그 사이 누가 가입했을 수 있으니 다시 검사
        if (memberRepository.existsByLoginId(req.loginId())) {
            throw ApiException.conflict("이미 사용 중인 아이디입니다.");
        }
        if (memberRepository.existsByNickname(req.nickname())) {
            throw ApiException.conflict("이미 사용 중인 닉네임입니다.");
        }

        // 장르는 저장 전에 전부 확인 (같은 번호가 두 번 와도 한 번만)
        Set<Long> genreIds = new LinkedHashSet<>(req.genreIds() == null ? List.of() : req.genreIds());
        if (genreIds.contains(null)) {
            throw ApiException.badRequest("없는 장르입니다.");
        }
        List<Genre> genres = genreRepository.findAllById(genreIds);
        if (genres.size() != genreIds.size()) {
            throw ApiException.badRequest("없는 장르입니다.");
        }

        Member member = new Member();
        member.setLoginId(req.loginId());
        member.setPassword(passwordEncoder.encode(req.password()));
        member.setNickname(req.nickname());
        member.setRole(Role.USER);
        memberRepository.save(member);

        // 같은 트랜잭션: 여기서 실패하면 회원 저장도 함께 취소된다
        for (Genre genre : genres) {
            MemberGenre mg = new MemberGenre();
            mg.setMember(member);
            mg.setGenre(genre);
            memberGenreRepository.save(mg);
        }
        return new MemberIdResponse(member.getMemberId());
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
        if (loginId == null || !LOGIN_ID.matcher(loginId).matches()) {
            throw ApiException.badRequest("아이디는 영문 소문자와 숫자 4~20자로 입력해 주세요.");
        }
        // 이미 있으면 400 이 아니라 available=false
        return new AvailableResponse(!memberRepository.existsByLoginId(loginId));
    }

    @Override
    public AvailableResponse checkNickname(String nickname) {
        if (nickname == null || nickname.isBlank() || nickname.length() < 2 || nickname.length() > 20) {
            throw ApiException.badRequest("닉네임은 2~20자로 입력해 주세요.");
        }
        return new AvailableResponse(!memberRepository.existsByNickname(nickname));
    }

    @Override
    public AvailableResponse checkBizNo(String bizNo) {
        if (bizNo == null || !BIZ_NO.matcher(bizNo).matches()) {
            throw ApiException.badRequest("사업자등록번호 형식이 올바르지 않습니다.");
        }
        return new AvailableResponse(!memberRepository.existsByBizNo(bizNo));
    }

    @Override
    public LoginResponse login(LoginRequest req) {
        // 아이디가 없는 경우와 비밀번호가 틀린 경우를 같은 문장으로 (어느 쪽인지 알려주지 않기)
        Member member = memberRepository.findByLoginId(req.loginId())
                .orElseThrow(() -> ApiException.unauthorized(LOGIN_FAILED));
        if (!passwordEncoder.matches(req.password(), member.getPassword())) {
            throw ApiException.unauthorized(LOGIN_FAILED);
        }
        String token = jwtProvider.create(member.getMemberId(), member.getNickname(), member.getRole());
        return new LoginResponse(token, member.getMemberId(), member.getNickname(), member.getRole());
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

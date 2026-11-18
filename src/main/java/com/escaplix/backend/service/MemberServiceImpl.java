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
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

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
        // 1) 중복 검사를 파일 저장보다 먼저: 어차피 실패할 가입이면 파일을 만들지 않는다
        if (memberRepository.existsByLoginId(req.loginId())) {
            throw ApiException.conflict("이미 사용 중인 아이디입니다.");
        }
        if (memberRepository.existsByNickname(req.nickname())) {
            throw ApiException.conflict("이미 사용 중인 닉네임입니다.");
        }
        if (memberRepository.existsByBizNo(req.bizNo())) {
            throw ApiException.conflict("이미 등록된 사업자등록번호입니다.");
        }

        // 2) 비공개 폴더에 저장 (형식·크기 검사는 FileStorage 가 함: jpg·png·webp, 5MB)
        String bizFilePath = fileStorage.savePrivate(bizFile, "biz");
        // 3) 이 트랜잭션이 어떤 이유로든 취소되면 방금 저장한 파일도 지운다 (DB 에 없는 파일이 남지 않게)
        deleteFileOnRollback(bizFilePath);

        Member member = new Member();
        member.setLoginId(req.loginId());
        member.setPassword(passwordEncoder.encode(req.password()));
        member.setNickname(req.nickname());
        member.setRole(Role.OWNER);
        member.setBizNo(req.bizNo());
        member.setBizFilePath(bizFilePath);
        // 동시에 같은 아이디로 가입한 경우 UNIQUE 위반이 여기서 나서 → 롤백 → 파일 삭제
        memberRepository.saveAndFlush(member);
        return new MemberIdResponse(member.getMemberId());
    }

    private void deleteFileOnRollback(String storedPath) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                if (status != STATUS_COMMITTED) {
                    fileStorage.deleteQuietly(storedPath);
                }
            }
        });
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
        Member member = findMe(login);
        String bizNo = member.getRole() == Role.OWNER ? member.getBizNo() : null;
        return new MyInfoResponse(member.getMemberId(), member.getLoginId(), member.getNickname(),
                member.getRole(), bizNo, member.getCreatedAt());
    }

    /**
     * 토큰의 회원번호로 회원을 찾는다. 토큰은 맞는데 회원이 없으면(DB를 다시 만든 경우 등) 401 → 프런트가 다시 로그인시킴
     */
    private Member findMe(LoginMember login) {
        return memberRepository.findById(login.memberId())
                .orElseThrow(() -> ApiException.unauthorized("다시 로그인해 주세요."));
    }

    @Override
    @Transactional
    public NicknameUpdateResponse updateNickname(LoginMember login, NicknameUpdateRequest req) {
        Member member = findMe(login);
        // 지금 내 닉네임 그대로면 "이미 사용 중"이 아니라 그냥 성공 (새 토큰만 준다)
        if (!member.getNickname().equals(req.nickname())) {
            if (memberRepository.existsByNickname(req.nickname())) {
                throw ApiException.conflict("이미 사용 중인 닉네임입니다.");
            }
            member.setNickname(req.nickname());
            // 동시에 같은 닉네임으로 바꾼 사람이 있으면 UNIQUE 위반이 여기서 나서 409 (토큰을 만들기 전에)
            memberRepository.flush();
        }
        // 복사본 없음: 후기·파티·채팅은 MEMBER 를 JOIN 하므로 예전 기록도 새 닉네임으로 보인다
        String token = jwtProvider.create(member.getMemberId(), member.getNickname(), member.getRole());
        return new NicknameUpdateResponse(member.getNickname(), token);
    }

    @Override
    @Transactional
    public void updatePassword(LoginMember login, PasswordUpdateRequest req) {
        Member member = findMe(login);
        // 틀려도 401 이 아니라 400 (401 이면 프런트가 로그아웃시킨다)
        if (!passwordEncoder.matches(req.currentPassword(), member.getPassword())) {
            throw ApiException.badRequest("현재 비밀번호가 맞지 않습니다.");
        }
        if (passwordEncoder.matches(req.newPassword(), member.getPassword())) {
            throw ApiException.badRequest("지금 비밀번호와 다른 비밀번호를 입력해 주세요.");
        }
        member.setPassword(passwordEncoder.encode(req.newPassword()));
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

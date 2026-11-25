package com.escaplix.backend.service;

import com.escaplix.backend.common.*;
import com.escaplix.backend.common.auth.*;
import com.escaplix.backend.common.file.FileDownload;
import com.escaplix.backend.dto.member.*;
import com.escaplix.backend.entity.*;
import org.springframework.web.multipart.MultipartFile;
import com.escaplix.backend.common.file.FileStorage;
import com.escaplix.backend.common.mail.EmailCodeStore;
import com.escaplix.backend.common.mail.EmailCodeStore.Purpose;
import com.escaplix.backend.common.mail.Mailer;
import com.escaplix.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * MemberService 구현 (A 담당).
 * 조회는 클래스의 readOnly 트랜잭션, 저장·수정·삭제 메서드에는 @Transactional 이 붙어 있다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;
    private final StoreRepository storeRepository;
    private final MemberGenreRepository memberGenreRepository;
    private final GenreRepository genreRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final FileStorage fileStorage;
    private final Mailer mailer;
    private final EmailCodeStore emailCodes;
    private final WishlistRepository wishlistRepository;
    private final PartyService partyService;

    /** 형식 규칙: 가입 DTO(@Pattern·@Size)와 같은 규칙. 중복 확인(A-3~5)은 쿼리 파라미터라 여기서 검사 */
    private static final Pattern LOGIN_ID = Pattern.compile("^[a-z0-9]{4,20}$");
    private static final Pattern BIZ_NO = Pattern.compile("^\\d{3}-\\d{2}-\\d{5}$");
    private static final String LOGIN_FAILED = "아이디 또는 비밀번호가 틀렸습니다.";
    /** @Email 은 "a@b" 도 통과시켜서, 점(.)이 있는 도메인까지 한 번 더 본다 */
    private static final Pattern EMAIL = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    // ---------------------------------------------------------------- 가입 이메일 인증

    /** 메일을 보내는 동안 DB 연결을 잡고 있지 않게 트랜잭션 밖에서 */
    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void sendSignupEmailCode(EmailCodeRequest req) {
        String email = normalizeEmail(req.email());
        if (memberRepository.existsByEmail(email)) {
            throw ApiException.conflict("이미 가입된 이메일입니다.");
        }
        String code = emailCodes.issue(Purpose.SIGNUP, email);
        try {
            mailer.send(email, "[Escaplix] 이메일 인증번호",
                    "Escaplix 회원가입 이메일 인증번호입니다.\n\n"
                    + "인증번호: " + code + "\n\n"
                    + "5분 안에 가입 화면에 입력해 주세요.\n"
                    + "본인이 요청하지 않았다면 이 메일은 무시하셔도 됩니다.");
        } catch (RuntimeException e) {
            emailCodes.consume(Purpose.SIGNUP, email);   // 못 보냈으면 60초 기다리지 않고 바로 다시 받을 수 있게
            throw e;
        }
    }

    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void verifySignupEmail(EmailVerifyRequest req) {
        emailCodes.verify(Purpose.SIGNUP, normalizeEmail(req.email()), req.code());
    }

    // ---------------------------------------------------------------- 비밀번호 재설정 (아이디 + 이메일 인증)
    //   인증 기록은 "아이디 이메일" 한 쌍으로 센다 (60초·5번·5분)
    //   아이디·이메일 조합이 틀리면 400 "아이디 또는 이메일이 올바르지 않습니다." (11/25 변경. 맞을 때만 인증번호 발급·메일)

    private static final String RESET_NOT_VERIFIED = "이메일 인증을 먼저 해 주세요.";

    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void sendPasswordResetCode(PasswordResetCodeRequest req) {
        String loginId = req.loginId().trim();
        String email = normalizeEmail(req.email());
        // 아이디·이메일 조합이 틀리면 바로 알려 줌 (화면 요청 11/25. 예전에는 맞든 틀리든 똑같이 답했음)
        if (findByLoginIdAndEmail(loginId, email) == null) {
            throw ApiException.badRequest("아이디 또는 이메일이 올바르지 않습니다.");
        }
        String code = emailCodes.issue(Purpose.PASSWORD_RESET, resetKey(loginId, email));   // 60초 안에 또 → 429
        mailer.sendLater(email, "[Escaplix] 비밀번호 재설정 인증번호",
                "Escaplix 비밀번호 재설정 인증번호입니다.\n\n"
                + "아이디: " + loginId + "\n"
                + "인증번호: " + code + "\n\n"
                + "5분 안에 비밀번호 찾기 화면에 입력해 주세요.\n"
                + "본인이 요청하지 않았다면 이 메일은 무시하셔도 됩니다. 비밀번호는 바뀌지 않아요.");
    }

    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void verifyPasswordReset(PasswordResetVerifyRequest req) {
        emailCodes.verify(Purpose.PASSWORD_RESET, resetKey(req.loginId().trim(), normalizeEmail(req.email())), req.code());
    }

    @Override
    @Transactional
    public void resetPassword(PasswordResetRequest req) {
        String loginId = req.loginId().trim();
        String email = normalizeEmail(req.email());
        String key = resetKey(loginId, email);
        if (!emailCodes.isVerified(Purpose.PASSWORD_RESET, key)) {
            throw ApiException.badRequest(RESET_NOT_VERIFIED);
        }
        Member member = findByLoginIdAndEmail(loginId, email);
        if (member == null) {   // 메일을 못 받았으니 인증번호를 맞힐 수 없지만, 우연히 맞힌 경우도 막는다
            throw ApiException.badRequest(RESET_NOT_VERIFIED);
        }
        member.setPassword(passwordEncoder.encode(req.newPassword()));
        // 다 바꾸면 인증 기록을 지운다 (같은 인증으로 또 못 바꾸게)
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                emailCodes.consume(Purpose.PASSWORD_RESET, key);
            }
        });
    }

    // ---------------------------------------------------------------- 회원 탈퇴
    //   줄은 지우지 않는다 (후기·파티·채팅이 이 회원을 가리킴). 대신 익명으로:
    //   아이디 "withdrawn#번호" (가입 규칙상 아무도 못 씀) · 닉네임 "탈퇴한 회원" · 이메일·사업자 정보 지움 · 비밀번호는 아무도 모르는 값
    //   → 같은 아이디·이메일·닉네임으로 다시 가입할 수 있다

    @Override
    @Transactional
    public void withdraw(LoginMember login, WithdrawRequest req) {
        Member me = findMe(login);
        if (me.getRole() == Role.ADMIN) {
            throw ApiException.forbidden("관리자 계정은 탈퇴할 수 없어요.");
        }
        if (!passwordEncoder.matches(req.password(), me.getPassword())) {
            throw ApiException.badRequest("비밀번호가 맞지 않아요.");
        }
        if (me.getRole() == Role.OWNER && storeRepository.countByOwner_MemberId(me.getMemberId()) > 0) {
            throw ApiException.conflict("매장이 남아 있어서 탈퇴할 수 없어요. 매장을 먼저 정리해 주세요.");
        }

        // 1) 안 끝난 파티: 방장이면 삭제, 참여자면 나가기. 끝난 파티 기록은 남김
        partyService.cleanUpForWithdrawal(me.getMemberId());
        // 2) 나만 보던 것: 찜 · 선호 장르
        wishlistRepository.deleteAllOfMember(me.getMemberId());
        memberGenreRepository.deleteByMember_MemberId(me.getMemberId());
        // 3) 익명으로 (후기·채팅은 "탈퇴한 회원"으로 남음)
        String bizFile = me.getBizFilePath();
        me.setLoginId("withdrawn#" + me.getMemberId());
        me.setNickname(Member.WITHDRAWN_NICKNAME);
        me.setEmail(null);
        me.setBizNo(null);
        me.setBizFilePath(null);
        me.setPassword(passwordEncoder.encode(java.util.UUID.randomUUID().toString()));   // 아무도 모르는 비밀번호
        me.setWithdrawnAt(java.time.LocalDateTime.now());
        memberRepository.flush();
        // 4) 사업자등록증 파일은 탈퇴가 커밋된 뒤에 지움 (실패해서 되돌아가면 파일은 그대로)
        if (bizFile != null) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    fileStorage.deleteQuietly(bizFile);
                }
            });
        }
    }

    /** 닉네임을 이미 누가 쓰는지. "탈퇴한 회원"은 탈퇴 표시용이라 누구도 못 씀 */
    private boolean nicknameTaken(String nickname) {
        return Member.WITHDRAWN_NICKNAME.equals(nickname == null ? null : nickname.trim())
                || memberRepository.existsByNickname(nickname);
    }

    // ---------------------------------------------------------------- 아이디 찾기

    /** 메일은 기다리지 않고 (응답 시간으로 회원 여부를 알 수 없게). 60초에 한 번 (남의 메일함에 계속 보내지 못하게) */
    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void findLoginId(EmailCodeRequest req) {
        String email = normalizeEmail(req.email());
        // 가입한 계정이 없으면 바로 알려 줌 (화면 요청 11/25. 예전에는 있든 없든 똑같이 답했음)
        Member found = memberRepository.findByEmail(email)
                .orElseThrow(() -> ApiException.notFound("입력한 이메일로 가입한 계정이 없습니다."));
        emailCodes.throttle(Purpose.FIND_ID, email);
        mailer.sendLater(email, "[Escaplix] 아이디 안내",
                "이 이메일로 가입한 Escaplix 아이디입니다.\n\n"
                + "아이디: " + found.getLoginId() + "\n\n"
                + "비밀번호가 기억나지 않으면 로그인 화면의 \"비밀번호를 잊으셨나요?\"에서 바꿀 수 있어요.\n"
                + "본인이 요청하지 않았다면 이 메일은 무시하셔도 됩니다.");
    }

    // ---------------------------------------------------------------- 내 이메일 등록·변경 (로그인)
    //   이메일 칸이 생기기 전에 가입한 회원이 이메일을 넣거나, 바꾸고 싶을 때. 새 이메일로 인증번호 → 맞으면 바로 바꿈

    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void sendMyEmailCode(LoginMember login, EmailCodeRequest req) {
        String email = normalizeEmail(req.email());
        Member me = findMe(login);
        if (email.equals(me.getEmail())) {
            throw ApiException.badRequest("지금 쓰고 있는 이메일이에요.");
        }
        if (memberRepository.existsByEmail(email)) {
            throw ApiException.conflict("이미 가입된 이메일입니다.");
        }
        String key = myEmailKey(login, email);
        String code = emailCodes.issue(Purpose.EMAIL_CHANGE, key);
        try {
            mailer.send(email, "[Escaplix] 이메일 인증번호",
                    "Escaplix 이메일 등록 인증번호입니다.\n\n"
                    + "인증번호: " + code + "\n\n"
                    + "5분 안에 내 정보 화면에 입력해 주세요.\n"
                    + "본인이 요청하지 않았다면 이 메일은 무시하셔도 됩니다.");
        } catch (RuntimeException e) {
            emailCodes.consume(Purpose.EMAIL_CHANGE, key);   // 못 보냈으면 바로 다시 받을 수 있게
            throw e;
        }
    }

    @Override
    @Transactional
    public void changeMyEmail(LoginMember login, EmailVerifyRequest req) {
        String email = normalizeEmail(req.email());
        String key = myEmailKey(login, email);
        emailCodes.verify(Purpose.EMAIL_CHANGE, key, req.code());   // 틀림·만료 400
        Member me = findMe(login);
        if (!email.equals(me.getEmail()) && memberRepository.existsByEmail(email)) {
            throw ApiException.conflict("이미 가입된 이메일입니다.");   // 인증하는 사이 다른 사람이 가입
        }
        me.setEmail(email);
        try {
            memberRepository.flush();   // 동시에 같은 이메일이 들어오면 UNIQUE 위반이 여기서
        } catch (DataIntegrityViolationException e) {
            throw ApiException.conflict("이미 가입된 이메일입니다.");
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                emailCodes.consume(Purpose.EMAIL_CHANGE, key);
            }
        });
    }

    private static String myEmailKey(LoginMember login, String email) {
        return login.memberId() + " " + email;
    }

    private static String resetKey(String loginId, String email) {
        return loginId + " " + email;   // 아이디엔 공백이 없어서 둘을 구분할 수 있음
    }

    /** 아이디와 이메일이 둘 다 맞는 회원. 없으면 null (이메일을 안 넣은 예전 회원도 null) */
    private Member findByLoginIdAndEmail(String loginId, String email) {
        return memberRepository.findByLoginId(loginId)
                .filter(m -> email.equals(m.getEmail()))
                .orElse(null);
    }

    /** 앞뒤 공백 지우고 소문자로 (Gmail 등은 대소문자를 구분하지 않아서 같은 주소를 하나로 본다) */
    static String normalizeEmail(String email) {
        String e = email == null ? "" : email.trim().toLowerCase(java.util.Locale.ROOT);
        if (e.length() > 100) {
            throw ApiException.badRequest("이메일은 100자 이내로 입력해 주세요.");
        }
        if (!EMAIL.matcher(e).matches()) {
            throw ApiException.badRequest("이메일 형식이 올바르지 않습니다.");
        }
        return e;
    }

    /**
     * 가입 직전 이메일 확인: 이미 가입된 이메일이면 409, 인증을 안 했으면 400.
     * 가입이 커밋되면 인증 기록을 지운다 (같은 인증으로 또 가입 못 하게. 가입이 실패하면 인증은 그대로라 다시 시도 가능)
     */
    private String checkSignupEmail(String rawEmail) {
        String email = normalizeEmail(rawEmail);
        if (memberRepository.existsByEmail(email)) {
            throw ApiException.conflict("이미 가입된 이메일입니다.");
        }
        if (!emailCodes.isVerified(Purpose.SIGNUP, email)) {
            throw ApiException.badRequest("이메일 인증을 먼저 해 주세요.");
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                emailCodes.consume(Purpose.SIGNUP, email);
            }
        });
        return email;
    }

    @Override
    @Transactional
    public MemberIdResponse signupUser(SignupUserRequest req) {
        // 중복 확인 버튼을 눌렀어도 그 사이 누가 가입했을 수 있으니 다시 검사
        if (memberRepository.existsByLoginId(req.loginId())) {
            throw ApiException.conflict("이미 사용 중인 아이디입니다.");
        }
        if (nicknameTaken(req.nickname())) {
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
        // 이메일은 마지막에 (다른 칸이 틀렸으면 그걸 먼저 알려 줌)
        String email = checkSignupEmail(req.email());

        Member member = new Member();
        member.setLoginId(req.loginId());
        member.setPassword(passwordEncoder.encode(req.password()));
        member.setNickname(req.nickname());
        member.setEmail(email);
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
        if (nicknameTaken(req.nickname())) {
            throw ApiException.conflict("이미 사용 중인 닉네임입니다.");
        }
        if (memberRepository.existsByBizNo(req.bizNo())) {
            throw ApiException.conflict("이미 등록된 사업자 등록번호입니다.");
        }
        String email = checkSignupEmail(req.email());

        // 2) 비공개 폴더에 저장 (형식·크기 검사는 FileStorage 가 함: jpg·png·webp, 5MB)
        String bizFilePath = fileStorage.savePrivate(bizFile, "biz");
        // 3) 이 트랜잭션이 어떤 이유로든 취소되면 방금 저장한 파일도 지운다 (DB 에 없는 파일이 남지 않게)
        deleteFileOnRollback(bizFilePath);

        Member member = new Member();
        member.setLoginId(req.loginId());
        member.setPassword(passwordEncoder.encode(req.password()));
        member.setNickname(req.nickname());
        member.setEmail(email);
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
        return new AvailableResponse(!nicknameTaken(nickname));
    }

    @Override
    public AvailableResponse checkBizNo(String bizNo) {
        if (bizNo == null || !BIZ_NO.matcher(bizNo).matches()) {
            throw ApiException.badRequest("사업자 등록번호 형식이 올바르지 않습니다.");
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
                member.getEmail(), member.getRole(), bizNo, member.getCreatedAt());
    }

    /**
     * 토큰의 회원번호로 회원을 찾는다. 토큰은 맞는데 회원이 없으면(DB를 다시 만든 경우 등) 401 → 프런트가 다시 로그인시킴
     */
    private Member findMe(LoginMember login) {
        return memberRepository.findById(login.memberId())
                .filter(m -> !m.isWithdrawn())
                .orElseThrow(() -> ApiException.unauthorized("다시 로그인해 주세요."));
    }

    @Override
    @Transactional
    public NicknameUpdateResponse updateNickname(LoginMember login, NicknameUpdateRequest req) {
        Member member = findMe(login);
        // 지금 내 닉네임 그대로면 "이미 사용 중"이 아니라 그냥 성공 (새 토큰만 준다)
        if (!member.getNickname().equals(req.nickname())) {
            if (nicknameTaken(req.nickname())) {
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
        List<Member> members = memberRepository.search(likePattern(keyword), role);

        // 사장의 매장 수는 사장마다 묻지 않고 한 번에 (상태와 상관없이)
        Map<Long, Long> storeCounts = new HashMap<>();
        for (Object[] r : memberRepository.countStoresByOwner()) {
            storeCounts.put((Long) r[0], ((Number) r[1]).longValue());
        }
        // 사장의 매장 목록도 한 번에 (회원 관리 "매장 N곳" 펼치기)
        Map<Long, List<AdminMemberItem.StoreBrief>> storesByOwner = new HashMap<>();
        for (Store s : storeRepository.findByOwnerIsNotNullOrderByCreatedAtAsc()) {
            storesByOwner.computeIfAbsent(s.getOwner().getMemberId(), k -> new java.util.ArrayList<>())
                    .add(new AdminMemberItem.StoreBrief(s.getStoreId(), s.getName(), s.getStatus()));
        }
        List<AdminMemberItem> items = members.stream().map(m -> {
            boolean owner = m.getRole() == Role.OWNER;
            return new AdminMemberItem(m.getMemberId(), m.getLoginId(), m.getNickname(), m.getRole(),
                    owner ? m.getBizNo() : null,
                    owner ? storeCounts.getOrDefault(m.getMemberId(), 0L) : null,
                    m.getCreatedAt(),
                    owner ? storesByOwner.getOrDefault(m.getMemberId(), List.of()) : null);
        }).toList();

        // 역할 탭 숫자: 검색 조건과 상관없이 전체. 회원이 없는 역할도 0 으로
        Map<Role, Long> countByRole = new LinkedHashMap<>();
        for (Role r : List.of(Role.USER, Role.OWNER, Role.ADMIN)) countByRole.put(r, 0L);
        for (Object[] r : memberRepository.countGroupByRole()) {
            countByRole.put((Role) r[0], ((Number) r[1]).longValue());
        }
        return new AdminMemberListResponse(items.size(), countByRole, items);
    }

    @Override
    public FileDownload getBizFile(Long memberId) {
        String path = memberRepository.findById(memberId)
                .filter(m -> m.getRole() == Role.OWNER)
                .map(Member::getBizFilePath)
                .orElseThrow(() -> ApiException.notFound("사업자등록증 파일이 없습니다."));
        // 비공개 폴더 밖을 가리키거나 파일이 실제로 없으면 loadPrivate 가 404
        return new FileDownload(fileStorage.loadPrivate(path), FileStorage.contentType(path));
    }

    /** 검색어 → "%검색어%" (앞뒤 공백 지움, % _ \ 는 글자 그대로 찾게 이스케이프). 비면 null */
    private static String likePattern(String keyword) {
        if (keyword == null || keyword.isBlank()) return null;
        String k = keyword.strip().replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
        return "%" + k + "%";
    }
}

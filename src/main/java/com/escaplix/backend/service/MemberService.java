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

/** A 담당 · 회원 (A-1 ~ A-11). 명세: docs/API.md (목업 레포) */
public interface MemberService {

    /** A-1 손님 회원가입 */
    MemberIdResponse signupUser(SignupUserRequest req);

    /** A-2 사장 회원가입 */
    MemberIdResponse signupOwner(SignupOwnerRequest req, MultipartFile bizFile);

    /** 가입 이메일 인증번호 받기 (이미 가입된 이메일이면 409, 60초 안에 또 받으면 429) */
    void sendSignupEmailCode(EmailCodeRequest req);

    /** 가입 이메일 인증번호 확인 (맞으면 30분 안에 가입) */
    void verifySignupEmail(EmailVerifyRequest req);

    /** 비밀번호 재설정 1: 아이디·이메일이 맞으면 인증번호 메일. 맞든 틀리든 같은 응답 (회원 여부를 알려 주지 않기) */
    void sendPasswordResetCode(PasswordResetCodeRequest req);

    /** 비밀번호 재설정 2: 인증번호 확인 */
    void verifyPasswordReset(PasswordResetVerifyRequest req);

    /** 비밀번호 재설정 3: 인증을 마쳤으면 새 비밀번호로 */
    void resetPassword(PasswordResetRequest req);

    /** 아이디 찾기: 그 이메일로 가입한 회원이 있으면 아이디를 메일로. 있든 없든 같은 응답 */
    void findLoginId(EmailCodeRequest req);

    /** 내 이메일 등록·변경 1: 새 이메일로 인증번호 (다른 회원이 쓰면 409) */
    void sendMyEmailCode(LoginMember login, EmailCodeRequest req);

    /** 내 이메일 등록·변경 2: 인증번호가 맞으면 바로 바꿈 */
    void changeMyEmail(LoginMember login, EmailVerifyRequest req);

    /** A-3 아이디 중복 확인 */
    AvailableResponse checkLoginId(String loginId);

    /** A-4 닉네임 중복 확인 */
    AvailableResponse checkNickname(String nickname);

    /** A-5 사업자등록번호 중복 확인 */
    AvailableResponse checkBizNo(String bizNo);

    /** A-6 로그인 */
    LoginResponse login(LoginRequest req);

    /** A-7 내 정보 */
    MyInfoResponse getMyInfo(LoginMember login);

    /** A-8 닉네임 바꾸기 */
    NicknameUpdateResponse updateNickname(LoginMember login, NicknameUpdateRequest req);

    /** A-9 비밀번호 바꾸기 */
    void updatePassword(LoginMember login, PasswordUpdateRequest req);

    /** A-10 회원 목록 (관리자) */
    AdminMemberListResponse findMembers(String keyword, Role role);

    /** A-11 사업자등록증 이미지 (관리자) */
    FileDownload getBizFile(Long memberId);
}

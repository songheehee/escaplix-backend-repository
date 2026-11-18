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

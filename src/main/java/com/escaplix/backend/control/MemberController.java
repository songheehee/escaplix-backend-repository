package com.escaplix.backend.control;

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
import com.escaplix.backend.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

/**
 * A · 회원 (A-1 ~ A-9)
 * 컨트롤러는 받고 넘기기만 한다: 검사·권한 확인·트랜잭션은 서비스에서. 응답은 DTO 만 (엔티티 금지).
 */
@RestController
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    /** A-1 손님 회원가입 · 공개 · 201 */
    @PostMapping("/api/members/user")
    @ResponseStatus(HttpStatus.CREATED)
    public MemberIdResponse signupUser(@Valid @RequestBody SignupUserRequest req) {
        return memberService.signupUser(req);
    }

    /** A-2 사장 회원가입 · 공개 · 201 */
    @PostMapping(value = "/api/members/owner", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public MemberIdResponse signupOwner(@Valid @ModelAttribute SignupOwnerRequest req, @RequestPart(value = "bizFile") MultipartFile bizFile) {
        return memberService.signupOwner(req, bizFile);
    }

    /** A-3 아이디 중복 확인 · 공개 · 200 */
    @GetMapping("/api/members/check-login-id")
    public AvailableResponse checkLoginId(@RequestParam String loginId) {
        return memberService.checkLoginId(loginId);
    }

    /** A-4 닉네임 중복 확인 · 공개 · 200 */
    @GetMapping("/api/members/check-nickname")
    public AvailableResponse checkNickname(@RequestParam String nickname) {
        return memberService.checkNickname(nickname);
    }

    /** A-5 사업자등록번호 중복 확인 · 공개 · 200 */
    @GetMapping("/api/members/check-biz-no")
    public AvailableResponse checkBizNo(@RequestParam String bizNo) {
        return memberService.checkBizNo(bizNo);
    }

    /** A-6 로그인 · 공개 · 200 */
    @PostMapping("/api/auth/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest req) {
        return memberService.login(req);
    }

    /** A-7 내 정보 · 로그인 · 200 */
    @GetMapping("/api/members/me")
    @Auth
    public MyInfoResponse getMyInfo(LoginMember login) {
        return memberService.getMyInfo(login);
    }

    /** A-8 닉네임 바꾸기 · 로그인 · 200 */
    @PutMapping("/api/members/me")
    @Auth
    public NicknameUpdateResponse updateNickname(LoginMember login, @Valid @RequestBody NicknameUpdateRequest req) {
        return memberService.updateNickname(login, req);
    }

    /** A-9 비밀번호 바꾸기 · 로그인 · 204 */
    @PutMapping("/api/members/me/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Auth
    public void updatePassword(LoginMember login, @Valid @RequestBody PasswordUpdateRequest req) {
        memberService.updatePassword(login, req);
    }
}

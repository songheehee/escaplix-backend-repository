package com.escaplix.backend.dto.member;

import jakarta.validation.constraints.*;
import java.util.List;

/** A-1 손님 가입 요청. 비밀번호 확인 칸은 화면에서만 비교 */
public record SignupUserRequest(
        @NotBlank(message = "아이디를 입력해 주세요.")
        @Pattern(regexp = "^[a-z0-9]{4,20}$", message = "아이디는 영문 소문자와 숫자 4~20자로 입력해 주세요.")
        String loginId,
        @NotBlank(message = "비밀번호를 입력해 주세요.")
        @Size(min = 4, max = 20, message = "비밀번호는 4~20자로 입력해 주세요.")
        String password,
        @NotBlank(message = "닉네임을 입력해 주세요.")
        @Size(min = 2, max = 20, message = "닉네임은 2~20자로 입력해 주세요.")
        String nickname,
        /** 가입 전에 인증번호로 확인한 이메일 (POST /api/members/email-code → /verify) */
        @NotBlank(message = "이메일을 입력해 주세요.")
        // 형식·길이(100자)는 앞뒤 공백을 지운 뒤 서비스(MemberServiceImpl.normalizeEmail)가 본다
        String email,
        /** 좋아하는 장르. 없으면 빈 배열 */
        List<Long> genreIds
) {}

package com.escaplix.backend.dto.member;

import jakarta.validation.constraints.*;

/** A-2 사장 가입 요청 (multipart 폼 칸). 사업자등록증 파일은 컨트롤러에서 bizFile 로 따로 받음 */
public record SignupOwnerRequest(
        @NotBlank(message = "아이디를 입력해 주세요.")
        @Pattern(regexp = "^[a-z0-9]{4,20}$", message = "아이디는 영문 소문자와 숫자 4~20자로 입력해 주세요.")
        String loginId,
        @NotBlank(message = "비밀번호를 입력해 주세요.")
        @Size(min = 4, max = 20, message = "비밀번호는 4~20자로 입력해 주세요.")
        String password,
        @NotBlank(message = "닉네임을 입력해 주세요.")
        @Size(min = 2, max = 20, message = "닉네임은 2~20자로 입력해 주세요.")
        String nickname,
        /** 가입 전에 인증번호로 확인한 이메일 */
        @NotBlank(message = "이메일을 입력해 주세요.")
        // 형식·길이(100자)는 앞뒤 공백을 지운 뒤 서비스(MemberServiceImpl.normalizeEmail)가 본다
        String email,
        @NotBlank(message = "사업자등록번호를 입력해 주세요.")
        @Pattern(regexp = "^\\d{3}-\\d{2}-\\d{5}$", message = "사업자등록번호 형식이 올바르지 않습니다.")
        String bizNo
) {}

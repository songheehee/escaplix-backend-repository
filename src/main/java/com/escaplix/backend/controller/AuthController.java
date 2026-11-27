package com.escaplix.backend.controller;

import com.escaplix.backend.common.ApiException;
import com.escaplix.backend.common.ErrorResponse;
import com.escaplix.backend.common.auth.RefreshCookie;
import com.escaplix.backend.common.auth.RefreshTokenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * A · 로그인 유지 (Refresh Token). 로그인(A-6)은 MemberController
 *   토큰은 쿠키로만 받는다 (요청 본문 없음)
 */
@RestController
@RequiredArgsConstructor
public class AuthController {

    private final RefreshTokenService refreshTokens;

    /**
     * A-32 Access Token 재발급 · 공개(쿠키) · 200 (본문은 로그인 응답과 같음)
     *   쿠키 없음·만료·이미 쓴 토큰·탈퇴 회원 401. 실패하면 쿠키도 지운다
     *   (예외를 던지지 않고 여기서 401 을 돌려줌 → 쿠키 지우는 헤더가 확실히 나감)
     */
    @PostMapping("/api/auth/refresh")
    public ResponseEntity<?> refresh(@CookieValue(value = RefreshCookie.NAME, required = false) String token,
                                     HttpServletRequest req, HttpServletResponse res) {
        try {
            RefreshTokenService.Issued issued = refreshTokens.rotate(token);
            RefreshCookie.write(req, res, issued.refreshToken());
            return ResponseEntity.ok(issued.login());
        } catch (ApiException e) {
            RefreshCookie.clear(req, res);
            return ResponseEntity.status(e.getStatus()).body(new ErrorResponse(e.getMessage()));
        }
    }

    /** A-33 로그아웃 · 공개(쿠키) · 204. 이 브라우저의 Refresh Token 을 지운다 (Access Token 은 프런트가 지움) */
    @PostMapping("/api/auth/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(@CookieValue(value = RefreshCookie.NAME, required = false) String token,
                       HttpServletRequest req, HttpServletResponse res) {
        refreshTokens.revoke(token);
        RefreshCookie.clear(req, res);
    }
}

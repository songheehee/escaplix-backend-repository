package com.escaplix.backend.common.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;

/**
 * Refresh Token 쿠키
 *   HttpOnly  화면 JS 가 못 읽음 (XSS 로 훔쳐 가지 못하게)
 *   SameSite=Strict  다른 사이트에서 보낸 요청엔 안 붙음 (화면 :5500 과 서버 :8081 은 같은 호스트라 같은 사이트)
 *   Path=/api/auth  로그인 유지 API 에만 붙음 (다른 API 요청엔 안 실림)
 *   만료 시각 없음(세션 쿠키)  브라우저를 닫으면 사라짐. 서버 쪽 기한(7일)은 DB 의 expires_at
 *   Secure  https 로 열었을 때만 (로컬 http 에서는 붙이면 쿠키가 안 저장됨)
 */
public final class RefreshCookie {

    public static final String NAME = "refresh_token";
    private static final String PATH = "/api/auth";

    private RefreshCookie() {}

    public static void write(HttpServletRequest req, HttpServletResponse res, String token) {
        res.addHeader(HttpHeaders.SET_COOKIE, base(req, token).build().toString());
    }

    public static void clear(HttpServletRequest req, HttpServletResponse res) {
        res.addHeader(HttpHeaders.SET_COOKIE, base(req, "").maxAge(0).build().toString());
    }

    private static ResponseCookie.ResponseCookieBuilder base(HttpServletRequest req, String value) {
        return ResponseCookie.from(NAME, value)
                .httpOnly(true)
                .secure(req.isSecure())
                .sameSite("Strict")
                .path(PATH);
    }
}

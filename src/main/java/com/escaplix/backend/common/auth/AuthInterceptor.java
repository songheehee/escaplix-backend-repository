package com.escaplix.backend.common.auth;

import com.escaplix.backend.common.ApiException;
import com.escaplix.backend.repository.MemberRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Arrays;

/**
 * /api/** 요청마다 Authorization: Bearer 토큰을 읽어 LoginMember 로 만들어 둔다.
 * - &#64;Auth 가 붙은 API: 토큰 없음·틀림 401, 역할 불일치 403
 * - 공개 API: 토큰이 맞으면 LoginMember 를 넣어 주고(찜 여부 등), 틀린 토큰은 그냥 무시 (비회원처럼)
 * - 탈퇴한 회원의 토큰: 토큰은 만료 전까지 살아 있어서 요청마다 DB 로 확인한다. @Auth 는 401, 공개 API 는 비회원처럼
 */
@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {

    public static final String ATTR = "loginMember";

    private final JwtProvider jwtProvider;
    private final MemberRepository memberRepository;

    @Override
    public boolean preHandle(HttpServletRequest req, HttpServletResponse res, Object handler) {
        if (!(handler instanceof HandlerMethod hm)) {
            return true;    // CORS 사전 요청, 정적 파일 등
        }
        Auth auth = hm.getMethodAnnotation(Auth.class);
        if (auth == null) {
            auth = hm.getBeanType().getAnnotation(Auth.class);
        }

        String header = req.getHeader("Authorization");
        String token = (header != null && header.startsWith("Bearer ")) ? header.substring(7) : null;

        if (auth == null) {
            if (token != null) {
                try {
                    LoginMember login = jwtProvider.parse(token);
                    if (!memberRepository.existsByMemberIdAndWithdrawnAtIsNotNull(login.memberId())) {
                        req.setAttribute(ATTR, login);
                    }
                } catch (ApiException ignored) {
                    // 공개 API 는 만료된 토큰이어도 비회원으로 처리
                }
            }
            return true;
        }

        if (token == null) {
            throw ApiException.unauthorized("로그인이 필요합니다.");
        }
        LoginMember login = jwtProvider.parse(token);
        if (memberRepository.existsByMemberIdAndWithdrawnAtIsNotNull(login.memberId())) {
            throw ApiException.unauthorized("다시 로그인해 주세요.");
        }
        if (auth.value().length > 0 && Arrays.stream(auth.value()).noneMatch(r -> r == login.role())) {
            throw ApiException.forbidden("권한이 없습니다.");
        }
        req.setAttribute(ATTR, login);
        return true;
    }
}

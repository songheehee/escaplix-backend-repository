package com.escaplix.backend.common.auth;

import com.escaplix.backend.common.ApiException;
import com.escaplix.backend.entity.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Date;

/** 토큰 만들기·읽기. 토큰 내용: memberId(sub), nickname, role. 비밀번호는 넣지 않음 */
@Component
public class JwtProvider {

    private final SecretKey key;
    private final Duration ttl;

    public JwtProvider(@Value("${jwt.secret:}") String secret,
                       @Value("${jwt.expiration-minutes:60}") long minutes) {
        if (secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException("jwt.secret 이 비었거나 32바이트보다 짧습니다. application-local.properties 를 확인하세요.");
        }
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.ttl = Duration.ofMinutes(minutes);
    }

    public String create(Long memberId, String nickname, Role role) {
        Date now = new Date();
        return Jwts.builder()
                .subject(String.valueOf(memberId))
                .claim("nickname", nickname)
                .claim("role", role.name())
                .issuedAt(now)
                .expiration(new Date(now.getTime() + ttl.toMillis()))
                .signWith(key)
                .compact();
    }

    /** 틀리거나 만료되면 401 */
    public LoginMember parse(String token) {
        try {
            Claims c = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
            return new LoginMember(Long.valueOf(c.getSubject()), c.get("nickname", String.class),
                    Role.valueOf(c.get("role", String.class)));
        } catch (JwtException | IllegalArgumentException e) {
            throw ApiException.unauthorized("로그인이 만료되었습니다. 다시 로그인해 주세요.");
        }
    }
}

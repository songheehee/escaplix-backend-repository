package com.escaplix.backend.common.auth;

import com.escaplix.backend.common.ApiException;
import com.escaplix.backend.dto.member.LoginResponse;
import com.escaplix.backend.entity.Member;
import com.escaplix.backend.entity.RefreshToken;
import com.escaplix.backend.repository.MemberRepository;
import com.escaplix.backend.repository.RefreshTokenRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;

/**
 * Refresh Token (로그인 유지)
 *   Access Token(JWT, 30분)이 끝나면 프런트가 POST /api/auth/refresh 로 새 Access Token 을 받는다
 *   - 원문: 무작위 32바이트. 쿠키(HttpOnly)로만 오가서 화면 JS 가 읽을 수 없다
 *   - DB: SHA-256 해시만 저장
 *   - 회전: 쓸 때마다 지우고 새로 발급 → 훔친 토큰을 한 번 쓰면 원래 주인의 토큰이 막혀 알아챌 수 있음
 *   - 없앰: 로그아웃(이 토큰) · 비밀번호 변경/재설정 · 탈퇴(이 회원 전부)
 */
@Service
public class RefreshTokenService {

    /** 새로 받은 Access Token + 쿠키에 넣을 새 Refresh Token 원문 */
    public record Issued(LoginResponse login, String refreshToken) {}

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String EXPIRED = "로그인이 만료되었습니다. 다시 로그인해 주세요.";

    private final RefreshTokenRepository repository;
    private final MemberRepository memberRepository;
    private final JwtProvider jwtProvider;
    private final Duration ttl;

    public RefreshTokenService(RefreshTokenRepository repository, MemberRepository memberRepository, JwtProvider jwtProvider,
                               @Value("${jwt.refresh-days:7}") long days) {
        this.repository = repository;
        this.memberRepository = memberRepository;
        this.jwtProvider = jwtProvider;
        this.ttl = Duration.ofDays(days);
    }

    /** 로그인 성공 뒤: 새 Refresh Token 발급 (원문을 돌려줌 → 컨트롤러가 쿠키로) */
    @Transactional
    public String issue(Long memberId) {
        repository.deleteExpiredOfMember(memberId, LocalDateTime.now());
        String raw = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes());
        RefreshToken t = new RefreshToken();
        t.setMemberId(memberId);
        t.setTokenHash(hash(raw));
        t.setExpiresAt(LocalDateTime.now().plus(ttl));
        repository.save(t);
        return raw;
    }

    /** 재발급: 쿠키의 토큰 확인 → 지우고 새 Access + 새 Refresh. 없음·만료·탈퇴 회원이면 401 */
    @Transactional
    public Issued rotate(String raw) {
        if (raw == null || raw.isBlank()) {
            throw ApiException.unauthorized(EXPIRED);
        }
        RefreshToken saved = repository.findByTokenHash(hash(raw)).orElseThrow(() -> ApiException.unauthorized(EXPIRED));
        repository.delete(saved);
        if (saved.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw ApiException.unauthorized(EXPIRED);
        }
        Member m = memberRepository.findById(saved.getMemberId()).orElse(null);
        if (m == null || m.isWithdrawn()) {
            throw ApiException.unauthorized(EXPIRED);
        }
        // 닉네임을 바꿨으면 새 토큰에도 새 닉네임 (DB 값으로 다시 만든다)
        String access = jwtProvider.create(m.getMemberId(), m.getNickname(), m.getRole());
        return new Issued(new LoginResponse(access, m.getMemberId(), m.getNickname(), m.getRole()), issue(m.getMemberId()));
    }

    /** 로그아웃: 이 토큰만 지움 (없어도 조용히) */
    @Transactional
    public void revoke(String raw) {
        if (raw != null && !raw.isBlank()) {
            repository.deleteByHash(hash(raw));
        }
    }

    /** 비밀번호 변경·재설정·탈퇴: 모든 기기에서 로그아웃 */
    @Transactional
    public void revokeAll(Long memberId) {
        repository.deleteAllOfMember(memberId);
    }

    private static byte[] randomBytes() {
        byte[] b = new byte[32];
        RANDOM.nextBytes(b);
        return b;
    }

    private static String hash(String raw) {
        try {
            byte[] d = MessageDigest.getInstance("SHA-256").digest(raw.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(d);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}

package com.escaplix.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * REFRESH_TOKEN 로그인 유지용 토큰 (A)
 *   토큰 원문은 브라우저 쿠키(HttpOnly)에만 있고, DB 에는 SHA-256 해시만 둔다 (DB 가 새도 토큰을 쓸 수 없게)
 *   한 번 쓰면 지우고 새로 발급 (재사용 막기)
 */
@Entity
@Table(name = "refresh_token")
@Getter
@Setter
@NoArgsConstructor
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sq_refresh_token")
    @SequenceGenerator(name = "sq_refresh_token", sequenceName = "sq_refresh_token", allocationSize = 1)
    @Column(name = "token_id")
    private Long tokenId;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "token_hash", nullable = false, length = 64)
    private String tokenHash;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;
}

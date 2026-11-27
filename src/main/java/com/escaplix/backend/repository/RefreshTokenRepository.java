package com.escaplix.backend.repository;

import com.escaplix.backend.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

/** REFRESH_TOKEN (A) */
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByTokenHash(String tokenHash);

    /** 로그아웃: 이 토큰만 */
    @Modifying
    @Query("delete from RefreshToken r where r.tokenHash = :hash")
    int deleteByHash(@Param("hash") String hash);

    /** 비밀번호 변경·재설정·탈퇴: 이 회원의 모든 기기에서 로그아웃 */
    @Modifying
    @Query("delete from RefreshToken r where r.memberId = :memberId")
    int deleteAllOfMember(@Param("memberId") Long memberId);

    /** 새로 발급할 때 이 회원의 기한 지난 토큰 정리 (표가 계속 커지지 않게) */
    @Modifying
    @Query("delete from RefreshToken r where r.memberId = :memberId and r.expiresAt < :now")
    int deleteExpiredOfMember(@Param("memberId") Long memberId, @Param("now") LocalDateTime now);
}

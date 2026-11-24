package com.escaplix.backend.repository;

import com.escaplix.backend.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

/** WISHLIST (B) */
public interface WishlistRepository extends JpaRepository<Wishlist, Long> {

    Optional<Wishlist> findByMember_MemberIdAndTheme_ThemeId(Long memberId, Long themeId);

    boolean existsByMember_MemberIdAndTheme_ThemeId(Long memberId, Long themeId);

    /** 찜 목록 (B-6): 최근 순 */
    List<Wishlist> findByMember_MemberIdOrderByCreatedAtDesc(Long memberId);

    /** 탈퇴할 때 내 찜 전부 */
    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.data.jpa.repository.Query("delete from Wishlist w where w.member.memberId = :memberId")
    int deleteAllOfMember(@org.springframework.data.repository.query.Param("memberId") Long memberId);
}

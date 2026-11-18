package com.escaplix.backend.repository;

import com.escaplix.backend.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

/** MEMBER (A) */
public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByLoginId(String loginId);

    boolean existsByLoginId(String loginId);

    boolean existsByNickname(String nickname);

    boolean existsByBizNo(String bizNo);

    long countByRole(Role role);
}

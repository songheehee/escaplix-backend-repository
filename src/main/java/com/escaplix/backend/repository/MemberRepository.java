package com.escaplix.backend.repository;

import com.escaplix.backend.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/** MEMBER (A) */
public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByLoginId(String loginId);

    boolean existsByLoginId(String loginId);

    boolean existsByNickname(String nickname);

    boolean existsByBizNo(String bizNo);

    boolean existsByEmail(String email);

    Optional<Member> findByEmail(String email);

    long countByRole(Role role);

    /**
     * 관리자 회원 목록 (A-10). null 인 조건은 빼고 묻는다. 가입 순(번호 오름차순)
     * @param kw 아이디·닉네임 일부 LIKE 패턴 ("%" + 이스케이프한 검색어 + "%"). null 이면 전체
     */
    @Query("select m from Member m" +
           " where (:role is null or m.role = :role)" +
           " and (:kw is null or m.loginId like :kw escape '\\' or m.nickname like :kw escape '\\')" +
           " order by m.memberId asc")
    List<Member> search(@Param("kw") String kw, @Param("role") Role role);

    /** 역할별 회원 수 (A-10 countByRole). [Role, Long] */
    @Query("select m.role, count(m) from Member m group by m.role")
    List<Object[]> countGroupByRole();

    /** 사장별 매장 수 (A-10 managedStoreCount). 상태와 상관없이 센다. [ownerId, Long] */
    @Query("select s.owner.memberId, count(s) from Store s where s.owner is not null group by s.owner.memberId")
    List<Object[]> countStoresByOwner();
}

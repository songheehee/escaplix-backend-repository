package com.escaplix.backend.repository;

import com.escaplix.backend.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

/** PARTY_MEMBER (A). 인원·목록·권한은 항상 JOINED 만 본다 */
public interface PartyMemberRepository extends JpaRepository<PartyMember, Long> {

    Optional<PartyMember> findByParty_PartyIdAndMember_MemberId(Long partyId, Long memberId);

    long countByParty_PartyIdAndStatus(Long partyId, PartyMemberStatus status);

    List<PartyMember> findByParty_PartyIdAndStatusOrderByJoinedAtAsc(Long partyId, PartyMemberStatus status);

    /** 파티 방(A-13) 참여자 목록. 닉네임까지 한 번에 (먼저 들어온 순) */
    @Query("select pm from PartyMember pm join fetch pm.member" +
           " where pm.party.partyId = :partyId and pm.status = :status order by pm.joinedAt asc, pm.id asc")
    List<PartyMember> findWithMember(@Param("partyId") Long partyId, @Param("status") PartyMemberStatus status);

    /** 내 채팅 목록 (A-22) */
    List<PartyMember> findByMember_MemberIdAndStatus(Long memberId, PartyMemberStatus status);
}

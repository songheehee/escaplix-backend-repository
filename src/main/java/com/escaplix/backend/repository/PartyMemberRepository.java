package com.escaplix.backend.repository;

import com.escaplix.backend.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

/** PARTY_MEMBER (A). 인원·목록·권한은 항상 JOINED 만 본다 */
public interface PartyMemberRepository extends JpaRepository<PartyMember, Long> {

    Optional<PartyMember> findByParty_PartyIdAndMember_MemberId(Long partyId, Long memberId);

    long countByParty_PartyIdAndStatus(Long partyId, PartyMemberStatus status);

    List<PartyMember> findByParty_PartyIdAndStatusOrderByJoinedAtAsc(Long partyId, PartyMemberStatus status);

    /** 내 채팅 목록 (A-22) */
    List<PartyMember> findByMember_MemberIdAndStatus(Long memberId, PartyMemberStatus status);
}

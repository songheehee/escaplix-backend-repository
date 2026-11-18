package com.escaplix.backend.repository;

import com.escaplix.backend.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

/** PARTY_MESSAGE (A) */
public interface PartyMessageRepository extends JpaRepository<PartyMessage, Long> {

    /** 처음 들어왔을 때 최근 50개 (뒤집어서 오름차순으로 보낸다) */
    List<PartyMessage> findTop50ByParty_PartyIdOrderByMessageIdDesc(Long partyId);

    /** 3초마다: after 다음 것 최대 100개 */
    List<PartyMessage> findTop100ByParty_PartyIdAndMessageIdGreaterThanOrderByMessageIdAsc(Long partyId, Long after);

    /** 내 채팅 목록의 마지막 메시지 */
    Optional<PartyMessage> findTopByParty_PartyIdOrderByMessageIdDesc(Long partyId);
}

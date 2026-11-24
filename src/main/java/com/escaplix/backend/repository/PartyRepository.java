package com.escaplix.backend.repository;

import com.escaplix.backend.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

/** PARTY (A). 목록(A-12)은 조건이 많아서 @Query 로 직접 작성 */
public interface PartyRepository extends JpaRepository<Party, Long> {

    /** 참여·나가기·강퇴·마감·삭제할 때 이 줄을 잠그고 읽는다 (SELECT ... FOR UPDATE) */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Party p where p.partyId = :partyId")
    Optional<Party> findByIdForUpdate(@Param("partyId") Long partyId);

    /** 탈퇴: 내가 방장이고 아직 안 끝난(플레이 시각 전) 파티. 삭제된 건 빼고 */
    @Query("select p.partyId from Party p where p.leader.memberId = :memberId and p.status <> :deleted and p.playAt > :now")
    java.util.List<Long> findActiveIdsLedBy(@Param("memberId") Long memberId, @Param("deleted") PartyStatus deleted,
                                            @Param("now") LocalDateTime now);

    /** 스케줄러: 기한 지난 RECRUITING → CLOSED */
    @Modifying
    @Query("update Party p set p.status = :to where p.status = :from and p.deadlineAt <= :now")
    int updateStatusWhereDeadlinePassed(@Param("from") PartyStatus from, @Param("to") PartyStatus to,
                                        @Param("now") LocalDateTime now);
}

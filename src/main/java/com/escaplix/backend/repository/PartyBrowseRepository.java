package com.escaplix.backend.repository;

import com.escaplix.backend.entity.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.*;

/**
 * A-12 파티 목록용 조회 모음. 조건이 많아서 JPQL 을 직접 쓴다 (값은 모두 setParameter 로 바인딩, 명세 1-7).
 * "목록에 보이는 파티" = status RECRUITING 이고 deadline_at 이 아직 안 지남.
 * 인원·장르·내 상태는 파티마다 따로 묻지 않고 한 번에 모아서 묻는다 (N+1 방지, Oracle IN 1000개 제한도 피함).
 */
@Repository
public class PartyBrowseRepository {

    /**
     * 목록에 보이는 파티 조건 (아래 쿼리들이 같이 씀). p = Party. 모집 마감(CLOSED)·삭제 전 파티는 다 보인다
     *   모집 중(RECRUITING): 모집 기한 전까지 (기한이 지나면 스케줄러가 CLOSED 로 바꿈)
     *   정원 마감(FULL): 플레이 시간 전까지 (정원이 찬 파티는 기한이 지나도 CLOSED 로 안 바뀜)
     */
    private static final String VISIBLE =
            "((p.status = :recruiting and p.deadlineAt > :now) or (p.status = :full and p.playAt > :now))";

    @PersistenceContext
    private EntityManager em;

    /**
     * 목록 파티 (모집 중 + 정원 마감). 가까운 일정순
     * @param keywordLike 띄어쓰기를 지운 테마 이름 LIKE 패턴 (ThemeBrowseRepository.likePattern 으로 만든다). null 이면 조건 없음
     * @param genreIds    하나라도 맞으면. null·빈 목록이면 조건 없음
     */
    public List<Party> findRecruiting(LocalDateTime now, String keywordLike, Collection<Long> genreIds, Long themeId) {
        StringBuilder jpql = new StringBuilder(
                "select p from Party p join fetch p.theme t join fetch t.store join fetch p.leader where " + VISIBLE);
        Map<String, Object> params = new HashMap<>();
        params.put("recruiting", PartyStatus.RECRUITING);
        params.put("full", PartyStatus.FULL);
        params.put("now", now);

        if (themeId != null) {
            jpql.append(" and t.themeId = :themeId");
            params.put("themeId", themeId);
        }
        if (keywordLike != null) {
            jpql.append(" and replace(t.title, ' ', '') like :kw escape '\\'");
            params.put("kw", keywordLike);
        }
        if (genreIds != null && !genreIds.isEmpty()) {
            // EXISTS 라서 장르가 여러 개 맞아도 파티가 한 번만 나온다
            jpql.append(" and exists (select 1 from ThemeGenre tg where tg.theme = t and tg.genre.genreId in :genreIds)");
            params.put("genreIds", genreIds);
        }
        jpql.append(" order by p.playAt asc, p.partyId asc");

        TypedQuery<Party> q = em.createQuery(jpql.toString(), Party.class);
        params.forEach(q::setParameter);
        return q.getResultList();
    }

    /** 목록에 보이는 파티들의 현재 인원 (JOINED 수). partyId → 인원 */
    public Map<Long, Integer> joinedCounts(LocalDateTime now) {
        List<Object[]> rows = em.createQuery(
                        "select pm.party.partyId, count(pm) from PartyMember pm join pm.party p" +
                        " where pm.status = :joined and " + VISIBLE + " group by pm.party.partyId", Object[].class)
                .setParameter("joined", PartyMemberStatus.JOINED)
                .setParameter("recruiting", PartyStatus.RECRUITING)
                .setParameter("full", PartyStatus.FULL)
                .setParameter("now", now)
                .getResultList();
        Map<Long, Integer> out = new HashMap<>();
        for (Object[] r : rows) out.put((Long) r[0], ((Number) r[1]).intValue());
        return out;
    }

    /** 목록에 보이는 파티들의 테마 장르 이름. themeId → 장르 이름들 (장르 번호순) */
    public Map<Long, List<String>> genreNames(LocalDateTime now) {
        List<Object[]> rows = em.createQuery(
                        "select tg.theme.themeId, g.name from ThemeGenre tg join tg.genre g" +
                        " where tg.theme.themeId in (select p.theme.themeId from Party p where " + VISIBLE + ")" +
                        " order by tg.theme.themeId, g.genreId", Object[].class)
                .setParameter("recruiting", PartyStatus.RECRUITING)
                .setParameter("full", PartyStatus.FULL)
                .setParameter("now", now)
                .getResultList();
        Map<Long, List<String>> out = new HashMap<>();
        for (Object[] r : rows) out.computeIfAbsent((Long) r[0], k -> new ArrayList<>()).add((String) r[1]);
        return out;
    }

    // ---------------------------------------------------------------- A-22 내 채팅 목록

    /** 내가 JOINED 인 파티 (A-22) 조건. 삭제된 파티와 강퇴된 파티는 빠진다. :me 를 바인딩 */
    private static final String MY_PARTY_IDS =
            "select x.party.partyId from PartyMember x where x.member.memberId = :me and x.status = :joined" +
            " and x.party.status <> :deleted";

    /** 내가 JOINED 인 파티 (삭제된 것 제외). 테마까지 한 번에 */
    public List<Party> findMyParties(Long memberId) {
        return em.createQuery("select p from Party p join fetch p.theme where p.partyId in (" + MY_PARTY_IDS + ")", Party.class)
                .setParameter("me", memberId)
                .setParameter("joined", PartyMemberStatus.JOINED)
                .setParameter("deleted", PartyStatus.DELETED)
                .getResultList();
    }

    /** 내 파티들의 현재 인원 (JOINED 수). partyId → 인원 */
    public Map<Long, Integer> joinedCountsOfMyParties(Long memberId) {
        List<Object[]> rows = em.createQuery(
                        "select pm.party.partyId, count(pm) from PartyMember pm" +
                        " where pm.status = :joined and pm.party.partyId in (" + MY_PARTY_IDS + ")" +
                        " group by pm.party.partyId", Object[].class)
                .setParameter("me", memberId)
                .setParameter("joined", PartyMemberStatus.JOINED)
                .setParameter("deleted", PartyStatus.DELETED)
                .getResultList();
        Map<Long, Integer> out = new HashMap<>();
        for (Object[] r : rows) out.put((Long) r[0], ((Number) r[1]).intValue());
        return out;
    }

    /** 내 파티들의 마지막 메시지 (번호가 가장 큰 것). 보낸 사람까지 한 번에. partyId → 메시지 */
    public Map<Long, PartyMessage> lastMessagesOfMyParties(Long memberId) {
        List<PartyMessage> rows = em.createQuery(
                        "select m from PartyMessage m join fetch m.member where m.messageId in (" +
                        " select max(m2.messageId) from PartyMessage m2 where m2.party.partyId in (" + MY_PARTY_IDS + ")" +
                        " group by m2.party.partyId)", PartyMessage.class)
                .setParameter("me", memberId)
                .setParameter("joined", PartyMemberStatus.JOINED)
                .setParameter("deleted", PartyStatus.DELETED)
                .getResultList();
        Map<Long, PartyMessage> out = new HashMap<>();
        for (PartyMessage m : rows) out.put(m.getParty().getPartyId(), m);
        return out;
    }

    /** 내 참여 상태 (JOINED / KICKED). partyId → 상태. 나간 파티는 줄이 없어서 안 나옴 */
    public Map<Long, PartyMemberStatus> myStatuses(Long memberId) {
        List<Object[]> rows = em.createQuery(
                        "select pm.party.partyId, pm.status from PartyMember pm where pm.member.memberId = :me", Object[].class)
                .setParameter("me", memberId)
                .getResultList();
        Map<Long, PartyMemberStatus> out = new HashMap<>();
        for (Object[] r : rows) out.put((Long) r[0], (PartyMemberStatus) r[1]);
        return out;
    }
}

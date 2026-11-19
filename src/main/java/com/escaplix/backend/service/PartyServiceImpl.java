package com.escaplix.backend.service;

import com.escaplix.backend.common.*;
import com.escaplix.backend.common.auth.*;
import com.escaplix.backend.dto.party.*;
import com.escaplix.backend.entity.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import com.escaplix.backend.common.time.DayTypeResolver;
import com.escaplix.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * PartyService 구현 (A 담당).
 * 조회는 클래스의 readOnly 트랜잭션, 저장·수정·삭제 메서드에는 @Transactional 이 붙어 있다.
 * 인원·상태를 바꾸는 API(참여·나가기·강퇴·마감·삭제)는 lockParty 로 PARTY 행을 잠그고 시작한다 (API.md A 공통 규칙).
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PartyServiceImpl implements PartyService {

    private static final DateTimeFormatter HH_MM = DateTimeFormatter.ofPattern("HH:mm");
    private static final String NO_PARTY = "없는 파티입니다.";

    private final PartyRepository partyRepository;
    private final PartyMemberRepository partyMemberRepository;
    private final PartyMessageRepository partyMessageRepository;
    private final PartyBrowseRepository partyBrowseRepository;
    private final ThemeRepository themeRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final MemberRepository memberRepository;
    private final DayTypeResolver dayTypeResolver;

    // ================================================================ A-12 ~ A-19

    @Override
    public List<PartyListItem> findParties(LoginMember login, PartySearchCondition cond) {
        LocalDateTime now = LocalDateTime.now();
        List<Party> parties = partyBrowseRepository.findRecruiting(now,
                ThemeBrowseRepository.likePattern(cond.keyword()), cond.genreIds(), cond.themeId());
        if (parties.isEmpty()) return List.of();

        Map<Long, Integer> counts = partyBrowseRepository.joinedCounts(now);
        Map<Long, List<String>> genres = partyBrowseRepository.genreNames(now);
        Map<Long, PartyMemberStatus> mine = partyBrowseRepository.myStatuses(login.memberId());

        return parties.stream().map(p -> {
            Theme t = p.getTheme();
            PartyMemberStatus my = mine.get(p.getPartyId());
            return new PartyListItem(p.getPartyId(), p.getTitle(), p.getIntro(),
                    t.getThemeId(), t.getTitle(), t.getStore().getName(),
                    genres.getOrDefault(t.getThemeId(), List.of()),
                    p.getPlayAt(), p.getDeadlineAt(),
                    counts.getOrDefault(p.getPartyId(), 0), p.getMaxPeople(),
                    p.getLeader().getNickname(), p.getStatus(),
                    my == PartyMemberStatus.JOINED, my == PartyMemberStatus.KICKED);
        }).toList();
    }

    @Override
    public PartyDetailResponse getParty(LoginMember login, Long partyId) {
        Party party = partyRepository.findById(partyId)
                .filter(p -> p.getStatus() != PartyStatus.DELETED)
                .orElseThrow(() -> ApiException.notFound(NO_PARTY));
        if (!isJoined(partyId, login.memberId())) {
            throw ApiException.forbidden("파티에 참여한 사람만 볼 수 있습니다.");
        }

        Long leaderId = party.getLeader().getMemberId();
        List<PartyDetailResponse.Participant> members = partyMemberRepository
                .findWithMember(partyId, PartyMemberStatus.JOINED).stream()
                .map(pm -> new PartyDetailResponse.Participant(pm.getMember().getMemberId(), pm.getMember().getNickname(),
                        pm.getMember().getMemberId().equals(leaderId), pm.getJoinedAt()))
                .toList();

        Theme t = party.getTheme();
        return new PartyDetailResponse(party.getPartyId(), party.getTitle(), party.getIntro(), party.getStatus(),
                new PartyDetailResponse.ThemeBrief(t.getThemeId(), t.getTitle(), t.getStore().getName(), t.getPlayMinutes()),
                party.getPlayAt(), party.getDeadlineAt(), party.getMaxPeople(), members.size(), members,
                leaderId.equals(login.memberId()) ? PartyRole.LEADER : PartyRole.MEMBER);
    }

    @Override
    @Transactional
    public PartyIdResponse createParty(LoginMember login, PartyCreateRequest req) {
        // 1) 공개된 테마인지 (테마 OPEN + 매장 APPROVED)
        Theme theme = themeRepository.findById(req.themeId())
                .filter(t -> t.getStatus() == ThemeStatus.OPEN && t.getStore().getStatus() == StoreStatus.APPROVED)
                .orElseThrow(() -> ApiException.notFound("없는 테마입니다."));

        // 2) 값 검사 (지금 이후인지는 DTO 의 @Future 가 봄)
        if (req.deadlineAt().isAfter(req.playAt())) {
            throw ApiException.badRequest("모집 기한은 플레이 시간 전이어야 합니다.");
        }
        int min = Math.max(2, theme.getMinPeople());
        int max = theme.getMaxPeople();
        if (req.maxPeople() < min || req.maxPeople() > max) {
            throw ApiException.badRequest("모집 인원은 " + min + "~" + max + "명 사이로 정해 주세요.");
        }
        // 시간표: 그날 유형(주중/주말·공휴일)에 그 시각("HH:mm")이 있어야 함. 초가 붙은 시각은 시간표에 없음
        LocalDateTime playAt = req.playAt();
        DayType dayType = dayTypeResolver.of(playAt.toLocalDate());
        if (playAt.getSecond() != 0 || playAt.getNano() != 0
                || !timeSlotRepository.existsByTheme_ThemeIdAndDayTypeAndStartTime(theme.getThemeId(), dayType, playAt.format(HH_MM))) {
            throw ApiException.badRequest("그 시간에는 이 테마 시간표가 없습니다.");
        }

        // 3) 한 트랜잭션: PARTY + 방장 PARTY_MEMBER(JOINED) + 안내 줄(JOIN)
        Member leader = me(login);
        Party party = new Party();
        party.setTheme(theme);
        party.setLeader(leader);
        party.setTitle(req.title().strip());
        party.setIntro(req.intro().strip());
        party.setPlayAt(playAt);
        party.setDeadlineAt(req.deadlineAt());
        party.setMaxPeople(req.maxPeople());
        party.setStatus(PartyStatus.RECRUITING);
        partyRepository.save(party);

        addMember(party, leader);
        notice(party, leader, MessageType.JOIN);
        return new PartyIdResponse(party.getPartyId());
    }

    @Override
    @Transactional
    public void deleteParty(LoginMember login, Long partyId) {
        Party party = lockParty(partyId);
        requireLeader(party, login);
        party.setStatus(PartyStatus.DELETED);   // 실제로 지우지 않음 (결정 P1)
    }

    @Override
    @Transactional
    public PartyJoinResponse join(LoginMember login, Long partyId) {
        Party party = lockParty(partyId);

        // 내 줄 먼저: 이미 들어온 사람에게 "정원이 다 찼습니다" 대신 정확한 이유를 알려 주려고
        partyMemberRepository.findByParty_PartyIdAndMember_MemberId(partyId, login.memberId()).ifPresent(pm -> {
            throw ApiException.conflict(pm.getStatus() == PartyMemberStatus.KICKED
                    ? "강퇴된 파티에는 다시 참여할 수 없습니다."
                    : "이미 참여한 파티입니다.");
        });

        if (party.getStatus() == PartyStatus.CLOSED || !party.getDeadlineAt().isAfter(LocalDateTime.now())) {
            throw ApiException.conflict("모집이 끝난 파티입니다.");
        }
        long count = countJoined(partyId);
        if (party.getStatus() == PartyStatus.FULL || count >= party.getMaxPeople()) {
            throw ApiException.conflict("정원이 다 찼습니다.");
        }

        Member member = me(login);
        addMember(party, member);
        notice(party, member, MessageType.JOIN);
        int current = (int) count + 1;
        if (current >= party.getMaxPeople()) {
            party.setStatus(PartyStatus.FULL);
        }
        return new PartyJoinResponse(partyId, current, party.getMaxPeople(), party.getStatus());
    }

    @Override
    @Transactional
    public void leave(LoginMember login, Long partyId) {
        Party party = lockParty(partyId);
        if (isLeader(party, login)) {
            throw ApiException.badRequest("방장은 나갈 수 없습니다. 파티를 삭제해 주세요.");
        }
        PartyMember row = partyMemberRepository.findByParty_PartyIdAndMember_MemberId(partyId, login.memberId())
                .filter(pm -> pm.getStatus() == PartyMemberStatus.JOINED)
                .orElseThrow(() -> ApiException.notFound("참여하지 않은 파티입니다."));

        partyMemberRepository.delete(row);   // 줄을 지워서 나중에 다시 참여할 수 있음
        notice(party, row.getMember(), MessageType.LEAVE);
        reopenIfFull(party);
    }

    @Override
    @Transactional
    public void kick(LoginMember login, Long partyId, Long memberId) {
        Party party = lockParty(partyId);
        requireLeader(party, login);
        if (memberId.equals(login.memberId())) {
            throw ApiException.badRequest("자기 자신은 강퇴할 수 없습니다.");
        }
        PartyMember row = partyMemberRepository.findByParty_PartyIdAndMember_MemberId(partyId, memberId)
                .filter(pm -> pm.getStatus() == PartyMemberStatus.JOINED)
                .orElseThrow(() -> ApiException.notFound("파티에 참여 중인 사람이 아닙니다."));

        row.setStatus(PartyMemberStatus.KICKED);   // 줄을 남겨서 다시 참여 못 하게 (결정 P2)
        notice(party, row.getMember(), MessageType.KICK);
        reopenIfFull(party);
    }

    @Override
    @Transactional
    public PartyStatusResponse close(LoginMember login, Long partyId) {
        Party party = lockParty(partyId);
        requireLeader(party, login);
        if (party.getStatus() == PartyStatus.CLOSED) {
            throw ApiException.conflict("이미 마감된 파티입니다.");
        }
        party.setStatus(PartyStatus.CLOSED);
        return new PartyStatusResponse(partyId, PartyStatus.CLOSED);
    }

    // ================================================================ A-20 ~ A-22

    @Override
    public List<MessageResponse> getMessages(LoginMember login, Long partyId, Long after) {
        // TODO(A) 내가 JOINED 아니면 403 (강퇴 직후 포함), DELETED 면 404
        // TODO(A) after 없으면 최근 50개(뒤집어서 오름차순), 있으면 그다음 최대 100개
        // TODO(A) nickname 은 member 에서 (JOIN), mine = 내 memberId
        throw ApiException.notImplemented("A-20 새 채팅 받기");
    }

    @Override
    @Transactional
    public MessageResponse sendMessage(LoginMember login, Long partyId, MessageSendRequest req) {
        // TODO(A) 내가 JOINED 아니면 403. CLOSED 파티도 채팅 가능
        // TODO(A) content.strip() 이 비면 400
        // TODO(A) PARTY_MESSAGE(CHAT) 저장
        throw ApiException.notImplemented("A-21 채팅 보내기");
    }

    @Override
    public List<MyPartyItem> getMyParties(LoginMember login) {
        // TODO(A) 내가 JOINED 인 파티 (DELETED 제외)
        // TODO(A) 마지막 메시지 시각 최신순, ended = play_at 지남
        throw ApiException.notImplemented("A-22 내 채팅(내 파티) 목록");
    }

    // ================================================================ 도우미

    /** PARTY 행을 잠그고 읽는다 (SELECT ... FOR UPDATE). 없거나 DELETED 면 404 */
    private Party lockParty(Long partyId) {
        return partyRepository.findByIdForUpdate(partyId)
                .filter(p -> p.getStatus() != PartyStatus.DELETED)
                .orElseThrow(() -> ApiException.notFound(NO_PARTY));
    }

    private boolean isLeader(Party party, LoginMember login) {
        return party.getLeader().getMemberId().equals(login.memberId());
    }

    private void requireLeader(Party party, LoginMember login) {
        if (!isLeader(party, login)) {
            throw ApiException.forbidden("방장만 할 수 있습니다.");
        }
    }

    private boolean isJoined(Long partyId, Long memberId) {
        return partyMemberRepository.findByParty_PartyIdAndMember_MemberId(partyId, memberId)
                .map(pm -> pm.getStatus() == PartyMemberStatus.JOINED)
                .orElse(false);
    }

    private long countJoined(Long partyId) {
        return partyMemberRepository.countByParty_PartyIdAndStatus(partyId, PartyMemberStatus.JOINED);
    }

    /** 토큰의 회원이 DB 에 없으면 (탈퇴 등) 401 */
    private Member me(LoginMember login) {
        return memberRepository.findById(login.memberId())
                .orElseThrow(() -> ApiException.unauthorized("다시 로그인해 주세요."));
    }

    private void addMember(Party party, Member member) {
        PartyMember pm = new PartyMember();
        pm.setParty(party);
        pm.setMember(member);
        pm.setStatus(PartyMemberStatus.JOINED);
        partyMemberRepository.save(pm);
    }

    /** 채팅 안내 줄 (JOIN / LEAVE / KICK). 내용 없이 대상만 (결정 P3) */
    private void notice(Party party, Member target, MessageType type) {
        PartyMessage m = new PartyMessage();
        m.setParty(party);
        m.setMember(target);
        m.setMsgType(type);
        partyMessageRepository.save(m);
    }

    /** 나가기·강퇴로 자리가 났을 때: FULL 이었고 기한 전이면 다시 RECRUITING */
    private void reopenIfFull(Party party) {
        if (party.getStatus() == PartyStatus.FULL && party.getDeadlineAt().isAfter(LocalDateTime.now())) {
            party.setStatus(PartyStatus.RECRUITING);
        }
    }
}

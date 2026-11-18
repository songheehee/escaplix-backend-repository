package com.escaplix.backend.service;

import com.escaplix.backend.common.*;
import com.escaplix.backend.common.auth.*;
import com.escaplix.backend.dto.party.*;

import java.util.List;

import com.escaplix.backend.common.time.DayTypeResolver;
import com.escaplix.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * PartyService 구현 (A 담당). 지금은 전부 501 "아직 구현 전". TODO 를 채우고 throw 줄을 지운다.
 * 조회는 클래스의 readOnly 트랜잭션, 저장·수정·삭제 메서드에는 @Transactional 이 붙어 있다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PartyServiceImpl implements PartyService {

    private final PartyRepository partyRepository;
    private final PartyMemberRepository partyMemberRepository;
    private final PartyMessageRepository partyMessageRepository;
    private final ThemeRepository themeRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final MemberRepository memberRepository;
    private final DayTypeResolver dayTypeResolver;

    @Override
    public List<PartyListItem> findParties(LoginMember login, PartySearchCondition cond) {
        // TODO(A) status=RECRUITING AND deadline_at > SYSDATE 만, play_at 오름차순
        // TODO(A) genreIds 는 하나라도 맞으면 (THEME_GENRE 에 EXISTS)
        // TODO(A) keyword 는 테마 이름 일부, 띄어쓰기 무시
        // TODO(A) joined/kicked 는 내 PARTY_MEMBER 상태로
        throw ApiException.notImplemented("A-12 파티 목록 (모집 중만)");
    }

    @Override
    public PartyDetailResponse getParty(LoginMember login, Long partyId) {
        // TODO(A) 없거나 DELETED 면 404, 내가 JOINED 아니면 403
        // TODO(A) members 는 JOINED 만, myRole = 방장이면 LEADER
        throw ApiException.notImplemented("A-13 파티 방 정보");
    }

    @Override
    @Transactional
    public PartyIdResponse createParty(LoginMember login, PartyCreateRequest req) {
        // TODO(A) 테마 OPEN + 매장 APPROVED 아니면 404
        // TODO(A) deadlineAt ≤ playAt, maxPeople 은 테마 인원 범위 → 아니면 400
        // TODO(A) dayTypeResolver.of(playAt 날짜) 로 주중/주말 → TIME_SLOT 에 그 시각("HH:mm")이 없으면 400
        // TODO(A) PARTY + PARTY_MEMBER(방장, JOINED) + PARTY_MESSAGE(JOIN) 한 트랜잭션
        throw ApiException.notImplemented("A-14 파티 만들기");
    }

    @Override
    @Transactional
    public void deleteParty(LoginMember login, Long partyId) {
        // TODO(A) partyRepository.findByIdForUpdate 로 잠그고 읽기
        // TODO(A) 방장 아니면 403, 이미 DELETED 면 404
        // TODO(A) status = DELETED (실제로 지우지 않음, 결정 P1)
        throw ApiException.notImplemented("A-15 파티 삭제");
    }

    @Override
    @Transactional
    public PartyJoinResponse join(LoginMember login, Long partyId) {
        // TODO(A) findByIdForUpdate 로 잠그기. DELETED 면 404
        // TODO(A) RECRUITING 이고 기한 전인지 → 아니면 409
        // TODO(A) 내 줄이 JOINED 면 409 "이미 참여", KICKED 면 409 "강퇴된 파티에는 다시 참여할 수 없습니다."
        // TODO(A) JOINED 수 < 정원 확인 → PARTY_MEMBER(JOINED) + 안내 줄(JOIN) → 정원 차면 FULL
        throw ApiException.notImplemented("A-16 참여");
    }

    @Override
    @Transactional
    public void leave(LoginMember login, Long partyId) {
        // TODO(A) 잠그기. 방장이면 400 "방장은 나갈 수 없습니다. 파티를 삭제해 주세요."
        // TODO(A) 내 JOINED 줄 삭제(재참여 가능) + 안내 줄(LEAVE)
        // TODO(A) FULL 이었고 기한 전이면 RECRUITING
        throw ApiException.notImplemented("A-17 나가기");
    }

    @Override
    @Transactional
    public void kick(LoginMember login, Long partyId, Long memberId) {
        // TODO(A) 잠그기. 방장 아니면 403, 자기 자신이면 400, 대상이 JOINED 아니면 404
        // TODO(A) 대상 줄을 지우지 않고 status = KICKED (재참여 불가, 결정 P2) + 안내 줄(KICK)
        // TODO(A) FULL 이었고 기한 전이면 RECRUITING
        throw ApiException.notImplemented("A-18 강퇴");
    }

    @Override
    @Transactional
    public PartyStatusResponse close(LoginMember login, Long partyId) {
        // TODO(A) 잠그기. 방장 아니면 403
        // TODO(A) RECRUITING/FULL → CLOSED. 이미 CLOSED 면 409
        throw ApiException.notImplemented("A-19 모집 마감");
    }

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
}

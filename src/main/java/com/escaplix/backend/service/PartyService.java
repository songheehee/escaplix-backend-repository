package com.escaplix.backend.service;

import com.escaplix.backend.common.*;
import com.escaplix.backend.common.auth.*;
import com.escaplix.backend.common.file.FileDownload;
import com.escaplix.backend.dto.admin.*;
import com.escaplix.backend.dto.member.*;
import com.escaplix.backend.dto.owner.*;
import com.escaplix.backend.dto.party.*;
import com.escaplix.backend.dto.review.*;
import com.escaplix.backend.dto.store.*;
import com.escaplix.backend.dto.theme.*;
import com.escaplix.backend.entity.*;
import java.util.List;

/** A 담당 · 파티 (A-12 ~ A-22). 명세: docs/API.md (목업 레포) */
public interface PartyService {

    /** A-12 파티 목록 (모집 중만) */
    List<PartyListItem> findParties(LoginMember login, PartySearchCondition cond);

    /** A-13 파티 방 정보 */
    PartyDetailResponse getParty(LoginMember login, Long partyId);

    /** A-14 파티 만들기 */
    PartyIdResponse createParty(LoginMember login, PartyCreateRequest req);

    /** A-15 파티 삭제 */
    void deleteParty(LoginMember login, Long partyId);

    /** A-16 참여 */
    PartyJoinResponse join(LoginMember login, Long partyId);

    /** A-17 나가기 */
    void leave(LoginMember login, Long partyId);

    /**
     * 회원 탈퇴 때 (MemberService 가 부름). 아직 안 끝난 파티만 정리하고 끝난 파티 기록은 그대로 둔다
     *  - 내가 방장: 삭제 (DELETED)
     *  - 내가 참여자: 나가기 (나가기 안내 줄, 정원 마감이었으면 다시 모집 중)
     */
    void cleanUpForWithdrawal(Long memberId);

    /** A-18 강퇴 */
    void kick(LoginMember login, Long partyId, Long memberId);

    /** A-19 모집 마감 */
    PartyStatusResponse close(LoginMember login, Long partyId);

    /** A-20 새 채팅 받기 */
    List<MessageResponse> getMessages(LoginMember login, Long partyId, Long after);

    /** A-21 채팅 보내기 */
    MessageResponse sendMessage(LoginMember login, Long partyId, MessageSendRequest req);

    /** A-22 내 채팅(내 파티) 목록 */
    List<MyPartyItem> getMyParties(LoginMember login);
}

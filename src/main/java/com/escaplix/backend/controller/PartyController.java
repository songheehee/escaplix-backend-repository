package com.escaplix.backend.controller;

import com.escaplix.backend.common.auth.*;
import com.escaplix.backend.dto.party.*;
import com.escaplix.backend.entity.*;
import com.escaplix.backend.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * A · 파티 (A-12 ~ A-22)
 * 컨트롤러는 받고 넘기기만 한다: 검사·권한 확인·트랜잭션은 서비스에서. 응답은 DTO 만 (엔티티 금지).
 */
@RestController
@RequiredArgsConstructor
public class PartyController {

    private final PartyService partyService;

    /** A-12 파티 목록 (모집 중만) · 로그인 · 200 */
    @GetMapping("/api/parties")
    @Auth
    public List<PartyListItem> findParties(LoginMember login, @ModelAttribute PartySearchCondition cond) {
        return partyService.findParties(login, cond);
    }

    /** A-13 파티 방 정보 · USER · 200 */
    @GetMapping("/api/parties/{partyId}")
    @Auth(Role.USER)
    public PartyDetailResponse getParty(LoginMember login, @PathVariable Long partyId) {
        return partyService.getParty(login, partyId);
    }

    /** A-14 파티 만들기 · USER · 201 */
    @PostMapping("/api/parties")
    @ResponseStatus(HttpStatus.CREATED)
    @Auth(Role.USER)
    public PartyIdResponse createParty(LoginMember login, @Valid @RequestBody PartyCreateRequest req) {
        return partyService.createParty(login, req);
    }

    /** A-15 파티 삭제 · USER · 204 */
    @DeleteMapping("/api/parties/{partyId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Auth(Role.USER)
    public void deleteParty(LoginMember login, @PathVariable Long partyId) {
        partyService.deleteParty(login, partyId);
    }

    /** A-16 참여 · USER · 201 */
    @PostMapping("/api/parties/{partyId}/join")
    @ResponseStatus(HttpStatus.CREATED)
    @Auth(Role.USER)
    public PartyJoinResponse join(LoginMember login, @PathVariable Long partyId) {
        return partyService.join(login, partyId);
    }

    /** A-17 나가기 · USER · 204 */
    @DeleteMapping("/api/parties/{partyId}/join")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Auth(Role.USER)
    public void leave(LoginMember login, @PathVariable Long partyId) {
        partyService.leave(login, partyId);
    }

    /** A-18 강퇴 · USER · 204 */
    @DeleteMapping("/api/parties/{partyId}/members/{memberId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Auth(Role.USER)
    public void kick(LoginMember login, @PathVariable Long partyId, @PathVariable Long memberId) {
        partyService.kick(login, partyId, memberId);
    }

    /** A-19 모집 마감 · USER · 200 */
    @PutMapping("/api/parties/{partyId}/close")
    @Auth(Role.USER)
    public PartyStatusResponse close(LoginMember login, @PathVariable Long partyId) {
        return partyService.close(login, partyId);
    }

    /** A-20 새 채팅 받기 · USER · 200 */
    @GetMapping("/api/parties/{partyId}/messages")
    @Auth(Role.USER)
    public List<MessageResponse> getMessages(LoginMember login, @PathVariable Long partyId, @RequestParam(required = false) Long after) {
        return partyService.getMessages(login, partyId, after);
    }

    /** A-21 채팅 보내기 · USER · 201 */
    @PostMapping("/api/parties/{partyId}/messages")
    @ResponseStatus(HttpStatus.CREATED)
    @Auth(Role.USER)
    public MessageResponse sendMessage(LoginMember login, @PathVariable Long partyId, @Valid @RequestBody MessageSendRequest req) {
        return partyService.sendMessage(login, partyId, req);
    }

    /** A-22 내 채팅(내 파티) 목록 · USER · 200 */
    @GetMapping("/api/my/parties")
    @Auth(Role.USER)
    public List<MyPartyItem> getMyParties(LoginMember login) {
        return partyService.getMyParties(login);
    }
}

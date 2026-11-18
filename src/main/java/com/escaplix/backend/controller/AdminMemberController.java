package com.escaplix.backend.controller;

import com.escaplix.backend.common.auth.*;
import com.escaplix.backend.common.file.FileDownload;
import com.escaplix.backend.dto.member.*;
import com.escaplix.backend.entity.*;
import com.escaplix.backend.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * A · 관리자 회원 관리 (A-10, A-11)
 * 컨트롤러는 받고 넘기기만 한다: 검사·권한 확인·트랜잭션은 서비스에서. 응답은 DTO 만 (엔티티 금지).
 */
@RestController
@RequiredArgsConstructor
public class AdminMemberController {

    private final MemberService memberService;

    /** A-10 회원 목록 (관리자) · ADMIN · 200 */
    @GetMapping("/api/admin/members")
    @Auth(Role.ADMIN)
    public AdminMemberListResponse findMembers(@RequestParam(required = false) String keyword, @RequestParam(required = false) Role role) {
        return memberService.findMembers(keyword, role);
    }

    /** A-11 사업자등록증 이미지 (관리자) · ADMIN · 200 */
    @GetMapping("/api/admin/members/{memberId}/biz-file")
    @Auth(Role.ADMIN)
    public ResponseEntity<Resource> getBizFile(@PathVariable Long memberId) {
        FileDownload file = memberService.getBizFile(memberId);
        return ResponseEntity.ok().contentType(MediaType.parseMediaType(file.contentType())).body(file.resource());
    }
}

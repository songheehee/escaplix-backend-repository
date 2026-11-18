package com.escaplix.backend.dto.member;

import com.escaplix.backend.entity.*;
import java.util.List;
import java.util.Map;

/** A-10 회원 목록. countByRole 은 검색 조건과 상관없이 전체 수 */
public record AdminMemberListResponse(
        long total,
        Map<Role, Long> countByRole,
        List<AdminMemberItem> members
) {}

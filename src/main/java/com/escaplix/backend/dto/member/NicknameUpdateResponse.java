package com.escaplix.backend.dto.member;


/** A-8 응답. 토큰에 닉네임이 들어 있어서 새 토큰을 준다 */
public record NicknameUpdateResponse(
        String nickname,
        String token
) {}

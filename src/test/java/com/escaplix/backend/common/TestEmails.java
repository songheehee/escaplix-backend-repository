package com.escaplix.backend.common;

import com.escaplix.backend.common.mail.EmailCodeStore;
import com.escaplix.backend.common.mail.EmailCodeStore.Purpose;

/**
 * 테스트 도우미: 가입 전에 이메일 인증을 마친 상태로 만든다 (메일 없이 보관소에 바로).
 * 인증 흐름 자체(API·메일 본문·틀림·만료)는 member/SignupEmailTests 에서 따로 본다
 */
public final class TestEmails {

    private TestEmails() {}

    /** email 을 "인증 완료"로 만들고 그대로 돌려준다 (가입 요청 JSON 에 넣을 값) */
    public static String verified(EmailCodeStore store, String email) {
        String code = store.issue(Purpose.SIGNUP, email);
        store.verify(Purpose.SIGNUP, email, code);
        return email;
    }
}

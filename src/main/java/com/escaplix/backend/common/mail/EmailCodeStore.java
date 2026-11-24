package com.escaplix.backend.common.mail;

import com.escaplix.backend.common.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 이메일 인증번호 보관 (메모리). 서버가 한 대라 DB 테이블 없이 충분하다. 서버를 다시 켜면 진행 중인 인증은 처음부터.
 * 규칙: 6자리 숫자 · 5분 유효 · 5번 틀리면 무효 · 다시 받기는 60초 뒤부터 · 맞히면 30분 안에 가입(또는 재설정)을 끝내야 함
 * 키는 (용도, 이메일). 같은 이메일이라도 가입용 인증과 비밀번호 재설정용 인증은 따로 센다.
 */
@Component
public class EmailCodeStore {

    /** 인증 용도 */
    public enum Purpose { SIGNUP, PASSWORD_RESET }

    static final Duration CODE_TTL = Duration.ofMinutes(5);
    static final Duration RESEND_AFTER = Duration.ofSeconds(60);
    static final Duration VERIFIED_TTL = Duration.ofMinutes(30);
    static final int MAX_TRIES = 5;

    private static final SecureRandom RANDOM = new SecureRandom();

    private final Map<String, Entry> entries = new ConcurrentHashMap<>();
    private Clock clock = Clock.systemDefaultZone();

    private static final class Entry {
        final String code;
        final Instant sentAt;
        int wrong;
        Instant verifiedAt;   // null 이면 아직 못 맞힘
        Entry(String code, Instant sentAt) { this.code = code; this.sentAt = sentAt; }
    }

    /** 테스트에서 시간을 옮길 때만 */
    void setClock(Clock clock) { this.clock = clock; }

    /**
     * 새 인증번호를 만들어 돌려준다 (보내기는 부른 쪽이 Mailer 로). 이전 번호는 무효.
     * 60초 안에 다시 요청하면 429
     */
    public String issue(Purpose purpose, String email) {
        Instant now = clock.instant();
        cleanUp(now);
        String key = key(purpose, email);
        Entry old = entries.get(key);
        if (old != null && old.verifiedAt == null && now.isBefore(old.sentAt.plus(RESEND_AFTER))) {
            long wait = (Duration.between(now, old.sentAt.plus(RESEND_AFTER)).toMillis() + 999) / 1000;   // 올림 (0.2초 남으면 1초)
            throw new ApiException(HttpStatus.TOO_MANY_REQUESTS, "인증번호는 " + wait + "초 뒤에 다시 받을 수 있어요.");
        }
        String code = String.format("%06d", RANDOM.nextInt(1_000_000));
        entries.put(key, new Entry(code, now));
        return code;
    }

    /** 인증번호 확인. 맞으면 "인증됨"으로 표시 (30분 유효). 틀리거나 만료면 400 */
    public void verify(Purpose purpose, String email, String code) {
        Instant now = clock.instant();
        String key = key(purpose, email);
        Entry e = entries.get(key);
        if (e == null || now.isAfter(e.sentAt.plus(CODE_TTL))) {
            entries.remove(key);
            throw ApiException.badRequest("인증번호가 만료됐어요. 인증번호를 다시 받아 주세요.");
        }
        if (e.verifiedAt != null) return;   // 이미 맞힘 (확인 버튼 두 번)
        synchronized (e) {
            if (!e.code.equals(code == null ? "" : code.trim())) {
                e.wrong++;
                if (e.wrong >= MAX_TRIES) {
                    entries.remove(key);
                    throw ApiException.badRequest("인증번호를 " + MAX_TRIES + "번 틀렸어요. 인증번호를 다시 받아 주세요.");
                }
                throw ApiException.badRequest("인증번호가 맞지 않아요. (" + e.wrong + "/" + MAX_TRIES + ")");
            }
            e.verifiedAt = now;
        }
    }

    /** 인증을 마친 이메일인지 (가입·재설정 직전에 확인). 다 쓰면 consume 으로 지운다 */
    public boolean isVerified(Purpose purpose, String email) {
        Entry e = entries.get(key(purpose, email));
        return e != null && e.verifiedAt != null && clock.instant().isBefore(e.verifiedAt.plus(VERIFIED_TTL));
    }

    /** 인증 기록을 지운다 (가입·재설정을 마친 뒤: 같은 인증으로 두 번 못 쓰게) */
    public void consume(Purpose purpose, String email) {
        entries.remove(key(purpose, email));
    }

    private static String key(Purpose purpose, String email) {
        return purpose + ":" + email;
    }

    /** 오래된 기록 정리 (요청이 올 때마다 한 번. 몇 개 안 되니 다 훑어도 됨) */
    private void cleanUp(Instant now) {
        entries.values().removeIf(e -> e.verifiedAt != null
                ? now.isAfter(e.verifiedAt.plus(VERIFIED_TTL))
                : now.isAfter(e.sentAt.plus(CODE_TTL)));
    }
}

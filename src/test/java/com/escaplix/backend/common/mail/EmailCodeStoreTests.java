package com.escaplix.backend.common.mail;

import com.escaplix.backend.common.ApiException;
import com.escaplix.backend.common.mail.EmailCodeStore.Purpose;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** 인증번호 보관소의 시간 규칙 (스프링 없이). 시계를 직접 옮긴다 */
class EmailCodeStoreTests {

    static final String EMAIL = "clock@test.com";

    EmailCodeStore store;
    Instant now;

    @BeforeEach
    void setUp() {
        store = new EmailCodeStore();
        now = Instant.parse("2026-11-24T00:00:00Z");
        tick(Duration.ZERO);
    }

    private void tick(Duration d) {
        now = now.plus(d);
        store.setClock(Clock.fixed(now, ZoneId.of("Asia/Seoul")));
    }

    @Test
    void 인증번호는_6자리_숫자() {
        assertThat(store.issue(Purpose.SIGNUP, EMAIL)).matches("\\d{6}");
    }

    @Test
    void 다시_받기는_60초_뒤부터_이전_번호는_무효() {
        String first = store.issue(Purpose.SIGNUP, EMAIL);
        tick(Duration.ofSeconds(59));
        assertThatThrownBy(() -> store.issue(Purpose.SIGNUP, EMAIL)).isInstanceOf(ApiException.class)
                .hasMessage("인증번호는 1초 뒤에 다시 받을 수 있어요.");
        tick(Duration.ofSeconds(1));
        String second = store.issue(Purpose.SIGNUP, EMAIL);
        if (!first.equals(second)) {   // 우연히 같은 번호가 나오면 이 확인은 건너뜀
            assertThatThrownBy(() -> store.verify(Purpose.SIGNUP, EMAIL, first)).isInstanceOf(ApiException.class);
        }
        store.verify(Purpose.SIGNUP, EMAIL, second);
        assertThat(store.isVerified(Purpose.SIGNUP, EMAIL)).isTrue();
    }

    @Test
    void 인증번호는_5분_지나면_만료() {
        String code = store.issue(Purpose.SIGNUP, EMAIL);
        tick(Duration.ofMinutes(5).plusSeconds(1));
        assertThatThrownBy(() -> store.verify(Purpose.SIGNUP, EMAIL, code))
                .hasMessage("인증번호가 만료됐어요. 인증번호를 다시 받아 주세요.");
    }

    @Test
    void 맞힌_뒤_30분이_지나면_인증도_만료() {
        String code = store.issue(Purpose.SIGNUP, EMAIL);
        store.verify(Purpose.SIGNUP, EMAIL, code);
        tick(Duration.ofMinutes(29));
        assertThat(store.isVerified(Purpose.SIGNUP, EMAIL)).isTrue();
        tick(Duration.ofMinutes(2));
        assertThat(store.isVerified(Purpose.SIGNUP, EMAIL)).isFalse();
    }

    @Test
    void 용도가_다르면_따로_센다_다_쓰면_지운다() {
        String code = store.issue(Purpose.SIGNUP, EMAIL);
        store.verify(Purpose.SIGNUP, EMAIL, code);
        assertThat(store.isVerified(Purpose.PASSWORD_RESET, EMAIL)).isFalse();
        store.issue(Purpose.PASSWORD_RESET, EMAIL);   // 가입용을 받은 직후여도 재설정용은 바로 받을 수 있음

        store.consume(Purpose.SIGNUP, EMAIL);
        assertThat(store.isVerified(Purpose.SIGNUP, EMAIL)).isFalse();
    }
}

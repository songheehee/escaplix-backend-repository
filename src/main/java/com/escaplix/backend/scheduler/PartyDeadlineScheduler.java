package com.escaplix.backend.scheduler;

import com.escaplix.backend.entity.PartyStatus;
import com.escaplix.backend.repository.PartyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/** 모집 기한이 지난 RECRUITING 파티를 5분마다 CLOSED 로 (ERD 5-3, A 담당) */
@Slf4j
@Component
@RequiredArgsConstructor
public class PartyDeadlineScheduler {

    private final PartyRepository partyRepository;

    @Scheduled(cron = "0 */5 * * * *")
    @Transactional
    public void closeExpired() {
        int n = partyRepository.updateStatusWhereDeadlinePassed(PartyStatus.RECRUITING, PartyStatus.CLOSED, LocalDateTime.now());
        if (n > 0) {
            log.info("모집 기한 지난 파티 {}개 마감", n);
        }
    }
}

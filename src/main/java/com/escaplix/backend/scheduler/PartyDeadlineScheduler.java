package com.escaplix.backend.scheduler;

import com.escaplix.backend.entity.PartyStatus;
import com.escaplix.backend.repository.PartyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 모집 기한이 지난 RECRUITING·FULL 파티를 CLOSED 로 (ERD 5-3, A 담당). 하루 한 번 새벽 3시
 * 그 사이에도 화면에는 PartyServiceImpl.shownStatus 가 기한 지난 파티를 CLOSED 로 보여 준다
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PartyDeadlineScheduler {

    private final PartyRepository partyRepository;

    @Scheduled(cron = "0 0 3 * * *", zone = "Asia/Seoul") // 새벽 3시에 한번
    @Transactional
    public void closeExpired() {
        LocalDateTime now = LocalDateTime.now();
        int n = partyRepository.updateStatusWhereDeadlinePassed(PartyStatus.RECRUITING, PartyStatus.CLOSED, now)
                + partyRepository.updateStatusWhereDeadlinePassed(PartyStatus.FULL, PartyStatus.CLOSED, now);   // 정원이 차도 기한이 지나면 모집 마감
        if (n > 0) {
            log.info("모집 기한 지난 파티 {}개 마감", n);
        }
    }
}

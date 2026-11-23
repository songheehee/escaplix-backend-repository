package com.escaplix.backend.dto.review;

import java.util.List;

/**
 * D-10 내 통계. 내 후기 전부 기준 (숨김 포함). 비율은 % 반올림, 나눌 수가 0이면 0
 *  - 탈출 성공률   = 탈출 성공 / 플레이
 *  - 노힌트 성공률 = 힌트 0개로 탈출 성공 / 플레이
 *  - 평균 남은 시간 = 탈출 성공한 판의 남은 시간 평균 (실패는 빼고 계산). 성공이 없으면 avgRemainSeconds 는 null
 *    avgRemainRate 는 판마다 (남은 시간 / 제한 시간) 의 평균 % (테마마다 제한 시간이 달라서)
 *  - 전국 제패율   = 플레이한 공개 테마 / 전체 공개 테마 (지역별로도)
 */
public record StatsResponse(
        int playCount,
        int escapeCount,
        int escapeRate,
        int noHintCount,
        int noHintRate,
        Integer avgRemainSeconds,
        int avgRemainRate,
        int conquerCount,
        int themeCount,
        int conquerRate,
        List<RegionStat> byRegion
) {

    /** 큰 지역 하나 (서울·경기 ...): 그 지역 공개 테마 중 내가 플레이한 수 */
    public record RegionStat(Long regionId, String name, int playedCount, int themeCount, int rate) {
    }
}

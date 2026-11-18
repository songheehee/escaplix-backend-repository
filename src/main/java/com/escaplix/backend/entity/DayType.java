package com.escaplix.backend.entity;

/** TIME_SLOT.day_type 날 유형. DB 에는 이 이름이 대문자 문자열로 그대로 저장된다 (@Enumerated(EnumType.STRING)) */
public enum DayType {
    /** 월~금 */
    WEEKDAY,
    /** 토·일·공휴일 */
    WEEKEND
}

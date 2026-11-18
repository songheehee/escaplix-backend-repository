package com.escaplix.backend.entity;

/** THEME.source 누가 등록했나. DB 에는 이 이름이 대문자 문자열로 그대로 저장된다 (@Enumerated(EnumType.STRING)) */
public enum ThemeSource {
    /** 사장 등록 */
    OWNER,
    /** 관리자 직접 등록 (바로 OPEN) */
    ADMIN,
    /** 크롤링 (나중 기능) */
    CRAWL
}

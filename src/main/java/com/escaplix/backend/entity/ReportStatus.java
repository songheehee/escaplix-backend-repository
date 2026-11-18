package com.escaplix.backend.entity;

/** REPORT.status 신고 상태. DB 에는 이 이름이 대문자 문자열로 그대로 저장된다 (@Enumerated(EnumType.STRING)) */
public enum ReportStatus {
    /** 접수됨 */
    WAIT,
    /** 처리 완료 (나중 기능) */
    DONE
}

package com.escaplix.backend.common;

/** 중복 확인 API(A-3, A-4, A-5, C-2) 공통 응답 */
public record AvailableResponse(boolean available) {
}

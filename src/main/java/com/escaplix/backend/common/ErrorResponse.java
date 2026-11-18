package com.escaplix.backend.common;

/** 모든 실패 응답의 모양: {"message": "화면에 그대로 보여 줄 문장"} */
public record ErrorResponse(String message) {
}

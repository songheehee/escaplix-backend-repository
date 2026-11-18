package com.escaplix.backend.common;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * 서비스에서 실패를 알릴 때 던진다. GlobalExceptionHandler 가 {"message": "..."} 로 바꿔 응답한다.
 * 예) throw new ApiException(HttpStatus.CONFLICT, "이미 사용 중인 아이디입니다.");
 */
@Getter
public class ApiException extends RuntimeException {

    private final HttpStatus status;

    public ApiException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public static ApiException badRequest(String message) { return new ApiException(HttpStatus.BAD_REQUEST, message); }
    public static ApiException unauthorized(String message) { return new ApiException(HttpStatus.UNAUTHORIZED, message); }
    public static ApiException forbidden(String message) { return new ApiException(HttpStatus.FORBIDDEN, message); }
    public static ApiException notFound(String message) { return new ApiException(HttpStatus.NOT_FOUND, message); }
    public static ApiException conflict(String message) { return new ApiException(HttpStatus.CONFLICT, message); }

    /** 스켈레톤 표시. 구현하면 이 줄을 지운다. 프런트에는 501 + "아직 구현 전" 으로 보인다 */
    public static ApiException notImplemented(String api) {
        return new ApiException(HttpStatus.NOT_IMPLEMENTED, "아직 구현 전입니다: " + api);
    }
}

package com.escaplix.backend.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

/** 예외를 상태 코드 + {"message"} 로 바꾼다. 컨트롤러·서비스에서 try-catch 로 응답을 만들지 않는다 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErrorResponse> api(ApiException e) {
        return ResponseEntity.status(e.getStatus()).body(new ErrorResponse(e.getMessage()));
    }

    /** @Valid 실패 (본문 JSON, 폼). 첫 번째 칸의 메시지만 보낸다 */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<ErrorResponse> bind(BindException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .findFirst().map(f -> f.getDefaultMessage()).orElse("입력값을 확인해 주세요.");
        return bad(msg);
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ErrorResponse> methodValidation(HandlerMethodValidationException e) {
        return bad("입력값을 확인해 주세요.");
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class,
            MissingServletRequestParameterException.class, MissingServletRequestPartException.class})
    public ResponseEntity<ErrorResponse> badFormat(Exception e) {
        return bad("요청 형식이 올바르지 않습니다.");
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> tooBig(MaxUploadSizeExceededException e) {
        return bad("파일은 5MB까지 올릴 수 있습니다.");
    }

    /** UNIQUE·CHECK 위반. 서비스에서 먼저 검사하는 게 원칙이고 이건 마지막 방어선 */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> integrity(DataIntegrityViolationException e) {
        log.warn("DB 제약 위반: {}", e.getMostSpecificCause().getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse("이미 등록되었거나 허용되지 않는 값입니다."));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> unknown(Exception e) {
        log.error("처리하지 못한 예외", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse("서버 오류가 발생했습니다."));
    }

    private ResponseEntity<ErrorResponse> bad(String msg) {
        return ResponseEntity.badRequest().body(new ErrorResponse(msg));
    }
}

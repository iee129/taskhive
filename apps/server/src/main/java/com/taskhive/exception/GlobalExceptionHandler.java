package com.taskhive.exception;

import com.taskhive.dto.ErrorResponse;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusiness(BusinessException e) {
        ErrorCode code = e.getErrorCode();
        return ResponseEntity.status(code.getStatus())
                .body(new ErrorResponse(code.name(), code.getMessage(),
                        code.getStatus().value(), MDC.get("requestId"), null));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException e) {
        return ResponseEntity.badRequest()
                .body(new ErrorResponse("INVALID_INPUT", e.getMessage(),
                        400, MDC.get("requestId"), null));
    }

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<ErrorResponse> handleInvalidToken(InvalidTokenException e) {
        return ResponseEntity.status(401)
                .body(new ErrorResponse(ErrorCode.INVALID_TOKEN.name(), e.getMessage(),
                        401, MDC.get("requestId"), null));
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException e) {
        return ResponseEntity.status(401)
                .body(new ErrorResponse("BAD_CREDENTIALS", "이메일 또는 비밀번호가 올바르지 않습니다",
                        401, MDC.get("requestId"), null));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException e) {
        List<String> fields = e.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getField() + ": " + f.getDefaultMessage())
                .toList();
        return ResponseEntity.badRequest()
                .body(new ErrorResponse(ErrorCode.INVALID_INPUT.name(), "입력값이 올바르지 않습니다",
                        400, MDC.get("requestId"), fields));
    }
}

package com.taskhive.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    TASK_NOT_FOUND(HttpStatus.NOT_FOUND, "태스크를 찾을 수 없습니다"),
    PROJECT_NOT_FOUND(HttpStatus.NOT_FOUND, "프로젝트를 찾을 수 없습니다"),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다"),
    USER_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "이미 사용 중인 이메일입니다"),
    FORBIDDEN(HttpStatus.FORBIDDEN, "접근 권한이 없습니다"),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다"),
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "입력값이 올바르지 않습니다"),
    RATE_LIMIT_EXCEEDED(HttpStatus.TOO_MANY_REQUESTS, "요청이 너무 많습니다. 잠시 후 다시 시도해주세요"),
    EMAIL_NOT_VERIFIED(HttpStatus.FORBIDDEN, "이메일 인증이 필요합니다. 이메일함을 확인해주세요"),
    TOKEN_EXPIRED(HttpStatus.BAD_REQUEST, "토큰이 만료되었습니다"),
    TOKEN_ALREADY_USED(HttpStatus.BAD_REQUEST, "이미 사용된 토큰입니다"),
    MEMBER_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 프로젝트 멤버입니다"),
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "프로젝트 멤버를 찾을 수 없습니다"),
    LAST_OWNER(HttpStatus.BAD_REQUEST, "프로젝트에 Owner가 최소 1명 이상 있어야 합니다"),
    NOT_PROJECT_MEMBER(HttpStatus.FORBIDDEN, "프로젝트 멤버가 아닙니다"),
    AI_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "AI provider를 사용할 수 없습니다"),
    TOKEN_NOT_FOUND(HttpStatus.NOT_FOUND, "토큰을 찾을 수 없습니다"),
    WEBHOOK_NOT_FOUND(HttpStatus.NOT_FOUND, "웹훅을 찾을 수 없습니다"),
    SSRF_BLOCKED(HttpStatus.BAD_REQUEST, "사설 IP 또는 localhost URL은 허용되지 않습니다");

    private final HttpStatus status;
    private final String message;
}

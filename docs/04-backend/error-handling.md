# 에러 처리

## 구조화 예외 처리 (Phase 5~)

`ErrorCode` enum + `BusinessException` + `ErrorResponse` 3-계층으로 예외를 일원화합니다.

### ErrorCode

```java
@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    TASK_NOT_FOUND(HttpStatus.NOT_FOUND, "태스크를 찾을 수 없습니다"),
    PROJECT_NOT_FOUND(HttpStatus.NOT_FOUND, "프로젝트를 찾을 수 없습니다"),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다"),
    USER_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "이미 사용 중인 이메일입니다"),
    FORBIDDEN(HttpStatus.FORBIDDEN, "접근 권한이 없습니다"),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다"),
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "입력값이 올바르지 않습니다");

    private final HttpStatus status;
    private final String message;
}
```

### BusinessException

```java
@Getter
public class BusinessException extends RuntimeException {
    private final ErrorCode errorCode;

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
```

### ErrorResponse

```java
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
    String code,
    String message,
    int status,
    String requestId,
    List<String> fields      // validation 오류 시에만 포함
) {}
```

## 전역 예외 처리기

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusiness(BusinessException e) {
        ErrorCode code = e.getErrorCode();
        return ResponseEntity.status(code.getStatus())
                .body(new ErrorResponse(code.name(), code.getMessage(),
                        code.getStatus().value(), MDC.get("requestId"), null));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException e) {
        List<String> fields = e.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getField() + ": " + f.getDefaultMessage())
                .toList();
        return ResponseEntity.badRequest()
                .body(new ErrorResponse("INVALID_INPUT", "입력값이 올바르지 않습니다",
                        400, MDC.get("requestId"), fields));
    }
}
```

## 에러 응답 형식

### 비즈니스 오류

```json
{
  "code": "TASK_NOT_FOUND",
  "message": "태스크를 찾을 수 없습니다",
  "status": 404,
  "requestId": "a1b2c3d4"
}
```

### 검증 오류 (다중 필드)

```json
{
  "code": "INVALID_INPUT",
  "message": "입력값이 올바르지 않습니다",
  "status": 400,
  "requestId": "a1b2c3d4",
  "fields": [
    "email: 이메일 형식이 아닙니다",
    "password: 8자 이상이어야 합니다"
  ]
}
```

> `fields`는 `@JsonInclude(NON_NULL)` 적용 — 검증 오류가 없을 때는 JSON에서 생략됨.

## 예외 유형별 처리 전략

| 예외 클래스 | HTTP 코드 | ErrorCode |
|------------|----------|-----------|
| `BusinessException(TASK_NOT_FOUND)` | 404 | `TASK_NOT_FOUND` |
| `BusinessException(PROJECT_NOT_FOUND)` | 404 | `PROJECT_NOT_FOUND` |
| `BusinessException(FORBIDDEN)` | 403 | `FORBIDDEN` |
| `BusinessException(INVALID_TOKEN)` | 401 | `INVALID_TOKEN` |
| `InvalidTokenException` | 401 | `INVALID_TOKEN` |
| `BadCredentialsException` | 401 | `BAD_CREDENTIALS` |
| `MethodArgumentNotValidException` | 400 | `INVALID_INPUT` |
| `IllegalArgumentException` | 400 | `INVALID_INPUT` |

## requestId 추적 (MDC)

`RequestIdFilter`가 모든 요청에 `requestId`를 MDC에 주입합니다.

```java
@Component
public class RequestIdFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(...) {
        String requestId = Optional.ofNullable(request.getHeader("X-Request-Id"))
                .filter(s -> !s.isBlank())
                .orElse(UUID.randomUUID().toString().substring(0, 8));
        MDC.put("requestId", requestId);
        response.setHeader("X-Request-Id", requestId);
        try { filterChain.doFilter(request, response); }
        finally { MDC.remove("requestId"); }
    }
}
```

- 클라이언트가 `X-Request-Id` 헤더를 보내면 그대로 사용
- 없으면 UUID 앞 8자리를 서버에서 생성
- 응답 헤더에도 `X-Request-Id`를 포함 → 클라이언트 측 로그 상관 가능

## 민감 정보 노출 방지

- 스택 트레이스는 응답 본문에 절대 포함 금지
- 상세 오류는 서버 로그(SLF4J)에만 기록
- `spring.jpa.show-sql=false` (기본값)

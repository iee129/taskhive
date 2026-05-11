# 에러 처리

## 전역 예외 처리 (예정)

`@RestControllerAdvice`를 사용한 중앙 집중식 예외 처리:

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
            MethodArgumentNotValidException ex) {
        List<String> errors = ex.getBindingResult().getFieldErrors().stream()
            .map(e -> e.getField() + ": " + e.getDefaultMessage())
            .toList();
        return ResponseEntity.badRequest()
            .body(new ErrorResponse(400, "Validation Failed", errors));
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(
            BadCredentialsException ex) {
        return ResponseEntity.status(401)
            .body(new ErrorResponse(401, "Unauthorized", "이메일 또는 비밀번호가 올바르지 않습니다"));
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(
            EntityNotFoundException ex) {
        return ResponseEntity.status(404)
            .body(new ErrorResponse(404, "Not Found", ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(Exception ex) {
        return ResponseEntity.status(500)
            .body(new ErrorResponse(500, "Internal Server Error", "서버 오류가 발생했습니다"));
    }
}
```

## 에러 응답 DTO

```java
public record ErrorResponse(
    int status,
    String error,
    Object message,       // String 또는 List<String>
    String timestamp
) {
    public ErrorResponse(int status, String error, Object message) {
        this(status, error, message,
             ZonedDateTime.now(ZoneOffset.UTC).toString());
    }
}
```

## 예외 유형별 처리 전략

| 예외 클래스 | HTTP 코드 | 원인 |
|------------|----------|------|
| `MethodArgumentNotValidException` | 400 | `@Valid` 검증 실패 |
| `BadCredentialsException` | 401 | 로그인 실패 |
| `EntityNotFoundException` | 404 | 리소스 없음 |
| `AccessDeniedException` | 403 | 권한 없음 |
| `DataIntegrityViolationException` | 409 | 이메일 중복 등 DB 제약 위반 |
| `Exception` (catch-all) | 500 | 예기치 않은 서버 오류 |

## 민감 정보 노출 방지

- 스택 트레이스는 응답 본문에 절대 포함 금지
- `spring.jpa.show-sql=true`는 개발 프로파일에서만 활성화
- 500 응답은 일반 메시지만 반환 ("서버 오류가 발생했습니다")
- 상세 오류는 서버 로그(SLF4J)에만 기록

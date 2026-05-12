# 에러 처리

## 전역 예외 처리

`@RestControllerAdvice`를 사용한 중앙 집중식 예외 처리:

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    }

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<Map<String, String>> handleInvalidToken(InvalidTokenException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", e.getMessage()));
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, String>> handleBadCredentials(BadCredentialsException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "이메일 또는 비밀번호가 올바르지 않습니다"));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, List<String>>> handleValidation(MethodArgumentNotValidException e) {
        List<String> errors = e.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getField() + ": " + f.getDefaultMessage())
                .toList();
        return ResponseEntity.badRequest().body(Map.of("errors", errors));
    }
}
```

## 에러 응답 형식

### 단일 오류

```json
{ "error": "메시지" }
```

### 검증 오류 (다중 필드)

```json
{ "errors": ["email: 이메일 형식이 아닙니다", "password: 8자 이상이어야 합니다"] }
```

## 예외 유형별 처리 전략

| 예외 클래스 | HTTP 코드 | 원인 |
|------------|----------|------|
| `IllegalArgumentException` | 400 | 이메일 중복 등 비즈니스 규칙 위반 |
| `MethodArgumentNotValidException` | 400 | `@Valid` 검증 실패 |
| `InvalidTokenException` | 401 | 유효하지 않거나 만료된 Refresh Token |
| `BadCredentialsException` | 401 | 로그인 실패 (이메일/비밀번호 불일치) |

## 민감 정보 노출 방지

- 스택 트레이스는 응답 본문에 절대 포함 금지
- `spring.jpa.show-sql=false` (기본값)
- 상세 오류는 서버 로그(SLF4J)에만 기록

# 입력 검증

## Bean Validation 전략

Spring Boot의 `spring-boot-starter-validation`이 포함하는 Jakarta Bean Validation 3.0 사용.  
Controller 계층에서 `@Valid`로 진입점 검증 — Service로 유효하지 않은 데이터가 전달되지 않음.

## DTO 검증 예시

### RegisterRequest

```java
public record RegisterRequest(
    @NotBlank(message = "이메일은 필수입니다")
    @Email(message = "올바른 이메일 형식이 아닙니다")
    String email,

    @NotBlank(message = "비밀번호는 필수입니다")
    @Size(min = 8, message = "비밀번호는 8자 이상이어야 합니다")
    String password,

    @NotBlank(message = "이름은 필수입니다")
    @Size(max = 100, message = "이름은 100자 이하여야 합니다")
    String name
) {}
```

### CreateTaskRequest (예정)

```java
public record CreateTaskRequest(
    @NotBlank(message = "제목은 필수입니다")
    @Size(max = 500, message = "제목은 500자 이하여야 합니다")
    String title,

    String description,

    @FutureOrPresent(message = "마감일은 오늘 이후여야 합니다")
    LocalDate dueDate
) {}
```

## Controller에서 @Valid 사용

```java
@PostMapping("/register")
public ResponseEntity<AuthResponse> register(
        @Valid @RequestBody RegisterRequest request) {
    // @Valid 실패 시 MethodArgumentNotValidException 자동 발생
    // → GlobalExceptionHandler가 400 응답 반환
    return ResponseEntity.ok(authService.register(request));
}
```

## 검증 애노테이션 레퍼런스

| 애노테이션 | 설명 |
|-----------|------|
| `@NotNull` | null 불허 |
| `@NotBlank` | null 및 공백 문자열 불허 |
| `@NotEmpty` | null 및 빈 컬렉션/문자열 불허 |
| `@Email` | 이메일 형식 검사 |
| `@Size(min, max)` | 문자열·컬렉션 크기 |
| `@Min` / `@Max` | 숫자 범위 |
| `@Future` | 미래 날짜만 허용 |
| `@FutureOrPresent` | 오늘 이후 날짜 허용 |
| `@Pattern(regexp)` | 정규식 검증 |

## 검증 계층 분리 원칙

| 계층 | 검증 대상 | 도구 |
|------|----------|------|
| Controller | 형식·필수값 | `@Valid` + Bean Validation |
| Service | 비즈니스 규칙 (이메일 중복 등) | 직접 코드 |
| Repository / DB | 무결성 제약 | UNIQUE, NOT NULL, CHECK |

Service에서 Bean Validation을 반복하지 않음 — Controller에서 이미 검증 완료.

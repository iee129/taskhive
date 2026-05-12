# 백엔드 프로젝트 구조

```
auth/
├── pom.xml
└── src/
    ├── main/
    │   ├── java/com/taskhive/
    │   │   ├── TaskHiveApplication.java
    │   │   ├── config/
    │   │   │   ├── JwtUtil.java                  # JWT 생성·검증
    │   │   │   ├── JwtFilter.java                # 토큰 추출 및 SecurityContext 주입
    │   │   │   └── SecurityConfig.java           # 필터 체인, CORS, @EnableMethodSecurity
    │   │   ├── controller/
    │   │   │   ├── AuthController.java           # /register, /login, /refresh, /logout, /me, /password
    │   │   │   ├── AdminController.java          # /api/admin/health (@PreAuthorize ADMIN)
    │   │   │   └── TaskController.java           # GET/POST/PUT/DELETE /api/tasks/*
    │   │   ├── service/
    │   │   │   ├── AuthService.java              # 회원가입·로그인·비밀번호 변경
    │   │   │   ├── RefreshTokenService.java      # 발급·Rotation·무효화
    │   │   │   ├── TaskService.java              # 태스크 CRUD
    │   │   │   └── UserDetailsServiceImpl.java   # Spring Security UserDetails
    │   │   ├── repository/
    │   │   │   ├── UserRepository.java
    │   │   │   ├── RefreshTokenRepository.java   # findByTokenForUpdate (PESSIMISTIC_WRITE)
    │   │   │   └── TaskRepository.java
    │   │   ├── model/
    │   │   │   ├── User.java                     # users 테이블 Entity (role 포함)
    │   │   │   ├── RefreshToken.java             # refresh_tokens 테이블 Entity
    │   │   │   ├── Task.java                     # tasks 테이블 Entity
    │   │   │   └── enums/Role.java               # USER, ADMIN
    │   │   ├── dto/
    │   │   │   ├── AuthRequest.java
    │   │   │   ├── AuthResponse.java
    │   │   │   ├── RegisterRequest.java
    │   │   │   ├── RefreshResponse.java
    │   │   │   ├── PasswordChangeRequest.java
    │   │   │   ├── TaskRequest.java
    │   │   │   └── TaskResponse.java
    │   │   └── exception/
    │   │       ├── GlobalExceptionHandler.java
    │   │       └── InvalidTokenException.java    # 401 전용 예외
    │   └── resources/
    │       ├── application.yml                   # 공통 설정
    │       └── application-test.yml             # H2 인메모리 (테스트)
    └── test/
        └── java/com/taskhive/
            ├── controller/
            │   ├── AuthControllerTest.java       # 8개 통합 테스트
            │   ├── AuthRefreshControllerTest.java # 9개 통합 테스트
            │   └── AdminControllerTest.java      # 2개 통합 테스트
            └── service/
                ├── AuthServiceTest.java          # 6개 단위 테스트
                └── RefreshTokenServiceTest.java  # 8개 단위 테스트
```

## `application.yml` 핵심 설정

```yaml
taskhive:
  jwt:
    secret: ${JWT_SECRET:changeme-replace-in-production-with-256bit-key}
    expiration-ms: 900000         # Access Token 15분
    refresh-expiration-ms: 604800000  # Refresh Token 7일
  cors:
    allowed-origins: ${CORS_ORIGINS:http://localhost:5173}

spring:
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: false
```

## 네이밍 규칙

| 계층 | 접미사 | 예시 |
|------|--------|------|
| Controller | `Controller` | `TaskController` |
| Service | `Service` | `AuthService` |
| Repository | `Repository` | `UserRepository` |
| Entity | 없음 | `User`, `Task` |
| DTO | `Request` / `Response` | `RegisterRequest`, `AuthResponse` |
| 예외 | `Exception` | `InvalidTokenException` |

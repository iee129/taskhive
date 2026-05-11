# 백엔드 프로젝트 구조

```
backend/
├── pom.xml
└── src/
    ├── main/
    │   ├── java/com/taskhive/
    │   │   ├── TaskHiveApplication.java          # @SpringBootApplication 진입점
    │   │   ├── config/
    │   │   │   ├── JwtUtil.java                  # JWT 생성·검증
    │   │   │   ├── JwtFilter.java                # 토큰 추출 및 SecurityContext 주입
    │   │   │   └── SecurityConfig.java           # 필터 체인, CORS, 권한 규칙
    │   │   ├── controller/
    │   │   │   ├── AuthController.java           # POST /api/auth/register, /login
    │   │   │   └── TaskController.java           # GET/POST/PUT/DELETE /api/tasks/*
    │   │   ├── service/
    │   │   │   ├── AuthService.java              # 회원가입·로그인 로직
    │   │   │   └── TaskService.java              # 태스크 CRUD 로직
    │   │   ├── repository/
    │   │   │   ├── UserRepository.java           # JpaRepository<User, Long>
    │   │   │   └── TaskRepository.java           # JpaRepository<Task, Long>
    │   │   ├── model/
    │   │   │   ├── User.java                     # users 테이블 Entity
    │   │   │   ├── Task.java                     # tasks 테이블 Entity
    │   │   │   └── Task.Status.java              # TODO / IN_PROGRESS / DONE
    │   │   └── dto/                              # (예정)
    │   │       ├── LoginRequest.java
    │   │       ├── RegisterRequest.java
    │   │       └── AuthResponse.java
    │   └── resources/
    │       ├── application.yml                   # 공통 설정 (JWT, CORS)
    │       └── application-dev.yml              # 개발 전용 (PostgreSQL DSN, show-sql)
    └── test/
        └── java/com/taskhive/
            └── (테스트 클래스 — 예정)
```

## 주요 파일 설명

### `TaskHiveApplication.java`
```java
@SpringBootApplication
public class TaskHiveApplication {
    public static void main(String[] args) {
        SpringApplication.run(TaskHiveApplication.class, args);
    }
}
```

### `application.yml` 핵심 설정
```yaml
jwt:
  secret: ${JWT_SECRET}          # 환경 변수 주입 필수
  expiration: 86400000           # 24시간

spring:
  security:
    cors:
      origins: ${CORS_ORIGINS}   # 허용 Origin 목록
```

### `application-dev.yml`
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/taskhive
    username: taskhive
    password: taskhive
  jpa:
    show-sql: true
    hibernate:
      ddl-auto: update           # 개발 환경 자동 스키마 반영
```

## 네이밍 규칙

| 계층 | 접미사 | 예시 |
|------|--------|------|
| Controller | `Controller` | `TaskController` |
| Service | `Service` | `AuthService` |
| Repository | `Repository` | `UserRepository` |
| Entity | 없음 | `User`, `Task` |
| DTO | `Request` / `Response` | `LoginRequest`, `AuthResponse` |

# 백엔드 개요

## 기술 스택

| 항목 | 선택 | 버전 |
|------|------|------|
| 언어 | Java | 21 (LTS) |
| 프레임워크 | Spring Boot | 3.3.0 |
| 보안 | Spring Security | 6.x |
| 인증 | JWT (jjwt) | 0.12.5 |
| ORM | Spring Data JPA / Hibernate | 6.x |
| DB 드라이버 | PostgreSQL JDBC | 42.x |
| 빌드 | Maven | 3.9+ |
| 테스트 DB | H2 (인메모리) | 2.x |

## 레이어 구조

```
HTTP 요청
    ↓
JwtFilter (OncePerRequestFilter)
    ↓
SecurityConfig (필터 체인)
    ↓
Controller (@RestController)
    ↓
Service (@Service)
    ↓
Repository (JpaRepository)
    ↓
PostgreSQL
```

## 핵심 원칙

- **Stateless**: 서버는 세션을 저장하지 않음 — JWT에 모든 상태 포함
- **Layered Architecture**: Controller → Service → Repository 단방향 의존
- **Fail-fast Validation**: `@Valid` + Bean Validation으로 진입점에서 즉시 검증
- **Least Privilege**: `/api/auth/**` 외 모든 엔드포인트는 유효한 JWT 필요

## 모듈별 책임

| 패키지 | 책임 |
|--------|------|
| `config/` | 보안 설정, JWT 유틸리티, CORS |
| `controller/` | HTTP 요청 수신, 응답 직렬화 |
| `service/` | 비즈니스 로직, 트랜잭션 관리 |
| `repository/` | DB 접근, JPA 쿼리 |
| `model/` | JPA Entity 정의 |
| `dto/` | 요청/응답 DTO (Entity 직접 노출 금지) |

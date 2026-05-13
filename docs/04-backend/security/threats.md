# 보안 위협 및 대응

## OWASP Top 10 대응 현황

| 위협 | 상태 | 대응 방법 |
|------|------|----------|
| A01 접근 제어 실패 | 부분 구현 | JWT 필터로 미인증 요청 차단. 리소스 소유권 검증 미구현 |
| A02 암호화 실패 | 구현 완료 | BCrypt 해싱, HTTPS 강제 (Ingress TLS) |
| A03 인젝션 | 구현 완료 | JPA Parameterized Query — SQL 인젝션 원천 차단 |
| A05 보안 설정 오류 | 구현 완료 | CSRF 비활성화(Stateless), CORS Origin 제한 |
| A07 인증·식별 실패 | 부분 구현 | JWT 만료(24h), BCrypt. Rate Limiting 미구현 |
| A09 보안 로깅 실패 | 미구현 | 구조적 로깅 및 감사 로그 미적용 |

## JWT 관련 위협

### 토큰 탈취 (XSS)
- **위험**: `localStorage`에 저장된 토큰이 XSS 공격으로 탈취 가능
- **현재 대응**: React에서 `dangerouslySetInnerHTML` 사용 금지, CSP 헤더 적용 예정
- **미래 개선**: `HttpOnly Cookie`로 저장 방식 변경 검토

### 토큰 즉시 무효화 불가
- **위험**: 로그아웃 후에도 24시간 내 토큰 재사용 가능
- **현재 대응**: 클라이언트에서 토큰 삭제로 처리
- **미래 개선**: Redis 블랙리스트 또는 Refresh Token Rotation 도입

### Brute Force 공격
- **위험**: `/api/auth/login` 에 반복 요청으로 비밀번호 추측
- **현재 대응**: 미구현
- **미래 개선**: Spring Rate Limiter 또는 Nginx `limit_req` 모듈 적용

## SQL 인젝션 방어

JPA `@Query` 또는 `findBy*` 메서드는 내부적으로 PreparedStatement를 사용하므로 SQL 인젝션 불가:

```java
// 안전 (JPA가 ? 파라미터 처리)
Optional<User> findByEmail(String email);

// 위험 패턴 — 절대 사용 금지
@Query(value = "SELECT * FROM users WHERE email = '" + email + "'", nativeQuery = true)
```

## 비밀번호 정책

| 항목 | 현재 | 권장 |
|------|------|------|
| 해싱 알고리즘 | BCrypt (strength=10) | BCrypt 또는 Argon2id |
| 최소 길이 | 미검증 | 8자 이상 |
| 복잡도 | 미검증 | 대소문자+숫자 포함 |
| 유출 확인 | 미구현 | HaveIBeenPwned API 연동 (선택) |

## Secret 관리

| Secret | 저장 방식 | 주의사항 |
|--------|----------|---------|
| `JWT_SECRET` | K8s Secret / .env | Git 커밋 절대 금지 |
| DB 비밀번호 | K8s Secret | `kubectl create secret` |
| `.env` 파일 | `.gitignore` 등록 | 로컬 전용 |

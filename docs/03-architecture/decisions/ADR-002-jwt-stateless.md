# ADR-002: JWT Stateless 인증 방식 선택

**날짜**: 2026-05-12  
**상태**: 수락됨

## Context (배경)

사용자 인증이 필요하며, 이후 Kubernetes에서 백엔드 replicas를 수평 확장할 계획이 있습니다.

## Decision (결정)

**JWT (jjwt 0.12.5) + Stateless 세션** 방식을 선택합니다. 서버는 세션을 저장하지 않으며, 모든 상태는 토큰에 포함됩니다.

## Alternatives (고려한 대안)

| 대안 | 장점 | 단점 |
|------|------|------|
| HTTP Session (서버 세션) | 구현 단순 | 수평 확장 시 세션 공유 문제 (Redis 필요) |
| OAuth2 / 소셜 로그인 | UX 편의성 | 복잡도 급증, MVP 범위 초과 |
| JWT Stateless | 수평 확장 용이, 서버 상태 없음 | 토큰 즉시 무효화 불가 |

## Consequences (결과)

- 백엔드 replicas=N 으로 늘려도 세션 공유 불필요
- 토큰 만료(24시간)로 보안 위험 최소화
- 로그아웃 시 클라이언트에서 토큰 삭제로 처리 (서버 블랙리스트 미구현)
- Refresh Token 미구현 → 24시간 후 재로그인 필요

## 구현 파일

- `backend/src/main/java/com/taskhive/config/JwtUtil.java`
- `backend/src/main/java/com/taskhive/config/JwtFilter.java`
- `backend/src/main/java/com/taskhive/config/SecurityConfig.java`

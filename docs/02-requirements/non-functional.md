# 비기능 요구사항

## 성능

| 항목 | 목표 | 측정 방법 |
|------|------|-----------|
| API 응답 시간 (p99) | < 500ms (로컬 Docker 환경) | Actuator + 로그 타임스탬프 |
| 프론트엔드 초기 로드 | < 3초 (Lighthouse 기준) | Lighthouse CI |
| DB 쿼리 시간 | < 100ms (단순 조회) | Spring Boot show-sql 로그 |

## 보안

| 항목 | 요구사항 |
|------|---------|
| 비밀번호 저장 | BCrypt 해싱 (strength=10), 평문 저장 절대 금지 |
| JWT 시크릿 | 256비트 이상, 환경 변수로만 주입 (`JWT_SECRET`), git 커밋 금지 |
| JWT 만료 | 24시간 (86400000ms), Refresh Token은 미구현 |
| HTTPS | 프로덕션 환경에서 TLS 필수 (K8s Ingress TLS 또는 Nginx) |
| CORS | 허용 오리진 환경 변수로 제한 (`CORS_ORIGINS`) |
| SQL 인젝션 | Spring Data JPA 파라미터 바인딩으로 방지 |
| XSS | React JSX 기본 이스케이프 + Content-Security-Policy 헤더 |

## 가용성

| 항목 | 목표 |
|------|------|
| 로컬 개발 | Docker Compose 단일 머신, HA 불필요 |
| Kubernetes | Deployment replicas=2 (무중단 롤링 업데이트) |
| DB 장애 | PostgreSQL StatefulSet + PVC 영속성 보장 |

## 확장성

| 항목 | 전략 |
|------|------|
| 수평 확장 | Stateless JWT → 백엔드 replicas 독립 증가 가능 |
| DB 확장 | 초기: 단일 PostgreSQL. 향후 Read Replica 추가 가능 |
| 캐싱 | 현재 미적용. 향후 Redis 추가 고려 |

## 유지보수성

| 항목 | 기준 |
|------|------|
| 코드 커버리지 | 백엔드 서비스 레이어 70% 이상 목표 |
| 빌드 재현성 | Maven Wrapper + npm ci 로 완전 재현 가능 |
| 로그 | SLF4J + Logback, 구조화 로그 (JSON 포맷 프로덕션) |
| 문서 최신화 | 기능 변경 시 해당 docs/ 파일 동시 업데이트 |

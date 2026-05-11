# 개발 로드맵

> **포지션 목표**: 풀스택 (Java/Spring + React/TypeScript)
> **강조 역량**: 클린 아키텍처 · 보안/인증 · 테스트 · 성능 최적화 · UI/UX

---

## Phase 1 — 프로젝트 골격 ✅ 완료

- GitHub 레포지토리 생성 (Public)
- 디렉토리 구조 구성 (`auth/` · `frontend/` · `k8s/` · `docker/` · `docs/`)
- `.gitignore`, `README.md`

---

## Phase 2 — 백엔드 REST API + JWT 인증 기초 ✅ 완료

- JPA Entity: `User`, `Task` (Spring Data JPA + H2)
- Repository → Service → Controller 3계층 구현
- JWT 발급·검증 (`JwtUtil`, `JwtFilter`, `SecurityConfig`)
- `POST /api/auth/register`, `POST /api/auth/login`, `GET /api/auth/me`
- `GET/POST/PUT/DELETE /api/tasks/*`
- `GlobalExceptionHandler` — 비즈니스 예외 → HTTP 상태 코드 매핑
- `AuthenticationEntryPoint` — 미인증 요청 401 JSON 응답
- `@JsonInclude(NON_NULL)` — 응답 JSON 슬림화
- MockMvc 통합 테스트 8개, Mockito 단위 테스트 6개

---

## Phase 3 — 프론트엔드 React UI 기초 ✅ 완료

- Vite + React 18 + TypeScript 5 + Ant Design 5
- React Router v6 (`PrivateRoute` + `Layout`)
- Axios 클라이언트 + JWT 자동 주입 인터셉터 + 401 자동 로그아웃
- 로그인 / 회원가입 / 태스크 CRUD / 내 정보 페이지
- `npm run build` TypeScript 에러 0개

---

## Phase 4 — 인증 고도화 (보안) 🚧 예정

> Access Token + Refresh Token 이중 구조로 실무 수준 인증 구현

**백엔드**
- Refresh Token 엔티티 + `POST /api/auth/refresh` 엔드포인트
- Refresh Token Rotation (재발급 시 이전 토큰 무효화)
- `POST /api/auth/logout` — DB에서 Refresh Token 삭제
- Refresh Token → `HttpOnly; Secure; SameSite=Strict` 쿠키 전송 (XSS 방어)
- Access Token 만료 시간 단축 (15분)
- `USER` / `ADMIN` Role 기반 권한 (`@PreAuthorize`)
- 비밀번호 변경 `PUT /api/auth/password`

**프론트엔드**
- Axios 응답 인터셉터: 401 수신 시 `/api/auth/refresh` 자동 호출 후 원래 요청 재시도
- 탭 간 로그아웃 동기화 (`storage` 이벤트)

---

## Phase 5 — 아키텍처 고도화 (클린 코드) 🚧 예정

> 도메인 계층 정리 + 에러 처리 체계화 + API 문서화

**백엔드**
- 커스텀 예외 계층: `BusinessException` + `ErrorCode` enum (코드·메시지·HTTP 상태 일원화)
- `@Valid` Bean Validation 강화 (커스텀 `ConstraintValidator` 최소 1개)
- JPA Auditing: `BaseEntity` (`createdAt`, `updatedAt`, `createdBy`)
- Soft Delete (`deletedAt`, `@Where(clause = "deleted_at IS NULL")`)
- `Project` 리소스 도입 — `Task`는 `Project`에 소속 (1:N 관계)
- `GET /api/projects`, `POST /api/projects`, `PUT/DELETE /api/projects/:id`
- OpenAPI 3.0 (SpringDoc) — Swagger UI `/swagger-ui.html`

**공통**
- MDC 기반 요청 추적 (`X-Request-Id` 헤더 전파, 로그 상관)

---

## Phase 6 — 기능 확장 🚧 예정

> 실제 사용자가 쓸 수 있는 완성도 있는 기능 세트

**태스크 고도화**
- 우선순위 필드 (`LOW` / `MEDIUM` / `HIGH`)
- 담당자 지정 (프로젝트 멤버 중 1명)
- 태스크 검색 (`title` 키워드 포함 여부)
- 상태·우선순위·마감일 기준 필터
- 커서 기반 또는 오프셋 페이지네이션 (`page`, `size`, `sort`)

**댓글**
- `Comment` 엔티티 (`Task` 1:N)
- `GET/POST/DELETE /api/tasks/:id/comments`

**프론트엔드**
- 프로젝트 선택 → 해당 프로젝트의 태스크 목록 표시
- 검색창 + 상태/우선순위 필터 UI
- 무한 스크롤 또는 페이지네이션 컴포넌트
- 댓글 섹션 (태스크 상세 슬라이드 패널)
- 칸반 보드 뷰 (Drag & Drop, `@hello-pangea/dnd`)

---

## Phase 7 — 테스트 고도화 (TDD) 🚧 예정

> 커버리지·신뢰성 지표를 포트폴리오에 수치로 제시

**백엔드**
- Testcontainers (PostgreSQL) — H2 대신 실제 DB 환경 통합 테스트
- `@DataJpaTest` Repository 계층 슬라이스 테스트
- `@WebMvcTest` + MockMvc Controller 슬라이스 테스트 (현재 8개 → 30개+)
- JaCoCo 커버리지 리포트 + 빌드 실패 임계값 설정 (라인 80% 이상)
- 서비스 계층 경계 조건 (Boundary Value) 단위 테스트

**프론트엔드**
- Vitest + React Testing Library — 컴포넌트 렌더링 및 인터랙션 테스트
- MSW (Mock Service Worker) — API 목업으로 네트워크 독립 테스트
- 핵심 시나리오 E2E: Playwright (`회원가입 → 태스크 생성 → 로그아웃`)

---

## Phase 8 — 성능·최적화 🚧 예정

> 측정 가능한 수치로 최적화 결과를 증명

**프론트엔드**
- TanStack Query (React Query) 도입 — 서버 상태 캐싱, 자동 재요청, Optimistic Update
- React Router Lazy Loading (`React.lazy` + `Suspense`) — 초기 번들 크기 감소
- `React.memo` / `useMemo` / `useCallback` — 불필요한 리렌더링 제거
- Lighthouse CI — Performance·Accessibility·SEO 점수 측정 및 PR 코멘트 자동화

**백엔드**
- JPQL Fetch Join — N+1 쿼리 제거 (Hibernate `show_sql` + `explain` 전후 비교)
- 자주 조회되는 응답 Redis 캐싱 (`@Cacheable`, TTL 설정)
- DB 인덱스 추가 (`task.status`, `task.due_date`, `task.project_id`)
- Actuator + Micrometer — `/actuator/metrics` 엔드포인트 노출

---

## Phase 9 — UI/UX 완성도 🚧 예정

> 사용자 경험과 접근성 지표를 포트폴리오 화면으로 증명

- 반응형 레이아웃 (Ant Design Grid, 모바일 Drawer 메뉴)
- 다크 모드 (Ant Design `theme.algorithm` 토글 + `localStorage` 저장)
- 스켈레톤 로딩 UI (`Skeleton`, `Spin`)
- React Error Boundary — 페이지 단위 에러 격리 + fallback UI
- 전역 Toast / Notification 시스템 (성공·실패 피드백 일원화)
- Ant Design Form `validator` 실시간 피드백 (이메일 중복 비동기 검증 등)
- WCAG 2.1 AA 기준 접근성 — ARIA label, 키보드 네비게이션, 색상 대비

---

## Phase 10 — PostgreSQL 전환 + Docker Compose 통합 🚧 예정

> 로컬 H2에서 실제 RDB로 전환하여 프로덕션 유사 환경 구축

- H2 → PostgreSQL 마이그레이션 (`application-prod.yml`)
- Flyway 마이그레이션 스크립트 (`V1__init.sql`, `V2__add_project.sql` …)
- `auth/Dockerfile` Multi-stage (Maven build → JRE 21 slim)
- `frontend/Dockerfile` Multi-stage (Node build → Nginx serve)
- `docker-compose.yml` — PostgreSQL + Backend + Frontend + (Redis) 4-컨테이너
- Nginx `/api/*` 리버스 프록시 설정 (`proxy_pass`)
- `docker compose up -d` 한 명령으로 전체 스택 실행

---

## Phase 11 — CI/CD 🚧 예정

> 코드 품질 게이트를 자동화하여 신뢰할 수 있는 배포 파이프라인 구성

- GitHub Actions `ci.yml`:
  - Pull Request: Java 빌드 + 테스트 + JaCoCo 커버리지 코멘트
  - Pull Request: React 빌드 + Vitest + Playwright E2E
  - main merge: Docker 이미지 빌드 + GHCR 푸시 (`ghcr.io/…`)
- Branch protection rule — CI 통과 필수
- Dependabot — 의존성 자동 업데이트 PR

---

## Phase 12 — Kubernetes 배포 (선택) 🚧 예정

> 컨테이너 오케스트레이션 이해도를 코드로 증명

- `Namespace`, `ConfigMap`, `Secret` 분리
- PostgreSQL `StatefulSet` + `PersistentVolumeClaim`
- Backend / Frontend `Deployment` + `ClusterIP Service`
- Nginx `Ingress` — 도메인 라우팅 (`/api/*` → backend, `/*` → frontend)
- `HorizontalPodAutoscaler` — CPU 기반 자동 스케일
- minikube 또는 kind 로컬 검증

---

## 향후 계획 (미정)

- 실시간 알림 (WebSocket + STOMP)
- OAuth2 소셜 로그인 (Google, GitHub)
- 파일 첨부 (AWS S3 / MinIO Presigned URL)
- 팀 초대 이메일 (JavaMailSender + 토큰 링크)

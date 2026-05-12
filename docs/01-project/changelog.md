# 변경 이력

[Keep a Changelog](https://keepachangelog.com/ko/1.0.0/) 형식을 따릅니다.
버전은 [Semantic Versioning](https://semver.org/lang/ko/) 을 따릅니다.

---

## [미출시]

### 추가 예정
- Phase 6.5: **WebSocket STOMP 실시간 동기화** (칸반 보드 카드 즉시 반영)

---

## [0.13.0] — 2026-05-12

### 추가
- `k8s/namespace.yaml` — taskhive 네임스페이스
- `k8s/database/` — PostgreSQL StatefulSet + Headless Service + PVC 5Gi
- `k8s/redis/` — Redis 7-alpine Deployment + ClusterIP Service
- `k8s/backend/hpa.yaml` — HPA (CPU 60% / Memory 70%, min 2 / max 6 Pod)
- `k8s/backend/configmap.yaml` — Redis·CORS·Ollama URL 환경변수 추가
- `k8s/ingress.yaml` — Nginx Ingress (taskhive.local), 잘못된 rewrite-target 제거
- `k8s/secret.example.yaml` — Secret 템플릿 (실제 `secret.yaml`은 gitignore)
- `k8s/README.md` — minikube 전체 배포 절차, HPA 검증, 롤링 업데이트 가이드

---

## [0.12.0] — 2026-05-12

### 추가
- `ci-backend.yml` — PR 트리거: `mvn verify` + JaCoCo 커버리지 PR 자동 코멘트 (`madrapps/jacoco-report`, 전체 80%·변경파일 70% 게이트)
- `ci-frontend.yml` — PR 트리거: `npm run build` + Vitest + Playwright E2E (chromium), 실패 시 playwright-report 아티팩트 업로드
- `cd-docker.yml` — `master` 병합 트리거: GHCR 백엔드·프론트엔드 이미지 빌드·푸시 (`latest` + `sha-{commit}` 태그, Buildx 레이어 캐시)
- `dependabot.yml` — maven·npm·github-actions 주간 자동 PR 생성

---

## [0.11.0] — 2026-05-12

### 추가
- Flyway `flyway-core` + `flyway-database-postgresql` 의존성 추가
- Flyway 마이그레이션 스크립트 V1~V6 (`users` → `projects` → `tasks` → `refresh_tokens` → `comments` → `task_activities` + 인덱스)
- `application-prod.yml` — prod 프로파일 신규 생성 (`show-sql: false`)
- Docker Compose: Redis 7-alpine + Ollama 서비스 추가, `postgres_data`/`ollama_data` 볼륨

### 변경
- `application.yml` — `ddl-auto: update` → `validate`, `flyway.enabled: true`
- `application-dev.yml` — `flyway.baseline-on-migrate: true` 추가 (기존 dev DB 호환)
- `application-test.yml` — `flyway.enabled: false` 추가 (H2 + create-drop 유지)
- `docker-compose.yml` — 빌드 컨텍스트 `../backend` → `../auth`, 프론트 포트 `3000:80` → `80:80`, 백엔드 env `CACHE_TYPE=redis` / `REDIS_HOST` / `CORS_ORIGINS` / `OLLAMA_URL` 추가
- `nginx.conf` — gzip 압축, `/assets/` 1년 캐시 (`immutable`), `proxy_read_timeout 60s`, `X-Forwarded-For` 헤더

---

## [0.10.0] — 2026-05-12

### 추가
- `ThemeContext` + `ThemeProvider` — localStorage 기반 다크/라이트 모드, `ConfigProvider` 알고리즘 동적 연결
- `ThemeToggle` — `SunOutlined`/`MoonOutlined` 아이콘 토글 버튼, `aria-label` 접근성
- `ErrorBoundary` — `getDerivedStateFromError`, Ant Design `Result` 폴백, `reset()` 지원
- `SkeletonTable` — `rows` prop, `role="status"`, `aria-label="로딩 중"` 접근성
- `NotificationProvider` — `notification.useNotification()` 기반 전역 `notifySuccess/Error/Warning` Context
- `useCheckEmail` — 이메일 중복 비동기 검증 훅 (Ant Design validator 형식)
- `GET /api/auth/check-email` — 이메일 사용 가능 여부 조회 (인증 불필요, `{ available: boolean }`)
- `AuthService.isEmailTaken()` — 이메일 중복 여부 조회 메서드

### 변경
- `Layout.tsx` — `Grid.useBreakpoint` 기반 반응형: 모바일 Drawer+햄버거, 데스크탑 Sider + ThemeToggle
- `App.tsx` — `ConfigProvider` + `useThemeContext()` 다크모드 알고리즘 적용
- `main.tsx` — `ThemeProvider` → `QueryClientProvider` → `NotificationProvider` 중첩 구조
- `TasksPage.tsx` — `isLoading` 시 `SkeletonTable` 렌더, 전체를 `ErrorBoundary`로 래핑
- `RegisterPage.tsx` — 이메일 `validateTrigger="onBlur"` + `useCheckEmail` 비동기 검증
- `SecurityConfig` — `/api/auth/check-email` permitAll 경로 추가

---

## [0.9.0] — 2026-05-12

### 추가
- `@tanstack/react-query` 도입 — `QueryClientProvider`, staleTime 30s, gcTime 5m
- `useTasks` / `useMutateTask` / `useProjects` 커스텀 훅 — useQuery + useMutation 기반
- `useOptimisticTaskStatus` — 칸반 드래그 시 즉시 UI 반영, 실패 시 자동 롤백
- `CacheConfig` — `@EnableCaching`, `spring.cache.type=simple` 기본 / `redis` 환경변수 전환
- `spring-boot-starter-cache` + `spring-boot-starter-data-redis` 의존성 추가
- `Task` 엔티티 — `status`, `priority`, `deleted_at`, `assignee_id` DB 인덱스 추가
- `TaskRepository.findAllWithAssociations()` — LEFT JOIN FETCH로 N+1 쿼리 제거
- `src/hooks/` 디렉터리 + `useTasks.ts`, `useMutateTask.ts`, `useProjects.ts`
- `src/types/project.ts` — `ProjectResponse` 타입 정의
- `frontend/.lighthouserc.json` — Lighthouse CI 임계값 (Performance/Accessibility ≥ 90)
- `.github/workflows/lighthouse.yml` — PR 트리거 Lighthouse CI (workflow scope 필요)

### 변경
- `App.tsx` — `BrowserRouter` 중복 제거, `lazy()` + `Suspense` 코드 스플리팅 (4개 페이지)
- `main.tsx` — `QueryClientProvider` + `BrowserRouter` 통합
- `TasksPage.tsx` — `useQuery` + `useMutation` 전환 (fetchTasks + useEffect 제거)
- `KanbanPage.tsx` — `useTasks` + `useOptimisticTaskStatus` 전환
- `FilterBar.tsx` — `React.memo` + `useCallback` 적용
- `vite.config.ts` — `manualChunks` 추가 (vendor-react, vendor-antd, vendor-query, vendor-dnd)
- `ProjectService.getMyProjects()` — `@Cacheable("projects")` 적용
- `ProjectService.createProject/updateProject/deleteProject` — `@CacheEvict` 추가
- `TaskService.getAllTasks()` — `findAllWithAssociations()` 사용
- `application.yml` — `spring.cache.*`, `spring.data.redis.*` 설정 추가

### 성능 개선 수치
- TasksPage 초기 청크: **477KB → 8KB** (gzip 149KB → 3KB)
- KanbanPage 청크: **100KB → 2.75KB**
- 태스크 목록 DB 쿼리: **N+1 → 1회** (JOIN FETCH)
- 프로젝트 목록 재방문: **API 재호출 → 캐시 히트** (staleTime 30s)

---

## [0.8.0] — 2026-05-12

### 추가
- Testcontainers BOM + `postgresql` + `spring-boot-testcontainers` 의존성
- `TestcontainersConfig` — `@Testcontainers(disabledWithoutDocker=true)` 기반 통합 테스트 베이스 클래스
- `TaskRepositoryTest` (12) — `@DataJpaTest` + H2, findFiltered/count 메서드 전수 검증
- `AuthIntegrationTest` (6) + `TaskIntegrationTest` (7) — Testcontainers PostgreSQL 전체 Spring Context 테스트
- 단위 테스트 신규: `TaskServiceTest`(17) · `CommentServiceTest`(9) · `StatsServiceTest`(5) · `ProjectServiceTest`(13) · `AiServiceTest`(5) · `UserDetailsServiceImplTest`(3) · `JwtUtilTest`(8)
- JaCoCo Maven 플러그인 — 라인 80% / 브랜치 70% 임계값, `mvn verify` 빌드 게이트
- `vitest` + `@testing-library/react` + `msw` + `@playwright/test` 설치
- MSW `handlers.ts` / `server.ts` — API 목업 (auth, tasks, stats)
- `FilterBar.test.tsx` (6) + `LoginPage.test.tsx` (5) — Vitest + RTL 컴포넌트 테스트
- `playwright.config.ts` + `e2e/auth.spec.ts` + `e2e/task-crud.spec.ts` — Playwright E2E 설정

### 변경
- `application-tc.yml` — Testcontainers 전용 Spring 프로파일 (`tc`) 추가

---

## [0.7.0] — 2026-05-12

### 추가
- `Task.Priority` enum — `LOW`, `MEDIUM`, `HIGH` (기본값 `MEDIUM`)
- `Comment` 엔티티 + `CommentRepository` — 태스크별 댓글 (작성자 FK)
- `CommentService` — 조회·등록·삭제 (작성자 본인만 삭제 가능, 403 검증)
- `CommentController` — `GET/POST/DELETE /api/tasks/{taskId}/comments`
- `TaskActivity` 엔티티 + `TaskActivityRepository` — Audit Log 기록 테이블
- `TaskActivityAspect` — `@AfterReturning` AOP, CREATED/UPDATED/DELETED/COMMENTED 자동 기록
- `StatsService` — 전체·상태별·우선순위별·기한초과·프로젝트·댓글 집계
- `StatsController` — `GET /api/stats`, `GET /api/stats/activities`, `GET /api/stats/activities/task/{id}`
- `AiService` — Ollama `llama3.2` 연동 (RestTemplate), 자연어 → `{title, description, priority}` JSON 파싱, 실패 시 fallback
- `AiController` — `POST /api/ai/suggest-task`, `POST /api/ai/create-task`
- `AppConfig` — `RestTemplate` 빈 등록
- `spring-boot-starter-aop` 의존성 추가
- `KanbanPage` — `@hello-pangea/dnd` 드래그앤드롭 3열 칸반 보드, 카드 이동 시 즉시 `PUT /api/tasks/{id}` 호출
- `StatsPage` — 통계 카드 대시보드 + 완료율 Progress + 활동 이력
- `FilterBar` 컴포넌트 — 상태·우선순위 드롭다운 + 키워드 검색
- `CommentList` 컴포넌트 — 태스크 Drawer 내 댓글 CRUD
- `ActivityFeed` 컴포넌트 — 활동 이력 Timeline
- `AiTaskInput` 컴포넌트 — 자연어 입력 → AI 제안 확인 → 태스크 생성 모달

### 수정
- `Task` 엔티티 — `priority` 컬럼 추가
- `TaskRequest` / `TaskResponse` — `priority` 필드 추가
- `TaskRepository` — `findFiltered(status, priority, search)` JPQL 동적 쿼리
- `TaskController` — `?status=&priority=&search=` 쿼리 파라미터 지원
- `TasksPage` — 우선순위 컬럼·FilterBar·Drawer 댓글·AI 생성 버튼 추가
- `Layout` — 칸반(`/kanban`), 통계(`/stats`) 메뉴 추가
- `App.tsx` — `/kanban`, `/stats` 라우트 등록
- `application.yml` — `taskhive.ollama.url`, `taskhive.ollama.model` 설정 추가

### 검증
- `mvn test` — 33개 테스트 전체 통과
- `npm run build` — TypeScript 에러 0개

---

## [0.6.0] — 2026-05-12

### 추가
- `ErrorCode` enum — 7개 에러 코드 (HttpStatus + 한글 메시지 내장)
- `BusinessException` — `ErrorCode` 기반 `RuntimeException`
- `ErrorResponse` record — `code`, `message`, `status`, `requestId`, `fields` (`@JsonInclude NON_NULL`)
- `BaseEntity` — `@MappedSuperclass` + JPA Auditing (`createdAt`, `updatedAt`)
- `RequestIdFilter` — `OncePerRequestFilter`, `X-Request-Id` 헤더 → MDC `requestId` 추적
- `ProjectController` — Project CRUD 5개 엔드포인트 (`/api/projects/**`)
- `ProjectService` — 소유자 권한 검증 + 소프트 삭제
- `ProjectRequest` / `ProjectResponse` DTO
- `ProjectRepository` — `findByOwnerIdAndDeletedAtIsNull`, `findByIdAndDeletedAtIsNull`
- SpringDoc OpenAPI `2.5.0` 의존성 — `/swagger-ui.html` Swagger UI 제공

### 수정
- `User`, `Task`, `Project` — `extends BaseEntity`, `@PrePersist` 제거
- `Task`, `Project` — `deleted_at` 소프트 삭제 컬럼 추가
- `TaskRepository` — `findAllByDeletedAtIsNull`, `findByIdAndDeletedAtIsNull` 쿼리 추가
- `TaskService` — `IllegalArgumentException` → `BusinessException` 전환, 소프트 삭제 적용
- `GlobalExceptionHandler` — `BusinessException` 핸들러 추가, 응답 형식 `ErrorResponse` 통일
- `SecurityConfig` — `/v3/api-docs/**`, `/swagger-ui/**`, `/swagger-ui.html` permitAll 추가
- `TaskHiveApplication` — `@EnableJpaAuditing` 추가

### 검증
- `mvn test` — 33개 테스트 전체 통과 (기존 32개 + 신규 1개 AC 검증)

---

## [0.5.0] — 2026-05-12

### 추가
- `RefreshToken` 엔티티 + `RefreshTokenRepository` (PESSIMISTIC_WRITE 비관적 락)
- `RefreshTokenService` — 발급·검증·Rotation·무효화
- `Role` enum (`USER`, `ADMIN`) + `User.role` 컬럼
- `POST /api/auth/refresh` — HttpOnly Cookie Refresh Token → 새 Access Token
- `POST /api/auth/logout` — Refresh Token 무효화 + Cookie 삭제
- `PUT /api/auth/password` — 현재 비밀번호 검증 후 변경
- `AdminController` — `GET /api/admin/health` (`@PreAuthorize("hasRole('ADMIN')")`)
- `SecurityConfig` — `@EnableMethodSecurity`, `accessDeniedHandler` (JSON 403)
- 프론트엔드 Axios 인터셉터 — 401 수신 시 `/refresh` 호출 + pending queue 패턴
- `InvalidTokenException` → 401 매핑 (`GlobalExceptionHandler`)

### 수정
- `ResponseCookie` 빌더로 교체 — `SameSite=Lax`, `HttpOnly=true`, `Path=/api/auth`
- Access Token 만료: 1h → 15분

### 검증
- `mvn test` — 33개 테스트 전체 통과
- `RefreshTokenServiceTest` 커버리지 ≥ 90%
- `AdminControllerTest` — 미인증 401, USER 역할 403 시나리오 통과

---

## [0.4.0] — 2026-05-12

### 추가
- `frontend/` — Vite + React 18 + TypeScript 5 + Ant Design 5 SPA 구현
- `src/api/client.ts` — Axios 인스턴스, JWT 자동 주입 인터셉터, 401 자동 로그아웃
- `src/api/auth.ts` — `register`, `login`, `me` API 함수
- `src/api/tasks.ts` — `getTasks`, `createTask`, `updateTask`, `deleteTask` API 함수
- `src/types/auth.ts` — `AuthRequest`, `RegisterRequest`, `AuthResponse` 타입
- `src/types/task.ts` — `TaskStatus`, `TaskRequest`, `TaskResponse` 타입
- `src/components/PrivateRoute.tsx` — 비인증 접근 시 `/login` 리다이렉트
- `src/components/Layout.tsx` — Ant Design Sider 레이아웃 + 로그아웃 버튼
- `src/pages/LoginPage.tsx` — 로그인 폼, JWT 저장 후 `/tasks` 이동
- `src/pages/RegisterPage.tsx` — 회원가입 폼, 성공 시 자동 로그인
- `src/pages/TasksPage.tsx` — 태스크 Table + 생성/수정 Modal + 삭제 Popconfirm
- `src/pages/ProfilePage.tsx` — `/api/auth/me` 결과 표시 (Ant Design Descriptions)
- `src/App.tsx` — React Router v6 라우팅 (공개/보호 경로 분리)

### 검증
- `npm run build` TypeScript 에러 0개

---

## [0.3.0] — 2026-05-12

### 추가
- `GlobalExceptionHandler` — `IllegalArgumentException` → 400, `BadCredentialsException` → 401, `MethodArgumentNotValidException` → 400 처리
- `GET /api/auth/me` — JWT로 현재 사용자 정보 조회 엔드포인트
- `AuthenticationEntryPoint` — 인증 실패 시 403 대신 401 JSON 응답 반환
- `AuthServiceTest` — `getMe` 정상/예외 단위 테스트 2개 추가 (총 6개)
- `AuthControllerTest` — 변조 JWT `/me` 401 통합 테스트 추가 (총 8개)

### 수정
- `SecurityConfig` — `/api/auth/register`, `/api/auth/login`만 permitAll (기존 `/api/auth/**` 전체에서 축소)
- `AuthResponse` — `@JsonInclude(NON_NULL)` 적용: `/me` 응답에서 `token: null` 미노출
- `auth/` 폴더명 변경 (기존 `backend/`)

---

## [0.2.0] — 2026-05-12

### 추가
- `User`, `Project`, `Task` JPA Entity
- `UserRepository`, `ProjectRepository`, `TaskRepository`
- DTO: `AuthRequest`, `RegisterRequest`, `AuthResponse`, `TaskRequest`, `TaskResponse`
- `JwtUtil` — JWT 생성 및 검증
- `JwtFilter` — 요청별 Bearer 토큰 파싱
- `SecurityConfig` — Spring Security Stateless 설정 + CORS
- `AuthService` — 회원가입 / 로그인
- `TaskService` — 태스크 CRUD
- `AuthController` — `POST /api/auth/register`, `POST /api/auth/login`
- `TaskController` — `GET/POST/PUT/DELETE /api/tasks/*`

### 검증
- `mvn compile` 통과

---

## [0.1.0] — 2026-05-12

### 추가
- 프로젝트 초기 골격 구성
- `backend/` — Spring Boot 3 boilerplate, `pom.xml`
- `frontend/` — React 18 + TypeScript 5 + Vite boilerplate
- `docker/docker-compose.yml` — PostgreSQL + Backend + Frontend 3-컨테이너
- `k8s/` — Namespace, Deployment, Service, StatefulSet, Ingress 매니페스트
- `scripts/build.sh`, `scripts/deploy.sh`
- `.gitignore`, `README.md`

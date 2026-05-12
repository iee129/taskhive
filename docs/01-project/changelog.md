# 변경 이력

[Keep a Changelog](https://keepachangelog.com/ko/1.0.0/) 형식을 따릅니다.
버전은 [Semantic Versioning](https://semver.org/lang/ko/) 을 따릅니다.

---

## [미출시]

### 추가 예정
- Phase 6: 페이지네이션 · 칸반 보드 · 댓글 · **Audit Log (AOP)** · **통계 대시보드** · AI 자연어 태스크 생성 · 일간 다이제스트
- Phase 6.5: **WebSocket STOMP 실시간 동기화** (칸반 보드 카드 즉시 반영)
- Phase 7: Testcontainers · JaCoCo 80% · Playwright E2E
- Phase 8: TanStack Query · Redis 캐싱 · N+1 제거
- Phase 9: 다크모드 · 반응형 · Error Boundary
- Phase 10: Docker Compose 통합 (PostgreSQL + Redis + Ollama)
- Phase 11: GitHub Actions CI/CD · GHCR 이미지 푸시

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

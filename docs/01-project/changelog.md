# 변경 이력

[Keep a Changelog](https://keepachangelog.com/ko/1.0.0/) 형식을 따릅니다.
버전은 [Semantic Versioning](https://semver.org/lang/ko/) 을 따릅니다.

---

## [미출시]

### 추가 예정
- Docker Compose 통합 검증
- Kubernetes 배포 매니페스트 검증

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

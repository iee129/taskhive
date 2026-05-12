# Phase 3 — React 프론트엔드 구현 계획

## 요구사항 요약

TaskHive 백엔드(auth 브랜치)와 연동하는 React 프론트엔드를 구현한다.

- **기술 스택**: Vite + React 18 + TypeScript 5 + Ant Design + Axios + React Router v6
- **브랜치**: `frontend` (master 기반 신규 생성)
- **대상 페이지**: 로그인, 회원가입, 태스크 목록/CRUD, 내 정보
- **백엔드 연동 API**: `POST /api/auth/register`, `POST /api/auth/login`, `GET /api/auth/me`, `GET|POST|PUT|DELETE /api/tasks/*`

---

## 폴더 구조

```
frontend/
  src/
    api/
      client.ts        # Axios 인스턴스 + JWT 인터셉터
      auth.ts          # register, login, me API 함수
      tasks.ts         # tasks CRUD API 함수
    components/
      Layout.tsx       # Ant Design Layout (헤더 + 사이드 메뉴 + 로그아웃)
      PrivateRoute.tsx  # 비인증 시 /login 리다이렉트
    pages/
      LoginPage.tsx
      RegisterPage.tsx
      TasksPage.tsx    # 목록 + 생성/수정/삭제 모달
      ProfilePage.tsx
    types/
      auth.ts          # AuthResponse, RegisterRequest, AuthRequest
      task.ts          # TaskRequest, TaskResponse, Task.Status
    App.tsx            # React Router 라우팅
    main.tsx
  index.html
  vite.config.ts
  tsconfig.json
  package.json
```

---

## 수용 기준 (Acceptance Criteria)

### AC-1. 프로젝트 초기화
- `npm run dev` 실행 시 `http://localhost:5173`에서 앱이 로드된다
- `npm run build` 성공 (TypeScript 에러 0개)

### AC-2. 인증 흐름
- 비로그인 상태에서 `/tasks`, `/profile` 접근 시 `/login`으로 리다이렉트된다
- 로그인 성공 시 JWT가 `localStorage['token']`에 저장되고 `/tasks`로 이동한다
- 회원가입 성공 시 자동 로그인 후 `/tasks`로 이동한다
- 로그아웃 시 `localStorage['token']` 제거 + `/login`으로 이동한다
- 모든 보호된 API 요청에 `Authorization: Bearer <token>` 헤더가 자동 포함된다

### AC-3. 태스크 목록 페이지 (`/tasks`)
- 로그인 후 `GET /api/tasks` 결과를 Ant Design Table로 표시한다
- 컬럼: 제목, 상태, 마감일, 생성일, 작업(수정/삭제)
- "새 태스크" 버튼 클릭 시 생성 모달이 열린다
- 생성 폼: title(필수), description, status, dueDate
- 생성/수정/삭제 후 목록이 자동 갱신된다

### AC-4. 태스크 CRUD
- `POST /api/tasks` 성공 시 새 항목이 목록에 추가된다
- `PUT /api/tasks/:id` 성공 시 수정된 항목이 목록에 반영된다
- `DELETE /api/tasks/:id` 성공 시 항목이 목록에서 제거된다
- API 에러 시 Ant Design `message.error`로 사용자에게 알린다

### AC-5. 내 정보 페이지 (`/profile`)
- `GET /api/auth/me` 결과로 이메일과 이름이 표시된다

---

## 구현 단계

### Step 1: 브랜치 및 프로젝트 초기화
```bash
git checkout master
git checkout -b frontend
cd frontend
npm create vite@latest . -- --template react-ts
npm install antd axios react-router-dom
npm install -D @types/react @types/react-dom
```

### Step 2: TypeScript 타입 정의
파일: `src/types/auth.ts`
```ts
export interface AuthRequest { email: string; password: string; }
export interface RegisterRequest { name: string; email: string; password: string; }
export interface AuthResponse { token?: string; email: string; name: string; }
```

파일: `src/types/task.ts`
```ts
export type TaskStatus = 'TODO' | 'IN_PROGRESS' | 'DONE';
export interface TaskRequest {
  title: string; description?: string;
  status?: TaskStatus; dueDate?: string;
}
export interface TaskResponse {
  id: number; title: string; description?: string;
  status: TaskStatus; dueDate?: string; createdAt: string;
}
```
> Task.Status 실제 값은 `auth/src/main/java/com/taskhive/model/Task.java` enum에서 확인 후 반영

### Step 3: Axios 클라이언트
파일: `src/api/client.ts`
- `baseURL: 'http://localhost:8080'`
- request 인터셉터: `localStorage.getItem('token')` → `Authorization: Bearer` 헤더 자동 주입
- response 인터셉터: 401 응답 시 `localStorage.removeItem('token')` + `/login` 리다이렉트

### Step 4: API 함수
파일: `src/api/auth.ts` — `register`, `login`, `me`
파일: `src/api/tasks.ts` — `getTasks`, `createTask`, `updateTask`, `deleteTask`

### Step 5: PrivateRoute 컴포넌트
파일: `src/components/PrivateRoute.tsx`
- `localStorage['token']` 없으면 `<Navigate to="/login" />`
- 있으면 `<Outlet />`

### Step 6: Layout 컴포넌트
파일: `src/components/Layout.tsx`
- Ant Design `Layout` + `Menu`
- 사이드: 태스크 목록, 내 정보 메뉴
- 헤더 우측: 로그아웃 버튼

### Step 7: 페이지 구현 (병렬)
- `LoginPage.tsx` — Ant Design `Form` + `login()` 호출
- `RegisterPage.tsx` — Ant Design `Form` + `register()` 호출
- `TasksPage.tsx` — `Table` + `Modal` (생성/수정) + `Popconfirm` (삭제)
- `ProfilePage.tsx` — `me()` 호출 + `Descriptions` 컴포넌트

### Step 8: App.tsx 라우팅
```tsx
<Routes>
  <Route path="/login" element={<LoginPage />} />
  <Route path="/register" element={<RegisterPage />} />
  <Route element={<PrivateRoute />}>
    <Route element={<Layout />}>
      <Route path="/tasks" element={<TasksPage />} />
      <Route path="/profile" element={<ProfilePage />} />
    </Route>
  </Route>
  <Route path="*" element={<Navigate to="/tasks" />} />
</Routes>
```

### Step 9: 빌드 검증
```bash
npm run build   # TypeScript 에러 0개
npm run dev     # 브라우저에서 전체 흐름 수동 확인
```

---

## 리스크 및 대응

| 리스크 | 대응 |
|--------|------|
| CORS 에러: 백엔드가 5173 포트 허용 안 함 | `auth/src/main/resources/application-dev.yml`의 `taskhive.cors.allowed-origins`에 `http://localhost:5173` 추가 확인 |
| Task.Status enum 값 불일치 | `auth/src/main/java/com/taskhive/model/Task.java` 읽어 실제 값 확인 후 타입 수정 |
| 백엔드 미기동 상태에서 개발 | Axios mock adapter 또는 `msw` 사용 고려 (선택) |
| localStorage JWT — XSS 취약 | Phase 3 범위에서는 허용, Phase 5+ httpOnly cookie 전환 검토 |

---

## 검증 방법

1. 백엔드(`auth/`) H2 프로파일로 기동
2. `npm run dev` 실행
3. 브라우저에서 수동 시나리오 수행:
   - 비로그인 → /tasks 접근 → /login 리다이렉트 확인
   - 회원가입 → 자동 로그인 → /tasks 확인
   - 태스크 생성 → 목록 갱신 확인
   - 태스크 수정, 삭제 확인
   - /profile에서 사용자 정보 확인
   - 로그아웃 → /login 리다이렉트 확인
4. `npm run build` 성공 확인

# 프론트엔드 테스트

## 설치된 도구

```bash
npm install -D vitest @vitest/ui @testing-library/react @testing-library/user-event \
               @testing-library/jest-dom msw jsdom @playwright/test
```

| 도구 | 버전 | 용도 |
|------|------|------|
| Vitest | ^4.x | 단위·컴포넌트 테스트 (Vite 기반, Jest 호환) |
| React Testing Library | ^16.x | DOM 기반 컴포넌트 렌더링 |
| @testing-library/user-event | ^14.x | 사용자 상호작용 시뮬레이션 |
| @testing-library/jest-dom | ^6.x | DOM matcher 확장 (`toBeInTheDocument` 등) |
| MSW | ^2.x | API 목업 (Node 환경 + 브라우저) |
| Playwright | ^1.x | E2E 브라우저 테스트 |

---

## Vitest 설정

별도 `vitest.config.ts` 사용 (vite.config.ts와 분리):

```typescript
// vitest.config.ts
import { defineConfig } from 'vitest/config';
import react from '@vitejs/plugin-react';
import path from 'path';

export default defineConfig({
  plugins: [react()],
  resolve: {
    alias: { '@': path.resolve(__dirname, './src') },
  },
  test: {
    environment: 'jsdom',
    globals: true,
    setupFiles: ['./src/test/setup.ts'],
    exclude: ['node_modules', 'e2e'],
  },
});
```

## 테스트 Setup

```typescript
// src/test/setup.ts
import '@testing-library/jest-dom';
import { afterAll, afterEach, beforeAll, vi } from 'vitest';
import { server } from '../mocks/server';

// Ant Design 요구 — jsdom에 window.matchMedia 없음
Object.defineProperty(window, 'matchMedia', {
  writable: true,
  value: vi.fn().mockImplementation((query: string) => ({
    matches: false,
    media: query,
    onchange: null,
    addListener: vi.fn(),
    removeListener: vi.fn(),
    addEventListener: vi.fn(),
    removeEventListener: vi.fn(),
    dispatchEvent: vi.fn(),
  })),
});

beforeAll(() => server.listen({ onUnhandledRequest: 'error' }));
afterEach(() => server.resetHandlers());
afterAll(() => server.close());
```

---

## MSW API 목업

### handlers.ts

```typescript
// src/mocks/handlers.ts
import { http, HttpResponse } from 'msw';

const BASE = 'http://localhost:8080';

export const handlers = [
  http.post(`${BASE}/api/auth/login`, async ({ request }) => {
    const body = await request.json() as { email: string; password: string };
    if (body.email === 'test@example.com' && body.password === 'password123') {
      return HttpResponse.json({ token: 'mock-jwt-token', email: body.email, name: '테스터' });
    }
    return HttpResponse.json({ message: '잘못된 자격증명' }, { status: 401 });
  }),

  http.get(`${BASE}/api/tasks`, ({ request }) => {
    const url = new URL(request.url);
    const status = url.searchParams.get('status');
    const tasks = [
      { id: 1, title: '첫 번째 태스크', status: 'TODO', priority: 'MEDIUM' },
      { id: 2, title: '두 번째 태스크', status: 'IN_PROGRESS', priority: 'HIGH' },
    ];
    if (status) return HttpResponse.json(tasks.filter(t => t.status === status));
    return HttpResponse.json(tasks);
  }),

  // POST /api/tasks, PUT /api/tasks/:id, DELETE /api/tasks/:id ...
];
```

### server.ts (Node 환경)

```typescript
// src/mocks/server.ts
import { setupServer } from 'msw/node';
import { handlers } from './handlers';

export const server = setupServer(...handlers);
```

---

## 컴포넌트 테스트

### 파일 위치

```
frontend/src/tests/
  FilterBar.test.tsx     # 6개 테스트
  LoginPage.test.tsx     # 5개 테스트
```

### FilterBar.test.tsx

```typescript
describe('FilterBar', () => {
  it('초기 렌더링 — 검색 입력창 표시', () => {
    render(<FilterBar {...defaultProps} />);
    expect(screen.getByPlaceholderText('제목 검색')).toBeInTheDocument();
  });

  it('검색 입력 — onSearchChange 호출', async () => {
    const onSearchChange = vi.fn();
    render(<FilterBar {...defaultProps} onSearchChange={onSearchChange} />);
    await userEvent.type(screen.getByPlaceholderText('제목 검색'), '태스크');
    expect(onSearchChange).toHaveBeenCalled();
  });

  it('초기화 버튼 — onClear 호출', async () => {
    const onClear = vi.fn();
    render(<FilterBar {...defaultProps} onClear={onClear} />);
    await userEvent.click(screen.getByRole('button', { name: /초기화/i }));
    expect(onClear).toHaveBeenCalledTimes(1);
  });
});
```

### LoginPage.test.tsx (MSW 활용)

```typescript
it('정상 로그인 — 토큰 저장 후 이동', async () => {
  renderLogin();
  await userEvent.type(screen.getByLabelText('이메일'), 'test@example.com');
  await userEvent.type(screen.getByLabelText('비밀번호'), 'password123');
  await userEvent.click(screen.getByRole('button', { name: '로그인' }));

  await waitFor(() => {
    expect(localStorage.getItem('token')).toBe('mock-jwt-token');
    expect(mockNavigate).toHaveBeenCalledWith('/tasks');
  });
});

it('잘못된 자격증명 — 에러 메시지', async () => {
  renderLogin();
  await userEvent.type(screen.getByLabelText('이메일'), 'test@example.com');
  await userEvent.type(screen.getByLabelText('비밀번호'), 'wrongpassword');
  await userEvent.click(screen.getByRole('button', { name: '로그인' }));

  await waitFor(() => {
    expect(screen.getByText('이메일 또는 비밀번호가 올바르지 않습니다')).toBeInTheDocument();
  });
});
```

### 전체 결과

```
Test Files  2 passed (2)
     Tests  11 passed (11)
```

---

## Playwright E2E

### 설정

```typescript
// playwright.config.ts
export default defineConfig({
  testDir: './e2e',
  use: { baseURL: 'http://localhost:5173' },
  webServer: {
    command: 'npm run dev',
    url: 'http://localhost:5173',
    reuseExistingServer: !process.env.CI,
  },
});
```

### E2E 시나리오

```
e2e/
  auth.spec.ts        # 로그인 페이지 렌더링, 미인증 리다이렉트, 폼 유효성 검사
  task-crud.spec.ts   # 인증 없이 /tasks 접근 → 리다이렉트
```

```typescript
// e2e/auth.spec.ts
test('미인증 상태 — /tasks 접근 시 /login 리다이렉트', async ({ page }) => {
  await page.evaluate(() => localStorage.clear());
  await page.goto('/tasks');
  await expect(page).toHaveURL(/\/login/);
});

test('로그인 폼 — 필수 필드 유효성 검사', async ({ page }) => {
  await page.goto('/login');
  await page.getByRole('button', { name: '로그인' }).click();
  await expect(page.getByText(/필수/i).first()).toBeVisible();
});
```

E2E 중 백엔드 연동이 필요한 테스트는 `test.skip()`으로 마킹, CI 환경에서 별도 실행.

---

## package.json 스크립트

```json
{
  "scripts": {
    "test":          "vitest run",
    "test:watch":    "vitest",
    "test:coverage": "vitest run --coverage",
    "test:e2e":      "playwright test"
  }
}
```

## 실행

```bash
# 컴포넌트 테스트 (단발)
npm run test

# 커버리지 리포트
npm run test:coverage

# E2E (dev 서버 자동 기동)
npm run test:e2e

# 특정 파일만
npx vitest run src/tests/FilterBar.test.tsx
```

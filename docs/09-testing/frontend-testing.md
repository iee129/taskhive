# 프론트엔드 테스트

> **Phase 3** 이후 구현 예정. 이 문서는 채택할 전략과 예시를 기술.

## 도구

| 도구 | 용도 |
|------|------|
| Vitest | 단위·통합 테스트 (Jest 호환, Vite 기반) |
| React Testing Library | 컴포넌트 렌더링 테스트 |
| MSW (Mock Service Worker) | API 요청 모킹 |
| Playwright | E2E 브라우저 테스트 (예정) |

## 설치

```bash
npm install -D vitest @testing-library/react @testing-library/user-event
npm install -D @testing-library/jest-dom msw
```

```typescript
// vite.config.ts 추가
test: {
  globals: true,
  environment: 'jsdom',
  setupFiles: './src/test/setup.ts',
}
```

## 컴포넌트 테스트 — TaskCard

```typescript
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { TaskCard } from './TaskCard';

const mockTask = {
  id: 1, title: '테스트 태스크', description: null,
  status: 'TODO' as const, dueDate: null, createdAt: '2026-05-12T00:00:00Z'
};

test('태스크 제목이 렌더링됨', () => {
  render(<TaskCard task={mockTask} onStatusChange={vi.fn()} onDelete={vi.fn()} />);
  expect(screen.getByText('테스트 태스크')).toBeInTheDocument();
});

test('삭제 버튼 클릭 시 onDelete 호출', async () => {
  const onDelete = vi.fn();
  render(<TaskCard task={mockTask} onStatusChange={vi.fn()} onDelete={onDelete} />);
  await userEvent.click(screen.getByRole('button', { name: /삭제/i }));
  expect(onDelete).toHaveBeenCalledWith(1);
});
```

## API 모킹 — MSW

```typescript
// src/test/handlers.ts
import { http, HttpResponse } from 'msw';

export const handlers = [
  http.post('/api/auth/login', () =>
    HttpResponse.json({ token: 'mock-token', email: 'test@example.com', name: '테스터' })
  ),
  http.get('/api/tasks', () =>
    HttpResponse.json([{ id: 1, title: '모킹 태스크', status: 'TODO' }])
  ),
];
```

## E2E 테스트 — Playwright (예정)

```typescript
// tests/login.spec.ts
test('로그인 후 대시보드 이동', async ({ page }) => {
  await page.goto('/login');
  await page.fill('[name=email]', 'user@example.com');
  await page.fill('[name=password]', 'password123');
  await page.click('button[type=submit]');
  await expect(page).toHaveURL('/dashboard');
});
```

## 실행

```bash
npm run test          # Vitest watch 모드
npm run test -- --run  # 단발 실행
npx playwright test    # E2E (예정)
```

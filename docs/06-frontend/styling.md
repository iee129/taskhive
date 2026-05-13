# 스타일링

## 현재 전략

MVP 단계: **CSS Modules** 사용 — 클래스명 충돌 없음, 별도 라이브러리 불필요.

```
src/components/tasks/TaskCard.module.css
src/pages/DashboardPage.module.css
```

```typescript
// TaskCard.tsx
import styles from './TaskCard.module.css';

function TaskCard({ task }: TaskCardProps) {
  return (
    <div className={styles.card}>
      <h3 className={styles.title}>{task.title}</h3>
      <span className={`${styles.badge} ${styles[task.status.toLowerCase()]}`}>
        {task.status}
      </span>
    </div>
  );
}
```

## TailwindCSS 전환 계획 (Phase 3)

Phase 3 프론트엔드 구현 시 Tailwind 도입 검토:

```bash
npm install -D tailwindcss postcss autoprefixer
npx tailwindcss init -p
```

```typescript
// vite.config.ts에 추가 없음 — PostCSS가 자동 처리
// tailwind.config.js
export default {
  content: ['./src/**/*.{ts,tsx}'],
  theme: { extend: {} },
  plugins: [],
}
```

## 디자인 토큰 (예정)

```css
/* src/styles/tokens.css */
:root {
  --color-primary: #6366f1;    /* Indigo-500 */
  --color-success: #22c55e;    /* Green-500 */
  --color-warning: #f59e0b;    /* Amber-500 */
  --color-danger:  #ef4444;    /* Red-500 */
  --color-text:    #111827;
  --color-bg:      #f9fafb;

  --radius-sm: 4px;
  --radius-md: 8px;
  --shadow-card: 0 1px 3px rgba(0,0,0,0.12);
}
```

## Task Status 색상 매핑

| Status | 색상 | 의미 |
|--------|------|------|
| `TODO` | 회색 `#6b7280` | 미시작 |
| `IN_PROGRESS` | 파랑 `#3b82f6` | 진행 중 |
| `DONE` | 초록 `#22c55e` | 완료 |

## 반응형 중단점 (예정)

| 중단점 | 너비 | 레이아웃 |
|--------|------|---------|
| mobile | < 640px | 1열 카드 |
| tablet | 640–1024px | 2열 그리드 |
| desktop | > 1024px | 3열 그리드 + 사이드바 |

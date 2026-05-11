# TypeScript 코드 스타일

## 기본 원칙

- 들여쓰기: **2 spaces**
- 최대 줄 길이: **100자**
- `strict: true` 모드 필수
- `any` 타입 사용 금지

## 네이밍

| 유형 | 규칙 | 예시 |
|------|------|------|
| 컴포넌트 | PascalCase | `TaskCard`, `LoginPage` |
| 인터페이스·타입 | PascalCase | `Task`, `AuthResponse` |
| 함수·변수 | camelCase | `fetchTasks()`, `isLoading` |
| 상수 | UPPER_SNAKE_CASE | `API_BASE_URL` |
| 파일명 (컴포넌트) | PascalCase | `TaskCard.tsx` |
| 파일명 (유틸·훅) | camelCase | `useAuth.ts`, `dateFormatter.ts` |
| CSS Modules 클래스 | camelCase | `styles.taskCard` |
| Boolean | `is` / `has` 접두사 | `isOpen`, `hasError` |

## 타입 정의

```typescript
// interface: 확장 가능한 객체 구조
interface Task {
  id: number;
  title: string;
  status: TaskStatus;
  dueDate: string | null;
}

// type alias: 유니온·교차 타입
type TaskStatus = 'TODO' | 'IN_PROGRESS' | 'DONE';

// 함수 반환 타입 명시
async function fetchTasks(): Promise<Task[]> { ... }
```

## 컴포넌트 작성 규칙

```typescript
// Props 인터페이스 명시적 선언
interface TaskCardProps {
  task: Task;
  onDelete: (id: number) => void;
}

// 화살표 함수 컴포넌트
const TaskCard = ({ task, onDelete }: TaskCardProps) => {
  return <div>{task.title}</div>;
};

export default TaskCard;
```

## 금지 패턴

```typescript
// 금지: any 타입
const data: any = response.data;

// 금지: 비단언 (!.)
const user = getUser()!;

// 금지: == (느슨한 비교)
if (value == null) { }
// 대신: ===
if (value === null || value === undefined) { }

// 금지: console.log (프로덕션 빌드)
console.log('debug');
```

## ESLint + Prettier 설정

```json
// .eslintrc.json
{
  "extends": ["react-app", "react-app/jest"],
  "rules": {
    "@typescript-eslint/no-explicit-any": "error",
    "@typescript-eslint/no-non-null-assertion": "warn"
  }
}

// .prettierrc
{
  "semi": true,
  "singleQuote": true,
  "tabWidth": 2,
  "trailingComma": "es5",
  "printWidth": 100
}
```

## import 순서

```typescript
// 1. React
import { useState, useEffect } from 'react';
// 2. 외부 라이브러리
import axios from 'axios';
// 3. 내부 모듈 (절대 경로 → 상대 경로)
import { taskApi } from '../api/tasks';
import type { Task } from '../types/task';
// 4. 스타일
import styles from './TaskCard.module.css';
```

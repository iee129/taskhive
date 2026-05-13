# 태스크 목록 조회 시퀀스 다이어그램

## GET /api/tasks

```mermaid
sequenceDiagram
    actor User
    participant FE as Frontend (React)
    participant JF as JwtFilter
    participant TC as TaskController
    participant TS as TaskService
    participant TR as TaskRepository
    participant DB as PostgreSQL

    User->>FE: 대시보드 진입 / 목록 새로고침
    FE->>JF: GET /api/tasks\nAuthorization: Bearer {token}

    JF->>JF: extractToken(header)
    JF->>JF: jwtUtil.isValid(token)

    alt 토큰 무효 / 만료
        JF-->>FE: 401 Unauthorized
        FE-->>User: 로그인 페이지로 이동
    else 토큰 유효
        JF->>JF: SecurityContextHolder.set(authentication)
        JF->>TC: 요청 전달 (userEmail 포함)
        TC->>TS: getTasks(userEmail)
        TS->>TR: findByAssigneeEmail(userEmail)
        TR->>DB: SELECT t.* FROM tasks t\nJOIN users u ON t.assignee_id = u.id\nWHERE u.email = ?
        DB-->>TR: Task 레코드 목록
        TR-->>TS: List<Task>
        TS-->>TC: List<TaskResponse>
        TC-->>FE: 200 OK [{id, title, status, dueDate, ...}, ...]
        FE-->>User: 태스크 카드 목록 렌더링
    end
```

## 태스크 상태 필터링 (확장 예정)

```mermaid
sequenceDiagram
    participant FE as Frontend
    participant TC as TaskController
    participant DB as PostgreSQL

    FE->>TC: GET /api/tasks?status=IN_PROGRESS
    TC->>DB: SELECT * FROM tasks WHERE assignee_id=? AND status='IN_PROGRESS'
    DB-->>TC: 필터된 Task 목록
    TC-->>FE: 200 OK [...]
```

## 응답 예시

### GET /api/tasks 응답 (성공 — 200)
```json
[
  {
    "id": 1,
    "title": "API 명세 작성",
    "description": "REST API Markdown 문서 작성",
    "status": "IN_PROGRESS",
    "dueDate": "2026-06-01",
    "createdAt": "2026-05-10T09:00:00Z"
  },
  {
    "id": 2,
    "title": "프론트엔드 로그인 UI",
    "description": "React 로그인 폼 구현",
    "status": "TODO",
    "dueDate": "2026-06-15",
    "createdAt": "2026-05-11T14:00:00Z"
  }
]
```

### 빈 목록 (정상)
```json
[]
```

## Task.Status 전이

```mermaid
stateDiagram-v2
    [*] --> TODO : 생성
    TODO --> IN_PROGRESS : 작업 시작
    IN_PROGRESS --> DONE : 완료
    IN_PROGRESS --> TODO : 재오픈
    DONE --> IN_PROGRESS : 재작업
```

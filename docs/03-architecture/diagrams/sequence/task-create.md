# 태스크 생성 시퀀스 다이어그램

## POST /api/tasks

```mermaid
sequenceDiagram
    actor User
    participant FE as Frontend (React)
    participant JF as JwtFilter
    participant TC as TaskController
    participant TS as TaskService
    participant TR as TaskRepository
    participant DB as PostgreSQL

    User->>FE: 태스크 제목·설명·마감일 입력
    FE->>JF: POST /api/tasks\n Authorization: Bearer {token}

    JF->>JF: extractToken(header)
    JF->>JF: jwtUtil.isValid(token)

    alt 토큰 무효 / 만료
        JF-->>FE: 401 Unauthorized
        FE-->>User: 로그인 페이지로 이동
    else 토큰 유효
        JF->>JF: SecurityContextHolder.set(authentication)
        JF->>TC: 요청 전달 (인증 완료)
        TC->>TC: @Valid 입력 검증
        alt 검증 실패
            TC-->>FE: 400 Bad Request {errors}
            FE-->>User: 입력 오류 표시
        else 검증 통과
            TC->>TS: createTask(request, userEmail)
            TS->>TR: findUserByEmail(userEmail)
            TR->>DB: SELECT * FROM users WHERE email=?
            DB-->>TR: User
            TR-->>TS: User 객체
            TS->>TR: save(task)
            TR->>DB: INSERT INTO tasks(title, description, status, assignee_id, due_date, ...)
            DB-->>TR: 저장된 Task
            TR-->>TS: Task 객체
            TS-->>TC: TaskResponse
            TC-->>FE: 201 Created {id, title, status, ...}
            FE-->>User: 새 태스크 목록에 추가됨
        end
    end
```

## 요청/응답 예시

### 요청
```http
POST /api/tasks
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
Content-Type: application/json

{
  "title": "API 명세 작성",
  "description": "Swagger 또는 Markdown 형태로 REST API 명세 작성",
  "status": "TODO",
  "dueDate": "2026-06-01"
}
```

### 응답 (성공)
```json
{
  "id": 42,
  "title": "API 명세 작성",
  "description": "Swagger 또는 Markdown 형태로 REST API 명세 작성",
  "status": "TODO",
  "dueDate": "2026-06-01",
  "createdAt": "2026-05-12T10:30:00Z"
}
```

### 응답 (검증 실패 — 400)
```json
{
  "errors": [
    {"field": "title", "message": "제목은 필수입니다"},
    {"field": "dueDate", "message": "마감일은 오늘 이후여야 합니다"}
  ]
}
```

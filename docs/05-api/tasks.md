# 태스크 API

모든 엔드포인트는 `Authorization: Bearer {token}` 헤더 필요.

---

## GET /api/tasks

현재 로그인한 사용자에게 할당된 태스크 목록을 반환합니다.

### 요청

```http
GET /api/tasks
Authorization: Bearer {token}
```

### 응답 (200 OK)

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
    "description": null,
    "status": "TODO",
    "dueDate": null,
    "createdAt": "2026-05-11T14:00:00Z"
  }
]
```

담당 태스크가 없으면 빈 배열 `[]` 반환.

---

## POST /api/tasks

새 태스크를 생성합니다.

### 요청

```http
POST /api/tasks
Authorization: Bearer {token}
Content-Type: application/json
```

```json
{
  "title": "새 태스크 제목",
  "description": "선택적 설명",
  "dueDate": "2026-06-30"
}
```

| 필드 | 타입 | 필수 | 제약 |
|------|------|------|------|
| `title` | string | 필수 | 500자 이하 |
| `description` | string | 선택 | — |
| `dueDate` | string (date) | 선택 | `YYYY-MM-DD`, 오늘 이후 |

### 응답 (201 Created)

```json
{
  "id": 42,
  "title": "새 태스크 제목",
  "description": "선택적 설명",
  "status": "TODO",
  "dueDate": "2026-06-30",
  "createdAt": "2026-05-12T10:30:00Z"
}
```

---

## PUT /api/tasks/{id}

기존 태스크를 수정합니다.

### 요청

```http
PUT /api/tasks/42
Authorization: Bearer {token}
Content-Type: application/json
```

```json
{
  "title": "수정된 제목",
  "description": "수정된 설명",
  "status": "IN_PROGRESS",
  "dueDate": "2026-07-01"
}
```

| 필드 | 타입 | 필수 | 허용 값 |
|------|------|------|---------|
| `title` | string | 필수 | 500자 이하 |
| `description` | string | 선택 | — |
| `status` | string | 선택 | `TODO`, `IN_PROGRESS`, `DONE` |
| `dueDate` | string | 선택 | `YYYY-MM-DD` |

### 응답 (200 OK)

수정된 태스크 객체 반환 (생성 응답과 동일 형식).

### 에러

| 상황 | 코드 |
|------|------|
| 태스크 없음 | 404 |
| 권한 없음 | 403 |

---

## DELETE /api/tasks/{id}

태스크를 삭제합니다.

### 요청

```http
DELETE /api/tasks/42
Authorization: Bearer {token}
```

### 응답 (204 No Content)

응답 본문 없음.

### 에러

| 상황 | 코드 |
|------|------|
| 태스크 없음 | 404 |
| 권한 없음 | 403 |

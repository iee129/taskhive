# 댓글 API

> Phase 6에서 구현 완료. 모든 엔드포인트는 JWT 인증 필요.

## 엔드포인트 목록

| 메서드 | 경로 | 설명 |
|--------|------|------|
| GET | `/api/tasks/{taskId}/comments` | 태스크 댓글 목록 (오래된 순) |
| POST | `/api/tasks/{taskId}/comments` | 댓글 작성 |
| DELETE | `/api/tasks/{taskId}/comments/{commentId}` | 내 댓글 삭제 |

## GET /api/tasks/{taskId}/comments

**응답 `200 OK`**

```json
[
  {
    "id": 1,
    "content": "진행 상황 공유 부탁드립니다",
    "taskId": 5,
    "authorId": 2,
    "authorEmail": "user@example.com",
    "createdAt": "2026-05-12T10:30:00"
  }
]
```

## POST /api/tasks/{taskId}/comments

**요청**

```json
{ "content": "댓글 내용" }
```

**응답 `200 OK`** — 생성된 댓글 반환

## DELETE /api/tasks/{taskId}/comments/{commentId}

작성자 본인만 삭제 가능. 다른 사용자 요청 시 `403 FORBIDDEN`.

**응답 `204 No Content`**

## 에러 코드

| 상황 | ErrorCode | HTTP |
|------|-----------|------|
| 존재하지 않는 태스크 | `TASK_NOT_FOUND` | 404 |
| 본인 댓글 아님 | `FORBIDDEN` | 403 |
| 인증 없음 | — | 401 |

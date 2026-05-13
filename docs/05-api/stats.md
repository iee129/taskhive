# 통계 및 활동 이력 API

> Phase 6에서 구현 완료. 모든 엔드포인트는 JWT 인증 필요.

## 엔드포인트 목록

| 메서드 | 경로 | 설명 |
|--------|------|------|
| GET | `/api/stats` | 전체 통계 요약 |
| GET | `/api/stats/activities` | 최근 활동 50건 |
| GET | `/api/stats/activities/task/{taskId}` | 태스크별 활동 이력 |

## GET /api/stats

**응답 `200 OK`**

```json
{
  "totalTasks": 42,
  "todo": 18,
  "inProgress": 12,
  "done": 12,
  "lowPriority": 10,
  "mediumPriority": 22,
  "highPriority": 10,
  "overdue": 5,
  "totalProjects": 3,
  "totalComments": 28
}
```

## GET /api/stats/activities

최근 활동 50건을 최신 순으로 반환합니다.

**응답 `200 OK`**

```json
[
  {
    "id": 101,
    "taskId": 5,
    "taskTitle": "API 문서 작성",
    "actorEmail": "user@example.com",
    "action": "CREATED",
    "detail": null,
    "occurredAt": "2026-05-12T10:30:00"
  },
  {
    "id": 100,
    "taskId": 3,
    "taskTitle": "버그 수정",
    "actorEmail": "user@example.com",
    "action": "UPDATED",
    "detail": "상태: IN_PROGRESS",
    "occurredAt": "2026-05-12T09:15:00"
  }
]
```

### action 값

| 값 | 설명 |
|----|------|
| `CREATED` | 태스크 생성 |
| `UPDATED` | 태스크 수정 |
| `DELETED` | 태스크 삭제 |
| `COMMENTED` | 댓글 작성 |

## Audit Log 동작 원리

AOP(`@AfterReturning`)가 `TaskService`·`CommentService` 메서드 성공 후 자동으로 `task_activities` 테이블에 기록합니다.
`SecurityContextHolder`에서 현재 인증된 사용자 이메일을 추출하여 `actorEmail`에 저장합니다.

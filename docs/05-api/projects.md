# 프로젝트 API

> **구현 예정** — Phase 3 (프론트엔드 구현) 이후 백엔드 추가 예정.

## 예정 엔드포인트

| 메서드 | 경로 | 설명 |
|--------|------|------|
| GET | `/api/projects` | 내 프로젝트 목록 |
| POST | `/api/projects` | 프로젝트 생성 |
| GET | `/api/projects/{id}` | 프로젝트 상세 |
| PUT | `/api/projects/{id}` | 프로젝트 수정 |
| DELETE | `/api/projects/{id}` | 프로젝트 삭제 |
| GET | `/api/projects/{id}/tasks` | 프로젝트 소속 태스크 목록 |

## 예정 요청/응답 형식

### POST /api/projects (예정)

```json
{
  "name": "TaskHive 개발",
  "description": "TaskHive 프로젝트 개발 작업 모음"
}
```

### GET /api/projects 응답 (예정)

```json
[
  {
    "id": 1,
    "name": "TaskHive 개발",
    "description": "TaskHive 프로젝트 개발 작업 모음",
    "ownerId": 3,
    "taskCount": 12,
    "createdAt": "2026-05-01T00:00:00Z"
  }
]
```

## 설계 고려 사항

- 프로젝트 소속 태스크 조회 시 페이지네이션 적용 예정
- 프로젝트 멤버 초대 기능 (Phase 4 이후 고려)
- 프로젝트 삭제 시 소속 태스크의 `project_id`는 NULL 처리 (DB 제약 기반)

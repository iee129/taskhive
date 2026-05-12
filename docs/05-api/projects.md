# 프로젝트 API

> Phase 5에서 구현 완료. 모든 엔드포인트는 JWT 인증 필요.

## 엔드포인트 목록

| 메서드 | 경로 | 설명 | 인증 |
|--------|------|------|------|
| GET | `/api/projects` | 내 프로젝트 목록 (소유자 기준) | 필요 |
| GET | `/api/projects/{id}` | 프로젝트 상세 | 필요 |
| POST | `/api/projects` | 프로젝트 생성 | 필요 |
| PUT | `/api/projects/{id}` | 프로젝트 수정 (소유자만) | 필요 |
| DELETE | `/api/projects/{id}` | 프로젝트 소프트 삭제 (소유자만) | 필요 |

## GET /api/projects

JWT의 `sub`(이메일)로 필터링한 본인 소유 프로젝트 목록을 반환합니다.

**응답 `200 OK`**

```json
[
  {
    "id": 1,
    "name": "TaskHive 개발",
    "description": "TaskHive 프로젝트 개발 작업 모음",
    "ownerId": 3,
    "createdAt": "2026-05-12T10:00:00"
  }
]
```

## GET /api/projects/{id}

**응답 `200 OK`**

```json
{
  "id": 1,
  "name": "TaskHive 개발",
  "description": "TaskHive 프로젝트 개발 작업 모음",
  "ownerId": 3,
  "createdAt": "2026-05-12T10:00:00"
}
```

**응답 `404 Not Found`** (삭제된 프로젝트 포함)

```json
{
  "code": "PROJECT_NOT_FOUND",
  "message": "프로젝트를 찾을 수 없습니다",
  "status": 404,
  "requestId": "a1b2c3d4"
}
```

## POST /api/projects

**요청**

```json
{
  "name": "TaskHive 개발",
  "description": "TaskHive 프로젝트 개발 작업 모음"
}
```

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `name` | String | ✅ | 프로젝트 이름 |
| `description` | String | - | 프로젝트 설명 |

**응답 `200 OK`** — 생성된 프로젝트 반환

## PUT /api/projects/{id}

소유자만 수정 가능. 다른 사용자 요청 시 `403`.

**요청**

```json
{
  "name": "TaskHive 개발 v2",
  "description": "업데이트된 설명"
}
```

**응답 `200 OK`** — 수정된 프로젝트 반환

**응답 `403 Forbidden`**

```json
{
  "code": "FORBIDDEN",
  "message": "접근 권한이 없습니다",
  "status": 403,
  "requestId": "a1b2c3d4"
}
```

## DELETE /api/projects/{id}

소유자만 삭제 가능. DB 행은 유지되고 `deleted_at`에 시각을 기록합니다.

**응답 `204 No Content`**

## 소프트 삭제 동작

- 삭제 후 `GET /api/projects/{id}` → `404 PROJECT_NOT_FOUND`
- 삭제 후 `GET /api/projects` 목록에서 미노출
- DB `projects` 테이블에는 `deleted_at` 값으로 행이 유지됨

## 에러 코드 요약

| 상황 | ErrorCode | HTTP |
|------|-----------|------|
| 존재하지 않는 프로젝트 | `PROJECT_NOT_FOUND` | 404 |
| 소유자 아님 | `FORBIDDEN` | 403 |
| 인증 없음 | — (SecurityConfig 처리) | 401 |
| 이름 누락 | `INVALID_INPUT` | 400 |

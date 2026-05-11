# API 설계 원칙

## REST 규약

| 원칙 | 적용 방식 |
|------|----------|
| 리소스 중심 URL | `/api/tasks`, `/api/projects` (동사 금지) |
| HTTP 메서드 의미론 | GET(조회), POST(생성), PUT(전체 수정), PATCH(부분 수정), DELETE(삭제) |
| 복수형 명사 | `/tasks` (단수 `/task` 금지) |
| 계층 관계 | `/api/projects/{id}/tasks` (프로젝트 소속 태스크) |
| 버전 관리 | 현재 미적용. 필요 시 `/api/v1/` 접두사 추가 |

## 응답 형식

### 성공 응답

```json
{
  "id": 1,
  "title": "태스크 제목",
  "status": "TODO",
  "createdAt": "2026-05-12T10:00:00Z"
}
```

- Content-Type: `application/json`
- 날짜: ISO 8601 UTC (`2026-05-12T10:00:00Z`)
- ID: Long 타입 숫자

### 에러 응답

```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "제목은 필수입니다",
  "timestamp": "2026-05-12T10:00:00Z"
}
```

## HTTP 상태 코드

| 상황 | 코드 |
|------|------|
| 조회 성공 | 200 OK |
| 생성 성공 | 201 Created |
| 삭제 성공 (응답 없음) | 204 No Content |
| 입력 검증 실패 | 400 Bad Request |
| 인증 실패 / 토큰 없음 | 401 Unauthorized |
| 권한 없음 | 403 Forbidden |
| 리소스 없음 | 404 Not Found |
| 서버 오류 | 500 Internal Server Error |

## 현재 구현된 엔드포인트

```
POST   /api/auth/register     회원가입
POST   /api/auth/login        로그인

GET    /api/tasks             내 태스크 목록
POST   /api/tasks             태스크 생성
PUT    /api/tasks/{id}        태스크 수정
DELETE /api/tasks/{id}        태스크 삭제

GET    /actuator/health       헬스체크
```

## Pagination (예정)

대량 데이터 조회 시 페이지네이션 도입:
```
GET /api/tasks?page=0&size=20&sort=createdAt,desc
```

Spring Data JPA `Pageable` + `Page<T>` 응답 구조 사용 예정.

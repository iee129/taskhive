# 유스케이스

## UC-001: 회원가입

**액터**: 비인증 사용자  
**사전 조건**: 이메일이 시스템에 등록되지 않은 상태  
**사후 조건**: 사용자 계정 생성, JWT 발급

**정상 흐름**:
1. 사용자가 `POST /api/auth/register` 에 이름, 이메일, 비밀번호 전송
2. 시스템이 이메일 중복 여부 확인
3. 비밀번호를 BCrypt로 해싱
4. `users` 테이블에 저장
5. JWT 생성 후 응답 반환

**예외 흐름**:
- 이메일 중복: `400 Bad Request` + `"Email already in use"` 메시지
- 유효성 실패(비밀번호 8자 미만): `400 Bad Request` + 필드별 오류

---

## UC-002: 로그인

**액터**: 기존 사용자  
**사전 조건**: 계정이 존재함  
**사후 조건**: JWT 발급

**정상 흐름**:
1. `POST /api/auth/login` 에 이메일, 비밀번호 전송
2. Spring Security `AuthenticationManager`가 자격증명 검증
3. 검증 성공 시 JWT 생성 후 반환

**예외 흐름**:
- 비밀번호 불일치: `401 Unauthorized`

---

## UC-003: 태스크 생성

**액터**: 인증된 사용자  
**사전 조건**: 유효한 JWT 보유  
**사후 조건**: 새 태스크가 DB에 저장

**정상 흐름**:
1. `Authorization: Bearer <token>` 헤더와 함께 `POST /api/tasks` 전송
2. `JwtFilter`가 토큰 검증 및 SecurityContext 설정
3. `TaskService.createTask()` 실행
4. 태스크를 `TODO` 상태로 저장
5. 생성된 태스크 응답 반환

**예외 흐름**:
- 토큰 없음/만료: `403 Forbidden`
- 제목 누락: `400 Bad Request`

---

## UC-004: 태스크 상태 변경

```
sequenceDiagram
  Client->>API: PUT /api/tasks/{id} {status: "IN_PROGRESS"}
  API->>JwtFilter: 토큰 검증
  JwtFilter->>SecurityContext: 인증 설정
  API->>TaskService: updateTask(id, request)
  TaskService->>TaskRepository: save(task)
  TaskRepository-->>TaskService: 저장된 Task
  TaskService-->>API: TaskResponse
  API-->>Client: 200 OK {status: "IN_PROGRESS"}
```

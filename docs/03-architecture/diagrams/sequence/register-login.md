# 회원가입·로그인 시퀀스 다이어그램

## 회원가입 (POST /api/auth/register)

```mermaid
sequenceDiagram
    actor User
    participant FE as Frontend (React)
    participant AC as AuthController
    participant AS as AuthService
    participant UR as UserRepository
    participant JU as JwtUtil
    participant DB as PostgreSQL

    User->>FE: 이메일·비밀번호·이름 입력
    FE->>AC: POST /api/auth/register {email, password, name}
    AC->>AS: register(request)
    AS->>UR: existsByEmail(email)
    UR->>DB: SELECT email FROM users WHERE email=?
    DB-->>UR: 결과 (없음)
    UR-->>AS: false
    AS->>AS: BCrypt.encode(password)
    AS->>UR: save(user)
    UR->>DB: INSERT INTO users(...)
    DB-->>UR: 저장된 User
    AS->>JU: generateToken(email)
    JU-->>AS: JWT 문자열
    AS-->>AC: AuthResponse {token, email, name}
    AC-->>FE: 200 OK {token, email, name}
    FE->>FE: localStorage.setItem("token", token)
    FE-->>User: 대시보드로 이동
```

## 로그인 (POST /api/auth/login)

```mermaid
sequenceDiagram
    actor User
    participant FE as Frontend (React)
    participant AC as AuthController
    participant AS as AuthService
    participant AM as AuthenticationManager
    participant JU as JwtUtil
    participant DB as PostgreSQL

    User->>FE: 이메일·비밀번호 입력
    FE->>AC: POST /api/auth/login {email, password}
    AC->>AS: login(request)
    AS->>AM: authenticate(UsernamePasswordAuthenticationToken)
    AM->>DB: SELECT * FROM users WHERE email=?
    DB-->>AM: User 레코드
    AM->>AM: BCrypt.matches(password, hash)
    alt 인증 실패
        AM-->>AS: BadCredentialsException
        AS-->>AC: 401 Unauthorized
        AC-->>FE: 401 {message: "이메일 또는 비밀번호가 올바르지 않습니다"}
        FE-->>User: 오류 메시지 표시
    else 인증 성공
        AM-->>AS: Authentication 객체
        AS->>JU: generateToken(email)
        JU-->>AS: JWT 문자열
        AS-->>AC: AuthResponse {token, email, name}
        AC-->>FE: 200 OK {token, email, name}
        FE->>FE: localStorage.setItem("token", token)
        FE-->>User: 대시보드로 이동
    end
```

## JWT 유효기간 및 갱신 정책

| 항목 | 값 |
|------|-----|
| 토큰 유효기간 | 24시간 (86,400,000ms) |
| 갱신 방식 | 재로그인 (Refresh Token 미구현) |
| 저장 위치 | `localStorage` (클라이언트) |
| 만료 시 처리 | 401 응답 → 로그인 페이지 리다이렉트 |

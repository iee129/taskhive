# 컴포넌트 다이어그램 (C4 Level 3)

## Backend API 내부 컴포넌트

```mermaid
C4Component
    title Backend API 컴포넌트 다이어그램

    Container_Boundary(backend, "Backend API (Spring Boot)") {
        Component(secConfig, "SecurityConfig", "Spring Security", "필터 체인 구성.\nJWT 필터 등록, CORS, @EnableMethodSecurity.")
        Component(jwtFilter, "JwtFilter", "OncePerRequestFilter", "Authorization 헤더 추출 및 토큰 검증.\nSecurityContext 주입.")
        Component(jwtUtil, "JwtUtil", "JJWT 0.12.5", "토큰 생성(generateToken),\n검증(isValid), 이메일 추출.")
        Component(authCtrl, "AuthController", "Spring MVC", "POST /api/auth/register\nPOST /api/auth/login\nPOST /api/auth/refresh\nPOST /api/auth/logout\nPUT /api/auth/password")
        Component(adminCtrl, "AdminController", "Spring MVC", "GET /api/admin/health\n(@PreAuthorize ADMIN 전용)")
        Component(taskCtrl, "TaskController", "Spring MVC", "GET/POST/PUT/DELETE /api/tasks/*")
        Component(aiCtrl, "AiController", "Spring MVC", "POST /api/ai/parse-task\nGET /api/ai/digest")
        Component(authSvc, "AuthService", "Spring Service", "회원가입·로그인 비즈니스 로직.\nBCrypt 해싱, JWT 발급.")
        Component(rtSvc, "RefreshTokenService", "Spring Service", "Refresh Token 발급·검증·Rotation.\nPESSIMISTIC_WRITE 락.")
        Component(taskSvc, "TaskService", "Spring Service", "태스크 CRUD 비즈니스 로직.\n소유권 검증.")
        Component(aiSvc, "AiService", "Spring Service", "RestClient → Ollama API.\n프롬프트 구성 + JSON 파싱.")
        Component(userRepo, "UserRepository", "Spring Data JPA", "users 테이블 CRUD")
        Component(taskRepo, "TaskRepository", "Spring Data JPA", "tasks 테이블 CRUD")
        Component(rtRepo, "RefreshTokenRepository", "Spring Data JPA", "refresh_tokens 테이블 CRUD\nfindByTokenForUpdate (비관적 락)")
    }

    ContainerDb(db, "PostgreSQL 16", "관계형 DB", "users, tasks, projects, refresh_tokens")
    Container(ollama, "Ollama", "LLM 런타임", "Llama 3.2 3B :11434")

    Rel(secConfig, jwtFilter, "등록")
    Rel(jwtFilter, jwtUtil, "토큰 검증 위임")
    Rel(authCtrl, authSvc, "호출")
    Rel(authCtrl, rtSvc, "Refresh/Logout")
    Rel(taskCtrl, taskSvc, "호출")
    Rel(aiCtrl, aiSvc, "호출")
    Rel(authSvc, jwtUtil, "토큰 생성")
    Rel(authSvc, userRepo, "사용")
    Rel(rtSvc, rtRepo, "사용")
    Rel(taskSvc, taskRepo, "사용")
    Rel(aiSvc, ollama, "HTTP RestClient")
    Rel(userRepo, db, "JDBC")
    Rel(taskRepo, db, "JDBC")
    Rel(rtRepo, db, "JDBC")
```

## Frontend 컴포넌트 (예정)

```mermaid
graph TD
    subgraph Pages
        LP[LoginPage]
        RP[RegisterPage]
        DP[DashboardPage]
        TP[TaskDetailPage]
    end
    subgraph Components
        NB[Navbar]
        TL[TaskList]
        TC[TaskCard]
        TF[TaskForm]
    end
    subgraph Services
        AS[authService]
        TS[taskService]
        AI[apiClient - axios]
    end

    LP --> AS
    RP --> AS
    DP --> TL
    TL --> TC
    DP --> TF
    TF --> TS
    AS --> AI
    TS --> AI
```

## 패키지 구조

```
com.taskhive
├── config/         # JwtUtil, JwtFilter, SecurityConfig
├── controller/     # AuthController, TaskController
├── service/        # AuthService, TaskService
├── repository/     # UserRepository, TaskRepository
├── model/          # User, Task, Project (JPA Entity)
└── dto/            # LoginRequest, RegisterRequest, AuthResponse
```

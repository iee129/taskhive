# 컴포넌트 다이어그램 (C4 Level 3)

## Backend API 내부 컴포넌트

```mermaid
C4Component
    title Backend API 컴포넌트 다이어그램

    Container_Boundary(backend, "Backend API (Spring Boot)") {
        Component(secConfig, "SecurityConfig", "Spring Security", "필터 체인 구성.\nJWT 필터 등록, CORS, CSRF 설정.")
        Component(jwtFilter, "JwtFilter", "OncePerRequestFilter", "Authorization 헤더 추출 및 토큰 검증.\nSecurityContext 주입.")
        Component(jwtUtil, "JwtUtil", "JJWT 0.12.5", "토큰 생성(generateToken),\n검증(isValid), 이메일 추출.")
        Component(authCtrl, "AuthController", "Spring MVC", "POST /api/auth/register\nPOST /api/auth/login")
        Component(taskCtrl, "TaskController", "Spring MVC", "GET/POST/PUT/DELETE /api/tasks/*")
        Component(authSvc, "AuthService", "Spring Service", "회원가입·로그인 비즈니스 로직.\nBCrypt 해싱, JWT 발급.")
        Component(taskSvc, "TaskService", "Spring Service", "태스크 CRUD 비즈니스 로직.\n소유권 검증.")
        Component(userRepo, "UserRepository", "Spring Data JPA", "users 테이블 CRUD")
        Component(taskRepo, "TaskRepository", "Spring Data JPA", "tasks 테이블 CRUD")
    }

    ContainerDb(db, "PostgreSQL 16", "관계형 DB", "users, tasks, projects")

    Rel(secConfig, jwtFilter, "등록")
    Rel(jwtFilter, jwtUtil, "토큰 검증 위임")
    Rel(authCtrl, authSvc, "호출")
    Rel(taskCtrl, taskSvc, "호출")
    Rel(authSvc, jwtUtil, "토큰 생성")
    Rel(authSvc, userRepo, "사용")
    Rel(taskSvc, taskRepo, "사용")
    Rel(userRepo, db, "JDBC")
    Rel(taskRepo, db, "JDBC")
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

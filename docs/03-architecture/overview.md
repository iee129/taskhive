# 시스템 아키텍처 개요

## 전체 구성

TaskHive는 프론트엔드 SPA, 백엔드 REST API, PostgreSQL DB의 3-티어 아키텍처입니다.

```mermaid
graph TB
    subgraph Client["클라이언트 (브라우저)"]
        FE["React 18 + TypeScript\nVite SPA"]
    end

    subgraph Backend["백엔드 서버"]
        API["Spring Boot 3\nREST API :8080"]
        SEC["Spring Security\nJWT Filter Chain"]
    end

    subgraph Database["데이터베이스"]
        DB["PostgreSQL 16\n:5432"]
        RD["Redis 7\n:6379"]
    end

    subgraph AI["AI 서비스"]
        OL["Ollama\n(Llama 3.2 3B)\n:11434"]
    end

    subgraph Infra["인프라"]
        DC["Docker Compose\n(로컬 개발)"]
        K8S["Kubernetes\n(배포)"]
    end

    FE -->|"HTTP + Bearer JWT"| API
    API --> SEC
    SEC -->|"검증 통과 시"| API
    API -->|"JPA/Hibernate"| DB
    API -->|"@Cacheable"| RD
    API -->|"RestClient HTTP"| OL
    DC -.->|"오케스트레이션"| API
    DC -.->|"오케스트레이션"| DB
    DC -.->|"오케스트레이션"| OL
    K8S -.->|"배포 관리"| API
```

## 백엔드 레이어 구조

```mermaid
graph LR
    REQ["HTTP Request"] --> FILTER["JwtFilter\n(OncePerRequestFilter)"]
    FILTER --> CTR["Controller\n(@RestController)"]
    CTR --> SVC["Service\n(@Service)"]
    SVC --> REPO["Repository\n(JpaRepository)"]
    REPO --> DB[("PostgreSQL")]
```

| 레이어 | 패키지 | 책임 |
|--------|--------|------|
| Controller | `com.taskhive.controller` | HTTP 요청/응답 처리, 유효성 검사 위임 |
| Service | `com.taskhive.service` | 비즈니스 로직, 트랜잭션 관리 |
| Repository | `com.taskhive.repository` | DB CRUD, Spring Data JPA 인터페이스 |
| Model | `com.taskhive.model` | JPA Entity (테이블 매핑) |
| DTO | `com.taskhive.dto` | API 요청/응답 객체 (Entity 노출 방지) |
| Config | `com.taskhive.config` | Security, JWT, CORS 설정 |

## 인증 흐름 요약

1. 클라이언트 → `POST /api/auth/login` → JWT 발급
2. 클라이언트 → 이후 모든 요청에 `Authorization: Bearer <jwt>` 헤더 포함
3. `JwtFilter` → 토큰 파싱 → SecurityContext 설정
4. Spring Security → 인가 결정

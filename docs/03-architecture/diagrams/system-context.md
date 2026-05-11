# 시스템 컨텍스트 다이어그램 (C4 Level 1)

## 외부 시스템 관계

```mermaid
C4Context
    title TaskHive 시스템 컨텍스트

    Person(user, "사용자", "팀원 또는 개인 사용자.\n브라우저로 TaskHive에 접근.")

    System(taskhive, "TaskHive", "팀 작업·프로젝트 통합 관리 플랫폼.\nSpring Boot + React + PostgreSQL.")

    System_Ext(github, "GitHub", "소스 코드 저장소 + CI/CD.\niee129/taskhive")

    System_Ext(docker, "Docker Hub / GHCR", "컨테이너 이미지 레지스트리.\n(예정)")

    Rel(user, taskhive, "사용", "HTTPS")
    Rel(taskhive, github, "소스 푸시 / CI 트리거", "Git + GitHub Actions")
    Rel(github, docker, "이미지 빌드·푸시", "GitHub Actions (예정)")
```

## 텍스트 설명

| 액터/시스템 | 역할 |
|------------|------|
| **사용자** | 브라우저로 React SPA에 접근하여 태스크·프로젝트를 관리 |
| **TaskHive** | 프론트엔드(React) + 백엔드(Spring Boot) + DB(PostgreSQL) |
| **GitHub** | 코드 저장, PR 관리, GitHub Actions CI 실행 |
| **GHCR** | Docker 이미지 저장소 (Phase 6 구현 예정) |

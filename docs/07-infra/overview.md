# 인프라 개요

## 환경별 스택

| 환경 | 오케스트레이션 | 목적 |
|------|--------------|------|
| 로컬 개발 | Docker Compose | 개발자 로컬 실행 |
| CI | GitHub Actions | 빌드·테스트 자동화 |
| 프로덕션 | Kubernetes | 배포·스케일링·자동복구 |

## 아키텍처 개요

```mermaid
graph TD
    subgraph Local["로컬 (Docker Compose)"]
        DC_PG[postgres:16]
        DC_BE[backend:8080]
        DC_FE[frontend:80]
    end

    subgraph K8s["Kubernetes (프로덕션)"]
        ING[nginx-ingress]
        subgraph NS["taskhive namespace"]
            FE_DEP[frontend Deployment\nreplicas=2]
            BE_DEP[backend Deployment\nreplicas=2]
            DB_STS[postgres StatefulSet\nreplicas=1]
            PVC[(PersistentVolumeClaim)]
        end
        ING --> FE_DEP
        ING --> BE_DEP
        BE_DEP --> DB_STS
        DB_STS --> PVC
    end

    DEV[개발자] -->|docker compose up| Local
    GHA[GitHub Actions] -->|kubectl apply| K8s
```

## 컨테이너 이미지

| 서비스 | Dockerfile 위치 | 베이스 이미지 |
|--------|----------------|-------------|
| backend | `backend/Dockerfile` | `eclipse-temurin:21-jre-alpine` |
| frontend | `frontend/Dockerfile` | `node:20-alpine` + `nginx:alpine` |
| postgres | Docker Hub 공식 | `postgres:16-alpine` |

## 포트 매핑

| 서비스 | 컨테이너 포트 | 로컬 노출 포트 |
|--------|-------------|-------------|
| postgres | 5432 | 5432 |
| backend | 8080 | 8080 |
| frontend | 80 | 3000 |

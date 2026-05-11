# TaskHive 🐝

> 벌집처럼 유기적으로 연결되는 팀 작업·프로젝트 통합 관리 플랫폼

## 기술 스택

| 레이어 | 기술 |
|--------|------|
| 백엔드 | Java 21 + Spring Boot 3 + Spring Security (JWT) |
| 프론트엔드 | React 18 + TypeScript 5 + Vite |
| 데이터베이스 | PostgreSQL 16 |
| 로컬 개발 | Docker Compose |
| 배포 인프라 | Kubernetes |
| CI | GitHub Actions |

## 프로젝트 구조

```
taskhive/
├── backend/        # Spring Boot 3 REST API (Java 21)
├── frontend/       # React 18 + TypeScript 5 (Vite)
├── k8s/            # Kubernetes 매니페스트
├── docker/         # docker-compose.yml
├── docs/           # 아키텍처 · API 문서
└── scripts/        # 빌드 · 배포 스크립트
```

## 로컬 실행

### Docker Compose (권장)

```bash
docker-compose -f docker/docker-compose.yml up -d
```

| 서비스 | URL |
|--------|-----|
| 프론트엔드 | http://localhost:3000 |
| 백엔드 API | http://localhost:8080 |
| 헬스체크 | http://localhost:8080/actuator/health |

### 개별 실행

```bash
# 백엔드
cd backend && ./mvnw spring-boot:run

# 프론트엔드
cd frontend && npm install && npm run dev
```

### Kubernetes 배포

```bash
kubectl apply -f k8s/namespace.yaml
kubectl create secret generic taskhive-secret \
  --from-literal=db-password=<PASSWORD> \
  --from-literal=jwt-secret=<SECRET> \
  -n taskhive
kubectl apply -f k8s/database/ -f k8s/backend/ -f k8s/frontend/ -f k8s/ingress.yaml
```

## 개발 로드맵

- [x] Phase 1 — 레포 및 골격 생성
- [ ] Phase 2 — Spring Boot REST API + JWT 인증
- [ ] Phase 3 — React UI + Axios 클라이언트
- [ ] Phase 4 — Docker Compose 통합
- [ ] Phase 5 — Kubernetes 매니페스트
- [ ] Phase 6 — GitHub Actions CI/CD

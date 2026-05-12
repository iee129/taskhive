# TaskHive 🐝

> 벌집처럼 유기적으로 연결되는 팀 작업·프로젝트 통합 관리 플랫폼

## 기술 스택

| 레이어 | 기술 |
|--------|------|
| 백엔드 | Java 21 · Spring Boot 3 · Spring Security · WebSocket STOMP |
| 인증 | JWT (Access 15분 + Refresh 7일 HttpOnly Cookie) · Role 기반 권한 |
| 프론트엔드 | React 18 · TypeScript 5 · Vite · Ant Design 5 · TanStack Query |
| 데이터베이스 | PostgreSQL 16 · Flyway 마이그레이션 |
| 캐시 | Redis 7 |
| AI | Ollama (llama3.2) — 태스크 자연어 파싱 · 요약 |
| 인프라 | Docker Compose · Kubernetes (minikube 검증) |
| CI/CD | GitHub Actions · GHCR 이미지 푸시 · Dependabot |
| 테스트 | JUnit 5 · Testcontainers · JaCoCo · Vitest · Playwright E2E |

## 주요 기능

- **태스크 관리** — CRUD · 상태/우선순위 필터 · 페이지네이션 · Soft Delete
- **칸반 보드** — 드래그 앤 드롭 (`@hello-pangea/dnd`) + WebSocket 실시간 동기화
- **프로젝트** — 프로젝트 단위 태스크 그룹핑
- **댓글 & 활동 이력** — AOP 기반 자동 Audit Log
- **AI 연동** — Ollama 자연어 태스크 파싱 · 일일 요약 다이제스트
- **통계 대시보드** — 완료율 · 기한 초과 · 우선순위별 집계
- **인증 고도화** — Refresh Token Rotation · 비밀번호 변경 · 로그아웃
- **UI/UX** — 다크모드 · 반응형 레이아웃 · 스켈레톤 · ErrorBoundary · 전역 알림

## 프로젝트 구조

```
taskhive/
├── auth/           # Spring Boot 3 REST API (Java 21)
├── frontend/       # React 18 + TypeScript 5 (Vite)
├── docker/         # docker-compose.yml (5-서비스 스택)
├── k8s/            # Kubernetes 매니페스트 + HPA
├── docs/           # 아키텍처 · API · 운영 문서
└── .github/        # GitHub Actions CI/CD · Dependabot
```

## 로컬 실행

### Docker Compose (권장)

```bash
cd docker
docker compose up -d
```

| 서비스 | URL |
|--------|-----|
| 프론트엔드 | http://localhost |
| 백엔드 API | http://localhost:8080 |
| 헬스체크 | http://localhost:8080/actuator/health |
| Swagger UI | http://localhost:8080/swagger-ui.html |

> 첫 실행 후 AI 모델 다운로드 (선택):
> ```bash
> docker exec taskhive-ollama ollama pull llama3.2
> ```

### 개별 실행

```bash
# 백엔드 (H2 인메모리 DB 사용)
cd auth && mvn spring-boot:run

# 프론트엔드
cd frontend && npm install && npm run dev
```

### Kubernetes 배포 (minikube)

```bash
minikube start --cpus=4 --memory=4096
minikube addons enable ingress metrics-server

kubectl apply -f k8s/namespace.yaml
# k8s/secret.example.yaml 복사 후 값 채우기
kubectl apply -f k8s/secret.yaml
kubectl apply -f k8s/database/ -f k8s/redis/ -f k8s/backend/ -f k8s/frontend/
kubectl apply -f k8s/ingress.yaml

echo "$(minikube ip) taskhive.local" | sudo tee -a /etc/hosts
# → http://taskhive.local 접속
```

## 개발 로드맵

- [x] Phase 1 — 프로젝트 골격
- [x] Phase 2 — Spring Boot REST API + JWT 인증
- [x] Phase 3 — React UI 기초
- [x] Phase 4 — 인증 고도화 (Refresh Token · Role)
- [x] Phase 5 — 아키텍처 고도화 (ErrorCode · Soft Delete · OpenAPI · MDC)
- [x] Phase 6 — 기능 확장 (칸반 · 댓글 · AI · 통계 · Audit Log)
- [x] Phase 6.5 — WebSocket STOMP 실시간 동기화
- [x] Phase 7 — 테스트 고도화 (Testcontainers · JaCoCo · Vitest · Playwright)
- [x] Phase 8 — 성능 최적화 (TanStack Query · N+1 제거 · Redis 캐싱)
- [x] Phase 9 — UI/UX 완성도 (다크모드 · 반응형 · 스켈레톤 · ErrorBoundary)
- [x] Phase 10 — PostgreSQL 전환 + Docker Compose 통합
- [x] Phase 11 — CI/CD (GitHub Actions · GHCR · Dependabot)
- [x] Phase 12 — Kubernetes 배포 (HPA · Ingress · minikube 검증)

## 테스트 실행

```bash
# 백엔드 (JaCoCo 커버리지 포함)
cd auth && mvn verify

# 프론트엔드 유닛 테스트
cd frontend && npm run test

# E2E (Playwright)
cd frontend && npm run test:e2e
```

## 문서

상세 설계 문서는 [`docs/`](docs/) 디렉토리를 참고하세요.

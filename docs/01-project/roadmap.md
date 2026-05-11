# 개발 로드맵

## Phase 1 — 프로젝트 골격 ✅ 완료

- GitHub 레포지토리 생성 (Public)
- 디렉토리 구조 구성 (backend / frontend / k8s / docker / docs)
- `.gitignore`, `README.md`, CI 워크플로우 초안

## Phase 2 — 백엔드 REST API + JWT 인증 ✅ 완료

- JPA Entity: `User`, `Project`, `Task`
- Repository, Service, Controller 3계층 구현
- JWT 발급·검증 (`JwtUtil`, `JwtFilter`, `SecurityConfig`)
- REST API: `POST /api/auth/register`, `POST /api/auth/login`
- REST API: `GET/POST/PUT/DELETE /api/tasks/*`
- Maven 컴파일 통과

## Phase 3 — 프론트엔드 React UI 🚧 예정

- Vite + React 18 + TypeScript 5 프로젝트 초기화
- React Router 라우팅 구성
- Axios 클라이언트 + JWT 인터셉터
- 태스크 목록 / 생성 / 수정 / 삭제 UI 페이지

## Phase 4 — Docker Compose 통합 🚧 예정

- `backend/Dockerfile` Multi-stage 빌드 검증
- `frontend/Dockerfile` Node → Nginx 빌드 검증
- `docker/docker-compose.yml` 3-컨테이너 스택 실행 검증

## Phase 5 — Kubernetes 배포 🚧 예정

- PostgreSQL StatefulSet + PVC
- Backend / Frontend Deployment + ClusterIP Service
- Nginx Ingress 라우팅 설정
- minikube 또는 kind 로컬 검증

## Phase 6 — CI/CD 고도화 🚧 예정

- GitHub Actions: Java 빌드 + React 빌드 + Docker 이미지 GHCR 푸시
- PAT `workflow` 스코프 추가 후 `.github/workflows/ci.yml` 푸시

## 향후 계획 (미정)

- 프로젝트 API (`/api/projects/*`) 구현
- 댓글 기능 (`/api/comments/*`)
- 실시간 알림 (WebSocket)
- OAuth2 소셜 로그인

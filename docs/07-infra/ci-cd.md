# CI/CD

## GitHub Actions 파이프라인

파일 위치: `.github/workflows/ci.yml`

> **현재 상태**: PAT에 `workflow` scope가 없어 미푸시 상태. 로컬에 파일만 존재.  
> 해결: GitHub → Settings → Developer Settings → Personal Access Tokens → `workflow` 체크 추가 후 push.

## CI 워크플로 (`ci.yml`)

```yaml
name: CI

on:
  push:
    branches: [master, main]
  pull_request:
    branches: [master, main]

jobs:
  backend-test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          java-version: '21'
          distribution: 'temurin'
      - name: Build & Test
        run: mvn test -f backend/pom.xml

  frontend-build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-node@v4
        with:
          node-version: '20'
          cache: 'npm'
          cache-dependency-path: frontend/package-lock.json
      - name: Install & Build
        run: |
          cd frontend
          npm ci
          npm run build
```

## CD 계획 (Phase 6)

```
코드 push → GitHub Actions CI 통과
  → Docker 이미지 빌드
  → GHCR(GitHub Container Registry) 푸시
  → kubectl set image (롤링 업데이트)
```

```yaml
  docker-build-push:
    needs: [backend-test, frontend-build]
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - name: Log in to GHCR
        uses: docker/login-action@v3
        with:
          registry: ghcr.io
          username: ${{ github.actor }}
          password: ${{ secrets.GITHUB_TOKEN }}
      - name: Build & Push backend
        uses: docker/build-push-action@v5
        with:
          context: ./backend
          push: true
          tags: ghcr.io/iee129/taskhive-backend:latest
```

## 브랜치 전략

| 브랜치 | 용도 | CI 트리거 |
|--------|------|----------|
| `master` | 프로덕션 배포 기준 | push + PR |
| `feature/*` | 기능 개발 | PR 시 |
| `docs` | 문서 전용 | push |
| `{feature-name}` | 단일 단어 기능 브랜치 | PR 시 |

## Secret 관리 (GitHub)

| Secret | 용도 |
|--------|------|
| `GITHUB_TOKEN` | GHCR 로그인 (자동 제공) |
| `KUBE_CONFIG` | K8s 클러스터 접근 (예정) |

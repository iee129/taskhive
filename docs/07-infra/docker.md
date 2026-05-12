# Docker 설정

## Docker Compose (통합 스택)

`docker/docker-compose.yml` — 5개 서비스:

```bash
# 전체 스택 실행 (docker/ 디렉토리에서)
docker compose up -d

# 로그 확인
docker compose logs -f backend

# 재빌드
docker compose up -d --build backend

# 중지 및 볼륨 삭제
docker compose down -v
```

| 서비스 | 이미지 | 포트 | 역할 |
|--------|--------|------|------|
| `postgres` | postgres:16-alpine | 5432 | 메인 DB, healthcheck 포함 |
| `redis` | redis:7-alpine | 6379 | 캐시 레이어, healthcheck 포함 |
| `ollama` | ollama/ollama | 11434 | AI 추론, `ollama_data` 볼륨 |
| `backend` | `../auth` 빌드 | 8080 | Spring Boot API |
| `frontend` | `../frontend` 빌드 | **80** | Nginx + React SPA |

> 프론트엔드는 `http://localhost`로 접근. Nginx가 `/api/*` 요청을 `backend:8080`으로 프록시.

### 시작 순서

```
postgres (healthy) ──┐
                     ├─→ backend ──→ frontend
redis   (healthy) ───┘
ollama  (started)
```

### 환경 변수 (backend)

| 변수 | 기본값 | 설명 |
|------|--------|------|
| `JWT_SECRET` | 로컬 개발용 값 | 운영에서 반드시 교체 |
| `CACHE_TYPE` | `redis` | Redis 캐시 활성화 |
| `REDIS_HOST` | `redis` | Compose 서비스명 |
| `CORS_ORIGINS` | `http://localhost` | 허용 origin |
| `OLLAMA_URL` | `http://ollama:11434` | AI 서비스 URL |

## Backend Dockerfile

`auth/Dockerfile` — Multi-stage (JDK → JRE, `eclipse-temurin:21-alpine`):

```dockerfile
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN apk add --no-cache maven && \
    mvn dependency:go-offline -q && \
    mvn package -DskipTests -q

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S taskhive && adduser -S taskhive -G taskhive
USER taskhive
COPY --from=builder /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

## Frontend Dockerfile

`frontend/Dockerfile` — Multi-stage (Node → Nginx):

```dockerfile
FROM node:20-alpine AS builder
WORKDIR /app
COPY package.json package-lock.json* ./
RUN npm ci
COPY . .
RUN npm run build

FROM nginx:alpine
COPY --from=builder /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
```

## Nginx 설정 (frontend/nginx.conf)

```nginx
server {
    listen 80;
    root /usr/share/nginx/html;
    index index.html;

    gzip on;
    gzip_types text/plain text/css application/json application/javascript text/xml application/xml image/svg+xml;
    gzip_min_length 1024;

    location / {
        try_files $uri $uri/ /index.html;
        add_header Cache-Control "no-cache, no-store, must-revalidate";
    }

    location /assets/ {
        expires 1y;
        add_header Cache-Control "public, immutable";
    }

    location /api/ {
        proxy_pass http://backend:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_read_timeout 60s;
    }
}
```

## Ollama 모델 초기화

첫 실행 시 AI 모델을 수동으로 내려받아야 합니다:

```bash
docker exec taskhive-ollama ollama pull llama3.2
```

이후 재시작에서는 `ollama_data` 볼륨에 캐시되므로 재다운로드 불필요.

# Docker 설정

## Docker Compose (로컬 개발)

`docker/docker-compose.yml`:

```yaml
version: '3.9'

services:
  postgres:
    image: postgres:16-alpine
    environment:
      POSTGRES_DB: taskhive
      POSTGRES_USER: taskhive
      POSTGRES_PASSWORD: taskhive
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U taskhive"]
      interval: 10s
      retries: 5

  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"
    healthcheck:
      test: ["CMD", "redis-cli", "ping"]
      interval: 10s
      retries: 5

  ollama:
    image: ollama/ollama
    ports:
      - "11434:11434"
    volumes:
      - ollama_data:/root/.ollama
    environment:
      - OLLAMA_KEEP_ALIVE=24h
    # 최초 실행 시 모델 풀: docker exec taskhive-ollama-1 ollama pull llama3.2:3b

  backend:
    build:
      context: ../auth
      dockerfile: Dockerfile
    ports:
      - "8080:8080"
    environment:
      SPRING_PROFILES_ACTIVE: dev
      JWT_SECRET: local-dev-secret-key-32chars-minimum
      CORS_ORIGINS: http://localhost:5173
      OLLAMA_BASE_URL: http://ollama:11434
      OLLAMA_MODEL: llama3.2:3b
    depends_on:
      postgres:
        condition: service_healthy
      redis:
        condition: service_healthy
      ollama:
        condition: service_started

  frontend:
    build:
      context: ../frontend
      dockerfile: Dockerfile
    ports:
      - "3000:80"
    depends_on:
      - backend

volumes:
  postgres_data:
  ollama_data:
```

## Backend Dockerfile

```dockerfile
# backend/Dockerfile
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -q
COPY src/ ./src/
RUN mvn package -DskipTests -q

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

## Frontend Dockerfile

```dockerfile
# frontend/Dockerfile
FROM node:20-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
RUN npm run build

FROM nginx:alpine
COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
```

## Nginx 설정 (frontend/nginx.conf)

```nginx
server {
    listen 80;

    location /api/ {
        proxy_pass http://backend:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }

    location / {
        root /usr/share/nginx/html;
        try_files $uri $uri/ /index.html;  # SPA 라우팅
    }
}
```

## 자주 쓰는 명령어

```bash
# 전체 스택 실행
docker compose -f docker/docker-compose.yml up -d

# 로그 확인
docker compose -f docker/docker-compose.yml logs -f backend

# 재빌드
docker compose -f docker/docker-compose.yml up -d --build backend

# 중지 및 볼륨 삭제
docker compose -f docker/docker-compose.yml down -v
```

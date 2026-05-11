# 환경 구성

## 환경 개요

| 환경 | 목적 | 실행 방법 |
|------|------|----------|
| **local** | 개발자 로컬 | `docker compose up` |
| **dev** | Spring Boot 프로파일 | `SPRING_PROFILES_ACTIVE=dev` |
| **prod** | K8s 배포 | `kubectl apply -f k8s/` |

## 환경별 설정 차이

| 설정 항목 | local/dev | prod |
|-----------|----------|------|
| DB URL | `jdbc:postgresql://localhost:5432/taskhive` | K8s Secret |
| JWT Secret | `.env` 파일 | K8s Secret |
| `ddl-auto` | `update` | `validate` (Flyway 적용 후) |
| `show-sql` | `true` | `false` |
| CORS Origin | `http://localhost:3000` | `https://taskhive.example.com` |
| 로그 레벨 | `DEBUG` | `INFO` |
| Replicas | 1 | 2 |

## 로컬 환경 설정

### .env (docker compose)

```bash
# docker/.env (git 제외)
JWT_SECRET=local-dev-secret-key-at-least-32-chars
POSTGRES_PASSWORD=taskhive
CORS_ORIGINS=http://localhost:3000
```

### application-dev.yml

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/taskhive
    username: taskhive
    password: taskhive
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
logging:
  level:
    com.taskhive: DEBUG
```

## 프로덕션 환경 설정

### K8s ConfigMap (비민감 설정)

```yaml
# k8s/backend/configmap.yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: backend-config
  namespace: taskhive
data:
  SPRING_PROFILES_ACTIVE: "prod"
  CORS_ORIGINS: "https://taskhive.example.com"
  DATABASE_URL: "jdbc:postgresql://postgres-service:5432/taskhive"
```

### K8s Secret (민감 설정)

```bash
kubectl create secret generic taskhive-secrets \
  --from-literal=JWT_SECRET=<strong-random-secret> \
  --from-literal=DATABASE_PASSWORD=<strong-password> \
  -n taskhive
```

## 환경 변수 우선순위

Spring Boot 설정 우선순위 (높음 → 낮음):
1. 환경 변수 (`JWT_SECRET`, `DATABASE_URL`)
2. `application-{profile}.yml`
3. `application.yml`

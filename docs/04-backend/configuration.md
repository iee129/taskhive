# 설정 관리

## 설정 파일 구조

```
src/main/resources/
├── application.yml          # 공통 (모든 프로파일)
└── application-dev.yml      # 개발 전용 (로컬 PostgreSQL)
```

프로파일 활성화: `SPRING_PROFILES_ACTIVE=dev` 환경 변수 또는  
`java -jar app.jar --spring.profiles.active=dev`

## application.yml (공통)

```yaml
spring:
  application:
    name: taskhive

jwt:
  secret: ${JWT_SECRET}          # 필수 환경 변수
  expiration: 86400000           # 24시간 (ms)

cors:
  origins: ${CORS_ORIGINS:http://localhost:3000}  # 기본값 포함

management:
  endpoints:
    web:
      exposure:
        include: health
```

## application-dev.yml (개발)

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/taskhive
    username: taskhive
    password: taskhive
    driver-class-name: org.postgresql.Driver
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
    properties:
      hibernate:
        format_sql: true
```

## 환경 변수 목록

| 변수 | 필수 | 설명 | 예시 |
|------|------|------|------|
| `JWT_SECRET` | 필수 | HMAC-SHA256 서명 키 (32자 이상) | `supersecretkey123456789012345678` |
| `SPRING_PROFILES_ACTIVE` | 권장 | 활성 프로파일 | `dev` |
| `CORS_ORIGINS` | 선택 | 허용 Origin (쉼표 구분) | `http://localhost:3000` |
| `DATABASE_URL` | 프로덕션 | JDBC URL | `jdbc:postgresql://db:5432/taskhive` |
| `DATABASE_USERNAME` | 프로덕션 | DB 사용자 | `taskhive` |
| `DATABASE_PASSWORD` | 프로덕션 | DB 비밀번호 | K8s Secret으로 주입 |

## Secret 주입 방식별 가이드

### 로컬 개발 (.env 파일)
```bash
# .env (절대 커밋 금지)
JWT_SECRET=local-dev-secret-key-32chars-min
SPRING_PROFILES_ACTIVE=dev
```

### Docker Compose
```yaml
services:
  backend:
    env_file:
      - .env
```

### Kubernetes
```bash
kubectl create secret generic taskhive-secrets \
  --from-literal=JWT_SECRET=<secret> \
  --from-literal=DATABASE_PASSWORD=<password> \
  -n taskhive
```

ConfigMap (`k8s/backend/configmap.yaml`)에는 비민감 설정만:
```yaml
data:
  SPRING_PROFILES_ACTIVE: "prod"
  CORS_ORIGINS: "https://taskhive.example.com"
```

## Actuator 보안

헬스 엔드포인트(`/actuator/health`)만 공개 노출.  
프로덕션에서는 Actuator 포트를 내부 네트워크로 제한 권장:
```yaml
management:
  server:
    port: 8081   # 외부 Ingress에서 제외
```

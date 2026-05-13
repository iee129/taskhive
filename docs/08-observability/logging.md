# 로깅

## 백엔드 로깅 (SLF4J + Logback)

Spring Boot 기본 로깅 프레임워크: **SLF4J + Logback**

```java
@Service
public class AuthService {
    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    public AuthResponse register(RegisterRequest request) {
        log.info("회원가입 시도: email={}", request.email());
        // ...
        log.info("회원가입 완료: email={}", request.email());
        return response;
    }
}
```

## 로그 레벨 설정

```yaml
# application-dev.yml
logging:
  level:
    root: INFO
    com.taskhive: DEBUG          # 애플리케이션 코드 상세
    org.springframework.security: DEBUG  # 시큐리티 필터 추적
    org.hibernate.SQL: DEBUG     # 실행된 SQL 출력

# application-prod.yml (예정)
logging:
  level:
    root: WARN
    com.taskhive: INFO
```

## 로그 형식

### 개발 환경 (콘솔)
```
2026-05-12 10:30:00.123  INFO 1234 --- [main] c.t.service.AuthService : 회원가입 완료: email=user@example.com
```

### 프로덕션 (JSON 구조적 로깅 예정)
```json
{
  "timestamp": "2026-05-12T10:30:00.123Z",
  "level": "INFO",
  "logger": "com.taskhive.service.AuthService",
  "message": "회원가입 완료: email=user@example.com",
  "thread": "http-nio-8080-exec-1"
}
```

JSON 로깅은 `logstash-logback-encoder` 의존성 추가 후 활성화 (Phase 6 예정).

## 로그 수집 계획 (Phase 6)

```
K8s Pod 로그
  → Fluentd / Fluent Bit (DaemonSet)
    → Elasticsearch
      → Kibana (대시보드)
```

또는 경량 대안:
```
K8s Pod 로그 → Loki → Grafana
```

## MDC 요청 추적 (requestId)

`RequestIdFilter`가 모든 요청에 `requestId`를 MDC에 주입합니다 (Phase 5~).

```
클라이언트 → X-Request-Id: abc12345 헤더 전송 (선택)
           ← X-Request-Id: abc12345 응답 헤더에도 포함
```

- 클라이언트가 `X-Request-Id`를 보내면 그대로 사용
- 없으면 UUID 앞 8자리를 서버에서 자동 생성
- 에러 응답 `ErrorResponse.requestId` 필드에도 포함

**logback 패턴에 requestId 추가:**

```xml
<pattern>%d{HH:mm:ss} %-5level [%X{requestId}] %logger{36} - %msg%n</pattern>
```

**로그 출력 예시:**

```
10:30:00 INFO  [a1b2c3d4] c.t.service.TaskService - 태스크 생성 완료: id=5
10:30:00 ERROR [a1b2c3d4] c.t.e.GlobalExceptionHandler - TASK_NOT_FOUND
```

## 보안 로깅 원칙

- 비밀번호, JWT 토큰 절대 로그 기록 금지
- 이메일은 INFO 레벨 이하에서만 기록
- 에러 스택 트레이스는 서버 로그에만 — 응답 본문 포함 금지
- 민감 파라미터는 `[REDACTED]`로 마스킹

```java
// 올바른 예
log.debug("로그인 시도: email={}", email);

// 금지
log.debug("로그인: email={}, password={}", email, password);
```

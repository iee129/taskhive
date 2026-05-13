# 헬스체크

## Spring Boot Actuator

`/actuator/health` 엔드포인트는 K8s Probe와 로드밸런서가 사용.

### 설정

```yaml
# application.yml
management:
  endpoints:
    web:
      exposure:
        include: health
  endpoint:
    health:
      show-details: never    # 외부 노출 시 내부 정보 숨김
```

### 응답 형식

```json
{"status": "UP"}
```

DB 연결 포함 상세 응답 (내부 네트워크 전용):
```json
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP",
      "details": { "database": "PostgreSQL", "validationQuery": "isValid()" }
    },
    "diskSpace": { "status": "UP" }
  }
}
```

## Kubernetes Probe 설정

```yaml
# k8s/backend/deployment.yaml
livenessProbe:
  httpGet:
    path: /actuator/health
    port: 8080
  initialDelaySeconds: 30   # JVM 기동 대기
  periodSeconds: 10
  failureThreshold: 3

readinessProbe:
  httpGet:
    path: /actuator/health
    port: 8080
  initialDelaySeconds: 15
  periodSeconds: 5
  failureThreshold: 3
```

| Probe | 실패 시 동작 | 목적 |
|-------|-----------|------|
| `livenessProbe` | 파드 재시작 | 데드락·무한루프 감지 |
| `readinessProbe` | 트래픽 차단 | 배포 중 불완전 파드 보호 |

## 수동 헬스체크

```bash
# 로컬
curl http://localhost:8080/actuator/health

# K8s 파드 내부
kubectl exec -n taskhive deploy/backend -- \
  wget -qO- http://localhost:8080/actuator/health
```

## 프론트엔드 헬스체크

Nginx는 별도 헬스 엔드포인트 없음.  
K8s `readinessProbe`는 루트 경로(`/`)로 200 응답 확인:

```yaml
readinessProbe:
  httpGet:
    path: /
    port: 80
  initialDelaySeconds: 5
  periodSeconds: 10
```

# 모니터링

## 현재 상태

MVP 단계에서는 Spring Boot Actuator의 `/actuator/health`만 활성화.  
전체 메트릭·알림 시스템은 **Phase 6** 도입 예정.

## Spring Boot Actuator 메트릭 (Phase 6)

```yaml
# application.yml — Phase 6 활성화 예정
management:
  endpoints:
    web:
      exposure:
        include: health, metrics, prometheus
  metrics:
    export:
      prometheus:
        enabled: true
```

`/actuator/prometheus` 엔드포인트가 Prometheus 형식으로 메트릭 노출:
- JVM 힙 사용량
- HTTP 요청 수 / 응답 시간 (p50, p95, p99)
- DB 커넥션 풀 상태
- 활성 스레드 수

## 모니터링 스택 계획 (Phase 6)

```
Spring Boot /actuator/prometheus
  → Prometheus (메트릭 수집, 15s 간격)
    → Grafana (대시보드 시각화)
      → AlertManager (임계값 초과 시 Slack/이메일 알림)
```

K8s 배포:
```yaml
# Prometheus + Grafana: kube-prometheus-stack Helm Chart
helm repo add prometheus-community https://prometheus-community.github.io/helm-charts
helm install monitoring prometheus-community/kube-prometheus-stack -n monitoring
```

## 핵심 모니터링 지표

| 지표 | 임계값 (예시) | 알림 조건 |
|------|------------|----------|
| HTTP 5xx 비율 | < 1% | 5분간 1% 초과 |
| API p99 응답 시간 | < 500ms | 10분간 초과 |
| JVM 힙 사용률 | < 80% | 5분간 80% 초과 |
| DB 커넥션 풀 | < 90% | 90% 초과 |
| Pod restart 수 | 0 | 1회 이상 |

## Grafana 대시보드 계획

| 대시보드 | 내용 |
|---------|------|
| Spring Boot | JVM, HTTP, DB 커넥션 |
| Kubernetes | CPU, 메모리, Pod 상태 |
| PostgreSQL | 쿼리 시간, 연결 수, 테이블 크기 |
| Business | 가입자 수, 태스크 생성 수 (커스텀) |

## 로그 + 메트릭 통합 (예정)

```
로그: Loki ← Promtail ← K8s Pod 로그
메트릭: Prometheus ← Actuator
트레이스: Tempo ← Spring Boot Micrometer Tracing (예정)
대시보드: Grafana (모두 통합)
```

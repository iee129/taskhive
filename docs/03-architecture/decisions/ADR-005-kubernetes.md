# ADR-005: Kubernetes 배포 플랫폼 선택

**날짜**: 2026-05-12  
**상태**: 수락됨

## Context (배경)

프로덕션 배포 환경이 필요합니다. 컨테이너화는 Docker로 결정되어 있으며, 오케스트레이션 레이어를 선택해야 합니다.

## Decision (결정)

**Kubernetes (k8s)** 를 배포 플랫폼으로 선택합니다. 로컬 검증은 minikube 또는 kind를 사용합니다.

## Alternatives (고려한 대안)

| 대안 | 장점 | 단점 |
|------|------|------|
| Docker Compose (단독) | 설정 단순 | 프로덕션 HA 불가, 자동 복구 없음 |
| Docker Swarm | Docker 통합 쉬움 | 생태계 축소, K8s 대비 기능 제한 |
| Kubernetes | 업계 표준, 수평 확장, 자동 복구 | 러닝 커브 높음, 로컬 설정 복잡 |

## Consequences (결과)

- 로컬: `docker-compose.yml` 로 개발, K8s는 배포 전용
- Deployment replicas=2 로 무중단 롤링 업데이트
- PostgreSQL: StatefulSet + PVC 로 데이터 영속성 보장
- Ingress: nginx-ingress 로 `/api/*` → backend, `/*` → frontend 라우팅
- Secret 관리: `kubectl create secret` 로 직접 생성 (git 커밋 금지)

## 구현 파일

- `k8s/namespace.yaml`
- `k8s/backend/deployment.yaml`, `service.yaml`, `configmap.yaml`
- `k8s/frontend/deployment.yaml`, `service.yaml`
- `k8s/database/statefulset.yaml`, `service.yaml`
- `k8s/ingress.yaml`

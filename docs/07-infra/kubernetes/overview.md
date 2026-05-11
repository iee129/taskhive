# Kubernetes 개요

## 클러스터 구성

```
taskhive (namespace)
├── frontend      Deployment (replicas=2) + Service (ClusterIP)
├── backend       Deployment (replicas=2) + Service (ClusterIP) + ConfigMap
├── postgres      StatefulSet (replicas=1) + Service (ClusterIP) + PVC
└── ingress       nginx-ingress → frontend / backend 라우팅
```

## 파일 구조

```
k8s/
├── namespace.yaml
├── backend/
│   ├── deployment.yaml
│   ├── service.yaml
│   └── configmap.yaml
├── frontend/
│   ├── deployment.yaml
│   └── service.yaml
├── database/
│   ├── statefulset.yaml
│   └── service.yaml
└── ingress.yaml
```

## 로컬 검증 (minikube)

```bash
# minikube 시작
minikube start --driver=docker

# Ingress 애드온 활성화
minikube addons enable ingress

# 전체 적용
kubectl apply -f k8s/

# 상태 확인
kubectl get all -n taskhive

# Ingress IP 확인
minikube ip
```

## 배포 원칙

| 항목 | 설정 | 이유 |
|------|------|------|
| backend replicas | 2 | Stateless → 무중단 롤링 업데이트 |
| frontend replicas | 2 | 정적 파일 서빙 → 수평 확장 용이 |
| postgres replicas | 1 | StatefulSet — 단일 인스턴스 (고가용성은 Phase 7+ 검토) |
| 롤링 업데이트 | maxSurge=1, maxUnavailable=0 | 무중단 배포 보장 |
| Resource Limits | CPU/Memory 명시 | 노드 리소스 보호 |

## 헬스체크 연결

Kubernetes의 `livenessProbe` / `readinessProbe`는 `/actuator/health` 사용:

```yaml
livenessProbe:
  httpGet:
    path: /actuator/health
    port: 8080
  initialDelaySeconds: 30
  periodSeconds: 10

readinessProbe:
  httpGet:
    path: /actuator/health
    port: 8080
  initialDelaySeconds: 15
  periodSeconds: 5
```

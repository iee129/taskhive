# Kubernetes 배포 가이드

## 사전 요건

- [minikube](https://minikube.sigs.k8s.io/) v1.32+
- [kubectl](https://kubernetes.io/docs/tasks/tools/) v1.29+
- Docker (minikube 드라이버)

## 매니페스트 구조

```
k8s/
  namespace.yaml              # taskhive 네임스페이스
  secret.example.yaml         # Secret 템플릿 (복사 후 값 채워 사용)
  ingress.yaml                # Nginx Ingress (taskhive.local)
  database/
    statefulset.yaml          # PostgreSQL StatefulSet + PVC (5Gi)
    service.yaml              # Headless ClusterIP
  redis/
    deployment.yaml           # Redis 7-alpine
    service.yaml              # ClusterIP
  backend/
    configmap.yaml            # 환경변수 (DataSource, Redis, CORS, Ollama)
    deployment.yaml           # Spring Boot, replicas: 2, health probe
    service.yaml              # ClusterIP :8080
    hpa.yaml                  # HPA — CPU 60% / Memory 70%, max 6 pod
  frontend/
    deployment.yaml           # Nginx + React SPA, replicas: 2
    service.yaml              # ClusterIP :80
```

## minikube 로컬 배포

### 1. minikube 시작

```bash
minikube start --cpus=4 --memory=4096
minikube addons enable ingress
minikube addons enable metrics-server   # HPA 작동에 필요
```

### 2. GHCR 이미지를 minikube에 로드

```bash
# minikube Docker 환경으로 전환
eval $(minikube docker-env)

# 이미지 빌드 (또는 GHCR에서 pull)
docker build -t ghcr.io/iee129/taskhive-backend:latest ./auth
docker build -t ghcr.io/iee129/taskhive-frontend:latest ./frontend
```

### 3. Secret 생성

```bash
cp k8s/secret.example.yaml k8s/secret.yaml
# k8s/secret.yaml 의 base64 값을 실제 값으로 교체한 뒤:
kubectl apply -f k8s/secret.yaml
```

### 4. 전체 스택 배포

```bash
kubectl apply -f k8s/namespace.yaml
kubectl apply -f k8s/database/
kubectl apply -f k8s/redis/
kubectl apply -f k8s/backend/
kubectl apply -f k8s/frontend/
kubectl apply -f k8s/ingress.yaml
```

### 5. /etc/hosts 등록

```bash
echo "$(minikube ip) taskhive.local" | sudo tee -a /etc/hosts
```

### 6. 배포 검증

```bash
# 모든 Pod 정상 기동 확인
kubectl get pods -n taskhive

# HPA 상태 확인
kubectl get hpa -n taskhive

# Ingress IP 확인
kubectl get ingress -n taskhive

# 브라우저 또는 curl로 접속
curl http://taskhive.local/api/actuator/health
```

### 7. 정리

```bash
kubectl delete namespace taskhive
minikube stop
```

## 롤링 업데이트

```bash
# 새 이미지 태그로 업데이트 (무중단)
kubectl set image deployment/backend \
  backend=ghcr.io/iee129/taskhive-backend:sha-<commit> \
  -n taskhive
```

## 주의사항

- `secret.yaml`은 `.gitignore`에 추가해 절대 커밋 금지
- HPA는 `metrics-server` 활성화 필요 (`minikube addons enable metrics-server`)
- Ollama는 GPU 리소스 제약으로 k8s 매니페스트에서 제외 — Docker Compose 환경에서 운영

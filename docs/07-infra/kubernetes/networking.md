# Kubernetes 네트워킹

## Ingress 구성

```yaml
# k8s/ingress.yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: taskhive-ingress
  namespace: taskhive
  annotations:
    nginx.ingress.kubernetes.io/rewrite-target: /
spec:
  ingressClassName: nginx
  rules:
    - host: taskhive.example.com
      http:
        paths:
          - path: /api
            pathType: Prefix
            backend:
              service:
                name: backend-service
                port:
                  number: 8080
          - path: /
            pathType: Prefix
            backend:
              service:
                name: frontend-service
                port:
                  number: 80
```

## 라우팅 규칙

| 경로 | 대상 서비스 | 설명 |
|------|-----------|------|
| `/api/*` | `backend-service:8080` | REST API |
| `/*` | `frontend-service:80` | React SPA |
| `/actuator/*` | 외부 미노출 | 내부 네트워크만 |

## Service 유형

| 서비스 | 유형 | 이유 |
|--------|------|------|
| `frontend-service` | ClusterIP | Ingress를 통해서만 외부 접근 |
| `backend-service` | ClusterIP | Ingress + frontend만 내부 접근 |
| `postgres-service` | ClusterIP (Headless) | StatefulSet 전용, 외부 노출 금지 |

## 네임스페이스 격리

```yaml
# k8s/namespace.yaml
apiVersion: v1
kind: Namespace
metadata:
  name: taskhive
```

모든 리소스는 `taskhive` 네임스페이스에 배포 — 다른 앱과 격리.

## TLS (예정)

```yaml
# cert-manager + Let's Encrypt 적용 예정
spec:
  tls:
    - hosts:
        - taskhive.example.com
      secretName: taskhive-tls
  rules:
    - host: taskhive.example.com
```

## DNS

로컬 minikube 테스트 시:
```bash
echo "$(minikube ip) taskhive.local" | sudo tee -a /etc/hosts
```

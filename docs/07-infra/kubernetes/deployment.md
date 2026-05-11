# Kubernetes Deployment

## backend/deployment.yaml

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: backend
  namespace: taskhive
spec:
  replicas: 2
  selector:
    matchLabels:
      app: backend
  strategy:
    type: RollingUpdate
    rollingUpdate:
      maxSurge: 1
      maxUnavailable: 0
  template:
    metadata:
      labels:
        app: backend
    spec:
      containers:
        - name: backend
          image: ghcr.io/iee129/taskhive-backend:latest
          ports:
            - containerPort: 8080
          envFrom:
            - configMapRef:
                name: backend-config
          env:
            - name: JWT_SECRET
              valueFrom:
                secretKeyRef:
                  name: taskhive-secrets
                  key: JWT_SECRET
            - name: DATABASE_PASSWORD
              valueFrom:
                secretKeyRef:
                  name: taskhive-secrets
                  key: DATABASE_PASSWORD
          resources:
            requests:
              cpu: "250m"
              memory: "256Mi"
            limits:
              cpu: "500m"
              memory: "512Mi"
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

## backend/service.yaml

```yaml
apiVersion: v1
kind: Service
metadata:
  name: backend-service
  namespace: taskhive
spec:
  selector:
    app: backend
  ports:
    - port: 8080
      targetPort: 8080
  type: ClusterIP
```

## frontend/deployment.yaml

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: frontend
  namespace: taskhive
spec:
  replicas: 2
  selector:
    matchLabels:
      app: frontend
  template:
    metadata:
      labels:
        app: frontend
    spec:
      containers:
        - name: frontend
          image: ghcr.io/iee129/taskhive-frontend:latest
          ports:
            - containerPort: 80
          resources:
            requests:
              cpu: "100m"
              memory: "64Mi"
            limits:
              cpu: "200m"
              memory: "128Mi"
```

## 배포 명령어

```bash
# 이미지 태그 업데이트 (롤링 배포)
kubectl set image deployment/backend \
  backend=ghcr.io/iee129/taskhive-backend:v1.1.0 \
  -n taskhive

# 배포 상태 확인
kubectl rollout status deployment/backend -n taskhive

# 롤백
kubectl rollout undo deployment/backend -n taskhive
```

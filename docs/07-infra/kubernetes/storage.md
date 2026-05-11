# Kubernetes 스토리지

## PostgreSQL StatefulSet + PVC

```yaml
# k8s/database/statefulset.yaml
apiVersion: apps/v1
kind: StatefulSet
metadata:
  name: postgres
  namespace: taskhive
spec:
  serviceName: postgres-service
  replicas: 1
  selector:
    matchLabels:
      app: postgres
  template:
    metadata:
      labels:
        app: postgres
    spec:
      containers:
        - name: postgres
          image: postgres:16-alpine
          ports:
            - containerPort: 5432
          env:
            - name: POSTGRES_DB
              value: taskhive
            - name: POSTGRES_USER
              value: taskhive
            - name: POSTGRES_PASSWORD
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
          volumeMounts:
            - name: postgres-data
              mountPath: /var/lib/postgresql/data
  volumeClaimTemplates:
    - metadata:
        name: postgres-data
      spec:
        accessModes: ["ReadWriteOnce"]
        resources:
          requests:
            storage: 5Gi
```

## Headless Service

```yaml
# k8s/database/service.yaml
apiVersion: v1
kind: Service
metadata:
  name: postgres-service
  namespace: taskhive
spec:
  clusterIP: None    # Headless — StatefulSet DNS 안정성
  selector:
    app: postgres
  ports:
    - port: 5432
      targetPort: 5432
```

## 스토리지 설계 원칙

| 항목 | 선택 | 이유 |
|------|------|------|
| StatefulSet | O | 파드 재시작 시 동일 PVC 재마운트 보장 |
| PVC | ReadWriteOnce | 단일 파드만 쓰기 — PostgreSQL 단일 인스턴스 |
| 용량 | 5Gi | MVP 초기값 — 필요 시 PVC Resize |
| 백업 | 미구현 | Phase 6+: pg_dump CronJob 예정 |

## 데이터 영속성 보증

- 파드 재시작: PVC 유지 → 데이터 보존
- StatefulSet 삭제 후 재생성: PVC 잔존 → `kubectl apply` 시 재마운트
- 노드 장애: PVC는 노드가 아닌 클러스터 스토리지에 바인딩

## 백업 계획 (Phase 6)

```yaml
# CronJob: 매일 새벽 3시 pg_dump
apiVersion: batch/v1
kind: CronJob
metadata:
  name: postgres-backup
spec:
  schedule: "0 3 * * *"
  jobTemplate:
    spec:
      template:
        spec:
          containers:
            - name: backup
              image: postgres:16-alpine
              command: ["pg_dump", "-h", "postgres-service", "taskhive"]
```

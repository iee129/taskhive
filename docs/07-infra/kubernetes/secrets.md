# Kubernetes Secret 관리

## Secret 생성

```bash
kubectl create secret generic taskhive-secrets \
  --from-literal=JWT_SECRET=<32자-이상-랜덤-문자열> \
  --from-literal=DATABASE_PASSWORD=<강력한-비밀번호> \
  -n taskhive
```

Secret은 etcd에 base64 인코딩으로 저장됨. **절대 git에 커밋 금지.**

## 현재 Secret 목록

| Secret 이름 | Key | 용도 |
|------------|-----|------|
| `taskhive-secrets` | `JWT_SECRET` | JWT 서명 키 |
| `taskhive-secrets` | `DATABASE_PASSWORD` | PostgreSQL 비밀번호 |

## Deployment에서 Secret 참조

```yaml
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
```

## Secret 조회 및 관리

```bash
# Secret 목록 확인
kubectl get secrets -n taskhive

# Secret 값 확인 (base64 디코딩)
kubectl get secret taskhive-secrets -n taskhive \
  -o jsonpath='{.data.JWT_SECRET}' | base64 -d

# Secret 업데이트 (삭제 후 재생성)
kubectl delete secret taskhive-secrets -n taskhive
kubectl create secret generic taskhive-secrets \
  --from-literal=JWT_SECRET=<new-secret> \
  --from-literal=DATABASE_PASSWORD=<new-password> \
  -n taskhive

# Deployment 롤링 재시작 (Secret 반영)
kubectl rollout restart deployment/backend -n taskhive
```

## 보안 강화 방안 (Phase 6+)

| 방안 | 설명 |
|------|------|
| etcd 암호화 | `EncryptionConfiguration`으로 etcd 저장 암호화 |
| External Secrets Operator | AWS Secrets Manager / Vault 연동 |
| Sealed Secrets | 암호화된 Secret을 git에 안전하게 커밋 |
| RBAC | Secret 접근 권한을 필요한 ServiceAccount로만 제한 |

## .gitignore 확인

```gitignore
# Secret 파일 — 절대 커밋 금지
.env
.env.local
docker/.env
k8s/secrets*.yaml
*-secret.yaml
```

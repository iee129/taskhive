# 트러블슈팅

## 백엔드

### 포트 8080 이미 사용 중

```
Web server failed to start. Port 8080 was already in use.
```

```bash
# 사용 중인 프로세스 확인
lsof -ti:8080
# 종료
kill -9 $(lsof -ti:8080)
# 또는 다른 포트로 실행
mvn spring-boot:run -Dspring-boot.run.arguments=--server.port=8081
```

### JWT_SECRET 미설정

```
Could not resolve placeholder 'JWT_SECRET' in value "${JWT_SECRET}"
```

환경 변수 확인:
```bash
echo $JWT_SECRET
export JWT_SECRET=local-dev-secret-key-at-least-32-chars
```

### DB 연결 실패

```
Connection to localhost:5432 refused
```

```bash
# PostgreSQL 실행 여부 확인
pg_isready -h localhost -p 5432

# Docker Compose 사용 중이면
docker compose -f docker/docker-compose.yml ps
docker compose -f docker/docker-compose.yml logs postgres
```

### 401 Unauthorized

- `Authorization: Bearer ` 헤더가 누락되었거나 토큰이 만료됨
- 재로그인 후 새 토큰으로 요청

---

## 프론트엔드

### npm install 실패

```bash
# 캐시 정리 후 재시도
npm cache clean --force
rm -rf node_modules package-lock.json
npm install
```

### API 요청 CORS 오류

개발 서버에서 `http://localhost:8080`으로 직접 요청하지 말고 Vite dev server(`http://localhost:3000`)를 통해 요청해야 함.  
`vite.config.ts`의 proxy 설정 확인.

---

## Docker

### 이미지 빌드 실패

```bash
# 캐시 없이 재빌드
docker compose -f docker/docker-compose.yml build --no-cache backend
```

### postgres 컨테이너 헬스체크 실패

```bash
docker compose -f docker/docker-compose.yml logs postgres
# 볼륨 초기화
docker compose -f docker/docker-compose.yml down -v
docker compose -f docker/docker-compose.yml up -d
```

---

## Kubernetes

### 파드가 Pending 상태

```bash
kubectl describe pod <pod-name> -n taskhive
# Events 섹션에서 원인 확인 (리소스 부족, PVC 미바인딩 등)
```

### Secret 없어서 파드 CrashLoopBackOff

```bash
kubectl get events -n taskhive | grep -i secret
# Secret 재생성
kubectl create secret generic taskhive-secrets \
  --from-literal=JWT_SECRET=<값> \
  --from-literal=DATABASE_PASSWORD=<값> \
  -n taskhive
kubectl rollout restart deployment/backend -n taskhive
```

### minikube Ingress 미작동

```bash
minikube addons enable ingress
kubectl get pods -n ingress-nginx  # Running 상태 확인
```

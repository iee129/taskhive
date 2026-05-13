# 빠른 시작 가이드

5분 안에 TaskHive를 로컬에서 실행합니다.

## 전제 조건

| 도구 | 버전 | 확인 명령 |
|------|------|----------|
| Docker Desktop | 24.x+ | `docker --version` |
| Docker Compose | 2.x+ | `docker compose version` |
| Git | 2.x+ | `git --version` |

## 1. 저장소 클론

```bash
git clone https://github.com/iee129/taskhive.git
cd taskhive
```

## 2. 환경 변수 설정

```bash
cp docker/.env.example docker/.env
# .env 파일을 열어 JWT_SECRET을 32자 이상 랜덤 문자열로 변경
```

`.env.example`이 없다면:
```bash
cat > docker/.env << 'EOF'
JWT_SECRET=local-dev-secret-key-at-least-32-chars
POSTGRES_PASSWORD=taskhive
CORS_ORIGINS=http://localhost:3000
EOF
```

## 3. 실행

```bash
docker compose -f docker/docker-compose.yml up -d
```

## 4. 동작 확인

```bash
# 헬스체크
curl http://localhost:8080/actuator/health
# {"status":"UP"}

# 회원가입
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"me@example.com","password":"pass1234","name":"테스터"}'
```

브라우저에서 `http://localhost:3000` 접속 (Phase 3 이후 프론트엔드 활성화).

## 5. 중지

```bash
docker compose -f docker/docker-compose.yml down
# 데이터 포함 완전 삭제:
docker compose -f docker/docker-compose.yml down -v
```

## 다음 단계

- API 전체 명세 → [05-api/overview.md](../05-api/overview.md)
- 로컬 개발 상세 설정 → [local-setup.md](local-setup.md)
- 환경 변수 전체 목록 → [env-variables.md](env-variables.md)

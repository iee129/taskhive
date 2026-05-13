# 환경 변수 전체 목록

## 백엔드 환경 변수

| 변수 | 필수 | 기본값 | 설명 |
|------|------|--------|------|
| `JWT_SECRET` | 필수 | 없음 | JWT HMAC-SHA256 서명 키. **32자 이상** |
| `SPRING_PROFILES_ACTIVE` | 권장 | 없음 | `dev` (로컬), `prod` (K8s) |
| `CORS_ORIGINS` | 선택 | `http://localhost:3000` | 허용 Origin (쉼표 구분) |
| `DATABASE_URL` | 프로덕션 | application-dev.yml | JDBC URL |
| `DATABASE_USERNAME` | 프로덕션 | `taskhive` | DB 사용자 |
| `DATABASE_PASSWORD` | 프로덕션 | 없음 | DB 비밀번호 |
| `SERVER_PORT` | 선택 | `8080` | 백엔드 포트 |

## 프론트엔드 환경 변수 (Vite)

| 변수 | 기본값 | 설명 |
|------|--------|------|
| `VITE_API_BASE_URL` | `/api` | API 경로 (Nginx 프록시 기준) |
| `VITE_APP_TITLE` | `TaskHive` | 브라우저 탭 제목 |

## Docker Compose (.env)

```bash
# docker/.env
JWT_SECRET=local-dev-secret-key-at-least-32-chars
POSTGRES_PASSWORD=taskhive
CORS_ORIGINS=http://localhost:3000
```

## Kubernetes (Secret + ConfigMap)

### Secret (민감값)
```bash
kubectl create secret generic taskhive-secrets \
  --from-literal=JWT_SECRET=<값> \
  --from-literal=DATABASE_PASSWORD=<값> \
  -n taskhive
```

### ConfigMap (비민감값)
```yaml
data:
  SPRING_PROFILES_ACTIVE: "prod"
  CORS_ORIGINS: "https://taskhive.example.com"
  DATABASE_URL: "jdbc:postgresql://postgres-service:5432/taskhive"
  DATABASE_USERNAME: "taskhive"
```

## JWT_SECRET 생성 방법

```bash
# macOS / Linux — 랜덤 64자 문자열 생성
openssl rand -base64 48

# 또는
python3 -c "import secrets; print(secrets.token_urlsafe(48))"
```

## 보안 체크리스트

- [ ] `.env` 파일이 `.gitignore`에 포함되어 있음
- [ ] `JWT_SECRET`이 32자 이상임
- [ ] 프로덕션과 개발 환경의 `JWT_SECRET`이 다름
- [ ] K8s Secret이 git에 커밋되지 않음
- [ ] `application-dev.yml`에 하드코딩된 비밀번호가 없음

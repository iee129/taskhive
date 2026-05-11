# 로컬 개발 환경 설정

Docker 없이 백엔드·프론트엔드를 직접 실행하는 개발 환경 설정 가이드.

## 전제 조건

| 도구 | 버전 | 설치 |
|------|------|------|
| Java (JDK) | 21 LTS | [temurin.net](https://adoptium.net) |
| Maven | 3.9+ | `brew install maven` / [maven.apache.org](https://maven.apache.org) |
| Node.js | 20 LTS | [nodejs.org](https://nodejs.org) |
| PostgreSQL | 16 | `brew install postgresql@16` |
| Git | 2.x+ | 내장 또는 [git-scm.com](https://git-scm.com) |

## PostgreSQL 로컬 설정

```bash
# macOS (Homebrew)
brew services start postgresql@16

psql postgres -c "CREATE USER taskhive WITH PASSWORD 'taskhive';"
psql postgres -c "CREATE DATABASE taskhive OWNER taskhive;"
```

## 백엔드 실행

```bash
cd backend

# 환경 변수 설정
export JWT_SECRET=local-dev-secret-key-at-least-32-chars
export SPRING_PROFILES_ACTIVE=dev

# 실행
./mvnw spring-boot:run
# 또는
mvn spring-boot:run
```

서버 기동 확인: `curl http://localhost:8080/actuator/health`

## 프론트엔드 실행

```bash
cd frontend
npm install
npm run dev
```

개발 서버: `http://localhost:3000`  
`/api/*` 요청은 `vite.config.ts`의 proxy 설정으로 자동으로 `http://localhost:8080`으로 전달됨.

## IntelliJ IDEA 설정

1. **File → Open** → `taskhive/backend` 선택
2. **Project SDK** → Java 21 설정
3. **Run/Debug Configurations** → `TaskHiveApplication` 추가
   - Environment Variables: `JWT_SECRET=local-dev-secret;SPRING_PROFILES_ACTIVE=dev`
4. **Maven** 탭에서 의존성 새로고침

## VS Code 설정 (프론트엔드)

```bash
cd frontend
code .
# 권장 확장: ESLint, Prettier, TypeScript Vue Plugin (Volar 불필요)
```

`.vscode/settings.json`:
```json
{
  "editor.formatOnSave": true,
  "editor.defaultFormatter": "esbenp.prettier-vscode",
  "typescript.preferences.importModuleSpecifier": "relative"
}
```

## 핫 리로드

| 서비스 | 방법 |
|--------|------|
| Backend | `spring-boot-devtools` → 클래스 변경 시 자동 재시작 |
| Frontend | Vite HMR → 저장 즉시 브라우저 반영 |

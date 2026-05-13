# 용어 사전

## 도메인 용어

| 용어 | 정의 |
|------|------|
| **태스크(Task)** | 수행해야 할 단일 작업 단위. `TODO → IN_PROGRESS → DONE` 상태를 가짐 |
| **프로젝트(Project)** | 연관된 태스크들의 집합. 소유자(Owner)가 존재함 |
| **담당자(Assignee)** | 특정 태스크를 수행할 책임이 있는 사용자 |
| **소유자(Owner)** | 프로젝트를 생성하고 관리하는 사용자 |

## 기술 용어

| 용어 | 정의 |
|------|------|
| **JWT (JSON Web Token)** | 사용자 인증 정보를 인코딩한 자가 수록(Self-contained) 토큰. Header.Payload.Signature 구조 |
| **Bearer Token** | `Authorization: Bearer <token>` 형식으로 HTTP 헤더에 전달하는 JWT |
| **Stateless 인증** | 서버가 세션을 저장하지 않고 토큰만으로 인증하는 방식 |
| **JPA Entity** | 데이터베이스 테이블과 매핑되는 Java 클래스 (`@Entity` 어노테이션) |
| **DTO (Data Transfer Object)** | 레이어 간 데이터 전달에 사용하는 객체. Entity와 분리되어 API 계약을 정의 |
| **Repository** | 데이터베이스 CRUD 작업을 추상화하는 Spring Data JPA 인터페이스 |
| **HMR (Hot Module Replacement)** | 코드 변경 시 페이지 전체 새로고침 없이 모듈만 교체하는 Vite 기능 |
| **PVC (PersistentVolumeClaim)** | Kubernetes에서 영속 스토리지를 요청하는 리소스 |
| **StatefulSet** | 상태가 있는(DB 등) 파드를 관리하는 Kubernetes 컨트롤러 |
| **Ingress** | 클러스터 외부에서 내부 서비스로의 HTTP(S) 라우팅을 관리하는 K8s 리소스 |
| **ADR** | Architecture Decision Record. 기술적 결정과 그 근거를 기록하는 문서 |

## 약어

| 약어 | 풀네임 |
|------|--------|
| API | Application Programming Interface |
| REST | Representational State Transfer |
| CRUD | Create, Read, Update, Delete |
| JWT | JSON Web Token |
| JPA | Java Persistence API |
| K8s | Kubernetes |
| CI/CD | Continuous Integration / Continuous Delivery |
| CORS | Cross-Origin Resource Sharing |
| ORM | Object-Relational Mapping |

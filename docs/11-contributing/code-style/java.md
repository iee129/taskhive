# Java 코드 스타일

## 기본 원칙

- Google Java Style Guide 기반
- 들여쓰기: **4 spaces** (탭 금지)
- 최대 줄 길이: **120자**
- Lombok 적극 활용 (boilerplate 최소화)

## 네이밍

| 유형 | 규칙 | 예시 |
|------|------|------|
| 클래스·인터페이스 | PascalCase | `AuthService`, `UserRepository` |
| 메서드·변수 | camelCase | `generateToken()`, `userEmail` |
| 상수 | UPPER_SNAKE_CASE | `JWT_SECRET_KEY` |
| 패키지 | 소문자 | `com.taskhive.config` |
| Entity | 단수형 | `User`, `Task` (복수 금지) |
| Boolean 변수 | `is` / `has` 접두사 | `isValid`, `hasExpired` |

## Entity 작성 규칙

```java
@Entity
@Table(name = "users")
@Getter                     // Lombok — setter 제공 금지 (불변성)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 255)
    private String email;
}
```

- `public setter` 금지 — 비즈니스 메서드로 상태 변경
- `@Builder`로 생성, 기본 생성자는 `PROTECTED`

## Service 작성 규칙

```java
@Service
@RequiredArgsConstructor     // Lombok — final 필드 생성자 주입
@Transactional(readOnly = true)  // 기본 읽기 전용
public class TaskService {

    private final TaskRepository taskRepository;

    @Transactional               // 쓰기 작업에만 명시
    public Task createTask(CreateTaskRequest request, String userEmail) {
        // ...
    }
}
```

## 금지 패턴

```java
// 금지: 필드 주입
@Autowired
private UserRepository userRepository;

// 대신: 생성자 주입 (Lombok @RequiredArgsConstructor)
private final UserRepository userRepository;

// 금지: 빈 catch 블록
try { ... } catch (Exception e) { }

// 금지: System.out.println
System.out.println("debug");
// 대신: SLF4J
log.debug("debug message: {}", value);
```

## Record 활용 (DTO)

```java
// 간결한 DTO — record 사용
public record LoginRequest(
    @NotBlank String email,
    @NotBlank String password
) {}

public record AuthResponse(
    String token,
    String email,
    String name
) {}
```

## IntelliJ 설정

- Code Style: Google Style XML 임포트 ([google/styleguide](https://github.com/google/styleguide))
- Save Actions: Optimize imports on save, Reformat code on save

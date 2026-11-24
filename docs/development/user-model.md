# 회원 엔티티와 Repository (#36)

## 범위와 구조

ERD v1.1의 users를 `identity` 영역에 구현한다. User는 회원 데이터와 신규 회원의 초기 상태를 정의하고, UserRepository는 DB 접근을 담당한다. 로그인·비밀번호 해시 생성·이메일 인증·OAuth 연결·토큰·탈퇴 서비스는 후속 작업이다.

```text
identity/
  domain/User.java       회원 객체와 users 테이블 매핑
  domain/UserStatus.java 회원 상태
  domain/UserRole.java   회원 권한
  repository/UserRepository.java 회원 저장·조회
```

## 객체와 DB가 연결되는 방식

- `@Entity`: Hibernate가 DB에 저장·조회할 대상으로 인식한다. Spring 서비스 Bean을 등록하는 `@Component`와 다르다.
- `@Table(name = "users")`: 클래스명 User를 users 테이블에 연결하고 이메일·닉네임 유니크 제약을 선언한다.
- `@Id`, `@UuidGenerator(VERSION_7)`: 저장할 때 시간 정렬이 가능한 UUIDv7 식별자를 생성한다.
- `@Column`: 필드의 컬럼명·길이·NULL 허용 여부를 정의한다.
- `@Enumerated(STRING)`: 상태·권한을 숫자 순서가 아닌 이름으로 저장한다. PostgreSQL 물리 타입은 VARCHAR + CHECK로 허용값을 제한한다. ERD의 논리 ENUM 값은 그대로이며 PostgreSQL 네이티브 ENUM은 생성하지 않는다.
- `Instant`: 로그인 제한·탈퇴·생성/수정 시각을 UTC 기준의 시점으로 표현한다.
- `@CreationTimestamp`, `@UpdateTimestamp`: Hibernate가 저장·수정 시각을 채운다. market 패키지의 BaseTimeEntity에 identity가 의존하지 않도록 이번 모델 내부에서 선언한다.
- `@Getter`: 조회용 메서드를 Lombok이 생성한다. passwordHash는 자동 getter를 만들지 않는다. 엔티티를 API 응답으로 직접 반환하지 않고 DTO를 사용해야 한다.
- `@NoArgsConstructor(PROTECTED)`: JPA가 객체를 복원할 때 사용할 기본 생성자는 제공하되 외부의 무분별한 빈 객체 생성을 제한한다.
- setter와 전체 필드 Builder는 제공하지 않는다. 호출자가 관리자 권한·인증 완료 상태를 임의로 넣는 대신 목적별 생성 메서드를 사용한다.

## 신규 회원 생성

```java
User user = User.registerWithEmail("member@example.com", encodedPassword, "닉네임");
userRepository.save(user);
```

`encodedPassword`는 향후 서비스에서 PasswordEncoder로 만든 해시다. 이 메서드는 암호화를 수행하지 않으며, 원문 비밀번호를 전달하면 안 된다. 현재는 인증 서비스가 없어 이 값을 검증·비교하는 로그인 동작도 없다.

이메일은 앞뒤 공백 제거와 소문자 정규화를 수행한다. 이메일 회원은 PENDING_EMAIL, 기본 권한은 USER, 실패 횟수는 0이다. 구글 회원용 `registerWithGoogle(verifiedEmail)`은 비밀번호와 닉네임을 NULL로 두고 PENDING_NICKNAME으로 시작한다. 이 메서드 호출 전에 실제 구글 인증 결과를 검증하는 것은 향후 OAuth 서비스의 책임이다. ACTIVE 상태에는 닉네임이 필요하다는 DB CHECK도 둔다.

닉네임 최대 30자, 빈 이메일·해시·닉네임 방지는 최소 모델 검증이다. 이메일 문법·비밀번호 정책·닉네임 정책과 API 오류 변환은 회원가입 서비스·DTO 구현 시 추가한다. 상태 전환·실패 횟수 증가·탈퇴 취소 동작은 아직 구현하지 않았다.

## Repository

`JpaRepository<User, UUID>`의 User는 관리할 엔티티, UUID는 기본키 타입이다. 구현 클래스를 직접 작성하지 않아도 Spring Data JPA가 구현 객체를 만들고 주입한다.

- `save`, `findById`, `delete` 등: JpaRepository가 제공한다.
- `findByEmailIgnoreCase`: 이메일로 조회해 Optional<User>를 반환한다. 없으면 Optional.empty()다. 조회 입력의 앞뒤 공백 제거는 향후 서비스에서 수행한다.
- `existsByEmailIgnoreCase`, `existsByNickname`: 가입 중복 사전 확인용이다. 동시 가입 경합까지 막는 것은 DB UNIQUE 제약이다.

## 로컬과 PostgreSQL

local은 기존 메모리 H2와 create-drop을 유지한다. 새 H2 초기화 SQL은 CITEXT라는 대소문자 무시 문자열 도메인을 만든 뒤 JPA가 users를 생성하게 한다. PostgreSQL 호환 모드에서 VARCHAR_IGNORECASE 선언이 제한되므로 선언할 때만 REGULAR 모드로 바꾸고 즉시 PostgreSQL 모드로 복원한다. H2는 실제 CITEXT의 모든 로케일·유니코드 동작을 보장하지 않는다.

postgres는 Flyway `V1__create_users.sql`이 실제 CITEXT 확장과 users를 만들고 Hibernate는 validate만 한다. 이 마이그레이션은 회원 테이블만 만든다. 기존 market 엔티티 전체의 마이그레이션은 별도 범위이므로 이 파일만으로 postgres 프로필의 전체 앱 실행이 보장되는 것은 아니다.

## 검증

`./gradlew check bootJar`로 전체 검사·빌드한다. UserRepositoryTests는 회원 영역만 대상으로 H2의 저장·재조회, UUIDv7·초기값·시간값, 대소문자 무시 조회, 이메일·닉네임 중복 거부, 구글 회원의 NULL 허용, ACTIVE/닉네임 제약, 빈 필드 입력을 검증한다. 테스트는 트랜잭션을 롤백하므로 테스트 데이터를 직접 지울 필요가 없다.

기존 시장 모델은 H2에서 PostgreSQL 전용 타입·interval 예약어 관련 DDL 경고가 있다. 이번 회원 테스트는 그 테이블 생성 성공을 주장하지 않으며 시장 모델은 변경하지 않는다. PostgreSQL 16 실제 마이그레이션·CITEXT 동작 검증은 Docker 데몬이 꺼져 있어 미실행이다.

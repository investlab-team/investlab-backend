# InvestLab Backend

Java 21 LTS(Eclipse Temurin), Spring Boot 4.1.1, Gradle Wrapper 9.7.1. PostgreSQL 16, Spring Security JWT 인증 검증 뼈대입니다.

## 상태

회원가입·로그인 토큰 발급·리프레시·거래 API는 후속 이슈에서 구현합니다. 현재 공개 endpoint는 `/actuator/health`이며 다른 경로는 유효 JWT를 요구합니다. JWT HS256·issuer·audience·만료를 검사합니다. 서명키는 32바이트 이상이며 환경변수로만 전달합니다.

## 실행·검사

`application.yml`은 공통 설정과 기본 프로필 `local`을 지정합니다. 기본 Run 또는 `./gradlew bootRun`은 `application-local.yml`의 메모리 H2 DB와 서버를 함께 시작합니다. Docker나 별도 DB 서버, DB 환경 변수는 필요하지 않습니다. `http://localhost:8080/actuator/health`의 `UP` 응답으로 확인합니다. 기존 JWT 검증 설정의 개발용 키는 외부에서 지정하지 않으면 실행마다 임시 생성합니다.

### local: 메모리 H2로 빠른 개발

- JDBC URL: `jdbc:h2:mem:investlab;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1`
- 사용자 `sa`, 비밀번호 없음. 애플리케이션 내부의 임시 DB이며 H2 웹 콘솔은 활성화하지 않습니다.
- JPA `ddl-auto: create-drop`으로 엔티티에 맞춰 테이블을 생성하고 종료 시 제거합니다. 아직 엔티티가 없으면 업무 테이블도 생성되지 않습니다. 앱 프로세스를 다시 실행하면 데이터는 초기화됩니다.
- Flyway는 비활성화합니다. 따라서 PostgreSQL 마이그레이션을 검증하는 환경은 아닙니다.
- H2의 PostgreSQL 호환 모드는 실제 PostgreSQL과 동일하지 않습니다. 기본 CRUD를 빠르게 확인하고, PostgreSQL 전용 SQL·자료형·잠금·동시성은 개발 중에도 PostgreSQL에서 검증합니다.

### postgres: PostgreSQL 16에서 확인

`application-postgres.yml`을 사용하는 `postgres` 프로필을 선택합니다. `.env.example`을 참고해 DB·JWT 환경 변수를 IDE 또는 셸에 설정하고 개발·테스트용 PostgreSQL 16을 준비합니다(자동 .env 로딩 없음).

```bash
./gradlew bootRun --args='--spring.profiles.active=postgres'
```

IDE 실행 인자로 `--spring.profiles.active=postgres`를 지정해도 됩니다. Compose의 backend 서비스도 이 프로필을 명시적으로 사용합니다. PostgreSQL에서는 Flyway와 JPA `validate`를 사용하며, H2의 `create-drop` 설정은 적용되지 않습니다.

### 테스트와 PR 전 검증

`./gradlew check bootJar`로 검사·빌드합니다. 현재 health·인증 테스트는 `local` 프로필의 H2와 고정된 테스트용 JWT 키를 사용합니다. 테스트 실행만으로 업무 데이터 저장·조회가 검증되는 것은 아닙니다. DB 기능을 구현하면 PR 전 PostgreSQL 16 통합 테스트로 연결·마이그레이션·저장·조회·트랜잭션을 확인합니다. 테스트 데이터는 운영 DB가 아닌 별도 개발·테스트 DB에서 관리합니다.

참고: [Spring Boot DB 초기화](https://docs.spring.io/spring-boot/how-to/data-initialization.html), [H2 메모리 DB와 호환 모드](https://h2database.github.io/html/features.html).

## 책임

A 송상진(회원·계좌·랭킹·관심·AI 연동), B 박진호(주문·체결·잔고·초기화·시세), C 유광민(AI). 명세·인프라 송상진. 회의 설명과 CI 통과 후 작성자가 자기 PR을 머지할 수 있습니다. docs/architecture를 기준으로 하고 AI 내부 계약은 AI 레포의 고정 버전에서 참조합니다.

프론트는 미정이며 Thymeleaf 결정 전 templates/static을 구현하지 않습니다.

기본·개발 통합 브랜치는 `dev`입니다. 최신 `dev`에서 작업 브랜치를 만들고 PR도 `dev`로 보냅니다. 회의 설명과 CI 통과 후 작성자가 머지할 수 있습니다. `main`은 릴리스 기준으로 보존하며 반영 시점은 팀 합의 후 별도 PR로 정합니다.

## API 명세·담당 안내

[API 명세 및 A·B·C 담당 표](docs/api/README.md)에서 API 명세 PDF, 기능별 담당, 공개·내부 OpenAPI 계약을 확인합니다.

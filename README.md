# InvestLab Backend

Java 21 LTS(Eclipse Temurin), Spring Boot 4.1.1, Gradle Wrapper 9.7.1. PostgreSQL 16, Spring Security JWT 인증 검증 뼈대입니다.

## 상태

회원가입·로그인 토큰 발급·리프레시·거래 API는 후속 이슈에서 구현합니다. 현재 공개 endpoint는 `/actuator/health`이며 다른 경로는 유효 JWT를 요구합니다. JWT HS256·issuer·audience·만료를 검사합니다. 서명키는 32바이트 이상이며 환경변수로만 전달합니다.

## 실행·검사

application.yml은 공통 설정과 기본 프로필 local을 지정합니다. application-local.yml은 DB 없는 개발 실행, application-postgres.yml은 PostgreSQL 연결 설정입니다. 기본 실행은 DB 연결 없이 서버만 시작합니다. IDE에서 `InvestLabApplication.main()`을 Run하거나 `./gradlew bootRun`을 실행하면 됩니다. Docker·DB 환경 변수·IDE 전용 설정은 필요하지 않습니다. `http://localhost:8080/actuator/health`의 `UP` 응답으로 확인합니다. JWT 인증 보호는 유지하며 개발용 키는 환경 변수 미설정 시 실행마다 임시 생성합니다. DB·JPA·Flyway는 기본 실행에서 비활성화되므로 DB 저장 기능은 사용할 수 없습니다.

DB 기능 개발 시 `application-postgres.yml`을 사용하는 `postgres` 프로필을 선택합니다. `.env.example`을 참고해 DB·JWT 환경 변수를 IDE 또는 셸에 설정하고 PostgreSQL 16을 준비합니다(자동 .env 로딩 없음). `./gradlew bootRun --args='--spring.profiles.active=postgres'` 또는 IDE 실행 인자 `--spring.profiles.active=postgres`로 실행합니다. Gradle 설정 파일이나 `.vscode`를 수정할 필요는 없습니다. Docker Compose의 backend 서비스는 이 프로필을 명시적으로 사용합니다.

`./gradlew check bootJar`로 검사·빌드합니다. 현재 health·인증 테스트는 `local` 프로필과 고정된 테스트용 JWT 키를 사용해 DB 없이 실행합니다. H2는 사용하지 않습니다. DB 기능을 구현할 때는 별도의 PostgreSQL 16 통합 테스트에서 연결·마이그레이션·저장·조회·트랜잭션을 검증해야 합니다. `postgres` 프로필 선택만으로 이러한 동작이 자동 검증되는 것은 아닙니다.

## 책임

A 송상진(회원·계좌·랭킹·관심·AI 연동), B 박진호(주문·체결·잔고·초기화·시세), C 유광민(AI). 명세·인프라 송상진. 회의 설명과 CI 통과 후 작성자가 자기 PR을 머지할 수 있습니다. docs/architecture를 기준으로 하고 AI 내부 계약은 AI 레포의 고정 버전에서 참조합니다.

프론트는 미정이며 Thymeleaf 결정 전 templates/static을 구현하지 않습니다.

기본·개발 통합 브랜치는 `dev`입니다. 최신 `dev`에서 작업 브랜치를 만들고 PR도 `dev`로 보냅니다. 회의 설명과 CI 통과 후 작성자가 머지할 수 있습니다. `main`은 릴리스 기준으로 보존하며 반영 시점은 팀 합의 후 별도 PR로 정합니다.

## API 명세·담당 안내

[API 명세 및 A·B·C 담당 표](docs/api/README.md)에서 API 명세 PDF, 기능별 담당, 공개·내부 OpenAPI 계약을 확인합니다.

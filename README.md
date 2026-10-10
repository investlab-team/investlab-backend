# InvestLab Backend

Java 21 LTS(Eclipse Temurin), Spring Boot 4.1.1, Gradle Wrapper 9.7.1. PostgreSQL 16, Spring Security JWT 인증 검증 뼈대입니다.

## 상태

회원가입·로그인 토큰 발급·리프레시·거래 API는 후속 이슈에서 구현합니다. 현재 공개 endpoint는 `/actuator/health`이며 다른 경로는 유효 JWT를 요구합니다. JWT HS256·issuer·audience·만료를 검사합니다. 서명키는 32바이트 이상이며 환경변수로만 전달합니다.

## 실행·검사

`.env.example`을 참고해 환경변수를 IDE 또는 셸에 설정합니다(자동 .env 로딩 없음). PostgreSQL 준비 후 `./gradlew bootRun`을 실행합니다. `./gradlew check bootJar`로 검사·빌드합니다. 테스트 H2는 빠른 보안 뼈대 검사용이며 PostgreSQL 잠금·거래 정합성 검증을 대체하지 않습니다.

## 책임

A 송상진(회원·계좌·랭킹·관심·AI 연동), B 박진호(주문·체결·잔고·초기화·시세), C 유광민(AI). 명세·인프라 송상진. 회의 설명과 CI 통과 후 작성자가 자기 PR을 머지할 수 있습니다. docs/architecture를 기준으로 하고 AI 내부 계약은 AI 레포의 고정 버전에서 참조합니다.

프론트는 미정이며 Thymeleaf 결정 전 templates/static을 구현하지 않습니다.

기본·개발 통합 브랜치는 `dev`입니다. 최신 `dev`에서 작업 브랜치를 만들고 PR도 `dev`로 보냅니다. 회의 설명과 CI 통과 후 작성자가 머지할 수 있습니다. `main`은 릴리스 기준으로 보존하며 반영 시점은 팀 합의 후 별도 PR로 정합니다.

## API 명세·담당 안내

[API 명세 및 A·B·C 담당 표](docs/api/README.md)에서 API 명세 PDF, 기능별 담당, 공개·내부 OpenAPI 계약을 확인합니다.

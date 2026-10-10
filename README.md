# InvestLab Backend

Java 21 LTS(Eclipse Temurin), Spring Boot 4.1.1, Gradle Wrapper 9.7.1. MySQL 8.4 LTS, Spring Security JWT 인증 검증 뼈대입니다.

## 상태

회원가입·로그인 토큰 발급·리프레시·거래 API는 후속 이슈에서 구현합니다. 현재 공개 endpoint는 `/actuator/health`이며 다른 경로는 유효 JWT를 요구합니다. JWT HS256·issuer·audience·만료를 검사합니다. 서명키는 32바이트 이상이며 환경변수로만 전달합니다.

## 실행·검사

`.env.example`을 참고해 환경변수를 IDE 또는 셸에 설정합니다(자동 .env 로딩 없음). MySQL 준비 후 `./gradlew bootRun`을 실행합니다. `./gradlew check bootJar`로 검사·빌드합니다. 테스트 H2는 빠른 보안 뼈대 검사용이며 MySQL 잠금·거래 정합성 검증을 대체하지 않습니다.

## 책임

백엔드 송상진·박진호(기능별 미정), 명세·인프라 송상진. 회의 설명과 CI 통과 후 작성자가 자기 PR을 머지할 수 있습니다. docs/architecture를 기준으로 하고 AI 내부 계약은 AI 레포의 고정 버전에서 참조합니다.

프론트는 미정이며 Thymeleaf 결정 전 templates/static을 구현하지 않습니다.

초기 main에는 협업 템플릿과 README만 등록합니다. 앱 코드·전체 설정은 chore/1-initial-scaffold PR에서 검토 후 작성자가 머지할 수 있습니다.

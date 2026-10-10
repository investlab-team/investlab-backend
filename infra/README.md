# 통합 실행 — PostgreSQL 16

Docker Desktop을 실행하고 부모 폴더 아래 backend·ai 레포를 나란히 clone합니다. 셸에 POSTGRES_PASSWORD(관리자), CORE_DB_PASSWORD, RESEARCH_DB_PASSWORD, JWT_SECRET_BASE64, INVESTLAB_AI_SERVICE_TOKEN을 설정합니다. 실제 비밀번호와 토큰은 커밋하지 않습니다.

`docker compose -f infra/compose.integration.yaml up -d postgres`로 DB만 실행하거나, `docker compose -f infra/compose.integration.yaml --profile apps up --build`로 앱을 함께 실행합니다.

PostgreSQL 16 하나에 investlab_core·investlab_research 두 database와 각각의 전용 역할을 생성합니다. DB는 로컬 127.0.0.1:5432로만 공개합니다. 초기화 스크립트는 새 postgres-data 볼륨의 최초 기동 때만 실행됩니다. 기존 데이터가 있으면 별도의 백업·이관 계획이 필요하며 이 구성 변경이 기존 볼륨을 삭제하거나 이관하지 않습니다.

AI의 연구 DB 환경변수는 연결 계약을 위한 설정이며 아직 업무 Repository·Alembic 마이그레이션은 구현 전입니다. backend의 JDBC 드라이버는 PostgreSQL, Flyway는 PostgreSQL 모듈을 사용합니다. H2 보안 테스트는 PostgreSQL의 잠금·부분 인덱스·지연 FK를 검증하지 않습니다.

도메인 물리 설계 시 ERD v1.1의 서비스 경계를 넘는 FK를 계약 식별자 참조로 구분합니다. 벡터 저장소·Redis는 이번 전환으로 추가하지 않습니다.

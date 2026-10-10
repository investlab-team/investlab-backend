# 로컬 통합 실행 초안

Docker Desktop을 실행하고 부모 폴더 아래 backend·ai 레포를 나란히 clone합니다. 셸에 MYSQL_ROOT_PASSWORD, CORE_DB_PASSWORD, RESEARCH_DB_PASSWORD, JWT_SECRET_BASE64, INVESTLAB_AI_SERVICE_TOKEN을 설정합니다. 초기 DB 생성 스크립트에 전달하는 DB 비밀번호는 영문·숫자만 사용합니다(운영 프로비저닝 아님).

`docker compose -f infra/compose.integration.yaml up -d mysql`로 MySQL만 실행하거나, `docker compose -f infra/compose.integration.yaml --profile apps up --build`로 앱을 함께 실행합니다.

현재 서비스 인증·RAG 기반 준비 단계입니다. backend→AI HTTP 연동과 AI DB 조회는 후속입니다. Redis는 미확정이므로 포함하지 않습니다. 운영용 배포 파일이 아니며 이미지 digest 고정·Secret 저장소·네트워크·DB 최소권한·DB readiness를 운영 설정 단계에서 추가합니다.

이 환경에서는 Docker 데몬이 실행되지 않아 컨테이너 실행 검증을 아직 하지 않았습니다. 초기화는 새 빈 볼륨에서만 실행됩니다. 데이터 삭제 명령은 자동으로 실행하지 않습니다.

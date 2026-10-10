# 협업

모든 변경은 해당 레포 이슈를 먼저 등록합니다. 브랜치는 `feat/12-jwt-login`처럼 만들고 커밋은 `feat(auth): JWT 로그인 API 추가`, 본문은 `Refs #12`로 연결합니다. 완료 PR에는 `Closes #12`를 적습니다.

작성자 외 팀원 최소 1명이 승인하고 CI 통과 후 그 팀원이 Squash and merge합니다. 작성자의 직접 머지와 main 직접 push는 금지합니다. 초기 빈 레포의 기준 커밋은 생성 절차에서 별도로 기록합니다.

백엔드: 송상진·박진호(기능별 분담 미정), AI: 유광민, 명세·인프라: 송상진. 팀원 초대와 CODEOWNERS는 GitHub 계정 확인 후 설정합니다.

전체 기준: https://github.com/investlab-team/investlab-backend/blob/main/docs/architecture/repository-contracts.md

PR 리뷰 필수 설정만으로 자기 머지가 기술적으로 차단되지는 않습니다. 전용 머지 App과 보호 설정은 별도 구현 대상입니다.

# 협업

모든 변경은 해당 레포 이슈를 먼저 등록합니다. 브랜치는 `feat/12-jwt-login`처럼 만들고 커밋은 `feat(auth): JWT 로그인 API 추가`, 본문은 `Refs #12`로 연결합니다. 완료 PR에는 `Closes #12`를 적습니다.

회의에서 설명하고 CI 통과 후 작성자가 자기 PR을 Squash and merge할 수 있습니다. GitHub 승인은 필수가 아니며 dev·main 직접 push는 금지합니다. 기능 작업 브랜치는 최신 dev에서 생성하고 PR의 base를 dev로 설정합니다. main 반영은 릴리스 합의 시 별도 PR로 진행합니다. 초기 빈 레포의 기준 커밋은 생성 절차에서 별도로 기록합니다.

백엔드: 송상진·박진호(기능별 분담 미정), AI: 유광민, 명세·인프라: 송상진. 팀원 초대와 CODEOWNERS는 GitHub 계정 확인 후 설정합니다.

전체 기준: https://github.com/investlab-team/investlab-backend/blob/dev/docs/architecture/repository-contracts.md

자기 PR 머지 제한과 필수 승인 리뷰는 사용하지 않습니다. 회의에서 변경 내용과 검증 결과를 공유합니다.

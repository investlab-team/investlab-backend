# API 명세 및 기능별 담당

확정일: 2026-10-10. A = 송상진(백엔드), B = 박진호(백엔드), C = 유광민(AI).

## 명세 파일

- [API 명세서 v1.0 PDF](../requirements/인베스트랩_API명세서_v1.0.pdf): 요청·응답·오류·기능 ID를 확인한다.
- [공개 OpenAPI 계약](../../contracts/public-api.yaml): 현재 보안 뼈대의 계약이며 전체 업무 API는 아직 구현되지 않았다.
- [기능명세서 v1.1](../requirements/인베스트랩_기능명세서_v1.1.pdf): 필수·권장·후속 범위와 완료 기준.
- [ERD v1.1](../requirements/인베스트랩_ERD_테이블정의서_v1.1.pdf): 데이터 관계와 거래 트랜잭션 규칙.
- [AI 내부 OpenAPI 계약](https://github.com/investlab-team/investlab-ai/blob/dev/contracts/internal-ai-api.yaml): C의 구현과 A의 호출 연동 기준. 구현 시 사용하는 계약 커밋·버전을 고정한다.

이 문서는 기존 API 명세에 적용하는 담당자 부록이다. 아래 경로는 보관된 API v1.0을 기준으로 하며, 공개 경로의 공통 접두사는 `/api/v1`이다. 담당 배정은 구현 완료를 뜻하지 않는다.

ERD v1.1은 API v1.3을 참조하지만 현재 저장소에는 API v1.0만 있다. 버전 간 경로·정책 차이는 새 명세와 대조해 갱신한다. DB는 PostgreSQL 16으로 확정했다. Redis 및 임베딩 저장 방식은 별도 결정한다.

## A — 송상진 / 백엔드

| 영역·기능 ID | API v1.0 경로 | 구현 책임 |
| --- | --- | --- |
| AUTH-01부터 AUTH-06까지 | `POST /auth/signup`, `/auth/email/verify`, `/auth/email/resend`, `/auth/login`, `/auth/refresh`, `/auth/logout`; `GET /auth/oauth/google`, `/auth/oauth/google/callback` | 회원가입·이메일 인증·JWT·구글 로그인·토큰 관리 |
| USER-01 | `GET /me` | 내 정보 및 계좌 요약. 자산·순위는 B의 평가 기능과 A의 랭킹 기능 재사용 |
| ACCOUNT-01부터 ACCOUNT-03까지 | `POST /accounts`; `GET /accounts`, `/accounts/{accountId}`; `POST /accounts/{accountId}/close` | 계좌 개설·목록·상세·해지. 계좌 상세의 자산 계산은 B와 연결 |
| STOCK-01부터 STOCK-03까지 | `GET /instruments`, `/instruments/search`, `/instruments/{instrumentId}` | 종목 목록·검색·상세. B의 시세 기능과 C의 기업 데이터 연결 |
| STOCK-05, WATCH-01 | `/me/watch-items`, `/me/watchlists` 및 하위 등록·변경·삭제·이동 API | 관심종목·관심목록 및 소유권·개수 제한 |
| RANK-01부터 RANK-02까지 | `GET /rankings`, `/accounts/{accountId}/ranking`; `PATCH /accounts/{accountId}/ranking-preference` | 랭킹 배치·조회·노출 설정. 자산 평가 결과는 B의 기능 사용 |
| FIN-01, DATA-01 조회 | `GET /companies/{companyId}/financials`, `/companies/{companyId}/disclosures`, `/disclosures/{documentId}` | 공개 조회 API·권한·응답 변환. 수집·정규화·계산은 C |
| AI-01부터 AI-06까지, AI-09부터 AI-10까지 공개 연동 | `POST /ai/answers`; `GET /ai/answers/{answerId}` | 입력·권한 검사, 내부 AI 호출, 대화·답변·근거 이력 저장·조회 |
| MEMO-01〔권장〕 | `GET / POST /instruments/{instrumentId}/memos`; `PATCH / DELETE /memos/{memoId}` | 종목메모 관리 |

비밀번호 재설정, 닉네임 온보딩, 회원 탈퇴는 ERD v1.1에 반영된 A의 담당 기능이다. 해당 경로와 요청·응답은 새 API 명세와 대조해 추가한다.

## B — 박진호 / 백엔드

| 영역·기능 ID | API v1.0 경로 또는 내부 작업 | 구현 책임 |
| --- | --- | --- |
| TRADE-01부터 TRADE-05까지 | `POST /accounts/{accountId}/orders`; `GET /accounts/{accountId}/orders`, `/accounts/{accountId}/orders/{orderId}`, `/accounts/{accountId}/executions`; `POST /accounts/{accountId}/orders/{orderId}/cancel` | 매수·매도, 미체결 조회·취소, 주문·체결 이력 |
| TRADE-04 | 내부 1분 체결 배치·장 마감 처리 | 예약 해제·현금·보유·체결·원장·실현손익을 한 트랜잭션으로 변경 |
| PORT-01부터 PORT-02까지 | `GET /accounts/{accountId}/portfolio` 및 내부 자산 평가 기능 | 잔고·평가/실현손익·총자산·수익률·예약 현금/수량 |
| RESET-01 | `POST /accounts/{accountId}/reset` | 미체결 취소·예약 해제·보유 제거·현금 초기화·성과구간 교체 |
| SYNC-01, STOCK-04 | 내부 시세 수집; `GET /instruments/{instrumentId}/candles` | 시장 정책·운영일·시세 갱신·봉 데이터·차트 조회용 지표 |
| TRADE-06, QUOTE-01〔권장〕 | `GET /instruments/{instrumentId}/orderbook`; WebSocket 계약은 후속 확정 | 최우선 호가·호가주문·실시간 시세 |

## C — 유광민 / AI

| 영역·기능 ID | 구현 책임 | 백엔드 연결 |
| --- | --- | --- |
| DATA-01부터 DATA-03까지 | 공시·재무 수집, 원문 처리, 계정 매핑·정규화, 수집 실패 재시도 | A의 기업·재무 공개 조회 API에 데이터 제공 |
| RAG-01부터 RAG-06까지 | 전처리·청크·임베딩·기업/기간 필터 검색 | 검색 근거와 문서 식별정보 반환 |
| AI-01부터 AI-06까지, AI-09부터 AI-10까지 | 의도 분석·추가 질문·도구 선택·재무 조회/계산·답변 생성·출처·근거 부족 처리 | A의 공개 AI API에서 내부 호출 |
| 내부 AI API·운영 | 서비스 인증, 모델 연동, 오류 처리, 토큰·비용·응답시간 기록 | 계약 관리 송상진, AI 구현 유광민. A+C가 호환성 확인 |

공개 `/ai/answers`와 사용자 대화 이력은 A가 관리하고, Python의 분석·검색·계산은 C가 구현한다. AI는 계좌·주문·잔고를 변경하지 않는다. 수집·RAG 작업을 사용자용 공개 API로 임의 노출하지 않는다.

## 공동 경계와 완료 기준

- A+B: 계좌·성과구간 모델, 개설/초기화 규칙, 잠금 순서, 계좌 해지·회원 탈퇴와 거래 경합 검증. 거래 원장·예약량·손익 계산은 B의 기능을 재사용한다.
- A+C: 내부 AI 요청·응답·오류·대화 문맥, 기업/종목 ID 연결, 근거 식별자·이력 저장 형식.
- B+C: 주가 조회 방식, 시세 기준시각·누락·지연 상태 전달.
- 송상진: 명세·아키텍처·공통 인프라·CI 기준 유지. 각 담당자는 자기 기능의 DB 마이그레이션·테스트를 작성한다.
- 전체: 통합 테스트·배포 검증. 프론트 기술·담당은 미정이다.

모든 작업은 이슈에 기능 ID, 위 담당자, 목적, 완료 기준을 적고 PR로 연결한다. 완료 기준은 기능명세서 13절(14페이지)의 검증 시나리오와 최신 확정 정책을 사용한다. 권장·후속 기능은 필수 완료 후 이슈에서 담당자를 정한다. 전략·신호·알림·가상화폐 확장·과거 자산/랭킹 이력·개인 포트폴리오 AI는 이번 표로 필수 범위에 추가하지 않는다.

[시스템 아키텍처](../architecture/system-architecture.md) · [레포 간 협업 규약](../architecture/repository-contracts.md)

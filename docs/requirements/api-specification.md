# InvestLab API 명세서

> 기준 원본: v1.0 · Markdown 전환일: 2026-11-24. 앞으로 이 파일을 수정 기준으로 관리한다. 전환 시 원문의 제안·미확정·필수/권장/후속 표기를 유지했으며 구현 완료를 의미하지 않는다.

원본 기록: [인베스트랩_API명세서_v1.0.pdf](./인베스트랩_API명세서_v1.0.pdf) · [문서 안내](README.md)

> 버전 주의: 기능·DB v1.1과 보관된 API v1.0은 동일 시점의 계약이 아니다. DB 원문이 참조하는 API v1.3은 현재 저장소에 없다. 정책 충돌을 임의로 해소하지 않고 원문을 보존했으며, [문서 간 차이](README.md#문서-간-차이와-적용-원칙)를 먼저 확인한다.

## 목차

- [인베스트랩](#source-page-1)
- [01 공통 요청·응답 규약](#source-page-2)
- [02 회원가입·이메일 인증·로그인](#source-page-3)
- [03 토큰·OAuth·내 정보](#source-page-4)
- [04 모의계좌·초기화](#source-page-5)
- [05 종목·차트·호가](#source-page-6)
- [06 주문·취소·체결 내역](#source-page-7)
- [07 자산·랭킹](#source-page-8)
- [08 관심종목·목록](#source-page-9)
- [09 목록 편집·종목 이동](#source-page-10)
- [10 메모·기업·재무·공시 조회](#source-page-11)
- [11 AI 질의·답변](#source-page-12)
- [12 응답 모델 · 계좌와 자산](#source-page-13)
- [13 요청·응답 모델 · 주문](#source-page-14)
- [14 응답 모델 · 시세·랭킹·초기화](#source-page-15)
- [15 응답 모델 · 재무·공시·AI](#source-page-16)
- [16 거래·초기화 예시](#source-page-17)
- [17 오류 코드·주문 상태 전이](#source-page-18)
- [18 권장 API · 전략·신호·알림](#source-page-19)
- [19 운영 API · 후속 관리자 화면](#source-page-20)
- [20 WebSocket · 권장 시세 채널](#source-page-21)
- [21 정책 일치·미확정 설정](#source-page-22)
- [22 API 검수 체크리스트](#source-page-23)

<a id="source-page-1"></a>

<!-- 원본 PDF 1 / 23쪽 -->

## 인베스트랩

## API 명세서

### REST API · 데이터 계약 · 오류 규칙 v1.0

| 기준 | 내용 |
| --- | --- |
| 기준 문서 | 인베스트랩 기능명세서 v1.1, ERD·테이블 정의서 v1.0 |
| Base path | /api/v1. 배포 호스트는 미정이며 경로는 Base path 기준이다. |
| 범위 | 필수·권장·후속 API를 구분한다. 후속 API는 1차 출시 시 활성화하지 않아도 된다. |
| 상태 | 기존 서버를 조사한 결과가 아닌 신규 API 계약 제안. 정책 P-01\~08을 구현 전 확정한다. |
| 인증 | 일반 API는 Bearer 액세스 토큰. 리프레시는 HttpOnly 쿠키로 전달하는 웹 클라이언트 설계안. |
| 기능 연결 | API 제목에 기능 ID를 기재. 기능을 누락하지 않되 수집·배치·RAG 내부 처리는 사용자 API로 노출하지 않<br>는다. |

### 문서 구성

1\~3절 공통 계약·인증 → 4\~11절 서비스 API → 12\~17절 응답 모델·거래 예시 → 18\~22절 확장·내부 처리·정책·검증. 모델 이름 뒤 []는 배열, ?는 NULL 허용, 필수/선택은 요청값의 포함 여부를 뜻한다.

요청·응답 JSON의 필드는 camelCase, DB 열은 snake_case를 사용한다. API에 userId를 받아 권한을 결정하지 않고 인 증 토큰의 사용자 ID를 사용한다. API 키·OAuth 비밀값·원문 Refresh Token은 응답 본문이나 로그에 남기지 않는다.

---

<a id="source-page-2"></a>

<!-- 원본 PDF 2 / 23쪽 -->

## 01 공통 요청·응답 규약

| 항목 | 규칙 |
| --- | --- |
| 콘텐츠 형식 | 요청·응답 application/json, UTF-8. 204 응답은 본문 없음. 날짜는 YYYY-MM-DD, 시각은 ISO 8601<br>UTC(Z). |
| 식별자·수치 | ID는 UUID 문자열. 금액·가격·수량·비율은 Decimal 문자열. 수익률은 퍼센트 단위(예: "0.2" = 0.2%). 페이<br>지 크기·순위·연도는 정수. |
| 인증 헤더 | Authorization: Bearer {accessToken}. 타인 소유 리소스는 403 FORBIDDEN. 인증 없음/만료는 401<br>UNAUTHORIZED. |
| 멱등 헤더 | 계좌 생성·주문 생성·초기화에 Idempotency-Key 필수, 1\~128 ASCII 문자. 보관 24시간 제안. 동일 사용<br>자·작업·키·경로·본문은 원래 응답 재전송. |
| 목록 조회 | cursor 선택, limit 기본 20·최대 100. nextCursor가 null이면 마지막. 정렬은 각 API 기준, 동일 시각은 ID<br>로 안정 정렬. |
| 오류 응답 | error:{code,message,details[]}, requestId. details 원소는 field,reason. 내부 스택·DB 쿼리는 공개하지 않<br>는다. |
| 성공 응답 | 단건: data:{...}, meta:{requestId}. 목록: data:[...], meta:{requestId,nextCursor}. 추가 평가시각 등은 data에<br>명시한다. |
| 조회 필터 | from/to는 선택, from 포함·to 제외. 역전 기간 400. 조회 결과 없음은 200과 빈 목록. 개별 ID 없음은<br>404. |
| 범위 비활성 | 미출시 기능은 404 FEATURE_NOT_AVAILABLE. 거래 시장이 비활성/정책 미설정이면 503<br>MARKET_NOT_READY. 공급자 장애는 503 DATA_UNAVAILABLE. |

### 검증·동시 요청

같은 멱등 키의 본문/경로 불일치는 409 IDEMPOTENCY_CONFLICT, 진행 중은 409 REQUEST_IN_PROGRESS. 예약 현 금·수량 계산과 주문 상태 변경은 계좌 잠금 안에서 수행한다. 정상 처리된 API 응답을 재시도해도 자금 지급·주문·초기 화가 중복 발생하지 않아야 한다.

### HTTP 상태 기준

200 조회·처리 완료 / 201 생성 / 202 비동기 작업 접수 / 204 삭제 완료 / 400 형식 오류 / 401 인증 실패 / 403 권한 거부 / 404 없음 / 409 상태 충돌 / 410 인증 토큰 만료 / 422 도메인 검증 실패 / 429 호출 제한 / 503 외부·서비스 장 애.

---

<a id="source-page-3"></a>

<!-- 원본 PDF 3 / 23쪽 -->

## 02 회원가입·이메일 인증·로그인

### POST /auth/signup

AUTH-01 회원가입 · 필수 · 인증: 불필요

**요청** body: email(string, 필수, 최대254), password(string, 필수, 8\~72자 제안), passwordConfirm(string, 필수), nickname(string, 필수, 2\~30자).

**응답** 201 data:{userId, email, nickname, status:"PENDING_EMAIL"}. 계좌·자금 자동생성 없음.

**오류** 400 VALIDATION_ERROR / 409 EMAIL_ALREADY_EXISTS, NICKNAME_ALREADY_EXISTS

**처리** 비밀번호 해시 저장, 인증 메일 발송. 메일 전달 실패는 가입 롤백 대신 재발송 가능 상태로 남김.

### POST /auth/email/verify

AUTH-05 인증 링크 처리 · 필수 · 인증: 불필요

**요청** body: token(string, 필수). 프론트 인증 화면이 메일 링크 토큰을 전달.

**응답** 200 data:{verified:true}.

**오류** 400 INVALID_VERIFICATION_TOKEN / 410 VERIFICATION_TOKEN_EXPIRED

**처리** 24시간 유효. 사용 완료된 유효 토큰 재호출은 성공 상태 반환 제안.

### POST /auth/email/resend

AUTH-05 인증 재발송 · 필수 · 인증: 불필요

**요청** body: email(string, 필수).

**응답** 202 data:{accepted:true}. 가입 여부를 응답으로 구분하지 않음.

**오류** 400 VALIDATION_ERROR / 429 EMAIL_RESEND_LIMIT

**처리** 동일 계정/이메일당 1분 1회 제한. 응답 재시도 안내는 Retry-After 헤더.

### POST /auth/login

AUTH-02 이메일 로그인 · 필수 · 인증: 불필요

**요청** body: email, password(필수). 허용된 Origin 검증.

**응답** 200 data: TokenResult. Set-Cookie로 refreshToken과 CSRF 토큰 전달.

**오류** 401 INVALID_CREDENTIALS / 403 EMAIL_NOT_VERIFIED / 429 LOGIN_LOCKED

**처리** 비밀번호 5회 연속 실패 후 10분 잠금. 액세스 1시간, 리프레시 14일. 성공 시 실패 횟수 초기화.

### 인증 전용 모델

### TokenResult

| 필드 | 형식·필수 여부 | 설명 |
| --- | --- | --- |
| accessToken | string, 필수 | Bearer 인증용 토큰 |
| tokenType | string, 필수 | Bearer |
| expiresIn | int, 필수 | 3600(초) |
| user | User, 필수 | id, email, nickname, status. role은 필요 시 포함 |

---

<a id="source-page-4"></a>

<!-- 원본 PDF 4 / 23쪽 -->

## 03 토큰·OAuth·내 정보

### POST /auth/refresh

AUTH-03 토큰 갱신 · 필수 · 인증: Refresh 쿠키 + CSRF

**요청** body 없음. refreshToken 쿠키와 X-CSRF-Token 헤더.

**응답** 200 data:{accessToken,tokenType:"Bearer",expiresIn:3600}.

**오류** 401 INVALID_REFRESH_TOKEN / 403 CSRF_FAILED

**처리** 리프레시 만료·무효화 시 재로그인. 원문 토큰 대신 해시로 조회한다.

### POST /auth/logout

AUTH-04 로그아웃 · 필수 · 인증: Refresh 쿠키 + CSRF

**요청** body 없음. 현재 세션의 Refresh Token.

**응답** 204, Refresh/CSRF 쿠키 만료.

**오류** 401 UNAUTHORIZED / 403 CSRF_FAILED

**처리** 액세스 토큰은 최대 1시간 남을 수 있다. 즉시 액세스 차단은 별도 정책이며 본 계약에는 포함하지 않는다.

### GET /auth/oauth/google

AUTH-06 구글 인증 시작 · 필수 · 인증: 불필요

**요청** query: returnTo(string, 선택). 서버 허용 목록 내 프론트 상대 경로만 허용.

**응답** 302, Google 인증 화면으로 이동.

**오류** 400 INVALID_RETURN_URL / 503 OAUTH_UNAVAILABLE

**처리** 서버가 state·PKCE를 생성하고 일회용 상태를 짧은 만료시간으로 Redis 등에 저장한다.

### GET /auth/oauth/google/callback

AUTH-06 구글 콜백 · 필수 · 인증: OAuth 공급자 콜백

**요청** query: code, state(성공 시 필수) 또는 error.

**응답** 성공 303 프론트 허용 경로 이동, Refresh/CSRF 쿠키 설정. 프론트가 /auth/refresh로 액세스 발급.

**오류** 400 OAUTH_STATE_INVALID / 401 OAUTH_FAILED / 503 OAUTH_UNAVAILABLE

**처리** 토큰은 URL에 넣지 않는다. 검증된 provider subject·이메일로 신규 가입/기존 계정 연결.

### GET /me

USER-01 내 정보 · 필수 · 인증: Bearer

**요청** query: accountId(UUID, 선택).

**응답** 200 data:{user:User, accounts:AccountSummary[], selectedPortfolio:Portfolio?, selectedRanking:MyRanking?}.

**오류** 403 FORBIDDEN / 404 ACCOUNT_NOT_FOUND

**처리** 계좌 미선택 시 selected*는 null. 여러 계좌의 수익률을 더하지 않는다.

쿠키 설계 제안: refreshToken은 HttpOnly·Secure·SameSite=Lax·Path=/api/v1/auth. CSRF 토큰은 프론트가 읽어 헤더로 보내고 서버가 쿠 키·세션과 대조한다. 갱신·로그아웃은 허용 Origin도 검사한다. 쿠키 기반 로그인은 동일 사이트 배포를 전제로 하며 배포 구조 변경 시 CORS·SameSite를 재검토한다.

---

<a id="source-page-5"></a>

<!-- 원본 PDF 5 / 23쪽 -->

## 04 모의계좌·초기화

### POST /accounts

ACCOUNT-01 계좌 개설 · 필수 · 인증: Bearer

**요청** header: Idempotency-Key. body: name(string,1\~40,필수), market(KR_STOCK/CRYPTO_KRW,필수).

**응답** 201 data: Account. initialCapital/cashBalance="10000000", status=ACTIVE.

**오류** 409 ACCOUNT_LIMIT_EXCEEDED / 503 MARKET_NOT_READY

**처리** 사용자당 활성 계좌 3개. 시장별 계좌, 기본 자동개설 없음.

### GET /accounts

ACCOUNT-02 계좌 목록 · 필수 · 인증: Bearer

**요청** query: status(ACTIVE/CLOSED, 선택, 기본 ACTIVE).

**응답** 200 data: AccountSummary[], meta:{requestId,nextCursor:null}.

**오류** 401 UNAUTHORIZED

**처리** 생성시각 내림차순. 해지 목록도 본인 계좌만 제공한다.

### GET /accounts/{accountId}

ACCOUNT-02 계좌 상세 · 필수 · 인증: Bearer

**요청** path: accountId(UUID,필수).

**응답** 200 data: AccountDetail={account:Account,portfolio:Portfolio}.

**오류** 403 FORBIDDEN / 404 ACCOUNT_NOT_FOUND

**처리** 평가시세 부족 상태도 Portfolio.valuationStatus로 표현.

### POST /accounts/{accountId}/close

ACCOUNT-03 계좌 해지 · 필수 · 인증: Bearer

**요청** path: accountId. body 없음.

**응답** 200 data:{accountId,status:"CLOSED",closedAt}.

**오류** 409 HOLDINGS_REMAIN, OPEN_ORDERS_REMAIN / 403 FORBIDDEN

**처리** 보유수량·미체결 없을 때만 해지, 기존 이력 보존. 이미 해지이면 기존 결과 반환.

### POST /accounts/{accountId}/reset

RESET-01 자금 초기화 · 필수 · 인증: Bearer

**요청** header: Idempotency-Key. body: confirm(true,필수).

**응답** 200 data: ResetResult.

**오류** 409 RESET_DAILY_LIMIT, ACCOUNT_CLOSED / 403 FORBIDDEN / 400 CONFIRM_REQUIRED

**처리** 미체결 취소·예약 해제·포지션 제거·새 성과구간·현금 1,000만원. KST 하루 1회, 초기화 전 거래이력 보존.

### PATCH /accounts/{accountId}/ranking-preference

RANK-01 랭킹 공개 설정 · 필수 · 인증: Bearer

**요청** body: optOut(boolean,필수).

**응답** 200 data:{accountId,optOut}.

**오류** 403 FORBIDDEN / 404 ACCOUNT_NOT_FOUND

**처리** 옵트아웃은 기존 랭킹 응답에서도 즉시 제외. 재참여 요청시각을 accounts.ranking_eligible_after에 기록하고 그 이후 산정부터 반영.

---

<a id="source-page-6"></a>

<!-- 원본 PDF 6 / 23쪽 -->

## 05 종목·차트·호가

### GET /instruments

STOCK-01 종목 목록 · 필수 · 인증: Bearer

**요청** query: market(선택), sort(NAME_ASC/CHANGE_RATE_DESC/VOLUME_DESC,기본 NAME_ASC), cursor,limit.

**응답** 200 data: InstrumentSummary[].

**오류** 400 INVALID_SORT / 503 DATA_UNAVAILABLE

**처리** 동률은 instrumentId 오름차순. 시세 누락은 nullable 필드와 quoteStatus로 구분.

### GET /instruments/search

STOCK-02 검색 · 필수 · 인증: Bearer

**요청** query: q(string,1\~100,필수), market(선택), cursor,limit.

**응답** 200 data: InstrumentSummary[].

**오류** 400 VALIDATION_ERROR

**처리** 종목명·코드 검색. 결과 없음은 빈 배열.

### GET /instruments/{instrumentId}

STOCK-03 종목 상세 · 필수 · 인증: Bearer

**요청** path: instrumentId(UUID).

**응답** 200 data:{instrument:InstrumentSummary,companyId:UUID?,quote:Quote}.

**오류** 404 INSTRUMENT_NOT_FOUND

**처리** companyId는 가상화폐에서 null. 시세시각·지연 여부를 함께 제공한다.

### GET /instruments/{instrumentId}/candles

STOCK-04 차트 · 필수 / 분봉 권장 · 인증: Bearer

**요청** query: interval(D1/W1/M1/MIN1,기본 D1), from,to(선택), limit(int,기본·최대300).

**응답** 200 data: Candle[]. 시작시각 오름차순.

**오류** 400 INVALID_PERIOD / 404 FEATURE_NOT_AVAILABLE

**처리** M1=월봉, MIN1=1분봉. 데이터 없음은 빈 배열. 이동평균 5/20/60은 프론트가 계산하되 필요한 이전 봉을 포함해 요청한다.

### GET /instruments/{instrumentId}/orderbook

TRADE-06 최우선 호가 · 권장 · 인증: Bearer

**요청** path: instrumentId.

**응답** 200 data:{instrumentId,bidPrice:decimal?,bidQuantity:decimal?,askPrice:decimal?,askQuantity:decimal?,asOf:timestamp?,status:Quot eStatus}.

**오류** 404 ORDERBOOK_NOT_SUPPORTED / 503 DATA_UNAVAILABLE

**처리** 최우선 1호가만. LAST PRICE와 호가를 혼동하지 않는다. 지연 시 호가주문 UI 비활성화.

---

<a id="source-page-7"></a>

<!-- 원본 PDF 7 / 23쪽 -->

## 06 주문·취소·체결 내역

### POST /accounts/{accountId}/orders

TRADE-01/02/06 매수·매도 접수 · 필수 / 호가 권장 · 인증: Bearer

**요청** header: Idempotency-Key. body: OrderCreate.

**응답** 201 data: Order(status=PENDING). 실제 체결 정보는 아직 없음.

**오류** 422 INSUFFICIENT_CASH, INSUFFICIENT_QUANTITY, INVALID_QUANTITY, INVALID_PRICE_TICK, MIN_ORDER_NOT_MET / 409 MARKET_CLOSED, INSTRUMENT_HALTED, ACCOUNT_CLOSED / 503 PRICE_UNAVAILABLE

**처리** 예약금/수량 반영 후 접수. 호가주문은 type=LIMIT, source=ORDERBOOK, limitPrice=선택 호가.

### GET /accounts/{accountId}/orders

TRADE-05 주문·미체결 목록 · 필수 · 인증: Bearer

**요청** query: status(선택), instrumentId,side,from,to,cursor,limit(선택).

**응답** 200 data: Order[]. 생성시각 내림차순.

**오류** 400 VALIDATION_ERROR / 403 FORBIDDEN

**처리** 미체결은 status=PENDING으로 조회. 초기화 이전 주문도 기간·상태에 맞춰 반환.

### GET /accounts/{accountId}/orders/{orderId}

주문 상태 조회 · 필수 · 인증: Bearer

**요청** path: accountId,orderId.

**응답** 200 data:{order:Order,execution:Execution?}.

**오류** 403 FORBIDDEN / 404 ORDER_NOT_FOUND

**처리** 해당 계좌에 속하지 않는 orderId는 404. 체결 후 execution 포함.

### POST /accounts/{accountId}/orders/{orderId}/cancel

TRADE-05 주문 취소 · 필수 · 인증: Bearer

**요청** body 없음.

**응답** 200 data: Order(status=CANCELED).

**오류** 409 ORDER_ALREADY_FILLED, ORDER_REJECTED / 403 FORBIDDEN

**처리** PENDING만 취소. 이미 CANCELED면 기존 결과 반환. 예약량 즉시 해제.

### GET /accounts/{accountId}/executions

TRADE-03 체결 내역 · 필수 · 인증: Bearer

**요청** query: instrumentId,side,from,to,cursor,limit(선택).

**응답** 200 data: Execution[]. 체결시각 내림차순.

**오류** 400 INVALID_PERIOD / 403 FORBIDDEN

**처리** 해지·초기화 이전 이력 조회 가능. 체결은 주문 ID를 통해 소속 계좌·구간을 검증한다.

배치 체결은 내부 작업으로 공개 POST /execute 같은 API를 제공하지 않는다. 주문 상태는 조회 API로 확인하며 WebSocket 시세 갱신이 체결 완료를 뜻하지 않는다.

---

<a id="source-page-8"></a>

<!-- 원본 PDF 8 / 23쪽 -->

## 07 자산·랭킹

### GET /accounts/{accountId}/portfolio

PORT-01/02 보유·총자산 · 필수 · 인증: Bearer

**요청** path: accountId.

**응답** 200 data: Portfolio.

**오류** 403 FORBIDDEN / 404 ACCOUNT_NOT_FOUND

**처리** 현재 성과구간 기준. 총자산 계산 불가면 totalAssets·returnRate는 null, valuationStatus=INCOMPLETE.

### GET /rankings

RANK-01 시장별 랭킹 · 필수 · 인증: Bearer

**요청** query: market(필수), limit(int,기본·최대100).

**응답** 200 data:{runId:UUID?,asOf:timestamp?,entries:RankingEntry[]}.

**오류** 400 VALIDATION_ERROR

**처리** 순위 100위 이하에서 최대 100개 계좌만 제공. 동률은 계좌 ID로 안정 정렬. 아직 산정 없으면 runId/asOf=null, entries=[].

### GET /accounts/{accountId}/ranking

RANK-02 내 순위 · 필수 · 인증: Bearer

**요청** path: accountId.

**응답** 200 data: MyRanking.

**오류** 403 FORBIDDEN / 404 ACCOUNT_NOT_FOUND

**처리** status=RANKED/NOT_COMPUTED/OPTED_OUT/PENDING_RECALC/INELIGIBLE. 초기화 이후 이전 구간 순위는 숨긴다.

### GET /accounts/{accountId}/snapshots

PORT-03 자산 변화 · 후속 · 인증: Bearer

**요청** query: from,to,cursor,limit(선택).

**응답** 200 data:{id,periodId,asOf,cash,valuation,totalAssets,returnRate}[].

**오류** 400 INVALID_PERIOD / 403 FORBIDDEN

**처리** asOf 오름차순. 초기화 전후 구간을 분리 표시.

### GET /accounts/{accountId}/ranking-history

RANK-03 순위 이력 · 후속 · 인증: Bearer

**요청** query: from,to,cursor,limit(선택).

**응답** 200 data:{runId,periodId,asOf,rank,totalAssets,returnRate}[].

**오류** 400 INVALID_PERIOD / 403 FORBIDDEN

**처리** 본인 과거 결과만. 당시 순위는 이후 옵트아웃/초기화로 재계산하지 않는다.

---

<a id="source-page-9"></a>

<!-- 원본 PDF 9 / 23쪽 -->

## 08 관심종목·목록

### GET /me/watch-items

STOCK-05 관심종목 조회 · 필수 · 인증: Bearer

**요청** query: cursor,limit(선택).

**응답** 200 data:{id,instrument:InstrumentSummary,createdAt}[].

**오류** 401 UNAUTHORIZED

**처리** 등록시각 내림차순.

### POST /me/watch-items

STOCK-05 관심종목 등록 · 필수 · 인증: Bearer

**요청** body: instrumentId(UUID,필수).

**응답** 201 data:{id,instrumentId,createdAt}.

**오류** 409 WATCH_ITEM_EXISTS, WATCH_ITEM_LIMIT / 404 INSTRUMENT_NOT_FOUND

**처리** 사용자당 고유 관심종목 최대 100개.

### DELETE /me/watch-items/{itemId}

STOCK-05 관심종목 삭제 · 필수 · 인증: Bearer

**요청** path: itemId.

**응답** 204.

**오류** 403 FORBIDDEN / 404 WATCH_ITEM_NOT_FOUND

**처리** 모든 목록의 해당 소속 행도 제거한다.

### GET /me/watchlists

WATCH-01 관심목록 조회 · 필수 · 인증: Bearer

**요청** 요청값 없음.

**응답** 200 data:{id,name,itemCount,createdAt}[].

**오류** 401 UNAUTHORIZED

**처리** 최대 20개, 생성시각 오름차순. meta.nextCursor=null.

### POST /me/watchlists

WATCH-01 관심목록 생성 · 필수 · 인증: Bearer

**요청** body: name(string,1\~40,필수).

**응답** 201 data:{id,name,itemCount:0,createdAt}.

**오류** 409 WATCHLIST_LIMIT / 400 VALIDATION_ERROR

**처리** 최대 20개. 같은 이름 허용 제안, ID로 구분.

---

<a id="source-page-10"></a>

<!-- 원본 PDF 10 / 23쪽 -->

## 09 목록 편집·종목 이동

### PATCH /me/watchlists/{listId}

WATCH-01 목록명 변경 · 필수 · 인증: Bearer

**요청** body: name(string,1\~40,필수).

**응답** 200 data:{id,name,itemCount,createdAt}.

**오류** 403 FORBIDDEN / 404 WATCHLIST_NOT_FOUND

### DELETE /me/watchlists/{listId}

WATCH-01 목록 삭제 · 필수 · 인증: Bearer

**요청** path: listId.

**응답** 204.

**오류** 403 FORBIDDEN / 404 WATCHLIST_NOT_FOUND

**처리** 소속 행만 삭제. 관심종목 자체는 유지한다.

### GET /me/watchlists/{listId}/items

WATCH-01 목록 내 종목 · 필수 · 인증: Bearer

**요청** query: cursor,limit(선택).

**응답** 200 data:{id,instrument:InstrumentSummary,createdAt}[].

**오류** 403 FORBIDDEN / 404 WATCHLIST_NOT_FOUND

**처리** 관심종목 등록시각 내림차순.

### PUT /me/watchlists/{listId}/items/{itemId}

WATCH-01 종목을 목록에 추가 · 필수 · 인증: Bearer

**요청** path: listId,itemId. body 없음.

**응답** 204. 이미 같은 소속이면 그대로 성공.

**오류** 403 FORBIDDEN / 404 WATCH_ITEM_NOT_FOUND, WATCHLIST_NOT_FOUND

**처리** 두 리소스 모두 본인 소유. 한 관심종목을 여러 목록에 포함할 수 있다.

### DELETE /me/watchlists/{listId}/items/{itemId}

WATCH-01 목록에서만 제거 · 필수 · 인증: Bearer

**요청** path: listId,itemId.

**응답** 204.

**오류** 403 FORBIDDEN / 404 WATCHLIST_NOT_FOUND

**처리** 관심종목 유지. 본인 item의 소속이 이미 없으면 성공.

### POST /me/watchlists/{listId}/move-item

WATCH-01 목록 간 이동 · 필수 · 인증: Bearer

**요청** body: itemId(UUID),targetListId(UUID), 모두 필수.

**응답** 200 data:{itemId,fromListId,toListId}.

**오류** 409 ITEM_NOT_IN_LIST / 403 FORBIDDEN

**처리** 출발 소속 제거·도착 소속 추가를 원자적으로 처리. 같은 목록으로 이동은 변화 없이 성공.

---

<a id="source-page-11"></a>

<!-- 원본 PDF 11 / 23쪽 -->

## 10 메모·기업·재무·공시 조회

### GET / POST /instruments/{instrumentId}/memos

MEMO-01 메모 조회·작성 · 권장 · 인증: Bearer

**요청** GET: cursor,limit. POST body: body(string,1\~10,000,필수).

**응답** GET 200 data: Memo[]. POST 201 data: Memo.

**오류** 404 INSTRUMENT_NOT_FOUND / 400 VALIDATION_ERROR

**처리** 본인 메모만 작성시각 내림차순. Memo={id,instrumentId,body,createdAt,updatedAt}.

### PATCH / DELETE /memos/{memoId}

MEMO-01 메모 수정·삭제 · 권장 · 인증: Bearer

**요청** PATCH body: body(string,1\~10,000,필수). DELETE 본문 없음.

**응답** PATCH 200 data: Memo. DELETE 204.

**오류** 403 FORBIDDEN / 404 MEMO_NOT_FOUND

### GET /companies/{companyId}/financials

FIN-01 상세 재무 / DATA-02 · 필수 · 인증: Bearer

**요청** query: fiscalYear(int,선택), period(FY/Q1/H1/Q3,선택), scope(CONSOLIDATED/SEPARATE,선택), view(STATEMENTS/QUARTERS,기본 QUARTERS).

**응답** 200 data: FinancialView. 기본 최근 4분기 핵심 지표.

**오류** 404 COMPANY_NOT_FOUND / 400 INVALID_PERIOD / 503 DATA_UNAVAILABLE

**처리** 범위 미지정 시 연결 우선, 없으면 별도 표시. 누적값 차이로 분기값 산출, 필요한 값 없으면 null.

### GET /companies/{companyId}/disclosures

DATA-01 공시 목록 · 필수 · 인증: Bearer

**요청** query: documentType(ANNUAL/HALF/QUARTER/MAJOR),from,to,cursor,limit(선택).

**응답** 200 data: Disclosure[].

**오류** 404 COMPANY_NOT_FOUND / 400 INVALID_PERIOD

**처리** 공시시각 내림차순. 정정 관계도 표시한다.

### GET /disclosures/{documentId}

공시 상세·AI 출처 확인 · 필수 · 인증: Bearer

**요청** path: documentId.

**응답** 200 data: Disclosure + {companyId,version,supersedesId:UUID?}.

**오류** 404 DOCUMENT_NOT_FOUND

**처리** 원문 sourceUrl 제공. rawObjectKey 등 내부 저장경로는 노출하지 않는다.

---

<a id="source-page-12"></a>

<!-- 원본 PDF 12 / 23쪽 -->

## 11 AI 질의·답변

### POST /ai/answers

AI-01\~06·09\~10 자연어 질의 · 필수 · 인증: Bearer

**요청** body: question(string,1\~2,000,필수), companyIds(UUID[],선택), fiscalYear(int,선택), accountId(UUID,후속 선택).

**응답** 200 data: AiAnswer. status=COMPLETED/NEEDS_CLARIFICATION/INSUFFICIENT_EVIDENCE.

**오류** 400 VALIDATION_ERROR / 403 FORBIDDEN / 429 AI_RATE_LIMIT / 503 AI_TOOL_UNAVAILABLE

**처리** 동기 응답 제안. 타임아웃·요금·분당 호출량은 배포 전 설정. 질문이 불명확하면 추가질문 상태로 반환.

### GET /ai/answers/{answerId}

AI-09 이전 답변·출처 조회 · 필수 · 인증: Bearer

**요청** path: answerId.

**응답** 200 data: AiAnswer.

**오류** 403 FORBIDDEN / 404 ANSWER_NOT_FOUND

**처리** 본인 답변만. 저장 당시 응답과 출처를 반환하며 최신 데이터로 자동 재작성하지 않는다.

### 후속 AI도 같은 질의 API로 확장

AI-07 기업 비교, AI-08 추세, AI-11 포트폴리오, AI-12 거래내역 질의는 별도 엔드포인트 대신 /ai/answers의 의도 분 류와 Tool Routing을 확장한다. 미출시 의도는 404 FEATURE_NOT_AVAILABLE로 명확히 알린다. 후속 accountId를 받 더라도 서버가 본인 계좌인지 확인한다.

### 도구별 내부 입력·출력

| 도구 | 입력 → 출력 |
| --- | --- |
| StockPriceTool | instrumentId, 기간 → Quote/Candle, provider, asOf |
| FinancialStatementTool | companyId, 회계연도·기간·범위·항목 → 값·단위·출처 |
| FinancialMetricTool | 표준 항목값·기간 → metricCode, value?, formula, inputSources |
| DisclosureRAGTool | 질문·기업·기간·공시유형 → chunkId, content, score, source |
| PortfolioTool · 후속 | 인증 사용자, accountId → Portfolio |
| TransactionTool · 후속 | 인증 사용자, accountId, 조회조건 → Execution[] |

내부 도구는 사용자에게 호출 권한을 직접 제공하지 않는다. AI가 주문·초기화 API를 도구로 호출하지 않도록 읽기 전용 허용 목록을 유지 한다.

---

<a id="source-page-13"></a>

<!-- 원본 PDF 13 / 23쪽 -->

## 12 응답 모델 · 계좌와 자산

### Account / AccountSummary

| 필드 | 형식·필수 여부 | 설명 |
| --- | --- | --- |
| id, name, market | string, 필수 | 계좌 ID·이름·시장 |
| status | string, 필수 | ACTIVE / CLOSED |
| cashBalance, reservedCash | decimal, 필수 | 현금·예약금 |
| availableCash | decimal, 필수 | 현금 - 예약금 |
| currentPeriodId | UUID, 필수 | 현재 성과구간 |
| initialCapital | decimal, 필수 | 현재 구간 기준자산 |
| rankingOptOut | boolean, 필수 | 공개 제외 여부 |
| createdAt, closedAt | timestamp / ? | 생성·해지시각 |
| resetAvailable, nextResetAt | boolean / timestamp? | 초기화 가능 여부·다음 KST 자정 |

AccountSummary는 id,name,market,status에 totalAssets(decimal?), returnRate(decimal?), valuationStatus, asOf(timestamp?)를 포함한 목록 전용 모델이다. Account 상세와 필드를 혼용하지 않는다.

### Portfolio

| 필드 | 형식·필수 여부 | 설명 |
| --- | --- | --- |
| accountId, periodId | UUID, 필수 | 계좌·현재 구간 |
| cashBalance, reservedCash,<br>availableCash | decimal, 필수 | 예약금을 포함한 현금과 주문 가능액 |
| valuation, totalAssets | decimal?, 필수 키 | 보유 평가액·현금+평가액 |
| unrealizedPnl, returnRate | decimal?, 필수 키 | 미확정 손익·구간 수익률 |
| realizedPnl | decimal, 필수 | 누적 확정 손익 |
| valuationStatus | string, 필수 | COMPLETE / STALE / INCOMPLETE |
| asOf | timestamp?, 필수 키 | 사용 시세 중 가장 오래된 기준시각 |
| holdings | Holding[], 필수 | 보유수량 0 초과만 반환 |

Holding={instrumentId,name,quantity,reservedQuantity,averagePrice,costBasis,currentPrice?,valuation?,unrealizedPnl?,returnRate?,quote AsOf?,quoteStatus}. 가격 관련 값은 모두 decimal 문자열. 종목 수익률=(현재가-평균매입가)/평균매입가×100. 보유종목이 없으면 평가액 0, 총자산=현금, valuationStatus=COMPLETE.

---

<a id="source-page-14"></a>

<!-- 원본 PDF 14 / 23쪽 -->

## 13 요청·응답 모델 · 주문

### OrderCreate

| 필드 | 형식·필수 여부 | 설명 |
| --- | --- | --- |
| instrumentId | UUID, 필수 | 계좌와 같은 시장의 종목 |
| side | string, 필수 | BUY / SELL |
| type | string, 필수 | MARKET / LIMIT |
| quantity | decimal, 필수 | 0 초과, 주식 정수·가상화폐 설정 정밀도 |
| limitPrice | decimal, 조건부 | LIMIT이면 필수, MARKET이면 보내지 않음 |
| source | string, 선택 | FORM(기본) / ORDERBOOK |

### Order

| 필드 | 형식·필수 여부 | 설명 |
| --- | --- | --- |
| id, accountId, periodId,<br>instrumentId | UUID, 필수 | 주문과 소속 |
| side, type, source | string, 필수 | OrderCreate 열거형 |
| quantity | decimal, 필수 | 접수 수량 |
| limitPrice | decimal?, 필수 키 | 시장가는 null |
| status | string, 필수 | PENDING / FILLED / CANCELED / REJECTED |
| reservedCash, reservedQuantity | decimal, 필수 | 현재 주문 예약량 |
| reasonCode | string?, 필수 키 | 실패·취소·보류 사유 |
| createdAt, expiresAt | timestamp / ? | 접수시각·유효기간 |

### Execution

| 필드 | 형식·필수 여부 | 설명 |
| --- | --- | --- |
| id, orderId, instrumentId | UUID, 필수 | 체결·주문·종목 |
| side | string, 필수 | BUY / SELL |
| quantity, price, grossAmount | decimal, 필수 | 수량·체결가·거래금액 |
| fee, tax, realizedPnl | decimal, 필수 | 수수료·세금·실현손익 |
| executedAt, quoteAsOf | timestamp, 필수 | 체결시각·실제 사용 시세시각 |

---

<a id="source-page-15"></a>

<!-- 원본 PDF 15 / 23쪽 -->

## 14 응답 모델 · 시세·랭킹·초기화

### InstrumentSummary / Quote / Candle

| 필드 | 형식·필수 여부 | 설명 |
| --- | --- | --- |
| InstrumentSummary | object | id, market, symbol, name, currentPrice?, changeRate?, volume?,<br>quoteAsOf?, quoteStatus |
| Quote | object | price?, changeRate?, open?, high?, low?, volume?, marketCap?,<br>asOf?, receivedAt?, status |
| QuoteStatus | string | LIVE / DELAYED / UNAVAILABLE. LIVE는 설정된 유효시간 내 데이<br>터라는 의미 |
| Candle | object | openedAt, open, high, low, close, volume. openedAt은 시각, 나<br>머지는 decimal |

시세가 없는 경우 nullable 필드를 0으로 채우지 않는다. DELAYED는 마지막 값을 표시할 수 있지만 주문·체결에는 시장 설정의 유효시간 검증을 따로 적용한다.

### RankingEntry / MyRanking

| 필드 | 형식·필수 여부 | 설명 |
| --- | --- | --- |
| RankingEntry | object | accountId, rank(int), nickname, totalAssets(decimal),<br>returnRate(decimal) |
| MyRanking | object | accountId, status, runId?, asOf?, rank?, totalAssets?, returnRate? |
| status | string | RANKED / NOT_COMPUTED / OPTED_OUT / PENDING_RECALC<br>/ INELIGIBLE |

### ResetResult

| 필드 | 형식·필수 여부 | 설명 |
| --- | --- | --- |
| resetId, accountId | UUID, 필수 | 초기화·계좌 ID |
| previousPeriodId,<br>currentPeriodId | UUID, 필수 | 이전·새 성과구간 |
| cashBalance | decimal, 필수 | "10000000" |
| unrealizedPnl, realizedPnl | decimal, 필수 | "0" |
| canceledOrderCount | int, 필수 | 함께 취소한 미체결 주문 수 |
| resetAt, nextResetAt | timestamp, 필수 | 초기화시각·다음 KST 자정 |

### 수익률·랭킹 표시 기준

랭킹 값은 batch asOf 기준, 포트폴리오는 조회 시 시세 기준이므로 값이 다를 수 있다. 각 시각을 화면에 표시한다. 초 기화 이후에는 이전 구간의 랭킹을 숨기고 status=PENDING_RECALC를 반환한다. 가상화폐와 주식은 합산 랭킹을 제 공하지 않는다.

---

<a id="source-page-16"></a>

<!-- 원본 PDF 16 / 23쪽 -->

## 15 응답 모델 · 재무·공시·AI

### FinancialView / Disclosure

| 필드 | 형식·필수 여부 | 설명 |
| --- | --- | --- |
| FinancialView | object | companyId,scope,view,items:FinancialItem[] |
| FinancialItem | object | fiscalYear,period,metrics:{표준코드:decimal 또는<br>null},currency,sourceIds:UUID[],sourceUrls:string[],isDerived:boolea<br>n |
| scope | string | CONSOLIDATED / SEPARATE. 반환값으로 실제 적용 기준 표시 |
| period | string | STATEMENTS:FY/Q1/H1/Q3, QUARTERS:Q1/Q2/Q3/Q4 |
| Disclosure | object | id,title,documentType,publishedAt,fiscalYear?,sourceUrl |

### AiAnswer

| 필드 | 형식·필수 여부 | 설명 |
| --- | --- | --- |
| id | UUID, 필수 | 응답 ID |
| status | string, 필수 | COMPLETED / NEEDS_CLARIFICATION / INSUFFICIENT_EVIDENCE |
| intent | string, 필수 | COMPANY_INFO/FINANCIAL/METRIC/DISCLOSURE/COMPARE/TR<br>END/PORTFOLIO/TRANSACTION/UNKNOWN |
| answer | string, 필수 | 답변 또는 추가질문·근거 부족 안내 |
| facts | AiFact[], 필수 | 조회·계산한 수치, 없으면 [] |
| sources | AiSource[], 필수 | 출처 목록, 근거 부족이면 [] 가능 |
| asOf | timestamp, 필수 | 응답 작성시각 |

AiFact={label:string,value:decimal?,unit:string,period:string,formula:string?,sourceIds:UUID[]}. AiSource={id:UUID,kind:DISCLOSURE/FINANCIAL/MARKET/PORTFOLIO,title:string,url:string?,asOf:timestamp,documentId:UUID?,chunkId: UUID?,statementId:UUID?,locator:object}. sourceIds는 같은 답변 sources의 id를 참조한다.

공시 출처는 문서명·공시일·documentId와 원문 URL, 가능하면 절/페이지를 제공한다. 재무 출처는 재무제표 ID·기간·항목을 제공한다. 내 부 포트폴리오 출처는 외부 URL이 없어도 계좌·성과구간·조회시각으로 확인 가능해야 한다.

---

<a id="source-page-17"></a>

<!-- 원본 PDF 17 / 23쪽 -->

## 16 거래·초기화 예시

### ① 주문 접수 · 예시 ID는 실제 발급 UUID로 대체

```http
POST /api/v1/accounts/{accountId}/orders
Authorization: Bearer {accessToken}
Idempotency-Key: buy-example-001
```

```json
{
  "instrumentId": "{instrumentId}",
  "side": "BUY",
  "type": "LIMIT",
  "quantity": "10",
  "limitPrice": "50000",
  "source": "FORM"
}
```

201 응답: data에 id·accountId·periodId·instrumentId와 주문 조건, status:"PENDING", reservedCash:"500000", reservedQuantity:"0", reasonCode:null 등을 반환한다. 예시는 수수료·세금 0원이다.

### ② 다음 배치에서 조건 충족

시세 49,900원일 때 10주 전량 체결. 총액 499,000원, 현금 9,501,000원, 잔여 예약 해제. 주문 status:"FILLED", Execution.price:"49900", quantity:"10", fee:"0", tax:"0". 50,100원이면 미체결 상태를 유지한다.

### ③ 초기화

```http
POST /api/v1/accounts/{accountId}/reset
Idempotency-Key: reset-example-001
```

```json
{"confirm": true}
```

200 data는 새 currentPeriodId, cashBalance:"10000000", unrealizedPnl:"0", realizedPnl:"0", canceledOrderCount, resetAt, nextResetAt을 포함한다. 다음 GET portfolio의 holdings는 []. 기존 체결 조회에는 매수 이력이 남는다.

### ④ 같은 날 새 키로 다시 초기화

409 오류 예시: {"error":{"code":"RESET_DAILY_LIMIT", "message":"이 계좌는 오늘 이미 초기화했습니다.","details":[]}, "requestId":"req-example-004"}

같은 Idempotency-Key 재시도는 기존 성공 응답을 반환한다. 새로운 키로 같은 KST 날짜에 재시도하면 일일 제한에 걸린다. 키가 달라도 거래 정합성 제약을 우회하지 못한다.

### 시장가 가격 상승 예외

접수 시 최신가로 예약한 후 다음 배치 가격이 상승하면 다른 주문 예약금을 제외한 가용현금을 다시 계산한다. 전량 체결금액이 부족하면 REJECTED / INSUFFICIENT_CASH_AT_EXECUTION으로 종료하고 예약을 해제한다. 잔액을 음수로 만들거나 임의 부분체결하지 않는다.

---

<a id="source-page-18"></a>

<!-- 원본 PDF 18 / 23쪽 -->

## 17 오류 코드·주문 상태 전이

| HTTP / 코드 | 조건 |
| --- | --- |
| 400 VALIDATION_ERROR | 필수값·UUID·열거형·숫자 문자열·길이·기간 오류 |
| 401 UNAUTHORIZED / INVALID_CREDENTIALS | 토큰 없음/만료 또는 로그인 인증 실패 |
| 403 FORBIDDEN / EMAIL_NOT_VERIFIED | 타인 자원·관리 권한 없음 / 메일 미인증 |
| 404 *_NOT_FOUND | 존재하지 않는 계좌·주문·종목·공시·답변 등 |
| 409 ACCOUNT_LIMIT_EXCEEDED / RESET_DAILY_LIMIT | 활성 계좌 3개 초과 / 계좌별 KST 일일 초기화 제한 |
| 409 IDEMPOTENCY_CONFLICT / REQUEST_IN_PROGRESS | 같은 키 다른 내용 / 동일 요청 처리 중 |
| 409 MARKET_CLOSED / INSTRUMENT_HALTED | 거래시간 외 접수 / 거래정지 종목 |
| 422 INSUFFICIENT_CASH / INSUFFICIENT_QUANTITY | 주문 가능한 현금·수량 부족 |
| 422 INVALID_PRICE_TICK / INVALID_QUANTITY | 가격 호가단위 또는 수량 정밀도·양수 조건 위반 |
| 429 EMAIL_RESEND_LIMIT / LOGIN_LOCKED / AI_RATE_LIMIT | 재발송·인증 실패·AI 호출 제한. 가능하면 Retry-After 제공 |
| 503 PRICE_UNAVAILABLE / DATA_UNAVAILABLE | 신규 주문에 쓸 가격 없음 / 외부 자료 이용 불가 |
| 503 AI_TOOL_UNAVAILABLE / MARKET_NOT_READY | AI 도구 전면 실패 / 시장 정책·데이터 준비 안 됨 |

### 주문 전이

PENDING → FILLED: 유효 시세·잔액·수량·지정가 조건 충족. PENDING → CANCELED: 사용자 취소, 장 마감 만료, 자금 초기화. PENDING → REJECTED: 체결 시 잔액 부족, 거래 불가·시장 규칙 변경 등. PENDING 유지: 지정가 조건 미충족 또는 일시적 시세 지연. FILLED/CANCELED/REJECTED는 최종 상태로 되돌리지 않는다.

상태 reasonCode 예시: USER_CANCELED, EXPIRED, ACCOUNT_RESET, INSUFFICIENT_CASH_AT_EXECUTION, PRICE_STALE, TRADING_NOT_ALLOWED. HTTP 오류와 저장된 주문 사유는 별개다. 배치 실패는 GET 주문 응답의 status/reasonCode로 전달한다.

---

<a id="source-page-19"></a>

<!-- 원본 PDF 19 / 23쪽 -->

## 18 권장 API · 전략·신호·알림

### GET / POST /me/strategies

STRATEGY-01 전략 목록·생성 · 권장 · 인증: Bearer

**요청** GET: cursor,limit. POST: name(1\~80),market,conditions(StrategyCondition), 모두 필수.

**응답** GET 200 Strategy[]. POST 201 Strategy.

**오류** 400 INVALID_STRATEGY / 404 FEATURE_NOT_AVAILABLE

**처리** 조건은 JSON 규칙, 코드 실행 금지.

### PATCH / DELETE /me/strategies/{strategyId}

STRATEGY-01 전략 수정·삭제 · 권장 · 인증: Bearer

**요청** PATCH: name,enabled,conditions(최소1개). DELETE: 본문 없음.

**응답** PATCH 200 Strategy. DELETE 204.

**오류** 403 FORBIDDEN / 404 STRATEGY_NOT_FOUND

**처리** 수정 시 version 증가, 삭제 시 비활성/소프트 삭제로 신호 이력 보존.

### GET /me/signals

SIGNAL-01 신호 목록 · 권장 · 인증: Bearer

**요청** query: market,strategyId,from,to,cursor,limit(선택).

**응답** 200 Signal[].

**오류** 400 INVALID_PERIOD

**처리** 발생시각 내림차순.

### GET /me/notifications

SIGNAL-01 / ALERT-01 알림 목록 · 권장 / 공시 후속 · 인증: Bearer

**요청** query: unreadOnly(boolean,기본false),cursor,limit.

**응답** 200 {id,kind,payload,readAt?,createdAt}[].

**오류** 401 UNAUTHORIZED

**처리** SIGNAL은 권장, DISCLOSURE는 공시 자동동기화와 함께 후속.

### PATCH /me/notifications/{notificationId}

알림 읽음 처리 · 권장 · 인증: Bearer

**요청** body: read(true,필수).

**응답** 200 {id,readAt}.

**오류** 403 FORBIDDEN / 404 NOTIFICATION_NOT_FOUND

### 전략·신호 모델

Strategy={id,name,market,enabled,conditions,version,createdAt,updatedAt}. StrategyCondition={type:"MA_CROSS",shortPeriod:5|20,longPeriod:20|60,direction:"GOLDEN"|"DEAD",interval:"D1"}. shortPeriod는 longPeriod보다 작아야 한다. Signal={id,strategyId,strategyVersion,instrumentId,kind,occurredAt,evidence:{shortMa,longMa}}. 두 MA 값 은 decimal이다.

---

<a id="source-page-20"></a>

<!-- 원본 PDF 20 / 23쪽 -->

## 19 운영 API · 후속 관리자 화면

### GET /admin/data-status

ADMIN-01 갱신 상태 · 후속 · 인증: 관리자 Bearer

**요청** query: source(QUOTE/DISCLOSURE/FINANCIAL/VECTOR,선택).

**응답** 200 {source,lastSuccessAt?,successCount,failureCount,status}[].

**오류** 403 ADMIN_REQUIRED / 503 DATA_UNAVAILABLE

**처리** 대상별 최근 실행/갱신 상태를 제공한다.

### POST /admin/ingestion-jobs

SYNC-02 수집·재처리 시작 · 후속 · 인증: 관리자 Bearer

**요청** body: kind(DISCLOSURE/FINANCIAL/REINDEX),companyId,parameters(조건부), 모두 필수.

**응답** 202 {id,status:"QUEUED"}.

**오류** 400 INVALID_JOB_PARAMETERS / 409 JOB_ALREADY_RUNNING

**처리** DISCLOSURE는 from/to, FINANCIAL은 fiscalYear, REINDEX는 documentId 필수. 서버 작업 큐로 전달.

### GET /admin/ingestion-jobs/{jobId}

수집 작업 상태 · 후속 · 인증: 관리자 Bearer

**요청** path: jobId.

**응답** 200 {id,kind,companyId,status,stage,attemptCount,successCount,failureCount,lastErrorCode?,startedAt?,finishedAt?}.

**오류** 403 ADMIN_REQUIRED / 404 JOB_NOT_FOUND

### 1차 내부 작업 · 외부 API 아님

| 작업 | 실행 기준 / 결과 |
| --- | --- |
| 시세 갱신 SYNC-01 | 시장 공급자에서 현재가·호가·봉 수집 → Redis/price_candles 갱신. 유효시간·호출 주기는 설정값. |
| 1분 체결 TRADE-04 | 활성 시장의 PENDING 주문 → 계좌 단위 잠금·상태 검증 → 체결·예약 해제·원장 갱신. |
| 랭킹 RANK-01 | 매일 KST 00:10 → 시장별 공통 asOf → 성공 회차를 원자적으로 공개. |
| DATA-01\~03 | 1차는 관리 실행 경로에서 공시/재무 수집·정규화. 작업 ID·성공/실패 로그를 생성. |
| RAG-01\~06 | 수집→전처리→청크→임베딩→인덱스. 질문 시 필터·검색. 사용자에게 DB 관리 API를 노출하지 않음. |
| 후속 동기화 | 정해진 스케줄로 신규 공시 감지 → 같은 ingestion 파이프라인 재사용. |

MONITOR-01 이상 거래 감지와 외부 푸시 전송 API는 탐지 규칙·채널·수신설정이 미정이므로 예약된 공개 경로를 만들지 않았다. 범위 확 정 후 이 문서에 추가한다.

---

<a id="source-page-21"></a>

<!-- 원본 PDF 21 / 23쪽 -->

## 20 WebSocket · 권장 시세 채널

연결 경로 /api/v1/ws/quotes. 브라우저는 Bearer 토큰을 URL 쿼리에 넣지 않고 연결 직후 인증 메시지로 전달한다. 운 영 시 wss만 허용하고 허용 Origin을 확인하는 설계안이다.

| 방향·유형 | 메시지 계약 |
| --- | --- |
| 클라이언트 → auth | {type:"auth",accessToken:string}. 서버는 인증 전 구독을 허용하지 않는다. |
| 서버 → auth_ok | {type:"auth_ok",expiresAt:timestamp}. 인증 실패 시 error 후 연결 종료. |
| 클라이언트 → subscribe | {type:"subscribe",instrumentIds:UUID[],channels:["QUOTE","ORDERBOOK"]}. ORDERBOOK은<br>기능 활성화 시만. |
| 클라이언트 → unsubscribe | {type:"unsubscribe",instrumentIds:UUID[]}. 해당 종목의 구독 해제. |
| 서버 → quote | {type:"quote",instrumentId,price:decimal,changeRate:decimal,asOf:timestamp,receivedAt:times<br>tamp,sequence:int} |
| 서버 → orderbook | {type:"orderbook",instrumentId,bidPrice:decimal,askPrice:decimal,bidQuantity:decimal?,askQu<br>antity:decimal?,asOf,sequence} |
| 서버 → error | {type:"error",code,message}. AUTH_REQUIRED/INVALID_TOKEN/SUBSCRIPTION_LIMIT/ORDER<br>BOOK_NOT_SUPPORTED 등. |
| 양방향 연결 확인 | 클라이언트 ping → 서버 pong. 주기·인증 제한시간·구독 개수는 서버 설정으로 확정한다. |

### 재연결·데이터 공백

클라이언트는 단절 즉시 마지막 시세를 지연으로 표시한다. 재연결 후 다시 인증·구독하고 REST 상세/호가 조회로 최 신 상태를 복구한다. sequence는 연결별 증가값이며 재접속 시 초기화한다. 영구 이벤트 재생은 지원하지 않는 제안이 다.

### 보안·토큰 만료

액세스 토큰 만료 시 서버는 TOKEN_EXPIRED 오류 후 연결을 종료한다. 클라이언트는 /auth/refresh로 갱신한 뒤 재연 결한다. auth 메시지는 로그에서 토큰을 삭제한다. 시세 연결은 주문 접수·체결 채널과 구분되며 사용자가 본 가격에 체결되는 것을 보장하지 않는다.

---

<a id="source-page-22"></a>

<!-- 원본 PDF 22 / 23쪽 -->

## 21 정책 일치·미확정 설정

| 정책 | API·ERD 공통 적용 |
| --- | --- |
| P-01 | 계좌 최대 3개, 시장 분리, 초기금 1,000만원, 자동 계좌 개설 없음. signup은 회원만 생성한다. |
| P-02 | 1분 배치·전량 체결, 주식 장외 접수 거부·당일 만료, 가상화폐 지정가는 취소 전까지 유효. |
| P-03 | 비용·정밀도·시세 유효시간은 설정값. 예시만 비용 0원. 매수 수수료는 취득원가에 포함, 매도 비용·세금은 실현손<br>익에 반영. |
| P-04 | 현금/수량 예약, 체결 직전 재검증, 지연 시세 보류, 시장가 부족잔액은 실패 종료. |
| P-05 | 초기화 시 미체결 취소·예약 해제·구간 교체. 이전 거래이력 보존, 포지션만 제거. |
| P-06 | 계좌별 노출설정, 동률 공동순위·계좌 ID 정렬. 초기화 후 이전 랭킹 숨김, 다음 배치 반영. |
| P-07 | 연결 우선·별도 구분, ROA/ROE 평균 분모. 필요한 자료 없으면 계산 불가. |
| P-08 | 목록 삭제는 관심종목 유지, AI는 본인 데이터 조회·계산만 허용. |

### 배포 전에 확정할 설정값

시세·공시 공급자와 이용권한, 시장별 거래일·시간·수수료·세금·수량 단위·최소금액·반올림 방식, 시세 최대 경과시간, AI 모델·Vector DB·검색 K·호출/시간/비용 한도, 이메일 발송 환경, 쿠키 도메인·CORS·Origin 허용 목록, 로그·답변 ·Snapshot 보존기간을 확정한다.

### 계약 변경 관리

필드 추가는 기존 클라이언트가 모르는 필드를 무시할 수 있게 한다. 필드 삭제·의미 변경·상태값 재정의는 버전 변경 과 마이그레이션 대상으로 관리한다. 기능 ID → API → 테이블 → 검수 시나리오를 함께 수정한다.

---

<a id="source-page-23"></a>

<!-- 원본 PDF 23 / 23쪽 -->

## 22 API 검수 체크리스트

| 영역 | 필수 확인 |
| --- | --- |
| 인증 | 미인증·만료·위조 토큰, 로그인 5회 제한, 이메일 24시간·재발송 1분, OAuth state 실패, CSRF 없는<br>refresh/logout 거부 |
| 계좌 | 동시 4번째 개설 차단. 동일 멱등 키 재요청은 계좌 1개만 생성. 타인 계좌 조회·변경 거부 |
| 주문 | 접수 201과 체결 완료 구분, LIMIT 가격 검증, 중복 키 주문 1개, 예약 초과 차단, 시장가 상승 시 잔액 재검<br>증 |
| 취소·초기화 | 체결·취소 동시 실행 결과 일관성. 초기화 시 미체결 취소·잔고·포지션·손익 동시 변경. KST 자정 경계 테스<br>트 |
| 자산·랭킹 | Decimal 직렬화, 가격 누락 null, 현재 값·배치 값 기준시각 표시, 옵트아웃 즉시 제외, 초기화 이전 순위 숨<br>김 |
| 목록·메모 | 100/20개 한도, 타인 소속 연결 거부, 목록 삭제 후 관심종목 유지, 메모 본인 권한 |
| AI | 정형 수치·계산 공식·공시 출처 확인. 근거 없음 상태, 도구 실패 503, 개인 계좌 인가, 문서 지시문 무시 |
| 운영·실시간 | 실패 작업 추적·재시도, 00:10 배치, 시세 단절 지연 표시·재구독, 토큰 로그 미노출 |

### 작성 범위

이 명세서는 경로·요청·응답·상태·오류·모델·내부 실행 기준을 정의한다. 실제 서버 구현·부하 테스트·공급자 API 검증은 수행하지 않았다. 기능명세서 v1.1의 제안 정책을 팀에서 승인한 후 구현 기준으로 고정한다.

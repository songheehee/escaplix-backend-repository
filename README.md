# Escaplix 백엔드

방탈출 테마를 한곳에서 찾고, 같이 갈 사람(파티)을 모으고, 후기를 남기는 통합 예약 사이트의 백엔드입니다.

| 레포 | 내용 |
| --- | --- |
| **escaplix-backend-repository** (여기) | Spring Boot API 서버, DB 스크립트 |
| [escaplix-frontend-repository](https://github.com/songheehee/escaplix-frontend-repository) | 실제 화면 (HTML · JS · CSS, Live Server) |
| [escaplix-mockup-repository](https://github.com/songheehee/escaplix-mockup-repository) | 화면 목업(`visualization/`)과 **설계 문서**(`docs/ERD.md`, `docs/API.md`) |

> **기준 문서는 목업 레포의 `docs/ERD.md`와 `docs/API.md`입니다.** 코드의 주석 번호(A-1, C-16 …)는 API 명세의 번호와 같습니다. 명세와 코드가 다르면 명세가 맞고, 명세를 바꿔야 하면 [명세 먼저 고치기](#4-4-설계를-바꿀-때) 규칙을 따릅니다.

---

## 1. 누가 무엇을 맡나

| 담당 | 이름 | 맡는 기능 | API |
| --- | --- | --- | --- |
| **A** | 회수 | 회원·로그인, 파티·채팅, **공통 뼈대** | A-1 ~ A-22 (22개) |
| **B** | 송희 | 테마 탐색·검색, 찜, 매장 목록·상세 | B-1 ~ B-11 (11개) |
| **C** | 승현 | 사장 매장·테마·시간표, 관리자 승인 | C-1 ~ C-19 (19개) |
| **D** | 하나 | 후기·신고, 내 기록, 선호 장르·추천·통계 | D-1 ~ D-11 (11개) |

### 각자 고치는 파일

다른 사람 파일을 고쳐야 하면 먼저 주인에게 말하고, PR 설명에 적어 주세요.

| 담당 | controller | service | dto | entity · repository |
| --- | --- | --- | --- | --- |
| A | `MemberController`, `AdminMemberController`, `PartyController` | `MemberService(Impl)`, `PartyService(Impl)` | `dto/member`, `dto/party` | Member, Party, PartyMember, PartyMessage |
| B | `ThemeController`, `StoreController` | `ThemeService(Impl)`, `StoreService(Impl)` | `dto/theme`, `dto/store` | Wishlist |
| C | `OwnerStoreController`, `OwnerThemeController`, `AdminController` | `OwnerStoreService(Impl)`, `OwnerThemeService(Impl)`, `AdminService(Impl)` | `dto/owner`, `dto/admin` | Store, Theme, ThemeGenre, TimeSlot, Genre, Holiday |
| D | `ReviewController`, `MyRecordController` | `ReviewService(Impl)`, `RecommendService(Impl)` | `dto/review` | Review, Report, MemberGenre |

**공용 파일은 A가 관리합니다.** 아래 파일은 고칠 일이 있으면 A에게 요청하거나, PR에 A를 리뷰어로 넣어 주세요.

- `common/**`: 토큰·권한, 에러 응답, CORS, 파일 저장, 날 유형
- `scheduler/**`
- `build.gradle`, `application.properties`

- 다른 사람 테이블을 **읽는 건 자유**입니다. 예를 들어 B는 C의 THEME·STORE를, D는 A의 PARTY를 읽어도 됩니다.
- 남의 리포지토리에 조회 메서드를 **추가**하는 것도 괜찮습니다. 이때는 PR 설명에 적어 주세요.
- 엔티티 **컬럼을 바꾸는 건** 그 테이블 주인만 합니다. 바꿀 때는 SQL·ERD도 같이 바꿉니다([4번](#4-4-설계를-바꿀-때)).

### 일정

| 날짜 | 할 일 |
| --- | --- |
| 11/18 (수) | 설계 확정(ERD·API), **스켈레톤(이 코드)** |
| 11/19 (목) 개발 1 | **A: 오전에 A-1(가입)·A-6(로그인) 먼저.** 그래야 다른 사람이 토큰을 받아 테스트할 수 있음. B·C·D: 조회 API부터 |
| 11/20 (금) 개발 2 | 각자 API + 프런트 화면 연결 |
| 11/23 (월) 개발 3 | 각자 필수 기능 완성 |
| 11/24 (화) 개발 4 | 기능 사이 연결: 승인 → 목록 노출, 끝난 파티 → 후기, 추천 배너 |
| 11/25 (수) 개발 5 | 선택 기능(지도·통계) 또는 버그 수정. **기능 추가 마감** |
| 11/26 (목) | main 통합, 서로 점검 |
| 11/27 (금) | 발표 |

---

## 2. 처음 실행하기

### 2-1. DB 만들기 (Oracle XE 11g, hr 계정)

레포 폴더(`escaplix-backend-repository`)에서 순서대로 실행합니다. **쓰는 터미널에 맞는 쪽만** 따라 하세요.

> 내 터미널 구분법: 프롬프트가 `$`로 끝나면 **Git Bash**, `PS C:\...>`로 시작하면 **PowerShell**.
> 서로의 명령을 섞어 쓰면 안 됩니다. (Git Bash에 `$env:...`를 치면 `command not found`가 나고 한글이 깨집니다)

**① hr 계정 잠금 풀기 (처음 한 번)**

XE 11g는 hr 계정이 잠겨 있습니다(`ORA-28000`). SQL Developer에서 **SYSTEM**으로 접속해 실행합니다.

```sql
ALTER USER hr ACCOUNT UNLOCK IDENTIFIED BY 원하는비번;
```

**② 테이블 만들기 → 더미 데이터 넣기**

두 SQL 파일은 UTF-8입니다. Windows Oracle 기본값(MSWIN949)으로 읽으면 한글 주석·문자열이 깨져서 `ORA-00922`·`ORA-01756`이 나고 **일부만 들어갑니다.**
그래서 실행 전에 `NLS_LANG`을 UTF-8로 바꿉니다. (그 터미널 창에서만 적용되고, 창을 닫으면 사라집니다. PC 설정은 안 바뀝니다)

Git Bash:

```bash
export NLS_LANG=AMERICAN_AMERICA.AL32UTF8
echo exit | MSYS_NO_PATHCONV=1 sqlplus -S 'hr/비번@localhost:1521/xe' @sql/create_tables.sql
echo exit | MSYS_NO_PATHCONV=1 sqlplus -S 'hr/비번@localhost:1521/xe' @sql/insert_data.sql
```

PowerShell:

```powershell
$env:NLS_LANG="AMERICAN_AMERICA.AL32UTF8"
sqlplus 'hr/비번@localhost:1521/xe' '@sql/create_tables.sql'   # 끝나면 exit 입력
sqlplus 'hr/비번@localhost:1521/xe' '@sql/insert_data.sql'     # 끝나면 exit 입력
```

- `비번` 자리에 ①에서 정한 hr 비밀번호를 넣습니다. 작은따옴표 안이라 특수문자가 있어도 됩니다.
- Git Bash의 `echo exit |`는 끝나면 `SQL>`에 멈추지 않고 빠져나오게 합니다. PowerShell은 이걸 붙이면 `SP2-0042` 경고가 나서 빼고, 끝난 뒤 `SQL>`에 `exit`를 직접 칩니다.
- `sqlplus`를 못 찾으면 전체 경로로: Git Bash `/c/oraclexe/app/oracle/product/11.2.0/server/bin/sqlplus`, PowerShell `& "C:\oraclexe\app\oracle\product\11.2.0\server\bin\sqlplus.exe"`
- `create_tables.sql`을 **처음** 돌릴 때 나오는 `ORA-00942`·`ORA-02289`(지울 대상 없음)는 정상입니다.
- 성공 기준: Table created 14개, Sequence created 13개, Trigger created 2개. `insert_data.sql`은 에러 없이 `Commit complete.`
- **데이터가 꼬였으면 두 파일을 처음부터 다시** 돌립니다. `create_tables.sql`은 맨 앞에서 우리 테이블을 지우고 새로 만듭니다(hr 예제 테이블 EMPLOYEES 등은 안 건드림).
- SQL Developer로 하고 싶으면: 도구 → 환경설정 → 환경 → 인코딩을 `UTF-8`로 바꾼 뒤 파일을 다시 열고 F5(스크립트 실행).

더미 계정 (비밀번호 모두 `1234`): `admin`(관리자), `owner1`~`owner3`(사장), `user1`~`user6`(손님)

### 2-2. 내 PC 전용 설정 (커밋 금지)

Git Bash:

```bash
cp src/main/resources/application-local.properties.example src/main/resources/application-local.properties
```

PowerShell:

```powershell
Copy-Item src\main\resources\application-local.properties.example src\main\resources\application-local.properties
```

복사한 파일에 hr 비밀번호와 `jwt.secret`(32글자 이상 아무 문자열)을 적습니다. 이 파일은 `.gitignore`에 있어서 커밋되지 않습니다.

### 2-3. 실행·테스트

Git Bash:

```bash
./gradlew bootRun     # http://localhost:8081
./gradlew test        # Oracle 없이 H2로: 스프링이 뜨는지 + 토큰·권한·에러 형식 테스트
```

PowerShell:

```powershell
.\gradlew bootRun     # http://localhost:8081
.\gradlew test
```

- `Started BackendApplication`이 나오면 성공. `http://localhost:8081/api/members/me` → `{"message":"로그인이 필요합니다."}`면 정상입니다.
- 포트가 **8081**인 이유: Oracle XE가 자체 웹서버로 8080을 이미 씁니다.
- Oracle 11g라서 `OracleLegacyDialect`를 씁니다(`application.properties`). 기본 방언은 11g에서 페이징 SQL이 `ORA-00933`으로 깨집니다.
- 프런트(Live Server `127.0.0.1:5500`)에서 부르면 CORS가 이미 열려 있습니다.
- 아직 구현하지 않은 API는 **501** `{"message": "아직 구현 전입니다: A-1 손님 회원가입"}`로 답합니다.

**손으로 API 눌러보기**

- VS Code 확장 **REST Client**(Huachao Mao)를 깔고 `http/` 폴더의 `.http` 파일을 엽니다. 요청 위의 **Send Request**를 누르면 오른쪽에 응답이 뜹니다.
- `http/member.http`: 로그인(더미 계정 + 비번 `1234`) → 받은 토큰이 아래 요청에 자동으로 들어갑니다. 토큰이 필요한 API 테스트는 이걸 복사해서 쓰세요.
- 자기 API를 구현하면 `http/theme.http`, `http/owner.http`처럼 **담당별 파일**을 추가해 주세요.
- Git Bash의 `curl`로 한글을 보내면 깨져서 400·500이 납니다(서버 문제 아님). 한글 요청은 REST Client로 하세요.

**자주 나는 에러**

| 에러 | 원인 → 해결 |
| --- | --- |
| `ORA-28000` / `ORA-01017` | hr 잠김 또는 비밀번호 틀림 → ① |
| `ORA-12541` / `ORA-12514` | Oracle 꺼짐 → `services.msc`에서 `OracleServiceXE`, `OracleXETNSListener` 시작 |
| `ORA-00922` / `ORA-01756` / 테이블·데이터가 일부만 들어감 | `NLS_LANG` 안 바꿈(한글 깨짐) → 터미널 확인 후 ②를 처음부터 |
| `bash: :NLS_LANG=...: command not found` | Git Bash에 PowerShell 명령을 침 → Git Bash 쪽 명령 사용 |
| `Failed to determine a suitable driver class` | `application-local.properties` 없음 → 2-2 |
| `Port 8081 was already in use` | 서버가 이미 켜져 있음 → 기존 창의 서버를 끄기 |
| `Cannot find a Java installation ... 21` | JDK 21 없음. 인터넷이 되면 Gradle이 자동으로 받음. 안 되면 JDK 21 설치 |

---

## 3. 코드 구조와 쓰는 법

```
src/main/java/com/escaplix/backend
├── controller/        컨트롤러 10개: 주소·권한만. 받고 서비스로 넘긴다
├── service/        XxxService(인터페이스) + XxxServiceImpl(구현): 검사·권한 확인·트랜잭션
├── repository/     JpaRepository 14개
├── entity/         테이블 14개 + 상태값 enum 10개
├── dto/            요청·응답 record (member, party, theme, store, owner, admin, review)
├── common/         공용 (A)
│   ├── auth/       @Auth, LoginMember, JwtProvider, AuthInterceptor
│   ├── config/     CORS·인터셉터·정적 파일, BCrypt
│   ├── file/       FileStorage (사진·등록증 저장)
│   ├── time/       DayTypeResolver (날짜 → 주중/주말, 공휴일 포함)
│   └── ApiException, GlobalExceptionHandler
└── scheduler/      모집 기한 지난 파티 자동 마감 (5분마다)
```

### 3-1. 구현하는 순서 (예: A-7 내 정보)

스켈레톤에는 주소, DTO, 권한이 다 들어 있습니다. **`XxxServiceImpl`의 TODO만 채우면 됩니다.**

```java
// service/MemberServiceImpl.java (지금)
@Override
public MyInfoResponse getMyInfo(LoginMember login) {
    // TODO(A) login.memberId() 로 조회. bizNo 는 사장만
    throw ApiException.notImplemented("A-7 내 정보");
}

// 구현 후
@Override
public MyInfoResponse getMyInfo(LoginMember login) {
    Member m = memberRepository.findById(login.memberId())
            .orElseThrow(() -> ApiException.notFound("회원을 찾을 수 없습니다."));
    return new MyInfoResponse(m.getMemberId(), m.getLoginId(), m.getNickname(), m.getRole(),
            m.getBizNo(), m.getCreatedAt());
}
```

TODO에는 API 명세에 적힌 검사·상태 코드·트랜잭션 규칙을 요약해 두었습니다. 자세한 응답 예시와 오류 메시지는 `docs/API.md`의 같은 번호를 보세요.

### 3-2. 로그인한 사람 · 권한

- 컨트롤러 메서드에 붙은 `@Auth`가 **역할(USER/OWNER/ADMIN)** 을 검사합니다. 토큰이 없으면 401, 역할이 다르면 403이고, 이미 다 붙여 두었습니다.
- 서비스는 파라미터 `LoginMember login`으로 "지금 요청한 사람"을 받습니다.
  - **회원번호는 반드시 `login.memberId()`에서 꺼냅니다.** 요청 본문이나 주소의 회원번호를 "나"로 믿지 않습니다.
  - 공개 API에서는 로그인하지 않았으면 `login`이 `null`입니다(찜 여부·내 후기 표시용).
- **"내 매장인지, 방장인지, 내 후기인지"** 같은 주인 확인은 `@Auth`가 해 주지 않습니다. 서비스에서 직접 확인하고, 아니면 `ApiException.forbidden(...)`을 던집니다.
- 관리자 직접 등록(API.md의 `OWNER*`): 사장 API에 관리자도 들어옵니다. `login.is(Role.ADMIN)`이면 소유 확인을 건너뛰고 바로 공개 상태로 저장합니다.

### 3-3. 실패 응답

```java
throw ApiException.conflict("이미 사용 중인 아이디입니다.");    // → 409 {"message": "이미 사용 중인 아이디입니다."}
```

- 상태 코드별로 `badRequest`(400), `unauthorized`(401), `forbidden`(403), `notFound`(404), `conflict`(409)를 씁니다.
- 컨트롤러에서 try-catch로 응답을 만들지 않습니다. `GlobalExceptionHandler`가 한 곳에서 바꿉니다.
- 요청 DTO의 `@NotBlank`, `@Size` 같은 검사는 실패하면 **자동으로 400 + 한국어 메시지**가 나갑니다. 메시지는 DTO에 적혀 있습니다.

### 3-4. 자주 쓰는 공용 도구

| 도구 | 쓰는 곳 | 사용법 |
| --- | --- | --- |
| `PasswordEncoder` | A-1, A-2, A-6, A-9 | `encode(평문)` / `matches(평문, 해시)` |
| `JwtProvider` | A-6, A-8 | `create(memberId, nickname, role)` |
| `FileStorage` | A-2, C-3, C-4, C-7, C-8, A-11, C-15 | `savePoster(file)` → 공개 경로 / `savePrivate(file, "biz" \| "biz-place")` → 비공개 경로 / `loadPrivate(경로)`. 형식·5MB 검사 포함 |
| `DayTypeResolver` | A-14, B-1, B-3 | `of(date)` → WEEKDAY/WEEKEND (토·일·공휴일=주말), `holidayName(date)`. **이 규칙을 각자 다시 만들지 마세요** |
| `partyRepository.findByIdForUpdate` | A-15 ~ A-19 | 인원·상태를 바꿀 때 파티 줄을 잠그고 읽기 |

### 3-5. 엔티티 주의점

- **상태값은 대문자 문자열 그대로** 저장합니다(`@Enumerated(EnumType.STRING)`, enum 10개는 `entity/`에 있음). 숫자로 바꾸지 않기로 11/18에 정했습니다. `EnumType.ORDINAL`은 쓰지 않습니다.
- `REVIEW.escaped`만 DB에 `Y`/`N`으로 저장됩니다. 자바와 API에서는 `true`/`false`이고, 변환기가 자동으로 바꿉니다.
- THEME의 `reviewCount`·`ratingSum`·`updatedAt`은 **DB 트리거가 관리**합니다. 자바에서 값을 넣지 않습니다(`insertable = false, updatable = false`). 평균은 `theme.getAvgRating()`으로 구합니다.
- DB 기본값이 있는 컬럼(status, created_at 등)은 `@DynamicInsert` 덕분에 **null로 두면 DB 기본값**이 들어갑니다.
- 닉네임은 MEMBER에만 있습니다. 후기·파티·채팅에서는 `review.getMember().getNickname()`처럼 꺼냅니다. 닉네임을 바꾸면 모든 기록에 바로 반영됩니다.
- **매장·테마·파티는 DELETE하지 않습니다.** `status`로 관리합니다(파티 삭제 = `DELETED`).
- 손님 화면에 보이는 테마는 **`THEME.status = OPEN`이면서 `STORE.status = APPROVED`** 인 것만입니다.

---

## 4. 협업 컨벤션

### 4-1. Git

- **main에 직접 push하지 않습니다.** 모든 작업은 브랜치 → PR → 리뷰 1명 승인 → merge 순서입니다.
- 브랜치 이름은 `feature/기능`(예: `feature/party-join`)이고, 버그는 `fix/내용`입니다.
  - 프런트와 같은 기능을 하면 **두 레포의 브랜치 이름을 똑같이** 맞춥니다.
  - `feature`라는 이름만으로 된 브랜치는 만들지 않습니다. 있으면 `feature/xxx`를 만들 수 없습니다.
- **작업 시작 전**에 `git pull origin main`, **PR 올리기 전**에 한 번 더 main을 받아서 충돌을 내 쪽에서 해결합니다.
- 커밋 메시지는 `타입: 한 일` 형식입니다. 예: `feat: A-16 파티 참여 구현`

| 타입 | 쓰임 |
| --- | --- |
| feat | 기능 추가 (API 하나 구현 등) |
| fix | 버그 수정 |
| docs | 문서 (README, 주석) |
| style | 포맷만 바꿈 |
| refactor | 동작은 그대로, 구조만 개선 |
| test | 테스트 |
| chore | 설정·빌드·기타 |

- PR 하나에 API는 **1~3개**까지만 담습니다(리뷰하기 쉽게).
- PR 제목에 번호를 넣습니다. 예: `[A-16·A-17] 파티 참여·나가기`
- PR 설명에는 한 일, 남의 파일을 고쳤다면 그 파일, 테스트한 방법(Postman 캡처 등)을 적습니다.
- **merge 전에 `./gradlew test`가 통과해야 합니다.**

### 4-2. 이름 규칙

| 대상 | 규칙 | 예 |
| --- | --- | --- |
| 패키지 | 소문자 (수업과 같은 이름) | `controller`, `service`, `repository`, `entity`, `dto` |
| 컨트롤러 / 서비스 | `XxxController` / `XxxService` + `XxxServiceImpl` | `PartyController`, `PartyServiceImpl` |
| DTO | 요청 `XxxRequest`(폼은 `XxxForm`), 응답 `XxxResponse` · 목록 한 줄 `XxxItem`. 모두 `record` | `PartyCreateRequest`, `PartyListItem` |
| 메서드·변수 | camelCase, 동사로 시작 | `findParties`, `join` |
| 상수 | UPPER_SNAKE | `MAX_BYTES` |
| DB 테이블·컬럼 | snake_case | `party_member`, `deadline_at` |
| 시퀀스 | `sq_테이블` | `sq_party` |
| 제약조건 | `테이블_컬럼_pk/fk/uk/ck` | `party_leader_fk` |
| API 주소 | `/api/` + 소문자 복수 명사, 동작은 HTTP 메서드 | `GET /api/parties`, `POST /api/parties/{id}/join` |
| JSON 키 | camelCase | `partyId`, `deadlineAt` |
| 날짜 JSON | 날짜 `2026-11-20`, 일시 `2026-11-20T19:00:00`, 시각 `19:00` | |

### 4-3. 코드 규칙 (보안 포함)

1. **응답에는 DTO만** 넣습니다. 엔티티를 그대로 반환하면 비밀번호와 파일 경로가 새어 나갑니다.
2. **SQL은 바인딩만** 씁니다: JPA 메서드나 `@Query`의 `:파라미터`. 문자열을 이어 붙여 쿼리를 만들지 않습니다.
3. **비밀번호는 BCrypt로만** 다룹니다. 로그나 응답에 넣지 않습니다.
4. **회원번호는 토큰에서만** 꺼냅니다(`login.memberId()`).
5. **주인 확인은 서비스에서** 합니다. 화면에서 버튼을 숨겼어도 서버가 다시 확인합니다.
6. **링크 값**(`bookingUrl`, `websiteUrl`)은 `http://`나 `https://`로 시작해야 합니다. DTO에서 검사합니다.
7. **사업자·사업장 등록증은 비공개**입니다. `savePrivate`로 저장하고 관리자 API로만 내려 줍니다.
8. 저장·수정·삭제 메서드에는 `@Transactional`을 붙입니다(스켈레톤에 이미 붙어 있음). 여러 테이블을 함께 바꾸는 일은 한 메서드 안에서 처리합니다.
9. 프런트 규칙(참고): 글자는 `textContent`로만 넣고, 요청은 `js/common/api.js`를 거칩니다.

### 4-4. 설계를 바꿀 때

설계가 코드보다 먼저입니다. 아래 순서를 지켜 주세요.

1. **팀 채팅에 먼저 공유**합니다. 무엇을, 왜 바꾸는지 적습니다.
2. **목업 레포의 `docs/ERD.md`·`docs/API.md`를 먼저 고칩니다.**
3. DB를 바꾸면 **`sql/create_tables.sql`, 엔티티, ERD 세 곳을 같은 PR에서** 바꿉니다.
4. API 주소나 JSON 키를 바꾸면 **프런트 담당에게 꼭 알립니다.** 프런트가 같은 이름을 쓰고 있습니다.

---

## 5. 아직 정해지지 않은 것 · 할 일

| 할 일 | 담당 제안 | 메모 |
| --- | --- | --- |
| 비밀번호 길이 규칙 | A | 지금은 4~20자(더미 `1234`에 맞춤). 바꾸면 DTO 세 곳(A-1·A-2·A-9) 수정 |
| 테마 목록(B-1)·파티 목록(A-12) 쿼리 | B, A | 조건이 많아서 `@Query`(JPQL)나 Specification으로 직접 작성 |
| 프런트 `api.js`에 파일 업로드 함수 | A | `multipart/form-data`용. `Content-Type`은 직접 넣지 않기 |

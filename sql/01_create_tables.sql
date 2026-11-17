-- 방탈출 통합예약 (Escaplix) : 테이블 생성 (hr 계정에서 실행)
-- Oracle XE 11g 기준. 테이블 14개
--  * 한글 1글자 = 3바이트 (DB 문자셋이 AL32UTF8, 크기는 바이트 기준)
--    → VARCHAR2(60) 에는 한글 20자까지 들어감
--  * 서버 하나(통합)라서 회원번호 컬럼도 MEMBER 에 FK 를 건다
--    닉네임은 따로 저장하지 않고 필요할 때 JOIN member 로 가져온다
--  * 테마·매장은 DELETE 하지 말고 status 로 관리 (후기·찜·파티가 테마를 참조함)
--  * DEFAULT 가 있는 컬럼 : JPA 는 값이 null 이면 null 을 그대로 INSERT 해서 DEFAULT 가 안 들어감
--    → 엔티티에 @DynamicInsert 를 붙이거나 자바에서 값을 넣어 준다

-- 1. 테이블 삭제 (자식 → 부모 순서). 처음엔 "테이블 없음" 에러가 나도 무시
DROP TABLE party_message;
DROP TABLE party_member;
DROP TABLE party;
DROP TABLE report;
-- (review_reply 는 이전 버전에서 만든 테이블 정리용. 지금은 안 씀)
DROP TABLE review_reply;
DROP TABLE review;
DROP TABLE wishlist;
DROP TABLE member_genre;
DROP TABLE time_slot;
DROP TABLE theme_genre;
DROP TABLE theme;
DROP TABLE store;
DROP TABLE brand;
DROP TABLE genre;
DROP TABLE member;

-- 2. 시퀀스 삭제. 처음엔 "시퀀스 없음" 에러가 나도 무시
DROP SEQUENCE sq_member;
DROP SEQUENCE sq_genre;
DROP SEQUENCE sq_brand;
DROP SEQUENCE sq_store;
DROP SEQUENCE sq_theme;
DROP SEQUENCE sq_theme_genre;
DROP SEQUENCE sq_time_slot;
DROP SEQUENCE sq_wishlist;
DROP SEQUENCE sq_review;
-- (sq_review_reply 는 이전 버전 정리용)
DROP SEQUENCE sq_review_reply;
DROP SEQUENCE sq_report;
DROP SEQUENCE sq_member_genre;
DROP SEQUENCE sq_party;
DROP SEQUENCE sq_party_member;
DROP SEQUENCE sq_party_message;


-- 3. 회원 (손님 / 사장 / 관리자를 role 하나로 구분. 사장은 점주·본사를 owner_type 으로 구분)
CREATE TABLE member (
    member_id   NUMBER        NOT NULL,
    login_id    VARCHAR2(30)  NOT NULL,                     -- 영문·숫자
    password    VARCHAR2(60)  NOT NULL,                     -- BCrypt 해시 (항상 60자)
    nickname    VARCHAR2(60)  NOT NULL,                     -- 한글 20자
    role        VARCHAR2(10)  NOT NULL,                     -- USER / OWNER / ADMIN
    owner_type  VARCHAR2(10),                               -- 사장만: STORE(점주) / HQ(본사). 나머지는 NULL
    status      VARCHAR2(10)  DEFAULT 'ACTIVE' NOT NULL,    -- ACTIVE / BANNED(정지)
    created_at  DATE          DEFAULT SYSDATE NOT NULL,
    CONSTRAINT member_pk            PRIMARY KEY (member_id),
    CONSTRAINT member_login_id_uk   UNIQUE (login_id),
    CONSTRAINT member_nickname_uk   UNIQUE (nickname),
    CONSTRAINT member_role_ck       CHECK (role IN ('USER', 'OWNER', 'ADMIN')),
    -- 사장이면 owner_type 필수, 사장이 아니면 NULL. (IS NOT NULL 을 꼭 써야 함:
    -- CHECK 는 NULL 비교 결과(UNKNOWN)를 위반으로 보지 않아서, IN 만 쓰면 NULL 이 통과됨)
    CONSTRAINT member_owner_type_ck CHECK ((role = 'OWNER' AND owner_type IS NOT NULL AND owner_type IN ('STORE', 'HQ'))
                                        OR (role <> 'OWNER' AND owner_type IS NULL)),
    CONSTRAINT member_status_ck     CHECK (status IN ('ACTIVE', 'BANNED'))
);

-- 4. 장르 (코드 테이블 : 처음에 넣어 두고 거의 안 바뀜)
CREATE TABLE genre (
    genre_id  NUMBER        NOT NULL,
    name      VARCHAR2(30)  NOT NULL,                       -- 한글 10자
    CONSTRAINT genre_pk      PRIMARY KEY (genre_id),
    CONSTRAINT genre_name_uk UNIQUE (name)
);

-- 5. 브랜드 (프랜차이즈. 본사 계정이 등록)
CREATE TABLE brand (
    brand_id    NUMBER        NOT NULL,
    name        VARCHAR2(90)  NOT NULL,                     -- 한글 30자
    hq_id       NUMBER,                                     -- 본사 회원번호
    created_at  DATE          DEFAULT SYSDATE NOT NULL,
    CONSTRAINT brand_pk      PRIMARY KEY (brand_id),
    CONSTRAINT brand_hq_fk   FOREIGN KEY (hq_id) REFERENCES member(member_id),
    CONSTRAINT brand_name_uk UNIQUE (name)
);

-- 6. 매장 (brand_id 가 NULL 이면 일반 매장)
--    수정 권한 : 점주는 owner_id = 나, 본사는 brand.hq_id = 나 인 매장
CREATE TABLE store (
    store_id    NUMBER         NOT NULL,
    brand_id    NUMBER,                                     -- 프랜차이즈 브랜드. 일반 매장은 NULL
    owner_id    NUMBER,                                     -- 점주 회원번호. 본사 직영·크롤링 매장은 NULL
    name        VARCHAR2(150)  NOT NULL,                    -- 한글 50자
    area        VARCHAR2(30)   NOT NULL,                    -- 홍대 / 강남 / 건대 / 신촌 ... (필터용)
    address     VARCHAR2(300),                              -- 가게 주소 (공개 정보)
    lat         NUMBER(9,6),                                -- 위도 (지도, 선택)
    lng         NUMBER(9,6),                                -- 경도 (지도, 선택)
    phone       VARCHAR2(20),
    created_at  DATE           DEFAULT SYSDATE NOT NULL,
    CONSTRAINT store_pk       PRIMARY KEY (store_id),
    CONSTRAINT store_brand_fk FOREIGN KEY (brand_id) REFERENCES brand(brand_id),
    CONSTRAINT store_owner_fk FOREIGN KEY (owner_id) REFERENCES member(member_id)
);

-- 7. 테마 (사장이 등록·수정하면 PENDING → 관리자 승인 시 OPEN, 반려 시 REJECTED)
CREATE TABLE theme (
    theme_id      NUMBER          NOT NULL,
    store_id      NUMBER          NOT NULL,
    title         VARCHAR2(150)   NOT NULL,                 -- 한글 50자
    description   VARCHAR2(4000),                           -- 한글 약 1300자 (VARCHAR2 최대 4000바이트)
    difficulty    NUMBER(1)       NOT NULL,                 -- 1~5
    play_minutes  NUMBER(3)       NOT NULL,                 -- 제한 시간 (분)
    min_people    NUMBER(2)       NOT NULL,
    max_people    NUMBER(2)       NOT NULL,
    price         NUMBER(7)       NOT NULL,                 -- 1인 가격 (원)
    poster_url    VARCHAR2(500),                            -- img/posters/ 경로
    booking_url   VARCHAR2(500)   NOT NULL,                 -- "예약하러 가기" 링크
    source        VARCHAR2(10)    DEFAULT 'OWNER' NOT NULL, -- OWNER(사장 등록) / CRAWL(크롤링)
    status        VARCHAR2(10)    DEFAULT 'PENDING' NOT NULL, -- PENDING(승인 대기) / OPEN / REJECTED(반려) / CLOSED(운영 종료)
    created_at    DATE            DEFAULT SYSDATE NOT NULL,
    CONSTRAINT theme_pk         PRIMARY KEY (theme_id),
    CONSTRAINT theme_store_fk   FOREIGN KEY (store_id) REFERENCES store(store_id),
    CONSTRAINT theme_diff_ck    CHECK (difficulty BETWEEN 1 AND 5),
    CONSTRAINT theme_people_ck  CHECK (min_people >= 1 AND min_people <= max_people),
    CONSTRAINT theme_source_ck  CHECK (source IN ('OWNER', 'CRAWL')),
    CONSTRAINT theme_status_ck  CHECK (status IN ('PENDING', 'OPEN', 'REJECTED', 'CLOSED'))
);

-- 8. 테마-장르 (N:M 중간 테이블. 한 테마가 "공포 + 추리"처럼 장르 여러 개)
CREATE TABLE theme_genre (
    id        NUMBER  NOT NULL,                             -- 대체키
    theme_id  NUMBER  NOT NULL,
    genre_id  NUMBER  NOT NULL,
    CONSTRAINT theme_genre_pk       PRIMARY KEY (id),
    CONSTRAINT theme_genre_theme_fk FOREIGN KEY (theme_id) REFERENCES theme(theme_id),
    CONSTRAINT theme_genre_genre_fk FOREIGN KEY (genre_id) REFERENCES genre(genre_id),
    CONSTRAINT theme_genre_uk       UNIQUE (theme_id, genre_id)
);

-- 9. 예약 시간표
CREATE TABLE time_slot (
    slot_id   NUMBER        NOT NULL,
    theme_id  NUMBER        NOT NULL,
    start_at  DATE          NOT NULL,                       -- 날짜 + 시작 시간 (DATE 는 시·분까지 저장)
    status    VARCHAR2(10)  DEFAULT 'AVAILABLE' NOT NULL,   -- AVAILABLE / FULL
    CONSTRAINT time_slot_pk        PRIMARY KEY (slot_id),
    CONSTRAINT time_slot_theme_fk  FOREIGN KEY (theme_id) REFERENCES theme(theme_id),
    CONSTRAINT time_slot_uk        UNIQUE (theme_id, start_at),    -- 같은 시간표 두 번 등록 금지
    CONSTRAINT time_slot_status_ck CHECK (status IN ('AVAILABLE', 'FULL'))
);

-- 10. 찜 (회원 ↔ 테마 N:M 중간 테이블)
CREATE TABLE wishlist (
    wish_id     NUMBER  NOT NULL,
    member_id   NUMBER  NOT NULL,
    theme_id    NUMBER  NOT NULL,
    created_at  DATE    DEFAULT SYSDATE NOT NULL,
    CONSTRAINT wishlist_pk        PRIMARY KEY (wish_id),
    CONSTRAINT wishlist_member_fk FOREIGN KEY (member_id) REFERENCES member(member_id),
    CONSTRAINT wishlist_theme_fk FOREIGN KEY (theme_id) REFERENCES theme(theme_id),
    CONSTRAINT wishlist_uk       UNIQUE (member_id, theme_id)      -- 같은 테마 두 번 찜 금지
);

-- 11. 후기 (회원 ↔ 테마 N:M 인데 별점·성공 여부 같은 데이터도 가짐)
CREATE TABLE review (
    review_id    NUMBER         NOT NULL,
    member_id    NUMBER         NOT NULL,
    theme_id     NUMBER         NOT NULL,
    rating       NUMBER(1)      NOT NULL,                   -- 별점 1~5
    escaped      CHAR(1)        NOT NULL,                   -- 탈출 성공 Y / N
    hint_count   NUMBER(2)      DEFAULT 0 NOT NULL,         -- 사용한 힌트 수
    played_date  DATE           NOT NULL,                   -- 플레이한 날
    content      VARCHAR2(600),                             -- 한 줄 후기 (안 써도 됨). 한글 200자
    status       VARCHAR2(10)   DEFAULT 'VISIBLE' NOT NULL, -- VISIBLE / HIDDEN (신고 확인 후 관리자가 숨김)
    created_at   DATE           DEFAULT SYSDATE NOT NULL,
    CONSTRAINT review_pk         PRIMARY KEY (review_id),
    CONSTRAINT review_member_fk  FOREIGN KEY (member_id) REFERENCES member(member_id),
    CONSTRAINT review_theme_fk   FOREIGN KEY (theme_id) REFERENCES theme(theme_id),
    CONSTRAINT review_uk         UNIQUE (member_id, theme_id),     -- 한 테마에 후기 하나
    CONSTRAINT review_rating_ck  CHECK (rating BETWEEN 1 AND 5),
    CONSTRAINT review_escaped_ck CHECK (escaped IN ('Y', 'N')),
    CONSTRAINT review_status_ck  CHECK (status IN ('VISIBLE', 'HIDDEN'))
);

-- 12. 후기 신고
CREATE TABLE report (
    report_id    NUMBER         NOT NULL,
    review_id    NUMBER         NOT NULL,
    reporter_id  NUMBER         NOT NULL,                   -- 신고한 회원번호
    reason       VARCHAR2(900)  NOT NULL,                   -- 한글 300자
    status       VARCHAR2(10)   DEFAULT 'WAIT' NOT NULL,    -- WAIT(대기) / DONE(처리 완료)
    created_at   DATE           DEFAULT SYSDATE NOT NULL,
    CONSTRAINT report_pk        PRIMARY KEY (report_id),
    CONSTRAINT report_review_fk   FOREIGN KEY (review_id) REFERENCES review(review_id),
    CONSTRAINT report_reporter_fk FOREIGN KEY (reporter_id) REFERENCES member(member_id),
    CONSTRAINT report_uk        UNIQUE (review_id, reporter_id),   -- 같은 후기 두 번 신고 금지
    CONSTRAINT report_status_ck CHECK (status IN ('WAIT', 'DONE'))
);

-- 13. 선호 장르 (회원 ↔ 장르 N:M 중간 테이블)
CREATE TABLE member_genre (
    id         NUMBER  NOT NULL,                            -- 대체키
    member_id  NUMBER  NOT NULL,
    genre_id   NUMBER  NOT NULL,
    CONSTRAINT member_genre_pk        PRIMARY KEY (id),
    CONSTRAINT member_genre_member_fk FOREIGN KEY (member_id) REFERENCES member(member_id),
    CONSTRAINT member_genre_genre_fk FOREIGN KEY (genre_id) REFERENCES genre(genre_id),
    CONSTRAINT member_genre_uk       UNIQUE (member_id, genre_id)
);

-- 14. 파티 (방파기)
CREATE TABLE party (
    party_id    NUMBER         NOT NULL,
    theme_id    NUMBER         NOT NULL,
    leader_id   NUMBER         NOT NULL,                    -- 방장 회원번호
    title       VARCHAR2(300)  NOT NULL,                    -- 한글 100자
    play_at     DATE           NOT NULL,                    -- 예약 날짜·시간. 끝난 뒤 참여자에게 후기 팝업
    max_people  NUMBER(2)      NOT NULL,                    -- 모집 인원 (테마 최대 인원 이하인지는 서버에서 확인)
    status      VARCHAR2(12)   DEFAULT 'RECRUITING' NOT NULL, -- RECRUITING / FULL / CLOSED
    created_at  DATE           DEFAULT SYSDATE NOT NULL,
    CONSTRAINT party_pk        PRIMARY KEY (party_id),
    CONSTRAINT party_theme_fk  FOREIGN KEY (theme_id) REFERENCES theme(theme_id),
    CONSTRAINT party_leader_fk FOREIGN KEY (leader_id) REFERENCES member(member_id),
    CONSTRAINT party_people_ck CHECK (max_people >= 2),
    CONSTRAINT party_status_ck CHECK (status IN ('RECRUITING', 'FULL', 'CLOSED'))
);

-- 15. 파티 참여자 (회원 ↔ 파티 N:M 중간 테이블. 방장도 한 줄 넣는다)
CREATE TABLE party_member (
    id         NUMBER        NOT NULL,                      -- 대체키
    party_id   NUMBER        NOT NULL,
    member_id  NUMBER        NOT NULL,
    joined_at  DATE          DEFAULT SYSDATE NOT NULL,
    CONSTRAINT party_member_pk       PRIMARY KEY (id),
    CONSTRAINT party_member_party_fk  FOREIGN KEY (party_id) REFERENCES party(party_id),
    CONSTRAINT party_member_member_fk FOREIGN KEY (member_id) REFERENCES member(member_id),
    CONSTRAINT party_member_uk       UNIQUE (party_id, member_id)  -- 같은 파티 두 번 참여 금지
);

-- 16. 파티 채팅
CREATE TABLE party_message (
    message_id  NUMBER          NOT NULL,
    party_id    NUMBER          NOT NULL,
    member_id   NUMBER          NOT NULL,                   -- 보낸 회원번호
    content     VARCHAR2(1500)  NOT NULL,                   -- 한글 500자
    created_at  DATE            DEFAULT SYSDATE NOT NULL,
    CONSTRAINT party_message_pk       PRIMARY KEY (message_id),
    CONSTRAINT party_message_party_fk  FOREIGN KEY (party_id) REFERENCES party(party_id),
    CONSTRAINT party_message_member_fk FOREIGN KEY (member_id) REFERENCES member(member_id)
);


-- 17. 시퀀스 생성 (테이블마다 하나)
CREATE SEQUENCE sq_member;
CREATE SEQUENCE sq_genre;
CREATE SEQUENCE sq_brand;
CREATE SEQUENCE sq_store;
CREATE SEQUENCE sq_theme;
CREATE SEQUENCE sq_theme_genre;
CREATE SEQUENCE sq_time_slot;
CREATE SEQUENCE sq_wishlist;
CREATE SEQUENCE sq_review;
CREATE SEQUENCE sq_report;
CREATE SEQUENCE sq_member_genre;
CREATE SEQUENCE sq_party;
CREATE SEQUENCE sq_party_member;
CREATE SEQUENCE sq_party_message;


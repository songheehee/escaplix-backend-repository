-- ---------------------------------------------------------------
-- 로그인 유지 (Refresh Token) 표 추가 (이미 만들어 둔 DB 용. 데이터는 그대로 둔다)
--   새로 만드는 DB 는 create_tables.sql 에 이미 들어 있어서 이 파일을 돌릴 필요 없음
--   SQL Developer 에서 파일 전체 실행 (F5)
-- ---------------------------------------------------------------
-- 15-1. 로그인 유지 토큰 (Refresh Token). 원문은 브라우저 쿠키(HttpOnly)에만, 여기엔 SHA-256 해시(64자)만
--   쓸 때마다 지우고 새로 넣음. 로그아웃·비밀번호 변경·탈퇴하면 지움
CREATE TABLE refresh_token (
    token_id    NUMBER        NOT NULL,
    member_id   NUMBER        NOT NULL,
    token_hash  VARCHAR2(64)  NOT NULL,
    expires_at  DATE          NOT NULL,
    created_at  DATE          DEFAULT SYSDATE NOT NULL,
    CONSTRAINT refresh_token_pk        PRIMARY KEY (token_id),
    CONSTRAINT refresh_token_member_fk FOREIGN KEY (member_id) REFERENCES member(member_id),
    CONSTRAINT refresh_token_hash_uk   UNIQUE (token_hash)
);
CREATE INDEX refresh_token_member_ix ON refresh_token (member_id);
CREATE SEQUENCE sq_refresh_token;

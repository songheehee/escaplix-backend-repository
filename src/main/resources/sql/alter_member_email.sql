-- ---------------------------------------------------------------
-- 회원 이메일 칸 추가 (이미 만들어 둔 DB 용. 데이터는 그대로 둔다)
--   새로 만드는 DB 는 create_tables.sql 에 이미 들어 있어서 이 파일을 돌릴 필요 없음
--   SQL Developer 에서 이 파일 전체를 실행 (F5). 두 번 돌리면 "이미 있음" 오류만 나고 문제없음
-- ---------------------------------------------------------------
ALTER TABLE member ADD (email VARCHAR2(100));
-- NULL(이메일 없는 기존 회원)은 여러 개 허용됨
ALTER TABLE member ADD CONSTRAINT member_email_uk UNIQUE (email);

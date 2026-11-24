-- ---------------------------------------------------------------
-- 회원 탈퇴 (이미 만들어 둔 DB 용. 데이터는 그대로 둔다)
--   새로 만드는 DB 는 create_tables.sql 에 이미 들어 있어서 이 파일을 돌릴 필요 없음
--   alter_member_email.sql 을 먼저 돌린 DB 에서 실행. SQL Developer 에서 파일 전체 실행 (F5)
--
--   탈퇴하면 줄을 지우지 않고 withdrawn_at 을 채우고 닉네임을 "탈퇴한 회원"으로 바꾼다 (후기·채팅이 그 이름으로 남음)
--   그래서 닉네임 UNIQUE 는 탈퇴 안 한 회원끼리만 (함수 기반 UNIQUE 인덱스: 탈퇴 회원은 NULL 이라 안 셈)
--   사장이 탈퇴하면 사업자등록번호·등록증을 지운다 → member_owner_ck 도 탈퇴 회원은 예외
-- ---------------------------------------------------------------
ALTER TABLE member ADD (withdrawn_at DATE);

ALTER TABLE member DROP CONSTRAINT member_nickname_uk;
CREATE UNIQUE INDEX member_nickname_uk ON member (CASE WHEN withdrawn_at IS NULL THEN nickname END);

ALTER TABLE member DROP CONSTRAINT member_owner_ck;
ALTER TABLE member ADD CONSTRAINT member_owner_ck CHECK (
    (role = 'OWNER' AND (withdrawn_at IS NOT NULL OR (biz_no IS NOT NULL AND biz_file_path IS NOT NULL)))
    OR (role <> 'OWNER' AND biz_no IS NULL AND biz_file_path IS NULL));

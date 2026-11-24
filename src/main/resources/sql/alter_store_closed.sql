-- ---------------------------------------------------------------
-- 매장 폐업 (이미 만들어 둔 DB 용. 데이터는 그대로 둔다)
--   새로 만드는 DB 는 create_tables.sql 에 이미 들어 있어서 이 파일을 돌릴 필요 없음
--   SQL Developer 에서 파일 전체 실행 (F5)
--
--   매장 상태에 CLOSED(폐업) 추가. 사장·관리자 둘 다 폐업할 수 있고 되돌릴 수 없다
--   다시 열려면 같은 사업장 등록번호로 새로 등록 → 번호 UNIQUE 는 폐업 안 한 매장끼리만
--   (함수 기반 UNIQUE 인덱스: 폐업 매장은 NULL 이라 안 셈)
-- ---------------------------------------------------------------
ALTER TABLE store DROP CONSTRAINT store_status_ck;
ALTER TABLE store ADD CONSTRAINT store_status_ck CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED', 'CLOSED'));

ALTER TABLE store DROP CONSTRAINT store_biz_place_uk;
CREATE UNIQUE INDEX store_biz_place_uk ON store (CASE WHEN status <> 'CLOSED' THEN biz_place_no END);

-- ---------------------------------------------------------------
-- 매장 폼 바뀐 것 (이미 만들어 둔 DB 용. 데이터는 그대로 둔다)
--   새로 만드는 DB 는 create_tables.sql 에 이미 들어 있어서 이 파일을 돌릴 필요 없음
--   SQL Developer 에서 파일 전체 실행 (F5)
--
--   1) 사업장 관리번호 = 사업자등록번호 10자리 + 끝 1자리 → 000-00-00000-0 (14글자)
--      칸을 12 → 14 로 늘린다. 예전 10자리 더미 번호는 그대로 둠 (바꾸지 않고 다시 저장하면 통과)
--      함수 기반 UNIQUE 인덱스가 걸린 칸은 길이를 못 바꿔서 (ORA-30556) 인덱스를 지웠다가 다시 만든다
--   2) 매장 소개는 선택 → NOT NULL 을 푼다
-- ---------------------------------------------------------------
DROP INDEX store_biz_place_uk;
ALTER TABLE store MODIFY (biz_place_no VARCHAR2(14));
CREATE UNIQUE INDEX store_biz_place_uk ON store (CASE WHEN status <> 'CLOSED' THEN biz_place_no END);

ALTER TABLE store MODIFY (intro NULL);

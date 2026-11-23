SET DEFINE OFF;

-- 방탈출 통합예약 (Escaplix) : 크롤링 데이터 (자동 생성 파일, 손으로 고치지 말 것)
-- 만든 때 : 2026-11-23 11:36
-- 만든 방법 : ./gradlew crawl  (src/crawler/README.md)
-- 사이트 : 넥스트에디션 https://nextedition.co.kr/, 포인트나인 https://point-nine.com/layout/res/home.php?go=main
-- 양 : 매장 10 / 테마 42 / 시간표 202
--
-- insert_data.sql 다음에 실행. 여러 번 실행해도 됨 (있으면 고치고, 없으면 넣음)
--  * 매장 : 관리자 직접 등록처럼 owner_id NULL, 바로 APPROVED
--  * 사업장 번호 : 진짜 번호를 몰라서 999-{사이트}-{지점} 으로 만든 번호 (같은 매장을 다시 찾는 열쇠)
--  * 테마 : source = 'CRAWL', 바로 OPEN. 포스터는 사이트의 사진 주소를 그대로 씀
--  * 지우려면 맨 아래 주석의 DELETE 문


-- ==================== [넥스트에디션] 넥스트에디션 건대1호점  (https://nextedition.co.kr/branches/geondae-1/)
MERGE INTO store s
USING (SELECT '999-01-00015' AS biz_place_no FROM dual) x
   ON (s.biz_place_no = x.biz_place_no)
 WHEN MATCHED THEN UPDATE SET
      s.name = '넥스트에디션 건대1호점', s.area = '건대', s.address = '서울 광진구 자양동 17-5, 지하 1층', s.phone = '02-463-9967',
      s.website_url = 'https://nextedition.co.kr/branches/geondae-1/', s.hours = '영업시간은 매장 홈페이지에서 확인해 주세요',
      s.intro = '넥스트에디션 건대1호점. 공식 홈페이지에서 모은 정보예요. 예약과 정확한 가격은 매장 홈페이지에서 확인해 주세요.', s.lat = 37.541500, s.lng = 127.064813
 WHEN NOT MATCHED THEN INSERT
      (store_id, owner_id, name, area, address, phone, website_url, hours, intro, lat, lng, biz_place_no, status, approved_at)
      VALUES (sq_store.NEXTVAL, NULL, '넥스트에디션 건대1호점', '건대', '서울 광진구 자양동 17-5, 지하 1층', '02-463-9967',
              'https://nextedition.co.kr/branches/geondae-1/', '영업시간은 매장 홈페이지에서 확인해 주세요',
              '넥스트에디션 건대1호점. 공식 홈페이지에서 모은 정보예요. 예약과 정확한 가격은 매장 홈페이지에서 확인해 주세요.', 37.541500, 127.064813, x.biz_place_no, 'APPROVED', SYSDATE);

-- 「B아파트 13동 1313호」
MERGE INTO theme t
USING (SELECT store_id, 'B아파트 13동 1313호' AS title FROM store WHERE biz_place_no = '999-01-00015') x
   ON (t.store_id = x.store_id AND t.title = x.title)
 WHEN MATCHED THEN UPDATE SET
      t.difficulty = 3, t.play_minutes = 60, t.min_people = 2, t.max_people = 4, t.price = 22000,
      t.poster_url = 'https://img.reserv.company/nextedition/konkuk-1/b-apartment-building-13-unit-1313.jpg',
      t.booking_url = 'https://nextedition.co.kr/reservation/geondae-1/?theme=63',
      t.description = '미스테리 동호회 ''블랙홀''의 특별회원인 당신' ||
        CHR(10) ||
        '오늘밤 ''블랙홀''의 첫 전국 정모가 열린다' ||
        CHR(10) ||
        '모임 장소는 일가족이 살해 했다는 저주받은 아파트' ||
        CHR(10) ||
        '일가족이 살해 당한 후 이사오는 사람마다 하루도 버티지 못하고 도망나온다는 저주받은 B아파트 13동 1313호아파트' ||
        CHR(10) ||
        '당신은 이곳에서 무사히 살아 나갈수 있을것인가.'
 WHEN NOT MATCHED THEN INSERT
      (theme_id, store_id, title, difficulty, play_minutes, min_people, max_people, price, poster_url, booking_url, description, source, status)
      VALUES (sq_theme.NEXTVAL, x.store_id, x.title, 3, 60, 2, 4, 22000,
              'https://img.reserv.company/nextedition/konkuk-1/b-apartment-building-13-unit-1313.jpg',
              'https://nextedition.co.kr/reservation/geondae-1/?theme=63',
              '미스테리 동호회 ''블랙홀''의 특별회원인 당신' ||
        CHR(10) ||
        '오늘밤 ''블랙홀''의 첫 전국 정모가 열린다' ||
        CHR(10) ||
        '모임 장소는 일가족이 살해 했다는 저주받은 아파트' ||
        CHR(10) ||
        '일가족이 살해 당한 후 이사오는 사람마다 하루도 버티지 못하고 도망나온다는 저주받은 B아파트 13동 1313호아파트' ||
        CHR(10) ||
        '당신은 이곳에서 무사히 살아 나갈수 있을것인가.',
              'CRAWL', 'OPEN');
DELETE FROM theme_genre WHERE theme_id = (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-01-00015' AND t.title = 'B아파트 13동 1313호');
INSERT INTO theme_genre (id, theme_id, genre_id)
SELECT sq_theme_genre.NEXTVAL, t.theme_id, g.genre_id FROM theme t, store s, genre g
 WHERE t.store_id = s.store_id AND s.biz_place_no = '999-01-00015' AND t.title = 'B아파트 13동 1313호' AND g.name IN ('공포');

-- 「SOUL CHASER - 실종」
MERGE INTO theme t
USING (SELECT store_id, 'SOUL CHASER - 실종' AS title FROM store WHERE biz_place_no = '999-01-00015') x
   ON (t.store_id = x.store_id AND t.title = x.title)
 WHEN MATCHED THEN UPDATE SET
      t.difficulty = 4, t.play_minutes = 90, t.min_people = 2, t.max_people = 2, t.price = 22000,
      t.poster_url = 'https://img.reserv.company/nextedition/konkuk-1/soul-chaser-missing.jpg',
      t.booking_url = 'https://nextedition.co.kr/reservation/geondae-1/?theme=114',
      t.description = '소울 체이서란,' ||
        CHR(10) ||
        '이승에서 떠나지 못한 영혼들을 추적하여' ||
        CHR(10) ||
        '찾아내는 자들이다.' ||
        CHR(10) ||
        '이번 영혼에게는 어떠한 사연이 있는 것일까?' ||
        CHR(10) ||
        CHR(10) ||
        '* 야외에서 진행되는 테마입니다.' ||
        CHR(10) ||
        '* 야외 시설물(건물 옥상)이 아닌 온전히 야외에서만 즐기실 수 있는 테마입니다.' ||
        CHR(10) ||
        '* 추가로 궁금하신 점이 있으시면 매장으로 문의 부탁드립니다.'
 WHEN NOT MATCHED THEN INSERT
      (theme_id, store_id, title, difficulty, play_minutes, min_people, max_people, price, poster_url, booking_url, description, source, status)
      VALUES (sq_theme.NEXTVAL, x.store_id, x.title, 4, 90, 2, 2, 22000,
              'https://img.reserv.company/nextedition/konkuk-1/soul-chaser-missing.jpg',
              'https://nextedition.co.kr/reservation/geondae-1/?theme=114',
              '소울 체이서란,' ||
        CHR(10) ||
        '이승에서 떠나지 못한 영혼들을 추적하여' ||
        CHR(10) ||
        '찾아내는 자들이다.' ||
        CHR(10) ||
        '이번 영혼에게는 어떠한 사연이 있는 것일까?' ||
        CHR(10) ||
        CHR(10) ||
        '* 야외에서 진행되는 테마입니다.' ||
        CHR(10) ||
        '* 야외 시설물(건물 옥상)이 아닌 온전히 야외에서만 즐기실 수 있는 테마입니다.' ||
        CHR(10) ||
        '* 추가로 궁금하신 점이 있으시면 매장으로 문의 부탁드립니다.',
              'CRAWL', 'OPEN');
DELETE FROM theme_genre WHERE theme_id = (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-01-00015' AND t.title = 'SOUL CHASER - 실종');
INSERT INTO theme_genre (id, theme_id, genre_id)
SELECT sq_theme_genre.NEXTVAL, t.theme_id, g.genre_id FROM theme t, store s, genre g
 WHERE t.store_id = s.store_id AND s.biz_place_no = '999-01-00015' AND t.title = 'SOUL CHASER - 실종' AND g.name IN ('모험');

-- 「이불 밖은 위험해」
MERGE INTO theme t
USING (SELECT store_id, '이불 밖은 위험해' AS title FROM store WHERE biz_place_no = '999-01-00015') x
   ON (t.store_id = x.store_id AND t.title = x.title)
 WHEN MATCHED THEN UPDATE SET
      t.difficulty = 3, t.play_minutes = 60, t.min_people = 2, t.max_people = 4, t.price = 22000,
      t.poster_url = 'https://img.reserv.company/nextedition/konkuk-1/outside-the-blanket-is-dangerous.jpg',
      t.booking_url = 'https://nextedition.co.kr/reservation/geondae-1/?theme=64',
      t.description = '느지막한 주말 오후 이불속 당신!' ||
        CHR(10) ||
        '평소와는 다른 분위기에 이불 밖으로 나와보는데...'
 WHEN NOT MATCHED THEN INSERT
      (theme_id, store_id, title, difficulty, play_minutes, min_people, max_people, price, poster_url, booking_url, description, source, status)
      VALUES (sq_theme.NEXTVAL, x.store_id, x.title, 3, 60, 2, 4, 22000,
              'https://img.reserv.company/nextedition/konkuk-1/outside-the-blanket-is-dangerous.jpg',
              'https://nextedition.co.kr/reservation/geondae-1/?theme=64',
              '느지막한 주말 오후 이불속 당신!' ||
        CHR(10) ||
        '평소와는 다른 분위기에 이불 밖으로 나와보는데...',
              'CRAWL', 'OPEN');
DELETE FROM theme_genre WHERE theme_id = (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-01-00015' AND t.title = '이불 밖은 위험해');
INSERT INTO theme_genre (id, theme_id, genre_id)
SELECT sq_theme_genre.NEXTVAL, t.theme_id, g.genre_id FROM theme t, store s, genre g
 WHERE t.store_id = s.store_id AND s.biz_place_no = '999-01-00015' AND t.title = '이불 밖은 위험해' AND g.name IN ('감성');

-- 「썸」
MERGE INTO theme t
USING (SELECT store_id, '썸' AS title FROM store WHERE biz_place_no = '999-01-00015') x
   ON (t.store_id = x.store_id AND t.title = x.title)
 WHEN MATCHED THEN UPDATE SET
      t.difficulty = 3, t.play_minutes = 60, t.min_people = 2, t.max_people = 4, t.price = 22000,
      t.poster_url = 'https://img.reserv.company/nextedition/konkuk-1/fling.jpg',
      t.booking_url = 'https://nextedition.co.kr/reservation/geondae-1/?theme=60',
      t.description = '친구라기엔 너무 가깝고,' ||
        CHR(10) ||
        '연인이라기엔 조금은 먼 우리.' ||
        CHR(10) ||
        '우리는.. 어떤 사이일까?'
 WHEN NOT MATCHED THEN INSERT
      (theme_id, store_id, title, difficulty, play_minutes, min_people, max_people, price, poster_url, booking_url, description, source, status)
      VALUES (sq_theme.NEXTVAL, x.store_id, x.title, 3, 60, 2, 4, 22000,
              'https://img.reserv.company/nextedition/konkuk-1/fling.jpg',
              'https://nextedition.co.kr/reservation/geondae-1/?theme=60',
              '친구라기엔 너무 가깝고,' ||
        CHR(10) ||
        '연인이라기엔 조금은 먼 우리.' ||
        CHR(10) ||
        '우리는.. 어떤 사이일까?',
              'CRAWL', 'OPEN');
DELETE FROM theme_genre WHERE theme_id = (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-01-00015' AND t.title = '썸');
INSERT INTO theme_genre (id, theme_id, genre_id)
SELECT sq_theme_genre.NEXTVAL, t.theme_id, g.genre_id FROM theme t, store s, genre g
 WHERE t.store_id = s.store_id AND s.biz_place_no = '999-01-00015' AND t.title = '썸' AND g.name IN ('감성');

-- 「MONSTER:10800」
MERGE INTO theme t
USING (SELECT store_id, 'MONSTER:10800' AS title FROM store WHERE biz_place_no = '999-01-00015') x
   ON (t.store_id = x.store_id AND t.title = x.title)
 WHEN MATCHED THEN UPDATE SET
      t.difficulty = 5, t.play_minutes = 70, t.min_people = 2, t.max_people = 4, t.price = 22000,
      t.poster_url = 'https://img.reserv.company/nextedition/konkuk-1/monster-10800.jpg',
      t.booking_url = 'https://nextedition.co.kr/reservation/geondae-1/?theme=61',
      t.description = '''내 안에 몬스터가 깨어나고 있어''' ||
        CHR(10) ||
        CHR(10) ||
        '순간의 잘못된 선택이 커다란 악마를 만들어 냈다.' ||
        CHR(10) ||
        '1분 1초, 급박함과 긴장감 속에 서서히 닥쳐오는 재앙.' ||
        CHR(10) ||
        '내 손으로 만든 이 괴물은 내 손으로 직접 처리하겠어.' ||
        CHR(10) ||
        '끝을 모를 어둠 속에서, 점점 커져만 가는 몬스터를 막을 수 있을 것인가!'
 WHEN NOT MATCHED THEN INSERT
      (theme_id, store_id, title, difficulty, play_minutes, min_people, max_people, price, poster_url, booking_url, description, source, status)
      VALUES (sq_theme.NEXTVAL, x.store_id, x.title, 5, 70, 2, 4, 22000,
              'https://img.reserv.company/nextedition/konkuk-1/monster-10800.jpg',
              'https://nextedition.co.kr/reservation/geondae-1/?theme=61',
              '''내 안에 몬스터가 깨어나고 있어''' ||
        CHR(10) ||
        CHR(10) ||
        '순간의 잘못된 선택이 커다란 악마를 만들어 냈다.' ||
        CHR(10) ||
        '1분 1초, 급박함과 긴장감 속에 서서히 닥쳐오는 재앙.' ||
        CHR(10) ||
        '내 손으로 만든 이 괴물은 내 손으로 직접 처리하겠어.' ||
        CHR(10) ||
        '끝을 모를 어둠 속에서, 점점 커져만 가는 몬스터를 막을 수 있을 것인가!',
              'CRAWL', 'OPEN');
DELETE FROM theme_genre WHERE theme_id = (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-01-00015' AND t.title = 'MONSTER:10800');
INSERT INTO theme_genre (id, theme_id, genre_id)
SELECT sq_theme_genre.NEXTVAL, t.theme_id, g.genre_id FROM theme t, store s, genre g
 WHERE t.store_id = s.store_id AND s.biz_place_no = '999-01-00015' AND t.title = 'MONSTER:10800' AND g.name IN ('판타지');

-- 「다시봄」
MERGE INTO theme t
USING (SELECT store_id, '다시봄' AS title FROM store WHERE biz_place_no = '999-01-00015') x
   ON (t.store_id = x.store_id AND t.title = x.title)
 WHEN MATCHED THEN UPDATE SET
      t.difficulty = 4, t.play_minutes = 80, t.min_people = 2, t.max_people = 4, t.price = 22000,
      t.poster_url = 'https://img.reserv.company/nextedition/konkuk-1/spring-again.jpg',
      t.booking_url = 'https://nextedition.co.kr/reservation/geondae-1/?theme=62',
      t.description = '이건 아주 진부한 이야기입니다.'
 WHEN NOT MATCHED THEN INSERT
      (theme_id, store_id, title, difficulty, play_minutes, min_people, max_people, price, poster_url, booking_url, description, source, status)
      VALUES (sq_theme.NEXTVAL, x.store_id, x.title, 4, 80, 2, 4, 22000,
              'https://img.reserv.company/nextedition/konkuk-1/spring-again.jpg',
              'https://nextedition.co.kr/reservation/geondae-1/?theme=62',
              '이건 아주 진부한 이야기입니다.',
              'CRAWL', 'OPEN');
DELETE FROM theme_genre WHERE theme_id = (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-01-00015' AND t.title = '다시봄');
INSERT INTO theme_genre (id, theme_id, genre_id)
SELECT sq_theme_genre.NEXTVAL, t.theme_id, g.genre_id FROM theme t, store s, genre g
 WHERE t.store_id = s.store_id AND s.biz_place_no = '999-01-00015' AND t.title = '다시봄' AND g.name IN ('감성');

-- ==================== [넥스트에디션] 넥스트에디션 건대2호점  (https://nextedition.co.kr/branches/geondae-2/)
MERGE INTO store s
USING (SELECT '999-01-00013' AS biz_place_no FROM dual) x
   ON (s.biz_place_no = x.biz_place_no)
 WHEN MATCHED THEN UPDATE SET
      s.name = '넥스트에디션 건대2호점', s.area = '건대', s.address = '서울 광진구 아차산로 192, 별관 2,3층', s.phone = '02-462-1460',
      s.website_url = 'https://nextedition.co.kr/branches/geondae-2/', s.hours = '영업시간은 매장 홈페이지에서 확인해 주세요',
      s.intro = '넥스트에디션 건대2호점. 공식 홈페이지에서 모은 정보예요. 예약과 정확한 가격은 매장 홈페이지에서 확인해 주세요.', s.lat = 37.541557, s.lng = 127.064967
 WHEN NOT MATCHED THEN INSERT
      (store_id, owner_id, name, area, address, phone, website_url, hours, intro, lat, lng, biz_place_no, status, approved_at)
      VALUES (sq_store.NEXTVAL, NULL, '넥스트에디션 건대2호점', '건대', '서울 광진구 아차산로 192, 별관 2,3층', '02-462-1460',
              'https://nextedition.co.kr/branches/geondae-2/', '영업시간은 매장 홈페이지에서 확인해 주세요',
              '넥스트에디션 건대2호점. 공식 홈페이지에서 모은 정보예요. 예약과 정확한 가격은 매장 홈페이지에서 확인해 주세요.', 37.541557, 127.064967, x.biz_place_no, 'APPROVED', SYSDATE);

-- 「생존자」
MERGE INTO theme t
USING (SELECT store_id, '생존자' AS title FROM store WHERE biz_place_no = '999-01-00013') x
   ON (t.store_id = x.store_id AND t.title = x.title)
 WHEN MATCHED THEN UPDATE SET
      t.difficulty = 3, t.play_minutes = 65, t.min_people = 2, t.max_people = 4, t.price = 22000,
      t.poster_url = 'https://img.reserv.company/nextedition/konkuk-2/survivor.jpg',
      t.booking_url = 'https://nextedition.co.kr/reservation/geondae-2/?theme=327',
      t.description = '🎬시놉시스' ||
        CHR(10) ||
        '"생존자 발견! 생존자 발견!' ||
        CHR(10) ||
        '선생님, 괜찮으세요?"' ||
        CHR(10) ||
        CHR(10) ||
        '".."' ||
        CHR(10) ||
        CHR(10) ||
        '"선생님, 성함이 어떻게 되세요?"' ||
        CHR(10) ||
        CHR(10) ||
        '"유...유명호...입니다."' ||
        CHR(10) ||
        CHR(10) ||
        '🎞️테마 구성' ||
        CHR(10) ||
        '생존자는 프롤로그 5분 + 본편 50분 + 에필로그 10분으로 구성되어 있습니다. 따라서 5분 이상 지각하실 경우 입장이 제한되거나, 본편을 온전히 즐기지 못하실 수 있습니다.' ||
        CHR(10) ||
        CHR(10) ||
        '🪜난이도' ||
        CHR(10) ||
        '본 테마는 이용하시는 모두가 시간 내에 엔딩까지 즐길 수 있도록 준비되었기 때문에 별도의 난이도는 설정되어 있지 않습니다.' ||
        CHR(10) ||
        CHR(10) ||
        '⚠️주의' ||
        CHR(10) ||
        '청소년, 노약자, 임산부의 이용이 제한될 수 있으며, 욕설 및 잔인한 묘사가 포함되어 있으니 참고 부탁드립니다.' ||
        CHR(10) ||
        CHR(10) ||
        '※ 본 테마는 공포테마가 아닙니다. 다만, 장르 특성 상 개인에 따라 두려움을 느낄만한 구간이 있을 수 있으니 참고 부탁드립니다.'
 WHEN NOT MATCHED THEN INSERT
      (theme_id, store_id, title, difficulty, play_minutes, min_people, max_people, price, poster_url, booking_url, description, source, status)
      VALUES (sq_theme.NEXTVAL, x.store_id, x.title, 3, 65, 2, 4, 22000,
              'https://img.reserv.company/nextedition/konkuk-2/survivor.jpg',
              'https://nextedition.co.kr/reservation/geondae-2/?theme=327',
              '🎬시놉시스' ||
        CHR(10) ||
        '"생존자 발견! 생존자 발견!' ||
        CHR(10) ||
        '선생님, 괜찮으세요?"' ||
        CHR(10) ||
        CHR(10) ||
        '".."' ||
        CHR(10) ||
        CHR(10) ||
        '"선생님, 성함이 어떻게 되세요?"' ||
        CHR(10) ||
        CHR(10) ||
        '"유...유명호...입니다."' ||
        CHR(10) ||
        CHR(10) ||
        '🎞️테마 구성' ||
        CHR(10) ||
        '생존자는 프롤로그 5분 + 본편 50분 + 에필로그 10분으로 구성되어 있습니다. 따라서 5분 이상 지각하실 경우 입장이 제한되거나, 본편을 온전히 즐기지 못하실 수 있습니다.' ||
        CHR(10) ||
        CHR(10) ||
        '🪜난이도' ||
        CHR(10) ||
        '본 테마는 이용하시는 모두가 시간 내에 엔딩까지 즐길 수 있도록 준비되었기 때문에 별도의 난이도는 설정되어 있지 않습니다.' ||
        CHR(10) ||
        CHR(10) ||
        '⚠️주의' ||
        CHR(10) ||
        '청소년, 노약자, 임산부의 이용이 제한될 수 있으며, 욕설 및 잔인한 묘사가 포함되어 있으니 참고 부탁드립니다.' ||
        CHR(10) ||
        CHR(10) ||
        '※ 본 테마는 공포테마가 아닙니다. 다만, 장르 특성 상 개인에 따라 두려움을 느낄만한 구간이 있을 수 있으니 참고 부탁드립니다.',
              'CRAWL', 'OPEN');
DELETE FROM theme_genre WHERE theme_id = (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-01-00013' AND t.title = '생존자');
INSERT INTO theme_genre (id, theme_id, genre_id)
SELECT sq_theme_genre.NEXTVAL, t.theme_id, g.genre_id FROM theme t, store s, genre g
 WHERE t.store_id = s.store_id AND s.biz_place_no = '999-01-00013' AND t.title = '생존자' AND g.name IN ('스릴러');

-- 「동화나라 수비대」
MERGE INTO theme t
USING (SELECT store_id, '동화나라 수비대' AS title FROM store WHERE biz_place_no = '999-01-00013') x
   ON (t.store_id = x.store_id AND t.title = x.title)
 WHEN MATCHED THEN UPDATE SET
      t.difficulty = 5, t.play_minutes = 80, t.min_people = 2, t.max_people = 5, t.price = 22000,
      t.poster_url = 'https://img.reserv.company/nextedition/konkuk-2/fairy-tale-land-guardians.png',
      t.booking_url = 'https://nextedition.co.kr/reservation/geondae-2/?theme=67',
      t.description = '귀여운 동화들로 이루어진 동화나라.' ||
        CHR(10) ||
        CHR(10) ||
        '정체 모를 악당의 등장으로,' ||
        CHR(10) ||
        '동화나라의 동화들이 엉망이 되고 있다!' ||
        CHR(10) ||
        CHR(10) ||
        '이전과는 다른 이름, 다른 모습으로' ||
        CHR(10) ||
        '동화나라를 찾은 여러분!' ||
        CHR(10) ||
        CHR(10) ||
        '원작과 달라진 동화의 내용들을' ||
        CHR(10) ||
        '원래대로 되돌려 놓아야만 한다!' ||
        CHR(10) ||
        CHR(10) ||
        '동화나라 친구들의 평화와 안녕을 위해!' ||
        CHR(10) ||
        '동화나라 수비대! 출동!!'
 WHEN NOT MATCHED THEN INSERT
      (theme_id, store_id, title, difficulty, play_minutes, min_people, max_people, price, poster_url, booking_url, description, source, status)
      VALUES (sq_theme.NEXTVAL, x.store_id, x.title, 5, 80, 2, 5, 22000,
              'https://img.reserv.company/nextedition/konkuk-2/fairy-tale-land-guardians.png',
              'https://nextedition.co.kr/reservation/geondae-2/?theme=67',
              '귀여운 동화들로 이루어진 동화나라.' ||
        CHR(10) ||
        CHR(10) ||
        '정체 모를 악당의 등장으로,' ||
        CHR(10) ||
        '동화나라의 동화들이 엉망이 되고 있다!' ||
        CHR(10) ||
        CHR(10) ||
        '이전과는 다른 이름, 다른 모습으로' ||
        CHR(10) ||
        '동화나라를 찾은 여러분!' ||
        CHR(10) ||
        CHR(10) ||
        '원작과 달라진 동화의 내용들을' ||
        CHR(10) ||
        '원래대로 되돌려 놓아야만 한다!' ||
        CHR(10) ||
        CHR(10) ||
        '동화나라 친구들의 평화와 안녕을 위해!' ||
        CHR(10) ||
        '동화나라 수비대! 출동!!',
              'CRAWL', 'OPEN');
DELETE FROM theme_genre WHERE theme_id = (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-01-00013' AND t.title = '동화나라 수비대');
INSERT INTO theme_genre (id, theme_id, genre_id)
SELECT sq_theme_genre.NEXTVAL, t.theme_id, g.genre_id FROM theme t, store s, genre g
 WHERE t.store_id = s.store_id AND s.biz_place_no = '999-01-00013' AND t.title = '동화나라 수비대' AND g.name IN ('모험');

-- 「빛을 구해줘」
MERGE INTO theme t
USING (SELECT store_id, '빛을 구해줘' AS title FROM store WHERE biz_place_no = '999-01-00013') x
   ON (t.store_id = x.store_id AND t.title = x.title)
 WHEN MATCHED THEN UPDATE SET
      t.difficulty = 4, t.play_minutes = 60, t.min_people = 2, t.max_people = 5, t.price = 22000,
      t.poster_url = 'https://img.reserv.company/nextedition/konkuk-2/save-the-light.jpg',
      t.booking_url = 'https://nextedition.co.kr/reservation/geondae-2/?theme=68',
      t.description = '저희 빛의 요정들은' ||
        CHR(10) ||
        '태양의 빛을 행성들에게' ||
        CHR(10) ||
        '전달하는 역할을 하고 있어요.' ||
        CHR(10) ||
        CHR(10) ||
        '오늘도 많은 분들이' ||
        CHR(10) ||
        '견학 신청을 해주셨군요!' ||
        CHR(10) ||
        CHR(10) ||
        '자, 그럼 지금부터 저희 요정들과 함께' ||
        CHR(10) ||
        '빛이 만들어지는 과정을 살펴보도록 할까요?'
 WHEN NOT MATCHED THEN INSERT
      (theme_id, store_id, title, difficulty, play_minutes, min_people, max_people, price, poster_url, booking_url, description, source, status)
      VALUES (sq_theme.NEXTVAL, x.store_id, x.title, 4, 60, 2, 5, 22000,
              'https://img.reserv.company/nextedition/konkuk-2/save-the-light.jpg',
              'https://nextedition.co.kr/reservation/geondae-2/?theme=68',
              '저희 빛의 요정들은' ||
        CHR(10) ||
        '태양의 빛을 행성들에게' ||
        CHR(10) ||
        '전달하는 역할을 하고 있어요.' ||
        CHR(10) ||
        CHR(10) ||
        '오늘도 많은 분들이' ||
        CHR(10) ||
        '견학 신청을 해주셨군요!' ||
        CHR(10) ||
        CHR(10) ||
        '자, 그럼 지금부터 저희 요정들과 함께' ||
        CHR(10) ||
        '빛이 만들어지는 과정을 살펴보도록 할까요?',
              'CRAWL', 'OPEN');
DELETE FROM theme_genre WHERE theme_id = (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-01-00013' AND t.title = '빛을 구해줘');

-- 「Make-up」
MERGE INTO theme t
USING (SELECT store_id, 'Make-up' AS title FROM store WHERE biz_place_no = '999-01-00013') x
   ON (t.store_id = x.store_id AND t.title = x.title)
 WHEN MATCHED THEN UPDATE SET
      t.difficulty = 3, t.play_minutes = 70, t.min_people = 2, t.max_people = 5, t.price = 22000,
      t.poster_url = 'https://img.reserv.company/nextedition/konkuk-2/make-up.png',
      t.booking_url = 'https://nextedition.co.kr/reservation/geondae-2/?theme=66',
      t.description = '오늘은' ||
        CHR(10) ||
        '화장이나 한 번' ||
        CHR(10) ||
        '해볼까봐요.'
 WHEN NOT MATCHED THEN INSERT
      (theme_id, store_id, title, difficulty, play_minutes, min_people, max_people, price, poster_url, booking_url, description, source, status)
      VALUES (sq_theme.NEXTVAL, x.store_id, x.title, 3, 70, 2, 5, 22000,
              'https://img.reserv.company/nextedition/konkuk-2/make-up.png',
              'https://nextedition.co.kr/reservation/geondae-2/?theme=66',
              '오늘은' ||
        CHR(10) ||
        '화장이나 한 번' ||
        CHR(10) ||
        '해볼까봐요.',
              'CRAWL', 'OPEN');
DELETE FROM theme_genre WHERE theme_id = (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-01-00013' AND t.title = 'Make-up');

-- 「커튼콜」
MERGE INTO theme t
USING (SELECT store_id, '커튼콜' AS title FROM store WHERE biz_place_no = '999-01-00013') x
   ON (t.store_id = x.store_id AND t.title = x.title)
 WHEN MATCHED THEN UPDATE SET
      t.difficulty = 3, t.play_minutes = 60, t.min_people = 2, t.max_people = 5, t.price = 22000,
      t.poster_url = 'https://img.reserv.company/nextedition/konkuk-2/curtain-call.jpg',
      t.booking_url = 'https://nextedition.co.kr/reservation/geondae-2/?theme=70',
      t.description = '오늘도 우리 인형극장을 찾아주셔서 감사합니다.' ||
        CHR(10) ||
        CHR(10) ||
        '인형들과 함께하는 신나거나 감동적인 이야기들.' ||
        CHR(10) ||
        CHR(10) ||
        '오늘은 과연 어떤 이야기가 여러분을 기다리고 있을까요?' ||
        CHR(10) ||
        CHR(10) ||
        '바로 지금,' ||
        CHR(10) ||
        '오늘 이야기의 화려한 막이 올라갑니다!'
 WHEN NOT MATCHED THEN INSERT
      (theme_id, store_id, title, difficulty, play_minutes, min_people, max_people, price, poster_url, booking_url, description, source, status)
      VALUES (sq_theme.NEXTVAL, x.store_id, x.title, 3, 60, 2, 5, 22000,
              'https://img.reserv.company/nextedition/konkuk-2/curtain-call.jpg',
              'https://nextedition.co.kr/reservation/geondae-2/?theme=70',
              '오늘도 우리 인형극장을 찾아주셔서 감사합니다.' ||
        CHR(10) ||
        CHR(10) ||
        '인형들과 함께하는 신나거나 감동적인 이야기들.' ||
        CHR(10) ||
        CHR(10) ||
        '오늘은 과연 어떤 이야기가 여러분을 기다리고 있을까요?' ||
        CHR(10) ||
        CHR(10) ||
        '바로 지금,' ||
        CHR(10) ||
        '오늘 이야기의 화려한 막이 올라갑니다!',
              'CRAWL', 'OPEN');
DELETE FROM theme_genre WHERE theme_id = (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-01-00013' AND t.title = '커튼콜');
INSERT INTO theme_genre (id, theme_id, genre_id)
SELECT sq_theme_genre.NEXTVAL, t.theme_id, g.genre_id FROM theme t, store s, genre g
 WHERE t.store_id = s.store_id AND s.biz_place_no = '999-01-00013' AND t.title = '커튼콜' AND g.name IN ('판타지');

-- 「방탈출 아카데미」
MERGE INTO theme t
USING (SELECT store_id, '방탈출 아카데미' AS title FROM store WHERE biz_place_no = '999-01-00013') x
   ON (t.store_id = x.store_id AND t.title = x.title)
 WHEN MATCHED THEN UPDATE SET
      t.difficulty = 3, t.play_minutes = 60, t.min_people = 2, t.max_people = 4, t.price = 22000,
      t.poster_url = 'https://img.reserv.company/nextedition/konkuk-2/escape-room-academy.jpg',
      t.booking_url = 'https://nextedition.co.kr/reservation/geondae-2/?theme=69',
      t.description = '방탈출에 입문하고 싶으신 분들,' ||
        CHR(10) ||
        '방탈출을 잘 하고 싶으신 분들을 위해 준비했습니다.' ||
        CHR(10) ||
        '방탈출의 정석. 방탈출의 개념원리.' ||
        CHR(10) ||
        '기출 유형 파악부터 복습까지!' ||
        CHR(10) ||
        '마감 일주일 전입니다.' ||
        CHR(10) ||
        '어서 수강신청을 서둘러주세요!' ||
        CHR(10) ||
        CHR(10) ||
        '[ 예약 전 필독 사항 ]' ||
        CHR(10) ||
        '- 예약 시, 메모 란에 어떤 과정으로 진행하실 지 적어주세요!' ||
        CHR(10) ||
        '(현장에서 과정을 정할 시 셋팅에 따라 시간차감이 되실 수 있습니다.)' ||
        CHR(10) ||
        CHR(10) ||
        '1. Normal 과정 (난이도 ***) - 기본가이드 + 참고서 (힌트폰 제공/힌트 무제한) [방탈출 경험 20회 미만 추천]' ||
        CHR(10) ||
        'Hard 과정 (난이도 ****) - 기본가이드 (힌트폰 제공/힌트 무제한) [방탈출 경험 50회 미만 추천]' ||
        CHR(10) ||
        'Master 과정 (난이도 *****) - 심화가이드 (힌트폰 지급X / 노힌트) [방탈출 경험 50회 이상 추천]' ||
        CHR(10) ||
        CHR(10) ||
        '2. Hard 과정의 랭킹 기준은 [탈출 시간] 이며,' ||
        CHR(10) ||
        'Master 과정의 랭킹 기준은 [탈출시간과 80점이상 획득]입니다.' ||
        CHR(10) ||
        '(Normal 과정은 랭킹에서 제외.)' ||
        CHR(10) ||
        CHR(10) ||
        '3. Master 과정으로 진행하실 경우, 절반 이상 진행하지 못하고 실패 시 남은 문제들에 대한 설명을 해드릴 수 없으니, 신중하게 선택 부탁드립니다.'
 WHEN NOT MATCHED THEN INSERT
      (theme_id, store_id, title, difficulty, play_minutes, min_people, max_people, price, poster_url, booking_url, description, source, status)
      VALUES (sq_theme.NEXTVAL, x.store_id, x.title, 3, 60, 2, 4, 22000,
              'https://img.reserv.company/nextedition/konkuk-2/escape-room-academy.jpg',
              'https://nextedition.co.kr/reservation/geondae-2/?theme=69',
              '방탈출에 입문하고 싶으신 분들,' ||
        CHR(10) ||
        '방탈출을 잘 하고 싶으신 분들을 위해 준비했습니다.' ||
        CHR(10) ||
        '방탈출의 정석. 방탈출의 개념원리.' ||
        CHR(10) ||
        '기출 유형 파악부터 복습까지!' ||
        CHR(10) ||
        '마감 일주일 전입니다.' ||
        CHR(10) ||
        '어서 수강신청을 서둘러주세요!' ||
        CHR(10) ||
        CHR(10) ||
        '[ 예약 전 필독 사항 ]' ||
        CHR(10) ||
        '- 예약 시, 메모 란에 어떤 과정으로 진행하실 지 적어주세요!' ||
        CHR(10) ||
        '(현장에서 과정을 정할 시 셋팅에 따라 시간차감이 되실 수 있습니다.)' ||
        CHR(10) ||
        CHR(10) ||
        '1. Normal 과정 (난이도 ***) - 기본가이드 + 참고서 (힌트폰 제공/힌트 무제한) [방탈출 경험 20회 미만 추천]' ||
        CHR(10) ||
        'Hard 과정 (난이도 ****) - 기본가이드 (힌트폰 제공/힌트 무제한) [방탈출 경험 50회 미만 추천]' ||
        CHR(10) ||
        'Master 과정 (난이도 *****) - 심화가이드 (힌트폰 지급X / 노힌트) [방탈출 경험 50회 이상 추천]' ||
        CHR(10) ||
        CHR(10) ||
        '2. Hard 과정의 랭킹 기준은 [탈출 시간] 이며,' ||
        CHR(10) ||
        'Master 과정의 랭킹 기준은 [탈출시간과 80점이상 획득]입니다.' ||
        CHR(10) ||
        '(Normal 과정은 랭킹에서 제외.)' ||
        CHR(10) ||
        CHR(10) ||
        '3. Master 과정으로 진행하실 경우, 절반 이상 진행하지 못하고 실패 시 남은 문제들에 대한 설명을 해드릴 수 없으니, 신중하게 선택 부탁드립니다.',
              'CRAWL', 'OPEN');
DELETE FROM theme_genre WHERE theme_id = (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-01-00013' AND t.title = '방탈출 아카데미');
INSERT INTO theme_genre (id, theme_id, genre_id)
SELECT sq_theme_genre.NEXTVAL, t.theme_id, g.genre_id FROM theme t, store s, genre g
 WHERE t.store_id = s.store_id AND s.biz_place_no = '999-01-00013' AND t.title = '방탈출 아카데미' AND g.name IN ('모험');

-- 「어제, 그리고 오늘」
MERGE INTO theme t
USING (SELECT store_id, '어제, 그리고 오늘' AS title FROM store WHERE biz_place_no = '999-01-00013') x
   ON (t.store_id = x.store_id AND t.title = x.title)
 WHEN MATCHED THEN UPDATE SET
      t.difficulty = 4, t.play_minutes = 60, t.min_people = 2, t.max_people = 4, t.price = 22000,
      t.poster_url = 'https://img.reserv.company/nextedition/konkuk-2/yesterday-and-today.png',
      t.booking_url = 'https://nextedition.co.kr/reservation/geondae-2/?theme=65',
      t.description = '어제와 다른 오늘을 살아갑니다' ||
        CHR(10) ||
        CHR(10) ||
        '''이불밖은 위험해''의 속편입니다.' ||
        CHR(10) ||
        '세계관이 연결되어 있어서 전작을 하시고' ||
        CHR(10) ||
        '진행하시는것을 추천드립니다. :D' ||
        CHR(10) ||
        '(전작을 하시지 않아도 플레이 하시는데 지장은 없습니다!)'
 WHEN NOT MATCHED THEN INSERT
      (theme_id, store_id, title, difficulty, play_minutes, min_people, max_people, price, poster_url, booking_url, description, source, status)
      VALUES (sq_theme.NEXTVAL, x.store_id, x.title, 4, 60, 2, 4, 22000,
              'https://img.reserv.company/nextedition/konkuk-2/yesterday-and-today.png',
              'https://nextedition.co.kr/reservation/geondae-2/?theme=65',
              '어제와 다른 오늘을 살아갑니다' ||
        CHR(10) ||
        CHR(10) ||
        '''이불밖은 위험해''의 속편입니다.' ||
        CHR(10) ||
        '세계관이 연결되어 있어서 전작을 하시고' ||
        CHR(10) ||
        '진행하시는것을 추천드립니다. :D' ||
        CHR(10) ||
        '(전작을 하시지 않아도 플레이 하시는데 지장은 없습니다!)',
              'CRAWL', 'OPEN');
DELETE FROM theme_genre WHERE theme_id = (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-01-00013' AND t.title = '어제, 그리고 오늘');
INSERT INTO theme_genre (id, theme_id, genre_id)
SELECT sq_theme_genre.NEXTVAL, t.theme_id, g.genre_id FROM theme t, store s, genre g
 WHERE t.store_id = s.store_id AND s.biz_place_no = '999-01-00013' AND t.title = '어제, 그리고 오늘' AND g.name IN ('감성');

-- ==================== [넥스트에디션] 넥스트에디션 건대 보네르관  (https://nextedition.co.kr/branches/geondae-bonheur/)
MERGE INTO store s
USING (SELECT '999-01-00016' AS biz_place_no FROM dual) x
   ON (s.biz_place_no = x.biz_place_no)
 WHEN MATCHED THEN UPDATE SET
      s.name = '넥스트에디션 건대 보네르관', s.area = '건대', s.address = '서울 광진구 화양동 9-19, 지하 1층', s.phone = '010-6668-7451',
      s.website_url = 'https://nextedition.co.kr/branches/geondae-bonheur/', s.hours = '영업시간은 매장 홈페이지에서 확인해 주세요',
      s.intro = '넥스트에디션 건대 보네르관. 공식 홈페이지에서 모은 정보예요. 예약과 정확한 가격은 매장 홈페이지에서 확인해 주세요.', s.lat = 37.541576, s.lng = 127.069322
 WHEN NOT MATCHED THEN INSERT
      (store_id, owner_id, name, area, address, phone, website_url, hours, intro, lat, lng, biz_place_no, status, approved_at)
      VALUES (sq_store.NEXTVAL, NULL, '넥스트에디션 건대 보네르관', '건대', '서울 광진구 화양동 9-19, 지하 1층', '010-6668-7451',
              'https://nextedition.co.kr/branches/geondae-bonheur/', '영업시간은 매장 홈페이지에서 확인해 주세요',
              '넥스트에디션 건대 보네르관. 공식 홈페이지에서 모은 정보예요. 예약과 정확한 가격은 매장 홈페이지에서 확인해 주세요.', 37.541576, 127.069322, x.biz_place_no, 'APPROVED', SYSDATE);

-- 「세렌디피티(SERENDIPITY)」
MERGE INTO theme t
USING (SELECT store_id, '세렌디피티(SERENDIPITY)' AS title FROM store WHERE biz_place_no = '999-01-00016') x
   ON (t.store_id = x.store_id AND t.title = x.title)
 WHEN MATCHED THEN UPDATE SET
      t.difficulty = 4, t.play_minutes = 100, t.min_people = 2, t.max_people = 4, t.price = 22000,
      t.poster_url = 'https://img.reserv.company/nextedition/konkuk-bonheur/serendipity.png',
      t.booking_url = 'https://nextedition.co.kr/reservation/geondae-bonheur/?theme=41',
      t.description = NULL
 WHEN NOT MATCHED THEN INSERT
      (theme_id, store_id, title, difficulty, play_minutes, min_people, max_people, price, poster_url, booking_url, description, source, status)
      VALUES (sq_theme.NEXTVAL, x.store_id, x.title, 4, 100, 2, 4, 22000,
              'https://img.reserv.company/nextedition/konkuk-bonheur/serendipity.png',
              'https://nextedition.co.kr/reservation/geondae-bonheur/?theme=41',
              NULL,
              'CRAWL', 'OPEN');
DELETE FROM theme_genre WHERE theme_id = (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-01-00016' AND t.title = '세렌디피티(SERENDIPITY)');
INSERT INTO theme_genre (id, theme_id, genre_id)
SELECT sq_theme_genre.NEXTVAL, t.theme_id, g.genre_id FROM theme t, store s, genre g
 WHERE t.store_id = s.store_id AND s.biz_place_no = '999-01-00016' AND t.title = '세렌디피티(SERENDIPITY)' AND g.name IN ('감성', '판타지');

-- ==================== [넥스트에디션] 넥스트에디션 부천점  (https://nextedition.co.kr/branches/bucheon/)
MERGE INTO store s
USING (SELECT '999-01-00012' AS biz_place_no FROM dual) x
   ON (s.biz_place_no = x.biz_place_no)
 WHEN MATCHED THEN UPDATE SET
      s.name = '넥스트에디션 부천점', s.area = '부천', s.address = '경기 부천시 원미구 심곡동 175-9, 6층', s.phone = '032-667-7772',
      s.website_url = 'https://nextedition.co.kr/branches/bucheon/', s.hours = '영업시간은 매장 홈페이지에서 확인해 주세요',
      s.intro = '넥스트에디션 부천점. 공식 홈페이지에서 모은 정보예요. 예약과 정확한 가격은 매장 홈페이지에서 확인해 주세요.', s.lat = 37.485522, s.lng = 126.781732
 WHEN NOT MATCHED THEN INSERT
      (store_id, owner_id, name, area, address, phone, website_url, hours, intro, lat, lng, biz_place_no, status, approved_at)
      VALUES (sq_store.NEXTVAL, NULL, '넥스트에디션 부천점', '부천', '경기 부천시 원미구 심곡동 175-9, 6층', '032-667-7772',
              'https://nextedition.co.kr/branches/bucheon/', '영업시간은 매장 홈페이지에서 확인해 주세요',
              '넥스트에디션 부천점. 공식 홈페이지에서 모은 정보예요. 예약과 정확한 가격은 매장 홈페이지에서 확인해 주세요.', 37.485522, 126.781732, x.biz_place_no, 'APPROVED', SYSDATE);

-- 「진시황」
MERGE INTO theme t
USING (SELECT store_id, '진시황' AS title FROM store WHERE biz_place_no = '999-01-00012') x
   ON (t.store_id = x.store_id AND t.title = x.title)
 WHEN MATCHED THEN UPDATE SET
      t.difficulty = 3, t.play_minutes = 60, t.min_people = 2, t.max_people = 5, t.price = 22000,
      t.poster_url = 'https://img.reserv.company/nextedition/bucheon/qin-shi-huang.jpg',
      t.booking_url = 'https://nextedition.co.kr/reservation/bucheon/?theme=55',
      t.description = '“기원전 221년 천하를 통일한 진시황은' ||
        CHR(10) ||
        CHR(10) ||
        '영생의 비밀을 찾아 동쪽의 작은 나라로 서복을 보낸다' ||
        CHR(10) ||
        CHR(10) ||
        '서복은 그곳에서 영생을 찾았는지는 알려지지 않았는데..'
 WHEN NOT MATCHED THEN INSERT
      (theme_id, store_id, title, difficulty, play_minutes, min_people, max_people, price, poster_url, booking_url, description, source, status)
      VALUES (sq_theme.NEXTVAL, x.store_id, x.title, 3, 60, 2, 5, 22000,
              'https://img.reserv.company/nextedition/bucheon/qin-shi-huang.jpg',
              'https://nextedition.co.kr/reservation/bucheon/?theme=55',
              '“기원전 221년 천하를 통일한 진시황은' ||
        CHR(10) ||
        CHR(10) ||
        '영생의 비밀을 찾아 동쪽의 작은 나라로 서복을 보낸다' ||
        CHR(10) ||
        CHR(10) ||
        '서복은 그곳에서 영생을 찾았는지는 알려지지 않았는데..',
              'CRAWL', 'OPEN');
DELETE FROM theme_genre WHERE theme_id = (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-01-00012' AND t.title = '진시황');
INSERT INTO theme_genre (id, theme_id, genre_id)
SELECT sq_theme_genre.NEXTVAL, t.theme_id, g.genre_id FROM theme t, store s, genre g
 WHERE t.store_id = s.store_id AND s.biz_place_no = '999-01-00012' AND t.title = '진시황' AND g.name IN ('모험');

-- 「집으로」
MERGE INTO theme t
USING (SELECT store_id, '집으로' AS title FROM store WHERE biz_place_no = '999-01-00012') x
   ON (t.store_id = x.store_id AND t.title = x.title)
 WHEN MATCHED THEN UPDATE SET
      t.difficulty = 3, t.play_minutes = 60, t.min_people = 2, t.max_people = 5, t.price = 22000,
      t.poster_url = 'https://img.reserv.company/nextedition/bucheon/homeward.jpg',
      t.booking_url = 'https://nextedition.co.kr/reservation/bucheon/?theme=54',
      t.description = '친구 집에 다녀오던 동생과 나' ||
        CHR(10) ||
        '그러나 갑자기 납치, 감금을 당하게 되고...' ||
        CHR(10) ||
        CHR(10) ||
        '아빠 엄마 집에 가고 싶어요'
 WHEN NOT MATCHED THEN INSERT
      (theme_id, store_id, title, difficulty, play_minutes, min_people, max_people, price, poster_url, booking_url, description, source, status)
      VALUES (sq_theme.NEXTVAL, x.store_id, x.title, 3, 60, 2, 5, 22000,
              'https://img.reserv.company/nextedition/bucheon/homeward.jpg',
              'https://nextedition.co.kr/reservation/bucheon/?theme=54',
              '친구 집에 다녀오던 동생과 나' ||
        CHR(10) ||
        '그러나 갑자기 납치, 감금을 당하게 되고...' ||
        CHR(10) ||
        CHR(10) ||
        '아빠 엄마 집에 가고 싶어요',
              'CRAWL', 'OPEN');
DELETE FROM theme_genre WHERE theme_id = (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-01-00012' AND t.title = '집으로');
INSERT INTO theme_genre (id, theme_id, genre_id)
SELECT sq_theme_genre.NEXTVAL, t.theme_id, g.genre_id FROM theme t, store s, genre g
 WHERE t.store_id = s.store_id AND s.biz_place_no = '999-01-00012' AND t.title = '집으로' AND g.name IN ('스릴러');

-- 「쌩얼」
MERGE INTO theme t
USING (SELECT store_id, '쌩얼' AS title FROM store WHERE biz_place_no = '999-01-00012') x
   ON (t.store_id = x.store_id AND t.title = x.title)
 WHEN MATCHED THEN UPDATE SET
      t.difficulty = 3, t.play_minutes = 60, t.min_people = 2, t.max_people = 5, t.price = 22000,
      t.poster_url = 'https://img.reserv.company/nextedition/bucheon/bare-face.jpg',
      t.booking_url = 'https://nextedition.co.kr/reservation/bucheon/?theme=53',
      t.description = '띵동' ||
        CHR(10) ||
        '''누구세요?''' ||
        CHR(10) ||
        '''나야... ''' ||
        CHR(10) ||
        '''응? 이 시간에 웬일이야?''' ||
        CHR(10) ||
        '''나 너한테 할말있어...앞에 놀이터에 있을 테니 잠깐 나와''' ||
        CHR(10) ||
        CHR(10) ||
        '''? 지금 막 화장을 지운 참인데 나오라고?' ||
        CHR(10) ||
        '숨겼던 나의 쌩얼을 너한테 보여줄 수 없어'''
 WHEN NOT MATCHED THEN INSERT
      (theme_id, store_id, title, difficulty, play_minutes, min_people, max_people, price, poster_url, booking_url, description, source, status)
      VALUES (sq_theme.NEXTVAL, x.store_id, x.title, 3, 60, 2, 5, 22000,
              'https://img.reserv.company/nextedition/bucheon/bare-face.jpg',
              'https://nextedition.co.kr/reservation/bucheon/?theme=53',
              '띵동' ||
        CHR(10) ||
        '''누구세요?''' ||
        CHR(10) ||
        '''나야... ''' ||
        CHR(10) ||
        '''응? 이 시간에 웬일이야?''' ||
        CHR(10) ||
        '''나 너한테 할말있어...앞에 놀이터에 있을 테니 잠깐 나와''' ||
        CHR(10) ||
        CHR(10) ||
        '''? 지금 막 화장을 지운 참인데 나오라고?' ||
        CHR(10) ||
        '숨겼던 나의 쌩얼을 너한테 보여줄 수 없어''',
              'CRAWL', 'OPEN');
DELETE FROM theme_genre WHERE theme_id = (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-01-00012' AND t.title = '쌩얼');
INSERT INTO theme_genre (id, theme_id, genre_id)
SELECT sq_theme_genre.NEXTVAL, t.theme_id, g.genre_id FROM theme t, store s, genre g
 WHERE t.store_id = s.store_id AND s.biz_place_no = '999-01-00012' AND t.title = '쌩얼' AND g.name IN ('감성', '코믹');

-- 「주르륵」
MERGE INTO theme t
USING (SELECT store_id, '주르륵' AS title FROM store WHERE biz_place_no = '999-01-00012') x
   ON (t.store_id = x.store_id AND t.title = x.title)
 WHEN MATCHED THEN UPDATE SET
      t.difficulty = 4, t.play_minutes = 60, t.min_people = 2, t.max_people = 5, t.price = 22000,
      t.poster_url = 'https://img.reserv.company/nextedition/bucheon/drip-drop.jpg',
      t.booking_url = 'https://nextedition.co.kr/reservation/bucheon/?theme=52',
      t.description = '갑작스런 폭우로 교통사고를 당하게 된 당신.' ||
        CHR(10) ||
        '간신히 목숨은 구했지만 사고의 충격으로 지난 4년간의 기억을 모두 잃게 된다.' ||
        CHR(10) ||
        '그리고 트라우마로 인해 차를 타지 못하게 되고' ||
        CHR(10) ||
        '비만 오면 왠지 모를 슬픔에 젖게 된다.'
 WHEN NOT MATCHED THEN INSERT
      (theme_id, store_id, title, difficulty, play_minutes, min_people, max_people, price, poster_url, booking_url, description, source, status)
      VALUES (sq_theme.NEXTVAL, x.store_id, x.title, 4, 60, 2, 5, 22000,
              'https://img.reserv.company/nextedition/bucheon/drip-drop.jpg',
              'https://nextedition.co.kr/reservation/bucheon/?theme=52',
              '갑작스런 폭우로 교통사고를 당하게 된 당신.' ||
        CHR(10) ||
        '간신히 목숨은 구했지만 사고의 충격으로 지난 4년간의 기억을 모두 잃게 된다.' ||
        CHR(10) ||
        '그리고 트라우마로 인해 차를 타지 못하게 되고' ||
        CHR(10) ||
        '비만 오면 왠지 모를 슬픔에 젖게 된다.',
              'CRAWL', 'OPEN');
DELETE FROM theme_genre WHERE theme_id = (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-01-00012' AND t.title = '주르륵');
INSERT INTO theme_genre (id, theme_id, genre_id)
SELECT sq_theme_genre.NEXTVAL, t.theme_id, g.genre_id FROM theme t, store s, genre g
 WHERE t.store_id = s.store_id AND s.biz_place_no = '999-01-00012' AND t.title = '주르륵' AND g.name IN ('감성');

-- ==================== [넥스트에디션] 넥스트에디션 분당서현점  (https://nextedition.co.kr/branches/bundang-seohyeon/)
MERGE INTO store s
USING (SELECT '999-01-00020' AS biz_place_no FROM dual) x
   ON (s.biz_place_no = x.biz_place_no)
 WHEN MATCHED THEN UPDATE SET
      s.name = '넥스트에디션 분당서현점', s.area = '분당', s.address = '경기 성남시 분당구 황새울로342번길 19 지하1층 101호 넥스트에디션', s.phone = '031-781-9020',
      s.website_url = 'https://nextedition.co.kr/branches/bundang-seohyeon/', s.hours = '영업시간은 매장 홈페이지에서 확인해 주세요',
      s.intro = '넥스트에디션 분당서현점. 공식 홈페이지에서 모은 정보예요. 예약과 정확한 가격은 매장 홈페이지에서 확인해 주세요.', s.lat = 37.386616, s.lng = 127.124287
 WHEN NOT MATCHED THEN INSERT
      (store_id, owner_id, name, area, address, phone, website_url, hours, intro, lat, lng, biz_place_no, status, approved_at)
      VALUES (sq_store.NEXTVAL, NULL, '넥스트에디션 분당서현점', '분당', '경기 성남시 분당구 황새울로342번길 19 지하1층 101호 넥스트에디션', '031-781-9020',
              'https://nextedition.co.kr/branches/bundang-seohyeon/', '영업시간은 매장 홈페이지에서 확인해 주세요',
              '넥스트에디션 분당서현점. 공식 홈페이지에서 모은 정보예요. 예약과 정확한 가격은 매장 홈페이지에서 확인해 주세요.', 37.386616, 127.124287, x.biz_place_no, 'APPROVED', SYSDATE);

-- 「평범한 하루」
MERGE INTO theme t
USING (SELECT store_id, '평범한 하루' AS title FROM store WHERE biz_place_no = '999-01-00020') x
   ON (t.store_id = x.store_id AND t.title = x.title)
 WHEN MATCHED THEN UPDATE SET
      t.difficulty = 5, t.play_minutes = 90, t.min_people = 2, t.max_people = 6, t.price = 22000,
      t.poster_url = 'https://img.reserv.company/nextedition/bundang-seohyeon/an-ordinary-day.jpg',
      t.booking_url = 'https://nextedition.co.kr/reservation/bundang-seohyeon/?theme=97',
      t.description = '요즘 두통을 앓고 나면' ||
        CHR(10) ||
        '잠깐의 기억이 지워지는 것 같다.' ||
        CHR(10) ||
        '이렇게 기억을 잃는 걸까?' ||
        CHR(10) ||
        '오늘도 이렇게 평범한 하루의' ||
        CHR(10) ||
        '소중함을 깨닫는다.'
 WHEN NOT MATCHED THEN INSERT
      (theme_id, store_id, title, difficulty, play_minutes, min_people, max_people, price, poster_url, booking_url, description, source, status)
      VALUES (sq_theme.NEXTVAL, x.store_id, x.title, 5, 90, 2, 6, 22000,
              'https://img.reserv.company/nextedition/bundang-seohyeon/an-ordinary-day.jpg',
              'https://nextedition.co.kr/reservation/bundang-seohyeon/?theme=97',
              '요즘 두통을 앓고 나면' ||
        CHR(10) ||
        '잠깐의 기억이 지워지는 것 같다.' ||
        CHR(10) ||
        '이렇게 기억을 잃는 걸까?' ||
        CHR(10) ||
        '오늘도 이렇게 평범한 하루의' ||
        CHR(10) ||
        '소중함을 깨닫는다.',
              'CRAWL', 'OPEN');
DELETE FROM theme_genre WHERE theme_id = (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-01-00020' AND t.title = '평범한 하루');
INSERT INTO theme_genre (id, theme_id, genre_id)
SELECT sq_theme_genre.NEXTVAL, t.theme_id, g.genre_id FROM theme t, store s, genre g
 WHERE t.store_id = s.store_id AND s.biz_place_no = '999-01-00020' AND t.title = '평범한 하루' AND g.name IN ('모험');

-- 「몽중몽」
MERGE INTO theme t
USING (SELECT store_id, '몽중몽' AS title FROM store WHERE biz_place_no = '999-01-00020') x
   ON (t.store_id = x.store_id AND t.title = x.title)
 WHEN MATCHED THEN UPDATE SET
      t.difficulty = 3, t.play_minutes = 60, t.min_people = 2, t.max_people = 6, t.price = 22000,
      t.poster_url = 'https://img.reserv.company/nextedition/bundang-seohyeon/dream-within-a-dream.png',
      t.booking_url = 'https://nextedition.co.kr/reservation/bundang-seohyeon/?theme=99',
      t.description = '방탈출 매니아 김씨' ||
        CHR(10) ||
        CHR(10) ||
        '평소에 공포테마는 무섭지도 않고 시시하다고 생각하여 잘 안 가던 김씨는 더 이상 갈 만한 테마들이 없는 나머지 속는 셈 치고 공포테마를 갔다 와보지만 여전히 시시하기만 한데..' ||
        CHR(10) ||
        CHR(10) ||
        '''에휴! 역시는 역시구나. 시시해'''
 WHEN NOT MATCHED THEN INSERT
      (theme_id, store_id, title, difficulty, play_minutes, min_people, max_people, price, poster_url, booking_url, description, source, status)
      VALUES (sq_theme.NEXTVAL, x.store_id, x.title, 3, 60, 2, 6, 22000,
              'https://img.reserv.company/nextedition/bundang-seohyeon/dream-within-a-dream.png',
              'https://nextedition.co.kr/reservation/bundang-seohyeon/?theme=99',
              '방탈출 매니아 김씨' ||
        CHR(10) ||
        CHR(10) ||
        '평소에 공포테마는 무섭지도 않고 시시하다고 생각하여 잘 안 가던 김씨는 더 이상 갈 만한 테마들이 없는 나머지 속는 셈 치고 공포테마를 갔다 와보지만 여전히 시시하기만 한데..' ||
        CHR(10) ||
        CHR(10) ||
        '''에휴! 역시는 역시구나. 시시해''',
              'CRAWL', 'OPEN');
DELETE FROM theme_genre WHERE theme_id = (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-01-00020' AND t.title = '몽중몽');
INSERT INTO theme_genre (id, theme_id, genre_id)
SELECT sq_theme_genre.NEXTVAL, t.theme_id, g.genre_id FROM theme t, store s, genre g
 WHERE t.store_id = s.store_id AND s.biz_place_no = '999-01-00020' AND t.title = '몽중몽' AND g.name IN ('공포');

-- 「테마명을 뭐로할지 못정하겠어요ㅠㅠ」
MERGE INTO theme t
USING (SELECT store_id, '테마명을 뭐로할지 못정하겠어요ㅠㅠ' AS title FROM store WHERE biz_place_no = '999-01-00020') x
   ON (t.store_id = x.store_id AND t.title = x.title)
 WHEN MATCHED THEN UPDATE SET
      t.difficulty = 4, t.play_minutes = 60, t.min_people = 2, t.max_people = 6, t.price = 22000,
      t.poster_url = 'https://img.reserv.company/nextedition/bundang-seohyeon/i-cant-decide-on-a-theme-name.jpg',
      t.booking_url = 'https://nextedition.co.kr/reservation/bundang-seohyeon/?theme=100',
      t.description = '저처럼 결정장애 있으신 분 계신가요?'
 WHEN NOT MATCHED THEN INSERT
      (theme_id, store_id, title, difficulty, play_minutes, min_people, max_people, price, poster_url, booking_url, description, source, status)
      VALUES (sq_theme.NEXTVAL, x.store_id, x.title, 4, 60, 2, 6, 22000,
              'https://img.reserv.company/nextedition/bundang-seohyeon/i-cant-decide-on-a-theme-name.jpg',
              'https://nextedition.co.kr/reservation/bundang-seohyeon/?theme=100',
              '저처럼 결정장애 있으신 분 계신가요?',
              'CRAWL', 'OPEN');
DELETE FROM theme_genre WHERE theme_id = (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-01-00020' AND t.title = '테마명을 뭐로할지 못정하겠어요ㅠㅠ');

-- 「너에게 가는 길」
MERGE INTO theme t
USING (SELECT store_id, '너에게 가는 길' AS title FROM store WHERE biz_place_no = '999-01-00020') x
   ON (t.store_id = x.store_id AND t.title = x.title)
 WHEN MATCHED THEN UPDATE SET
      t.difficulty = 2, t.play_minutes = 60, t.min_people = 2, t.max_people = 5, t.price = 22000,
      t.poster_url = 'https://img.reserv.company/nextedition/bundang-seohyeon/the-road-to-you.jpg',
      t.booking_url = 'https://nextedition.co.kr/reservation/bundang-seohyeon/?theme=96',
      t.description = '...?!' ||
        CHR(10) ||
        '하나뿐인 내 친구와 함께 간직하던 소중한 것이' ||
        CHR(10) ||
        '오늘, 그것도 방금!' ||
        CHR(10) ||
        '눈 앞에서 갑자기 사라져버렸다.' ||
        CHR(10) ||
        '''걱정하지마 친구! 내가 꼭 찾아올게!'''
 WHEN NOT MATCHED THEN INSERT
      (theme_id, store_id, title, difficulty, play_minutes, min_people, max_people, price, poster_url, booking_url, description, source, status)
      VALUES (sq_theme.NEXTVAL, x.store_id, x.title, 2, 60, 2, 5, 22000,
              'https://img.reserv.company/nextedition/bundang-seohyeon/the-road-to-you.jpg',
              'https://nextedition.co.kr/reservation/bundang-seohyeon/?theme=96',
              '...?!' ||
        CHR(10) ||
        '하나뿐인 내 친구와 함께 간직하던 소중한 것이' ||
        CHR(10) ||
        '오늘, 그것도 방금!' ||
        CHR(10) ||
        '눈 앞에서 갑자기 사라져버렸다.' ||
        CHR(10) ||
        '''걱정하지마 친구! 내가 꼭 찾아올게!''',
              'CRAWL', 'OPEN');
DELETE FROM theme_genre WHERE theme_id = (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-01-00020' AND t.title = '너에게 가는 길');
INSERT INTO theme_genre (id, theme_id, genre_id)
SELECT sq_theme_genre.NEXTVAL, t.theme_id, g.genre_id FROM theme t, store s, genre g
 WHERE t.store_id = s.store_id AND s.biz_place_no = '999-01-00020' AND t.title = '너에게 가는 길' AND g.name IN ('모험');

-- 「짠해」
MERGE INTO theme t
USING (SELECT store_id, '짠해' AS title FROM store WHERE biz_place_no = '999-01-00020') x
   ON (t.store_id = x.store_id AND t.title = x.title)
 WHEN MATCHED THEN UPDATE SET
      t.difficulty = 3, t.play_minutes = 60, t.min_people = 2, t.max_people = 6, t.price = 22000,
      t.poster_url = 'https://img.reserv.company/nextedition/bundang-seohyeon/cheers.png',
      t.booking_url = 'https://nextedition.co.kr/reservation/bundang-seohyeon/?theme=98',
      t.description = '어제도 야근, 오늘도 야근' ||
        CHR(10) ||
        '내 인생이 너무 짠합니다'
 WHEN NOT MATCHED THEN INSERT
      (theme_id, store_id, title, difficulty, play_minutes, min_people, max_people, price, poster_url, booking_url, description, source, status)
      VALUES (sq_theme.NEXTVAL, x.store_id, x.title, 3, 60, 2, 6, 22000,
              'https://img.reserv.company/nextedition/bundang-seohyeon/cheers.png',
              'https://nextedition.co.kr/reservation/bundang-seohyeon/?theme=98',
              '어제도 야근, 오늘도 야근' ||
        CHR(10) ||
        '내 인생이 너무 짠합니다',
              'CRAWL', 'OPEN');
DELETE FROM theme_genre WHERE theme_id = (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-01-00020' AND t.title = '짠해');
INSERT INTO theme_genre (id, theme_id, genre_id)
SELECT sq_theme_genre.NEXTVAL, t.theme_id, g.genre_id FROM theme t, store s, genre g
 WHERE t.store_id = s.store_id AND s.biz_place_no = '999-01-00020' AND t.title = '짠해' AND g.name IN ('감성');

-- 「익명의 여자」
MERGE INTO theme t
USING (SELECT store_id, '익명의 여자' AS title FROM store WHERE biz_place_no = '999-01-00020') x
   ON (t.store_id = x.store_id AND t.title = x.title)
 WHEN MATCHED THEN UPDATE SET
      t.difficulty = 2, t.play_minutes = 60, t.min_people = 2, t.max_people = 6, t.price = 22000,
      t.poster_url = 'https://img.reserv.company/nextedition/bundang-seohyeon/anonymous-woman.jpg',
      t.booking_url = 'https://nextedition.co.kr/reservation/bundang-seohyeon/?theme=101',
      t.description = '특별할 것 없는 일상을 살던 남자' ||
        CHR(10) ||
        '신기현' ||
        CHR(10) ||
        '그런 그에게 소개팅이 들어왔다.'
 WHEN NOT MATCHED THEN INSERT
      (theme_id, store_id, title, difficulty, play_minutes, min_people, max_people, price, poster_url, booking_url, description, source, status)
      VALUES (sq_theme.NEXTVAL, x.store_id, x.title, 2, 60, 2, 6, 22000,
              'https://img.reserv.company/nextedition/bundang-seohyeon/anonymous-woman.jpg',
              'https://nextedition.co.kr/reservation/bundang-seohyeon/?theme=101',
              '특별할 것 없는 일상을 살던 남자' ||
        CHR(10) ||
        '신기현' ||
        CHR(10) ||
        '그런 그에게 소개팅이 들어왔다.',
              'CRAWL', 'OPEN');
DELETE FROM theme_genre WHERE theme_id = (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-01-00020' AND t.title = '익명의 여자');
INSERT INTO theme_genre (id, theme_id, genre_id)
SELECT sq_theme_genre.NEXTVAL, t.theme_id, g.genre_id FROM theme t, store s, genre g
 WHERE t.store_id = s.store_id AND s.biz_place_no = '999-01-00020' AND t.title = '익명의 여자' AND g.name IN ('감성');

-- ==================== [넥스트에디션] 넥스트에디션 신림점  (https://nextedition.co.kr/branches/sillim/)
MERGE INTO store s
USING (SELECT '999-01-00010' AS biz_place_no FROM dual) x
   ON (s.biz_place_no = x.biz_place_no)
 WHEN MATCHED THEN UPDATE SET
      s.name = '넥스트에디션 신림점', s.area = '신림', s.address = '서울 관악구 남부순환로 1598, 지하 1층', s.phone = '02-882-3775',
      s.website_url = 'https://nextedition.co.kr/branches/sillim/', s.hours = '영업시간은 매장 홈페이지에서 확인해 주세요',
      s.intro = '넥스트에디션 신림점. 공식 홈페이지에서 모은 정보예요. 예약과 정확한 가격은 매장 홈페이지에서 확인해 주세요.', s.lat = 37.483766, s.lng = 126.928335
 WHEN NOT MATCHED THEN INSERT
      (store_id, owner_id, name, area, address, phone, website_url, hours, intro, lat, lng, biz_place_no, status, approved_at)
      VALUES (sq_store.NEXTVAL, NULL, '넥스트에디션 신림점', '신림', '서울 관악구 남부순환로 1598, 지하 1층', '02-882-3775',
              'https://nextedition.co.kr/branches/sillim/', '영업시간은 매장 홈페이지에서 확인해 주세요',
              '넥스트에디션 신림점. 공식 홈페이지에서 모은 정보예요. 예약과 정확한 가격은 매장 홈페이지에서 확인해 주세요.', 37.483766, 126.928335, x.biz_place_no, 'APPROVED', SYSDATE);

-- 「극」
MERGE INTO theme t
USING (SELECT store_id, '극' AS title FROM store WHERE biz_place_no = '999-01-00010') x
   ON (t.store_id = x.store_id AND t.title = x.title)
 WHEN MATCHED THEN UPDATE SET
      t.difficulty = 3, t.play_minutes = 70, t.min_people = 2, t.max_people = 6, t.price = 22000,
      t.poster_url = 'https://img.reserv.company/nextedition/sillim/the-play.jpg',
      t.booking_url = 'https://nextedition.co.kr/reservation/sillim/?theme=42',
      t.description = '신림 대표 공포 테마입니다.(시그니처)' ||
        CHR(10) ||
        '딱 맛있게 매운 공포 테마입니다.' ||
        CHR(10) ||
        '무서운 거 못하시는 분들도 극 테마는 재미있게 하고 가십니다.' ||
        CHR(10) ||
        '연극 무대를 배경으로 합니다.' ||
        CHR(10) ||
        CHR(10) ||
        '우리 약속 기억나?'
 WHEN NOT MATCHED THEN INSERT
      (theme_id, store_id, title, difficulty, play_minutes, min_people, max_people, price, poster_url, booking_url, description, source, status)
      VALUES (sq_theme.NEXTVAL, x.store_id, x.title, 3, 70, 2, 6, 22000,
              'https://img.reserv.company/nextedition/sillim/the-play.jpg',
              'https://nextedition.co.kr/reservation/sillim/?theme=42',
              '신림 대표 공포 테마입니다.(시그니처)' ||
        CHR(10) ||
        '딱 맛있게 매운 공포 테마입니다.' ||
        CHR(10) ||
        '무서운 거 못하시는 분들도 극 테마는 재미있게 하고 가십니다.' ||
        CHR(10) ||
        '연극 무대를 배경으로 합니다.' ||
        CHR(10) ||
        CHR(10) ||
        '우리 약속 기억나?',
              'CRAWL', 'OPEN');
DELETE FROM theme_genre WHERE theme_id = (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-01-00010' AND t.title = '극');

-- 「옛날옛날에」
MERGE INTO theme t
USING (SELECT store_id, '옛날옛날에' AS title FROM store WHERE biz_place_no = '999-01-00010') x
   ON (t.store_id = x.store_id AND t.title = x.title)
 WHEN MATCHED THEN UPDATE SET
      t.difficulty = 2, t.play_minutes = 60, t.min_people = 2, t.max_people = 6, t.price = 22000,
      t.poster_url = 'https://img.reserv.company/nextedition/sillim/once-upon-a-time.png',
      t.booking_url = 'https://nextedition.co.kr/reservation/sillim/?theme=46',
      t.description = '할아버지~할아버지~ 저 왔어요 ~~' ||
        CHR(10) ||
        '옛날이야기좀 해주세요 ~~' ||
        CHR(10) ||
        CHR(10) ||
        '끔찍히도 아끼는 손주의 한마디가, 날 당황하게 만들었다.' ||
        CHR(10) ||
        '과연 어떤 옛날이야기를 풀어나갈지' ||
        CHR(10) ||
        CHR(10) ||
        '환상적인 동화나라가 펼쳐집니다.' ||
        CHR(10) ||
        '일반사람들도 좋아하지만 특히 방탈출 초보 혹은 초중고 생들에게' ||
        CHR(10) ||
        '인기가 많아요.'
 WHEN NOT MATCHED THEN INSERT
      (theme_id, store_id, title, difficulty, play_minutes, min_people, max_people, price, poster_url, booking_url, description, source, status)
      VALUES (sq_theme.NEXTVAL, x.store_id, x.title, 2, 60, 2, 6, 22000,
              'https://img.reserv.company/nextedition/sillim/once-upon-a-time.png',
              'https://nextedition.co.kr/reservation/sillim/?theme=46',
              '할아버지~할아버지~ 저 왔어요 ~~' ||
        CHR(10) ||
        '옛날이야기좀 해주세요 ~~' ||
        CHR(10) ||
        CHR(10) ||
        '끔찍히도 아끼는 손주의 한마디가, 날 당황하게 만들었다.' ||
        CHR(10) ||
        '과연 어떤 옛날이야기를 풀어나갈지' ||
        CHR(10) ||
        CHR(10) ||
        '환상적인 동화나라가 펼쳐집니다.' ||
        CHR(10) ||
        '일반사람들도 좋아하지만 특히 방탈출 초보 혹은 초중고 생들에게' ||
        CHR(10) ||
        '인기가 많아요.',
              'CRAWL', 'OPEN');
DELETE FROM theme_genre WHERE theme_id = (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-01-00010' AND t.title = '옛날옛날에');
INSERT INTO theme_genre (id, theme_id, genre_id)
SELECT sq_theme_genre.NEXTVAL, t.theme_id, g.genre_id FROM theme t, store s, genre g
 WHERE t.store_id = s.store_id AND s.biz_place_no = '999-01-00010' AND t.title = '옛날옛날에' AND g.name IN ('판타지');

-- 「LOVER」
MERGE INTO theme t
USING (SELECT store_id, 'LOVER' AS title FROM store WHERE biz_place_no = '999-01-00010') x
   ON (t.store_id = x.store_id AND t.title = x.title)
 WHEN MATCHED THEN UPDATE SET
      t.difficulty = 3, t.play_minutes = 60, t.min_people = 2, t.max_people = 6, t.price = 22000,
      t.poster_url = 'https://img.reserv.company/nextedition/sillim/lover.png',
      t.booking_url = 'https://nextedition.co.kr/reservation/sillim/?theme=45',
      t.description = '사랑하는 수정이에게 전해줄 선물은 무엇일까?' ||
        CHR(10) ||
        '수정아 사랑해'
 WHEN NOT MATCHED THEN INSERT
      (theme_id, store_id, title, difficulty, play_minutes, min_people, max_people, price, poster_url, booking_url, description, source, status)
      VALUES (sq_theme.NEXTVAL, x.store_id, x.title, 3, 60, 2, 6, 22000,
              'https://img.reserv.company/nextedition/sillim/lover.png',
              'https://nextedition.co.kr/reservation/sillim/?theme=45',
              '사랑하는 수정이에게 전해줄 선물은 무엇일까?' ||
        CHR(10) ||
        '수정아 사랑해',
              'CRAWL', 'OPEN');
DELETE FROM theme_genre WHERE theme_id = (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-01-00010' AND t.title = 'LOVER');
INSERT INTO theme_genre (id, theme_id, genre_id)
SELECT sq_theme_genre.NEXTVAL, t.theme_id, g.genre_id FROM theme t, store s, genre g
 WHERE t.store_id = s.store_id AND s.biz_place_no = '999-01-00010' AND t.title = 'LOVER' AND g.name IN ('감성', '코믹');

-- 「씨프?? XX!!」
MERGE INTO theme t
USING (SELECT store_id, '씨프?? XX!!' AS title FROM store WHERE biz_place_no = '999-01-00010') x
   ON (t.store_id = x.store_id AND t.title = x.title)
 WHEN MATCHED THEN UPDATE SET
      t.difficulty = 4, t.play_minutes = 60, t.min_people = 2, t.max_people = 6, t.price = 22000,
      t.poster_url = 'https://img.reserv.company/nextedition/sillim/thief-xx.png',
      t.booking_url = 'https://nextedition.co.kr/reservation/sillim/?theme=43',
      t.description = '우린 유명한 맛집 중 휴가를 떠난 곳을 털기 위해' ||
        CHR(10) ||
        '모인 도둑 4인 정도..라고 생각해줘!'
 WHEN NOT MATCHED THEN INSERT
      (theme_id, store_id, title, difficulty, play_minutes, min_people, max_people, price, poster_url, booking_url, description, source, status)
      VALUES (sq_theme.NEXTVAL, x.store_id, x.title, 4, 60, 2, 6, 22000,
              'https://img.reserv.company/nextedition/sillim/thief-xx.png',
              'https://nextedition.co.kr/reservation/sillim/?theme=43',
              '우린 유명한 맛집 중 휴가를 떠난 곳을 털기 위해' ||
        CHR(10) ||
        '모인 도둑 4인 정도..라고 생각해줘!',
              'CRAWL', 'OPEN');
DELETE FROM theme_genre WHERE theme_id = (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-01-00010' AND t.title = '씨프?? XX!!');
INSERT INTO theme_genre (id, theme_id, genre_id)
SELECT sq_theme_genre.NEXTVAL, t.theme_id, g.genre_id FROM theme t, store s, genre g
 WHERE t.store_id = s.store_id AND s.biz_place_no = '999-01-00010' AND t.title = '씨프?? XX!!' AND g.name IN ('코믹');

-- 「Tester」
MERGE INTO theme t
USING (SELECT store_id, 'Tester' AS title FROM store WHERE biz_place_no = '999-01-00010') x
   ON (t.store_id = x.store_id AND t.title = x.title)
 WHEN MATCHED THEN UPDATE SET
      t.difficulty = 4, t.play_minutes = 60, t.min_people = 2, t.max_people = 6, t.price = 22000,
      t.poster_url = 'https://img.reserv.company/nextedition/sillim/tester.jpg',
      t.booking_url = 'https://nextedition.co.kr/reservation/sillim/?theme=44',
      t.description = '누굴 시험하는가.' ||
        CHR(10) ||
        CHR(10) ||
        '※ 해당 테마는 벽을 사이에 두고 협동하며 진행되는 테마입니다.'
 WHEN NOT MATCHED THEN INSERT
      (theme_id, store_id, title, difficulty, play_minutes, min_people, max_people, price, poster_url, booking_url, description, source, status)
      VALUES (sq_theme.NEXTVAL, x.store_id, x.title, 4, 60, 2, 6, 22000,
              'https://img.reserv.company/nextedition/sillim/tester.jpg',
              'https://nextedition.co.kr/reservation/sillim/?theme=44',
              '누굴 시험하는가.' ||
        CHR(10) ||
        CHR(10) ||
        '※ 해당 테마는 벽을 사이에 두고 협동하며 진행되는 테마입니다.',
              'CRAWL', 'OPEN');
DELETE FROM theme_genre WHERE theme_id = (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-01-00010' AND t.title = 'Tester');
INSERT INTO theme_genre (id, theme_id, genre_id)
SELECT sq_theme_genre.NEXTVAL, t.theme_id, g.genre_id FROM theme t, store s, genre g
 WHERE t.store_id = s.store_id AND s.biz_place_no = '999-01-00010' AND t.title = 'Tester' AND g.name IN ('추리');

-- ==================== [넥스트에디션] 넥스트에디션 잠실점  (https://nextedition.co.kr/branches/jamsil/)
MERGE INTO store s
USING (SELECT '999-01-00014' AS biz_place_no FROM dual) x
   ON (s.biz_place_no = x.biz_place_no)
 WHEN MATCHED THEN UPDATE SET
      s.name = '넥스트에디션 잠실점', s.area = '잠실', s.address = '서울 송파구 올림픽로 118, 지하 1층', s.phone = '02-423-8255',
      s.website_url = 'https://nextedition.co.kr/branches/jamsil/', s.hours = '영업시간은 매장 홈페이지에서 확인해 주세요',
      s.intro = '넥스트에디션 잠실점. 공식 홈페이지에서 모은 정보예요. 예약과 정확한 가격은 매장 홈페이지에서 확인해 주세요.', s.lat = 37.511415, s.lng = 127.083830
 WHEN NOT MATCHED THEN INSERT
      (store_id, owner_id, name, area, address, phone, website_url, hours, intro, lat, lng, biz_place_no, status, approved_at)
      VALUES (sq_store.NEXTVAL, NULL, '넥스트에디션 잠실점', '잠실', '서울 송파구 올림픽로 118, 지하 1층', '02-423-8255',
              'https://nextedition.co.kr/branches/jamsil/', '영업시간은 매장 홈페이지에서 확인해 주세요',
              '넥스트에디션 잠실점. 공식 홈페이지에서 모은 정보예요. 예약과 정확한 가격은 매장 홈페이지에서 확인해 주세요.', 37.511415, 127.083830, x.biz_place_no, 'APPROVED', SYSDATE);

-- 「카페라떼」
MERGE INTO theme t
USING (SELECT store_id, '카페라떼' AS title FROM store WHERE biz_place_no = '999-01-00014') x
   ON (t.store_id = x.store_id AND t.title = x.title)
 WHEN MATCHED THEN UPDATE SET
      t.difficulty = 3, t.play_minutes = 60, t.min_people = 2, t.max_people = 5, t.price = 22000,
      t.poster_url = 'https://img.reserv.company/nextedition/jamsil/cafe-latte.jpg',
      t.booking_url = 'https://nextedition.co.kr/reservation/jamsil/?theme=59',
      t.description = '위기에 빠진 세상.' ||
        CHR(10) ||
        CHR(10) ||
        '이 혼란을 막아야만 한다!'
 WHEN NOT MATCHED THEN INSERT
      (theme_id, store_id, title, difficulty, play_minutes, min_people, max_people, price, poster_url, booking_url, description, source, status)
      VALUES (sq_theme.NEXTVAL, x.store_id, x.title, 3, 60, 2, 5, 22000,
              'https://img.reserv.company/nextedition/jamsil/cafe-latte.jpg',
              'https://nextedition.co.kr/reservation/jamsil/?theme=59',
              '위기에 빠진 세상.' ||
        CHR(10) ||
        CHR(10) ||
        '이 혼란을 막아야만 한다!',
              'CRAWL', 'OPEN');
DELETE FROM theme_genre WHERE theme_id = (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-01-00014' AND t.title = '카페라떼');

-- 「작은 악마들」
MERGE INTO theme t
USING (SELECT store_id, '작은 악마들' AS title FROM store WHERE biz_place_no = '999-01-00014') x
   ON (t.store_id = x.store_id AND t.title = x.title)
 WHEN MATCHED THEN UPDATE SET
      t.difficulty = 4, t.play_minutes = 60, t.min_people = 2, t.max_people = 5, t.price = 22000,
      t.poster_url = 'https://img.reserv.company/nextedition/jamsil/little-devils.jpg',
      t.booking_url = 'https://nextedition.co.kr/reservation/jamsil/?theme=58',
      t.description = '1년 전.' ||
        CHR(10) ||
        CHR(10) ||
        '잊고 싶은 악몽같던 기억.'
 WHEN NOT MATCHED THEN INSERT
      (theme_id, store_id, title, difficulty, play_minutes, min_people, max_people, price, poster_url, booking_url, description, source, status)
      VALUES (sq_theme.NEXTVAL, x.store_id, x.title, 4, 60, 2, 5, 22000,
              'https://img.reserv.company/nextedition/jamsil/little-devils.jpg',
              'https://nextedition.co.kr/reservation/jamsil/?theme=58',
              '1년 전.' ||
        CHR(10) ||
        CHR(10) ||
        '잊고 싶은 악몽같던 기억.',
              'CRAWL', 'OPEN');
DELETE FROM theme_genre WHERE theme_id = (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-01-00014' AND t.title = '작은 악마들');
INSERT INTO theme_genre (id, theme_id, genre_id)
SELECT sq_theme_genre.NEXTVAL, t.theme_id, g.genre_id FROM theme t, store s, genre g
 WHERE t.store_id = s.store_id AND s.biz_place_no = '999-01-00014' AND t.title = '작은 악마들' AND g.name IN ('감성');

-- 「락 페스티벌」
MERGE INTO theme t
USING (SELECT store_id, '락 페스티벌' AS title FROM store WHERE biz_place_no = '999-01-00014') x
   ON (t.store_id = x.store_id AND t.title = x.title)
 WHEN MATCHED THEN UPDATE SET
      t.difficulty = 5, t.play_minutes = 60, t.min_people = 3, t.max_people = 3, t.price = 22000,
      t.poster_url = 'https://img.reserv.company/nextedition/jamsil/rock-festival.jpg',
      t.booking_url = 'https://nextedition.co.kr/reservation/jamsil/?theme=57',
      t.description = 'ROCK!' ||
        CHR(10) ||
        'LOCK!' ||
        CHR(10) ||
        '락!' ||
        CHR(10) ||
        CHR(10) ||
        '도전하라!' ||
        CHR(10) ||
        CHR(10) ||
        '* 문제가 매우 많고 어려운 문제방이기에,' ||
        CHR(10) ||
        '3인 이상이 플레이 하시는 것을 추천드립니다.'
 WHEN NOT MATCHED THEN INSERT
      (theme_id, store_id, title, difficulty, play_minutes, min_people, max_people, price, poster_url, booking_url, description, source, status)
      VALUES (sq_theme.NEXTVAL, x.store_id, x.title, 5, 60, 3, 3, 22000,
              'https://img.reserv.company/nextedition/jamsil/rock-festival.jpg',
              'https://nextedition.co.kr/reservation/jamsil/?theme=57',
              'ROCK!' ||
        CHR(10) ||
        'LOCK!' ||
        CHR(10) ||
        '락!' ||
        CHR(10) ||
        CHR(10) ||
        '도전하라!' ||
        CHR(10) ||
        CHR(10) ||
        '* 문제가 매우 많고 어려운 문제방이기에,' ||
        CHR(10) ||
        '3인 이상이 플레이 하시는 것을 추천드립니다.',
              'CRAWL', 'OPEN');
DELETE FROM theme_genre WHERE theme_id = (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-01-00014' AND t.title = '락 페스티벌');
INSERT INTO theme_genre (id, theme_id, genre_id)
SELECT sq_theme_genre.NEXTVAL, t.theme_id, g.genre_id FROM theme t, store s, genre g
 WHERE t.store_id = s.store_id AND s.biz_place_no = '999-01-00014' AND t.title = '락 페스티벌' AND g.name IN ('모험');

-- 「데.코.연 (데이트 코스 연구회)」
MERGE INTO theme t
USING (SELECT store_id, '데.코.연 (데이트 코스 연구회)' AS title FROM store WHERE biz_place_no = '999-01-00014') x
   ON (t.store_id = x.store_id AND t.title = x.title)
 WHEN MATCHED THEN UPDATE SET
      t.difficulty = 2, t.play_minutes = 60, t.min_people = 2, t.max_people = 5, t.price = 22000,
      t.poster_url = 'https://img.reserv.company/nextedition/jamsil/date-course-research-society.jpg',
      t.booking_url = 'https://nextedition.co.kr/reservation/jamsil/?theme=56',
      t.description = '누군가에게는' ||
        CHR(10) ||
        '유치할 수도 있는,' ||
        CHR(10) ||
        CHR(10) ||
        '누군가에게는' ||
        CHR(10) ||
        '입가에 미소를 짓게 할,' ||
        CHR(10) ||
        CHR(10) ||
        '그런 이야기.'
 WHEN NOT MATCHED THEN INSERT
      (theme_id, store_id, title, difficulty, play_minutes, min_people, max_people, price, poster_url, booking_url, description, source, status)
      VALUES (sq_theme.NEXTVAL, x.store_id, x.title, 2, 60, 2, 5, 22000,
              'https://img.reserv.company/nextedition/jamsil/date-course-research-society.jpg',
              'https://nextedition.co.kr/reservation/jamsil/?theme=56',
              '누군가에게는' ||
        CHR(10) ||
        '유치할 수도 있는,' ||
        CHR(10) ||
        CHR(10) ||
        '누군가에게는' ||
        CHR(10) ||
        '입가에 미소를 짓게 할,' ||
        CHR(10) ||
        CHR(10) ||
        '그런 이야기.',
              'CRAWL', 'OPEN');
DELETE FROM theme_genre WHERE theme_id = (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-01-00014' AND t.title = '데.코.연 (데이트 코스 연구회)');
INSERT INTO theme_genre (id, theme_id, genre_id)
SELECT sq_theme_genre.NEXTVAL, t.theme_id, g.genre_id FROM theme t, store s, genre g
 WHERE t.store_id = s.store_id AND s.biz_place_no = '999-01-00014' AND t.title = '데.코.연 (데이트 코스 연구회)' AND g.name IN ('감성');

-- ==================== [포인트나인] 포인트나인 강남점  (https://point-nine.com/layout/res/home.php?go=main)
MERGE INTO store s
USING (SELECT '999-02-00001' AS biz_place_no FROM dual) x
   ON (s.biz_place_no = x.biz_place_no)
 WHEN MATCHED THEN UPDATE SET
      s.name = '포인트나인 강남점', s.area = '강남', s.address = '서울시 강남구 역삼동 618-3 지하1층', s.phone = '070-7733-7879',
      s.website_url = 'https://point-nine.com/layout/res/home.php?go=main', s.hours = '11:00 ~ 23:10 (시간표 기준)',
      s.intro = '포인트나인 강남점. 공식 홈페이지에서 모은 정보예요. 예약과 정확한 가격은 매장 홈페이지에서 확인해 주세요.', s.lat = NULL, s.lng = NULL
 WHEN NOT MATCHED THEN INSERT
      (store_id, owner_id, name, area, address, phone, website_url, hours, intro, lat, lng, biz_place_no, status, approved_at)
      VALUES (sq_store.NEXTVAL, NULL, '포인트나인 강남점', '강남', '서울시 강남구 역삼동 618-3 지하1층', '070-7733-7879',
              'https://point-nine.com/layout/res/home.php?go=main', '11:00 ~ 23:10 (시간표 기준)',
              '포인트나인 강남점. 공식 홈페이지에서 모은 정보예요. 예약과 정확한 가격은 매장 홈페이지에서 확인해 주세요.', NULL, NULL, x.biz_place_no, 'APPROVED', SYSDATE);

-- 「EP1 : 시간이 멈춘 마을」
MERGE INTO theme t
USING (SELECT store_id, 'EP1 : 시간이 멈춘 마을' AS title FROM store WHERE biz_place_no = '999-02-00001') x
   ON (t.store_id = x.store_id AND t.title = x.title)
 WHEN MATCHED THEN UPDATE SET
      t.difficulty = 4, t.play_minutes = 70, t.min_people = 1, t.max_people = 4, t.price = 25000,
      t.poster_url = 'https://point-nine.com/file/theme/8_a.jpg',
      t.booking_url = 'https://point-nine.com/layout/res/home.php?go=rev.make&s_zizum=1',
      t.description = '다른 주민들은 모르겠지만,' ||
        CHR(10) ||
        CHR(10) ||
        '시간이 멈춘 우리 마을은 같은 하루가 반복된다.' ||
        CHR(10) ||
        CHR(10) ||
        '오늘도 다른 날과 다를 것이 없겠지만,' ||
        CHR(10) ||
        CHR(10) ||
        '영주로서의 책무를 다하기 위해' ||
        CHR(10) ||
        CHR(10) ||
        '마을을 한 바퀴 둘러보았다.'
 WHEN NOT MATCHED THEN INSERT
      (theme_id, store_id, title, difficulty, play_minutes, min_people, max_people, price, poster_url, booking_url, description, source, status)
      VALUES (sq_theme.NEXTVAL, x.store_id, x.title, 4, 70, 1, 4, 25000,
              'https://point-nine.com/file/theme/8_a.jpg',
              'https://point-nine.com/layout/res/home.php?go=rev.make&s_zizum=1',
              '다른 주민들은 모르겠지만,' ||
        CHR(10) ||
        CHR(10) ||
        '시간이 멈춘 우리 마을은 같은 하루가 반복된다.' ||
        CHR(10) ||
        CHR(10) ||
        '오늘도 다른 날과 다를 것이 없겠지만,' ||
        CHR(10) ||
        CHR(10) ||
        '영주로서의 책무를 다하기 위해' ||
        CHR(10) ||
        CHR(10) ||
        '마을을 한 바퀴 둘러보았다.',
              'CRAWL', 'OPEN');
DELETE FROM theme_genre WHERE theme_id = (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00001' AND t.title = 'EP1 : 시간이 멈춘 마을');
DELETE FROM time_slot WHERE theme_id = (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00001' AND t.title = 'EP1 : 시간이 멈춘 마을');
INSERT INTO time_slot (slot_id, theme_id, day_type, start_time) VALUES (sq_time_slot.NEXTVAL, (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00001' AND t.title = 'EP1 : 시간이 멈춘 마을'), 'WEEKDAY', '11:00');
INSERT INTO time_slot (slot_id, theme_id, day_type, start_time) VALUES (sq_time_slot.NEXTVAL, (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00001' AND t.title = 'EP1 : 시간이 멈춘 마을'), 'WEEKDAY', '12:30');
INSERT INTO time_slot (slot_id, theme_id, day_type, start_time) VALUES (sq_time_slot.NEXTVAL, (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00001' AND t.title = 'EP1 : 시간이 멈춘 마을'), 'WEEKDAY', '14:00');
INSERT INTO time_slot (slot_id, theme_id, day_type, start_time) VALUES (sq_time_slot.NEXTVAL, (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00001' AND t.title = 'EP1 : 시간이 멈춘 마을'), 'WEEKDAY', '15:30');
INSERT INTO time_slot (slot_id, theme_id, day_type, start_time) VALUES (sq_time_slot.NEXTVAL, (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00001' AND t.title = 'EP1 : 시간이 멈춘 마을'), 'WEEKDAY', '17:00');
INSERT INTO time_slot (slot_id, theme_id, day_type, start_time) VALUES (sq_time_slot.NEXTVAL, (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00001' AND t.title = 'EP1 : 시간이 멈춘 마을'), 'WEEKDAY', '18:30');
INSERT INTO time_slot (slot_id, theme_id, day_type, start_time) VALUES (sq_time_slot.NEXTVAL, (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00001' AND t.title = 'EP1 : 시간이 멈춘 마을'), 'WEEKDAY', '20:00');
INSERT INTO time_slot (slot_id, theme_id, day_type, start_time) VALUES (sq_time_slot.NEXTVAL, (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00001' AND t.title = 'EP1 : 시간이 멈춘 마을'), 'WEEKDAY', '21:30');
INSERT INTO time_slot (slot_id, theme_id, day_type, start_time) VALUES (sq_time_slot.NEXTVAL, (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00001' AND t.title = 'EP1 : 시간이 멈춘 마을'), 'WEEKEND', '11:00');
INSERT INTO time_slot (slot_id, theme_id, day_type, start_time) VALUES (sq_time_slot.NEXTVAL, (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00001' AND t.title = 'EP1 : 시간이 멈춘 마을'), 'WEEKEND', '12:30');
INSERT INTO time_slot (slot_id, theme_id, day_type, start_time) VALUES (sq_time_slot.NEXTVAL, (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00001' AND t.title = 'EP1 : 시간이 멈춘 마을'), 'WEEKEND', '14:00');
INSERT INTO time_slot (slot_id, theme_id, day_type, start_time) VALUES (sq_time_slot.NEXTVAL, (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00001' AND t.title = 'EP1 : 시간이 멈춘 마을'), 'WEEKEND', '15:30');
INSERT INTO time_slot (slot_id, theme_id, day_type, start_time) VALUES (sq_time_slot.NEXTVAL, (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00001' AND t.title = 'EP1 : 시간이 멈춘 마을'), 'WEEKEND', '17:00');
INSERT INTO time_slot (slot_id, theme_id, day_type, start_time) VALUES (sq_time_slot.NEXTVAL, (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00001' AND t.title = 'EP1 : 시간이 멈춘 마을'), 'WEEKEND', '18:30');
INSERT INTO time_slot (slot_id, theme_id, day_type, start_time) VALUES (sq_time_slot.NEXTVAL, (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00001' AND t.title = 'EP1 : 시간이 멈춘 마을'), 'WEEKEND', '20:00');
INSERT INTO time_slot (slot_id, theme_id, day_type, start_time) VALUES (sq_time_slot.NEXTVAL, (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00001' AND t.title = 'EP1 : 시간이 멈춘 마을'), 'WEEKEND', '21:30');

-- 「EP2 : 열쇠공의 이중생활」
MERGE INTO theme t
USING (SELECT store_id, 'EP2 : 열쇠공의 이중생활' AS title FROM store WHERE biz_place_no = '999-02-00001') x
   ON (t.store_id = x.store_id AND t.title = x.title)
 WHEN MATCHED THEN UPDATE SET
      t.difficulty = 4, t.play_minutes = 70, t.min_people = 1, t.max_people = 4, t.price = 25000,
      t.poster_url = 'https://point-nine.com/file/theme/6_a.jpg',
      t.booking_url = 'https://point-nine.com/layout/res/home.php?go=rev.make&s_zizum=1',
      t.description = '나는 마을의 열쇠공이다. 옆집 귀금속점 주인이 일전에 의뢰한 물건이 맘에 쏙 들었는지 귀한 답례품을 주고 갔다.' ||
        CHR(10) ||
        CHR(10) ||
        '구두쇠로 소문난 녀석이 웬일이지? 제법 만들기 어려웠던 것이지만,' ||
        CHR(10) ||
        CHR(10) ||
        '작업하는 재미가 있어서 꽤 괜찮은 결과물이 나온 덕인 것 같다. 보는 눈은 있어 가지고…' ||
        CHR(10) ||
        CHR(10) ||
        '흠 근데 이거 뭘 준 거지? 구두쇠 녀석이 비싼 걸 줬을 리 없으니 의심부터 해봐야겠어! 자, 어디 한번 보러 가볼까?'
 WHEN NOT MATCHED THEN INSERT
      (theme_id, store_id, title, difficulty, play_minutes, min_people, max_people, price, poster_url, booking_url, description, source, status)
      VALUES (sq_theme.NEXTVAL, x.store_id, x.title, 4, 70, 1, 4, 25000,
              'https://point-nine.com/file/theme/6_a.jpg',
              'https://point-nine.com/layout/res/home.php?go=rev.make&s_zizum=1',
              '나는 마을의 열쇠공이다. 옆집 귀금속점 주인이 일전에 의뢰한 물건이 맘에 쏙 들었는지 귀한 답례품을 주고 갔다.' ||
        CHR(10) ||
        CHR(10) ||
        '구두쇠로 소문난 녀석이 웬일이지? 제법 만들기 어려웠던 것이지만,' ||
        CHR(10) ||
        CHR(10) ||
        '작업하는 재미가 있어서 꽤 괜찮은 결과물이 나온 덕인 것 같다. 보는 눈은 있어 가지고…' ||
        CHR(10) ||
        CHR(10) ||
        '흠 근데 이거 뭘 준 거지? 구두쇠 녀석이 비싼 걸 줬을 리 없으니 의심부터 해봐야겠어! 자, 어디 한번 보러 가볼까?',
              'CRAWL', 'OPEN');
DELETE FROM theme_genre WHERE theme_id = (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00001' AND t.title = 'EP2 : 열쇠공의 이중생활');
DELETE FROM time_slot WHERE theme_id = (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00001' AND t.title = 'EP2 : 열쇠공의 이중생활');
INSERT INTO time_slot (slot_id, theme_id, day_type, start_time) VALUES (sq_time_slot.NEXTVAL, (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00001' AND t.title = 'EP2 : 열쇠공의 이중생활'), 'WEEKDAY', '11:10');
INSERT INTO time_slot (slot_id, theme_id, day_type, start_time) VALUES (sq_time_slot.NEXTVAL, (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00001' AND t.title = 'EP2 : 열쇠공의 이중생활'), 'WEEKDAY', '12:40');
INSERT INTO time_slot (slot_id, theme_id, day_type, start_time) VALUES (sq_time_slot.NEXTVAL, (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00001' AND t.title = 'EP2 : 열쇠공의 이중생활'), 'WEEKDAY', '14:10');
INSERT INTO time_slot (slot_id, theme_id, day_type, start_time) VALUES (sq_time_slot.NEXTVAL, (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00001' AND t.title = 'EP2 : 열쇠공의 이중생활'), 'WEEKDAY', '15:40');
INSERT INTO time_slot (slot_id, theme_id, day_type, start_time) VALUES (sq_time_slot.NEXTVAL, (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00001' AND t.title = 'EP2 : 열쇠공의 이중생활'), 'WEEKDAY', '17:10');
INSERT INTO time_slot (slot_id, theme_id, day_type, start_time) VALUES (sq_time_slot.NEXTVAL, (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00001' AND t.title = 'EP2 : 열쇠공의 이중생활'), 'WEEKDAY', '18:40');
INSERT INTO time_slot (slot_id, theme_id, day_type, start_time) VALUES (sq_time_slot.NEXTVAL, (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00001' AND t.title = 'EP2 : 열쇠공의 이중생활'), 'WEEKDAY', '20:10');
INSERT INTO time_slot (slot_id, theme_id, day_type, start_time) VALUES (sq_time_slot.NEXTVAL, (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00001' AND t.title = 'EP2 : 열쇠공의 이중생활'), 'WEEKDAY', '21:40');
INSERT INTO time_slot (slot_id, theme_id, day_type, start_time) VALUES (sq_time_slot.NEXTVAL, (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00001' AND t.title = 'EP2 : 열쇠공의 이중생활'), 'WEEKEND', '11:10');
INSERT INTO time_slot (slot_id, theme_id, day_type, start_time) VALUES (sq_time_slot.NEXTVAL, (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00001' AND t.title = 'EP2 : 열쇠공의 이중생활'), 'WEEKEND', '12:40');
INSERT INTO time_slot (slot_id, theme_id, day_type, start_time) VALUES (sq_time_slot.NEXTVAL, (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00001' AND t.title = 'EP2 : 열쇠공의 이중생활'), 'WEEKEND', '14:10');
INSERT INTO time_slot (slot_id, theme_id, day_type, start_time) VALUES (sq_time_slot.NEXTVAL, (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00001' AND t.title = 'EP2 : 열쇠공의 이중생활'), 'WEEKEND', '15:40');
INSERT INTO time_slot (slot_id, theme_id, day_type, start_time) VALUES (sq_time_slot.NEXTVAL, (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00001' AND t.title = 'EP2 : 열쇠공의 이중생활'), 'WEEKEND', '17:10');
INSERT INTO time_slot (slot_id, theme_id, day_type, start_time) VALUES (sq_time_slot.NEXTVAL, (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00001' AND t.title = 'EP2 : 열쇠공의 이중생활'), 'WEEKEND', '18:40');
INSERT INTO time_slot (slot_id, theme_id, day_type, start_time) VALUES (sq_time_slot.NEXTVAL, (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00001' AND t.title = 'EP2 : 열쇠공의 이중생활'), 'WEEKEND', '20:10');
INSERT INTO time_slot (slot_id, theme_id, day_type, start_time) VALUES (sq_time_slot.NEXTVAL, (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00001' AND t.title = 'EP2 : 열쇠공의 이중생활'), 'WEEKEND', '21:40');

-- 「EP3 : 눈 먼 귀금속상인의 후회」
MERGE INTO theme t
USING (SELECT store_id, 'EP3 : 눈 먼 귀금속상인의 후회' AS title FROM store WHERE biz_place_no = '999-02-00001') x
   ON (t.store_id = x.store_id AND t.title = x.title)
 WHEN MATCHED THEN UPDATE SET
      t.difficulty = 3, t.play_minutes = 70, t.min_people = 1, t.max_people = 4, t.price = 25000,
      t.poster_url = 'https://point-nine.com/file/theme/28_a.jpg',
      t.booking_url = 'https://point-nine.com/layout/res/home.php?go=rev.make&s_zizum=1',
      t.description = '오늘도... 출근... 완료' ||
        CHR(10) ||
        CHR(10) ||
        '한바탕 돌아다니고 와서 그런가' ||
        CHR(10) ||
        CHR(10) ||
        '괜히 더 따분한 기분이네' ||
        CHR(10) ||
        CHR(10) ||
        '뭐 재밌는 일 또 없나?'
 WHEN NOT MATCHED THEN INSERT
      (theme_id, store_id, title, difficulty, play_minutes, min_people, max_people, price, poster_url, booking_url, description, source, status)
      VALUES (sq_theme.NEXTVAL, x.store_id, x.title, 3, 70, 1, 4, 25000,
              'https://point-nine.com/file/theme/28_a.jpg',
              'https://point-nine.com/layout/res/home.php?go=rev.make&s_zizum=1',
              '오늘도... 출근... 완료' ||
        CHR(10) ||
        CHR(10) ||
        '한바탕 돌아다니고 와서 그런가' ||
        CHR(10) ||
        CHR(10) ||
        '괜히 더 따분한 기분이네' ||
        CHR(10) ||
        CHR(10) ||
        '뭐 재밌는 일 또 없나?',
              'CRAWL', 'OPEN');
DELETE FROM theme_genre WHERE theme_id = (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00001' AND t.title = 'EP3 : 눈 먼 귀금속상인의 후회');
DELETE FROM time_slot WHERE theme_id = (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00001' AND t.title = 'EP3 : 눈 먼 귀금속상인의 후회');
INSERT INTO time_slot (slot_id, theme_id, day_type, start_time) VALUES (sq_time_slot.NEXTVAL, (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00001' AND t.title = 'EP3 : 눈 먼 귀금속상인의 후회'), 'WEEKDAY', '11:20');
INSERT INTO time_slot (slot_id, theme_id, day_type, start_time) VALUES (sq_time_slot.NEXTVAL, (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00001' AND t.title = 'EP3 : 눈 먼 귀금속상인의 후회'), 'WEEKDAY', '12:50');
INSERT INTO time_slot (slot_id, theme_id, day_type, start_time) VALUES (sq_time_slot.NEXTVAL, (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00001' AND t.title = 'EP3 : 눈 먼 귀금속상인의 후회'), 'WEEKDAY', '14:20');
INSERT INTO time_slot (slot_id, theme_id, day_type, start_time) VALUES (sq_time_slot.NEXTVAL, (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00001' AND t.title = 'EP3 : 눈 먼 귀금속상인의 후회'), 'WEEKDAY', '15:50');
INSERT INTO time_slot (slot_id, theme_id, day_type, start_time) VALUES (sq_time_slot.NEXTVAL, (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00001' AND t.title = 'EP3 : 눈 먼 귀금속상인의 후회'), 'WEEKDAY', '17:20');
INSERT INTO time_slot (slot_id, theme_id, day_type, start_time) VALUES (sq_time_slot.NEXTVAL, (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00001' AND t.title = 'EP3 : 눈 먼 귀금속상인의 후회'), 'WEEKDAY', '18:50');
INSERT INTO time_slot (slot_id, theme_id, day_type, start_time) VALUES (sq_time_slot.NEXTVAL, (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00001' AND t.title = 'EP3 : 눈 먼 귀금속상인의 후회'), 'WEEKDAY', '20:20');
INSERT INTO time_slot (slot_id, theme_id, day_type, start_time) VALUES (sq_time_slot.NEXTVAL, (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00001' AND t.title = 'EP3 : 눈 먼 귀금속상인의 후회'), 'WEEKDAY', '21:50');
INSERT INTO time_slot (slot_id, theme_id, day_type, start_time) VALUES (sq_time_slot.NEXTVAL, (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00001' AND t.title = 'EP3 : 눈 먼 귀금속상인의 후회'), 'WEEKEND', '11:20');
INSERT INTO time_slot (slot_id, theme_id, day_type, start_time) VALUES (sq_time_slot.NEXTVAL, (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00001' AND t.title = 'EP3 : 눈 먼 귀금속상인의 후회'), 'WEEKEND', '12:50');
INSERT INTO time_slot (slot_id, theme_id, day_type, start_time) VALUES (sq_time_slot.NEXTVAL, (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00001' AND t.title = 'EP3 : 눈 먼 귀금속상인의 후회'), 'WEEKEND', '14:20');
INSERT INTO time_slot (slot_id, theme_id, day_type, start_time) VALUES (sq_time_slot.NEXTVAL, (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00001' AND t.title = 'EP3 : 눈 먼 귀금속상인의 후회'), 'WEEKEND', '15:50');
INSERT INTO time_slot (slot_id, theme_id, day_type, start_time) VALUES (sq_time_slot.NEXTVAL, (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00001' AND t.title = 'EP3 : 눈 먼 귀금속상인의 후회'), 'WEEKEND', '17:20');
INSERT INTO time_slot (slot_id, theme_id, day_type, start_time) VALUES (sq_time_slot.NEXTVAL, (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00001' AND t.title = 'EP3 : 눈 먼 귀금속상인의 후회'), 'WEEKEND', '18:50');
INSERT INTO time_slot (slot_id, theme_id, day_type, start_time) VALUES (sq_time_slot.NEXTVAL, (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00001' AND t.title = 'EP3 : 눈 먼 귀금속상인의 후회'), 'WEEKEND', '20:20');
INSERT INTO time_slot (slot_id, theme_id, day_type, start_time) VALUES (sq_time_slot.NEXTVAL, (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00001' AND t.title = 'EP3 : 눈 먼 귀금속상인의 후회'), 'WEEKEND', '21:50');

-- 「EP4 : 주인 없는 낡은 서점」
MERGE INTO theme t
USING (SELECT store_id, 'EP4 : 주인 없는 낡은 서점' AS title FROM store WHERE biz_place_no = '999-02-00001') x
   ON (t.store_id = x.store_id AND t.title = x.title)
 WHEN MATCHED THEN UPDATE SET
      t.difficulty = 4, t.play_minutes = 70, t.min_people = 1, t.max_people = 4, t.price = 25000,
      t.poster_url = 'https://point-nine.com/file/theme/14_a.jpg',
      t.booking_url = 'https://point-nine.com/layout/res/home.php?go=rev.make&s_zizum=1',
      t.description = '재회의 반가움도 잠시' ||
        CHR(10) ||
        CHR(10) ||
        '한시도 지체 할 틈이 없다.' ||
        CHR(10) ||
        CHR(10) ||
        '어서 내 임무를 완수 해야한다.' ||
        CHR(10) ||
        CHR(10) ||
        '주머니 속 구겨진 메모 한장과' ||
        CHR(10) ||
        CHR(10) ||
        '내 앞으로 온 편지 두 장.' ||
        CHR(10) ||
        CHR(10) ||
        '준비는 모두 끝난 것 같다.'
 WHEN NOT MATCHED THEN INSERT
      (theme_id, store_id, title, difficulty, play_minutes, min_people, max_people, price, poster_url, booking_url, description, source, status)
      VALUES (sq_theme.NEXTVAL, x.store_id, x.title, 4, 70, 1, 4, 25000,
              'https://point-nine.com/file/theme/14_a.jpg',
              'https://point-nine.com/layout/res/home.php?go=rev.make&s_zizum=1',
              '재회의 반가움도 잠시' ||
        CHR(10) ||
        CHR(10) ||
        '한시도 지체 할 틈이 없다.' ||
        CHR(10) ||
        CHR(10) ||
        '어서 내 임무를 완수 해야한다.' ||
        CHR(10) ||
        CHR(10) ||
        '주머니 속 구겨진 메모 한장과' ||
        CHR(10) ||
        CHR(10) ||
        '내 앞으로 온 편지 두 장.' ||
        CHR(10) ||
        CHR(10) ||
        '준비는 모두 끝난 것 같다.',
              'CRAWL', 'OPEN');
DELETE FROM theme_genre WHERE theme_id = (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00001' AND t.title = 'EP4 : 주인 없는 낡은 서점');
DELETE FROM time_slot WHERE theme_id = (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00001' AND t.title = 'EP4 : 주인 없는 낡은 서점');
INSERT INTO time_slot (slot_id, theme_id, day_type, start_time) VALUES (sq_time_slot.NEXTVAL, (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00001' AND t.title = 'EP4 : 주인 없는 낡은 서점'), 'WEEKDAY', '11:30');
INSERT INTO time_slot (slot_id, theme_id, day_type, start_time) VALUES (sq_time_slot.NEXTVAL, (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00001' AND t.title = 'EP4 : 주인 없는 낡은 서점'), 'WEEKDAY', '13:00');
INSERT INTO time_slot (slot_id, theme_id, day_type, start_time) VALUES (sq_time_slot.NEXTVAL, (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00001' AND t.title = 'EP4 : 주인 없는 낡은 서점'), 'WEEKDAY', '14:30');
INSERT INTO time_slot (slot_id, theme_id, day_type, start_time) VALUES (sq_time_slot.NEXTVAL, (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00001' AND t.title = 'EP4 : 주인 없는 낡은 서점'), 'WEEKDAY', '16:00');
INSERT INTO time_slot (slot_id, theme_id, day_type, start_time) VALUES (sq_time_slot.NEXTVAL, (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00001' AND t.title = 'EP4 : 주인 없는 낡은 서점'), 'WEEKDAY', '17:30');
INSERT INTO time_slot (slot_id, theme_id, day_type, start_time) VALUES (sq_time_slot.NEXTVAL, (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00001' AND t.title = 'EP4 : 주인 없는 낡은 서점'), 'WEEKDAY', '19:00');
INSERT INTO time_slot (slot_id, theme_id, day_type, start_time) VALUES (sq_time_slot.NEXTVAL, (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00001' AND t.title = 'EP4 : 주인 없는 낡은 서점'), 'WEEKDAY', '20:30');
INSERT INTO time_slot (slot_id, theme_id, day_type, start_time) VALUES (sq_time_slot.NEXTVAL, (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00001' AND t.title = 'EP4 : 주인 없는 낡은 서점'), 'WEEKDAY', '22:00');
INSERT INTO time_slot (slot_id, theme_id, day_type, start_time) VALUES (sq_time_slot.NEXTVAL, (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00001' AND t.title = 'EP4 : 주인 없는 낡은 서점'), 'WEEKEND', '11:30');
INSERT INTO time_slot (slot_id, theme_id, day_type, start_time) VALUES (sq_time_slot.NEXTVAL, (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00001' AND t.title = 'EP4 : 주인 없는 낡은 서점'), 'WEEKEND', '13:00');
INSERT INTO time_slot (slot_id, theme_id, day_type, start_time) VALUES (sq_time_slot.NEXTVAL, (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00001' AND t.title = 'EP4 : 주인 없는 낡은 서점'), 'WEEKEND', '14:30');
INSERT INTO time_slot (slot_id, theme_id, day_type, start_time) VALUES (sq_time_slot.NEXTVAL, (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00001' AND t.title = 'EP4 : 주인 없는 낡은 서점'), 'WEEKEND', '16:00');
INSERT INTO time_slot (slot_id, theme_id, day_type, start_time) VALUES (sq_time_slot.NEXTVAL, (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00001' AND t.title = 'EP4 : 주인 없는 낡은 서점'), 'WEEKEND', '17:30');
INSERT INTO time_slot (slot_id, theme_id, day_type, start_time) VALUES (sq_time_slot.NEXTVAL, (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00001' AND t.title = 'EP4 : 주인 없는 낡은 서점'), 'WEEKEND', '19:00');
INSERT INTO time_slot (slot_id, theme_id, day_type, start_time) VALUES (sq_time_slot.NEXTVAL, (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00001' AND t.title = 'EP4 : 주인 없는 낡은 서점'), 'WEEKEND', '20:30');
INSERT INTO time_slot (slot_id, theme_id, day_type, start_time) VALUES (sq_time_slot.NEXTVAL, (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00001' AND t.title = 'EP4 : 주인 없는 낡은 서점'), 'WEEKEND', '22:00');

-- ==================== [포인트나인] 포인트나인 건대점  (https://point-nine.com/layout/res/home.php?go=main)
MERGE INTO store s
USING (SELECT '999-02-00005' AS biz_place_no FROM dual) x
   ON (s.biz_place_no = x.biz_place_no)
 WHEN MATCHED THEN UPDATE SET
      s.name = '포인트나인 건대점', s.area = '건대', s.address = '서울시 광진구 화양동 11-7 3층', s.phone = '070-7537-0869',
      s.website_url = 'https://point-nine.com/layout/res/home.php?go=main', s.hours = '09:50 ~ 다음 날 00:50 (시간표 기준)',
      s.intro = '포인트나인 건대점. 공식 홈페이지에서 모은 정보예요. 예약과 정확한 가격은 매장 홈페이지에서 확인해 주세요.', s.lat = NULL, s.lng = NULL
 WHEN NOT MATCHED THEN INSERT
      (store_id, owner_id, name, area, address, phone, website_url, hours, intro, lat, lng, biz_place_no, status, approved_at)
      VALUES (sq_store.NEXTVAL, NULL, '포인트나인 건대점', '건대', '서울시 광진구 화양동 11-7 3층', '070-7537-0869',
              'https://point-nine.com/layout/res/home.php?go=main', '09:50 ~ 다음 날 00:50 (시간표 기준)',
              '포인트나인 건대점. 공식 홈페이지에서 모은 정보예요. 예약과 정확한 가격은 매장 홈페이지에서 확인해 주세요.', NULL, NULL, x.biz_place_no, 'APPROVED', SYSDATE);

-- 「Jack in the Show」
MERGE INTO theme t
USING (SELECT store_id, 'Jack in the Show' AS title FROM store WHERE biz_place_no = '999-02-00005') x
   ON (t.store_id = x.store_id AND t.title = x.title)
 WHEN MATCHED THEN UPDATE SET
      t.difficulty = 2, t.play_minutes = 75, t.min_people = 2, t.max_people = 4, t.price = 25000,
      t.poster_url = 'https://point-nine.com/file/theme/32_a.jpg',
      t.booking_url = 'https://point-nine.com/layout/res/home.php?go=rev.make&s_zizum=5',
      t.description = '이용등급 : 전체이용가' ||
        CHR(10) ||
        CHR(10) ||
        '이용시간 : 75분' ||
        CHR(10) ||
        CHR(10) ||
        '장르 : 다크 판타지' ||
        CHR(10) ||
        CHR(10) ||
        '공포도 : ★★☆☆☆' ||
        CHR(10) ||
        CHR(10) ||
        '최대인원: 4인' ||
        CHR(10) ||
        CHR(10) ||
        '★예약금( 34,000원 )이 있는 테마입니다! [070문자확인필수!!!]★' ||
        CHR(10) ||
        CHR(10) ||
        '★최소 15분 전, 매장에 도착해 주셔야 하며,' ||
        CHR(10) ||
        CHR(10) ||
        '입장시간(예약시간)이 지날 경우 관람(입장) 자체가 불가합니다.★' ||
        CHR(10) ||
        CHR(10) ||
        '1. 예약 후, 예약문자 받기 (070번호 차단 시 예약 확인 불가)' ||
        CHR(10) ||
        CHR(10) ||
        '- 안내문자가 오지 않는 경우: 홈페이지에서 예약번호로' ||
        CHR(10) ||
        CHR(10) ||
        '예약확인 후 매장 혹은 010-4162-8103으로 연락 꼭! 부탁드립니다.' ||
        CHR(10) ||
        CHR(10) ||
        '[매장에서는 고객님의 070 차단유무 확인이 불가합니다 ㅜㅜ]' ||
        CHR(10) ||
        CHR(10) ||
        '2. 문자 내용 속 계좌로 예약금 입금하기' ||
        CHR(10) ||
        CHR(10) ||
        '- 20분 내 입금필요' ||
        CHR(10) ||
        CHR(10) ||
        '- 예약금 미입금 확인시 예약취소문자 보내드리고 있습니다.' ||
        CHR(10) ||
        CHR(10) ||
        '늦게 문자를 보신다면 예약취소문자 받으시기 전 입금요망' ||
        CHR(10) ||
        CHR(10) ||
        '3. 예약금 확인 후 예약확정문자 ''초대장'' 받기' ||
        CHR(10) ||
        CHR(10) ||
        '- 예약취소 문자 받으신게 아니라면 취소처리 X' ||
        CHR(10) ||
        CHR(10) ||
        '초대장 문자를 기다려주세요. (1~2일 소요)' ||
        CHR(10) ||
        CHR(10) ||
        '- 기존 RETURN, ALBA와 관계 없는 단독 테마입니다.' ||
        CHR(10) ||
        CHR(10) ||
        '- 일반적인 방탈출과 달리, 75분간 펼쳐지는 극의 주인공이 되어서 진행되며, 이용하시는 모두가 이용시간 내에 극의 엔딩까지 즐기실 수 있도록 준비되어 있기 때문에 별도의 난이도는 표기되어 있지 않습니다. (위에 표기된 난이도는 시스템 상 삭제가 어려워 해둔 것이니 무' ||
        '시바랍니다)' ||
        CHR(10) ||
        CHR(10) ||
        '- JACK IN THE SHOW는 주인공인 여러분과 다수의 조연들이 함께 완성해 나가는 극 형식의 테마입니다.' ||
        CHR(10) ||
        CHR(10) ||
        '.' ||
        CHR(10) ||
        CHR(10) ||
        '내 이름은 John.' ||
        CHR(10) ||
        CHR(10) ||
        'JACK IN THE SHOW에' ||
        CHR(10) ||
        CHR(10) ||
        '들어 온지도 어느덧 3년째…' ||
        CHR(10) ||
        CHR(10) ||
        '난 언제쯤 무대에 설 수 있을까?'
 WHEN NOT MATCHED THEN INSERT
      (theme_id, store_id, title, difficulty, play_minutes, min_people, max_people, price, poster_url, booking_url, description, source, status)
      VALUES (sq_theme.NEXTVAL, x.store_id, x.title, 2, 75, 2, 4, 25000,
              'https://point-nine.com/file/theme/32_a.jpg',
              'https://point-nine.com/layout/res/home.php?go=rev.make&s_zizum=5',
              '이용등급 : 전체이용가' ||
        CHR(10) ||
        CHR(10) ||
        '이용시간 : 75분' ||
        CHR(10) ||
        CHR(10) ||
        '장르 : 다크 판타지' ||
        CHR(10) ||
        CHR(10) ||
        '공포도 : ★★☆☆☆' ||
        CHR(10) ||
        CHR(10) ||
        '최대인원: 4인' ||
        CHR(10) ||
        CHR(10) ||
        '★예약금( 34,000원 )이 있는 테마입니다! [070문자확인필수!!!]★' ||
        CHR(10) ||
        CHR(10) ||
        '★최소 15분 전, 매장에 도착해 주셔야 하며,' ||
        CHR(10) ||
        CHR(10) ||
        '입장시간(예약시간)이 지날 경우 관람(입장) 자체가 불가합니다.★' ||
        CHR(10) ||
        CHR(10) ||
        '1. 예약 후, 예약문자 받기 (070번호 차단 시 예약 확인 불가)' ||
        CHR(10) ||
        CHR(10) ||
        '- 안내문자가 오지 않는 경우: 홈페이지에서 예약번호로' ||
        CHR(10) ||
        CHR(10) ||
        '예약확인 후 매장 혹은 010-4162-8103으로 연락 꼭! 부탁드립니다.' ||
        CHR(10) ||
        CHR(10) ||
        '[매장에서는 고객님의 070 차단유무 확인이 불가합니다 ㅜㅜ]' ||
        CHR(10) ||
        CHR(10) ||
        '2. 문자 내용 속 계좌로 예약금 입금하기' ||
        CHR(10) ||
        CHR(10) ||
        '- 20분 내 입금필요' ||
        CHR(10) ||
        CHR(10) ||
        '- 예약금 미입금 확인시 예약취소문자 보내드리고 있습니다.' ||
        CHR(10) ||
        CHR(10) ||
        '늦게 문자를 보신다면 예약취소문자 받으시기 전 입금요망' ||
        CHR(10) ||
        CHR(10) ||
        '3. 예약금 확인 후 예약확정문자 ''초대장'' 받기' ||
        CHR(10) ||
        CHR(10) ||
        '- 예약취소 문자 받으신게 아니라면 취소처리 X' ||
        CHR(10) ||
        CHR(10) ||
        '초대장 문자를 기다려주세요. (1~2일 소요)' ||
        CHR(10) ||
        CHR(10) ||
        '- 기존 RETURN, ALBA와 관계 없는 단독 테마입니다.' ||
        CHR(10) ||
        CHR(10) ||
        '- 일반적인 방탈출과 달리, 75분간 펼쳐지는 극의 주인공이 되어서 진행되며, 이용하시는 모두가 이용시간 내에 극의 엔딩까지 즐기실 수 있도록 준비되어 있기 때문에 별도의 난이도는 표기되어 있지 않습니다. (위에 표기된 난이도는 시스템 상 삭제가 어려워 해둔 것이니 무' ||
        '시바랍니다)' ||
        CHR(10) ||
        CHR(10) ||
        '- JACK IN THE SHOW는 주인공인 여러분과 다수의 조연들이 함께 완성해 나가는 극 형식의 테마입니다.' ||
        CHR(10) ||
        CHR(10) ||
        '.' ||
        CHR(10) ||
        CHR(10) ||
        '내 이름은 John.' ||
        CHR(10) ||
        CHR(10) ||
        'JACK IN THE SHOW에' ||
        CHR(10) ||
        CHR(10) ||
        '들어 온지도 어느덧 3년째…' ||
        CHR(10) ||
        CHR(10) ||
        '난 언제쯤 무대에 설 수 있을까?',
              'CRAWL', 'OPEN');
DELETE FROM theme_genre WHERE theme_id = (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00005' AND t.title = 'Jack in the Show');
INSERT INTO theme_genre (id, theme_id, genre_id)
SELECT sq_theme_genre.NEXTVAL, t.theme_id, g.genre_id FROM theme t, store s, genre g
 WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00005' AND t.title = 'Jack in the Show' AND g.name IN ('판타지');
DELETE FROM time_slot WHERE theme_id = (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00005' AND t.title = 'Jack in the Show');
INSERT INTO time_slot (slot_id, theme_id, day_type, start_time) VALUES (sq_time_slot.NEXTVAL, (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00005' AND t.title = 'Jack in the Show'), 'WEEKDAY', '13:10');
INSERT INTO time_slot (slot_id, theme_id, day_type, start_time) VALUES (sq_time_slot.NEXTVAL, (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00005' AND t.title = 'Jack in the Show'), 'WEEKDAY', '14:30');
INSERT INTO time_slot (slot_id, theme_id, day_type, start_time) VALUES (sq_time_slot.NEXTVAL, (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00005' AND t.title = 'Jack in the Show'), 'WEEKDAY', '15:50');
INSERT INTO time_slot (slot_id, theme_id, day_type, start_time) VALUES (sq_time_slot.NEXTVAL, (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00005' AND t.title = 'Jack in the Show'), 'WEEKDAY', '17:10');
INSERT INTO time_slot (slot_id, theme_id, day_type, start_time) VALUES (sq_time_slot.NEXTVAL, (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00005' AND t.title = 'Jack in the Show'), 'WEEKDAY', '18:30');
INSERT INTO time_slot (slot_id, theme_id, day_type, start_time) VALUES (sq_time_slot.NEXTVAL, (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00005' AND t.title = 'Jack in the Show'), 'WEEKDAY', '19:50');
INSERT INTO time_slot (slot_id, theme_id, day_type, start_time) VALUES (sq_time_slot.NEXTVAL, (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00005' AND t.title = 'Jack in the Show'), 'WEEKDAY', '21:10');
INSERT INTO time_slot (slot_id, theme_id, day_type, start_time) VALUES (sq_time_slot.NEXTVAL, (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00005' AND t.title = 'Jack in the Show'), 'WEEKDAY', '22:30');
INSERT INTO time_slot (slot_id, theme_id, day_type, start_time) VALUES (sq_time_slot.NEXTVAL, (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00005' AND t.title = 'Jack in the Show'), 'WEEKEND', '10:00');
INSERT INTO time_slot (slot_id, theme_id, day_type, start_time) VALUES (sq_time_slot.NEXTVAL, (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00005' AND t.title = 'Jack in the Show'), 'WEEKEND', '10:50');
INSERT INTO time_slot (slot_id, theme_id, day_type, start_time) VALUES (sq_time_slot.NEXTVAL, (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00005' AND t.title = 'Jack in the Show'), 'WEEKEND', '11:40');
INSERT INTO time_slot (slot_id, theme_id, day_type, start_time) VALUES (sq_time_slot.NEXTVAL, (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00005' AND t.title = 'Jack in the Show'), 'WEEKEND', '12:30');
INSERT INTO time_slot (slot_id, theme_id, day_type, start_time) VALUES (sq_time_slot.NEXTVAL, (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00005' AND t.title = 'Jack in the Show'), 'WEEKEND', '13:20');
INSERT INTO time_slot (slot_id, theme_id, day_type, start_time) VALUES (sq_time_slot.NEXTVAL, (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00005' AND t.title = 'Jack in the Show'), 'WEEKEND', '14:10');
INSERT INTO time_slot (slot_id, theme_id, day_type, start_time) VALUES (sq_time_slot.NEXTVAL, (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00005' AND t.title = 'Jack in the Show'), 'WEEKEND', '15:00');
INSERT INTO time_slot (slot_id, theme_id, day_type, start_time) VALUES (sq_time_slot.NEXTVAL, (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00005' AND t.title = 'Jack in the Show'), 'WEEKEND', '15:50');
INSERT INTO time_slot (slot_id, theme_id, day_type, start_time) VALUES (sq_time_slot.NEXTVAL, (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00005' AND t.title = 'Jack in the Show'), 'WEEKEND', '16:40');
INSERT INTO time_slot (slot_id, theme_id, day_type, start_time) VALUES (sq_time_slot.NEXTVAL, (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00005' AND t.title = 'Jack in the Show'), 'WEEKEND', '17:30');
INSERT INTO time_slot (slot_id, theme_id, day_type, start_time) VALUES (sq_time_slot.NEXTVAL, (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00005' AND t.title = 'Jack in the Show'), 'WEEKEND', '18:20');
INSERT INTO time_slot (slot_id, theme_id, day_type, start_time) VALUES (sq_time_slot.NEXTVAL, (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00005' AND t.title = 'Jack in the Show'), 'WEEKEND', '19:10');
INSERT INTO time_slot (slot_id, theme_id, day_type, start_time) VALUES (sq_time_slot.NEXTVAL, (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00005' AND t.title = 'Jack in the Show'), 'WEEKEND', '20:00');
INSERT INTO time_slot (slot_id, theme_id, day_type, start_time) VALUES (sq_time_slot.NEXTVAL, (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00005' AND t.title = 'Jack in the Show'), 'WEEKEND', '20:50');
INSERT INTO time_slot (slot_id, theme_id, day_type, start_time) VALUES (sq_time_slot.NEXTVAL, (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00005' AND t.title = 'Jack in the Show'), 'WEEKEND', '21:40');
INSERT INTO time_slot (slot_id, theme_id, day_type, start_time) VALUES (sq_time_slot.NEXTVAL, (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00005' AND t.title = 'Jack in the Show'), 'WEEKEND', '22:30');
INSERT INTO time_slot (slot_id, theme_id, day_type, start_time) VALUES (sq_time_slot.NEXTVAL, (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00005' AND t.title = 'Jack in the Show'), 'WEEKEND', '23:20');

-- 「RETURN」
MERGE INTO theme t
USING (SELECT store_id, 'RETURN' AS title FROM store WHERE biz_place_no = '999-02-00005') x
   ON (t.store_id = x.store_id AND t.title = x.title)
 WHEN MATCHED THEN UPDATE SET
      t.difficulty = 3, t.play_minutes = 60, t.min_people = 2, t.max_people = 5, t.price = 25000,
      t.poster_url = 'https://point-nine.com/file/theme/33_a.jpg',
      t.booking_url = 'https://point-nine.com/layout/res/home.php?go=rev.make&s_zizum=5',
      t.description = '이용문의 : insta DM @pointnine_gd' ||
        CHR(10) ||
        CHR(10) ||
        '가까운 미래, 화려한 밤, 그리고 빛의 도시.' ||
        CHR(10) ||
        CHR(10) ||
        '이곳에서 마스크 상점을 운영하는 Zho 부부.' ||
        CHR(10) ||
        CHR(10) ||
        '이들은 평소 훌륭한 인품과 좋은 금실로 모든 이웃에게 존경받지만,' ||
        CHR(10) ||
        CHR(10) ||
        '수 년째 생기지 않는 아이로 인해 남모를 걱정을 안고 살아왔다.' ||
        CHR(10) ||
        CHR(10) ||
        '그러던 어느날, 구석진 골목길에 버려진 한 여자아이를 발견한 부부는' ||
        CHR(10) ||
        CHR(10) ||
        '아이의 부모를 찾아주려 꽤 오랜 시간을 애썼으나 뜻대로 되지 않았다.' ||
        CHR(10) ||
        CHR(10) ||
        '그들은 결국 이것을 신의 가호이자 부부의 운명으로 받아들이게 되고,' ||
        CHR(10) ||
        CHR(10) ||
        '누구보다 기쁜 마음으로 아이를 입양하며 부모로서의 책임을 다한다.' ||
        CHR(10) ||
        CHR(10) ||
        '불쌍히 버려졌던 아이는 Kolk라는 새 이름을 가지고 다시 태어나' ||
        CHR(10) ||
        CHR(10) ||
        '부부의 딸로서 여느 가족과 다를 바 없이 평범하게 자랄 수 있었다.' ||
        CHR(10) ||
        CHR(10) ||
        '그러나 최근 들어' ||
        CHR(10) ||
        CHR(10) ||
        '온 세상 곳곳에서 사람들이 사라지기 시작했다.' ||
        CHR(10) ||
        CHR(10) ||
        '알 수 없는 사건들이 오랜 시간 반복되자,' ||
        CHR(10) ||
        CHR(10) ||
        '심상치 않음을 느낀 부부는 Kolk에게 편지 한 통을 남긴다.'
 WHEN NOT MATCHED THEN INSERT
      (theme_id, store_id, title, difficulty, play_minutes, min_people, max_people, price, poster_url, booking_url, description, source, status)
      VALUES (sq_theme.NEXTVAL, x.store_id, x.title, 3, 60, 2, 5, 25000,
              'https://point-nine.com/file/theme/33_a.jpg',
              'https://point-nine.com/layout/res/home.php?go=rev.make&s_zizum=5',
              '이용문의 : insta DM @pointnine_gd' ||
        CHR(10) ||
        CHR(10) ||
        '가까운 미래, 화려한 밤, 그리고 빛의 도시.' ||
        CHR(10) ||
        CHR(10) ||
        '이곳에서 마스크 상점을 운영하는 Zho 부부.' ||
        CHR(10) ||
        CHR(10) ||
        '이들은 평소 훌륭한 인품과 좋은 금실로 모든 이웃에게 존경받지만,' ||
        CHR(10) ||
        CHR(10) ||
        '수 년째 생기지 않는 아이로 인해 남모를 걱정을 안고 살아왔다.' ||
        CHR(10) ||
        CHR(10) ||
        '그러던 어느날, 구석진 골목길에 버려진 한 여자아이를 발견한 부부는' ||
        CHR(10) ||
        CHR(10) ||
        '아이의 부모를 찾아주려 꽤 오랜 시간을 애썼으나 뜻대로 되지 않았다.' ||
        CHR(10) ||
        CHR(10) ||
        '그들은 결국 이것을 신의 가호이자 부부의 운명으로 받아들이게 되고,' ||
        CHR(10) ||
        CHR(10) ||
        '누구보다 기쁜 마음으로 아이를 입양하며 부모로서의 책임을 다한다.' ||
        CHR(10) ||
        CHR(10) ||
        '불쌍히 버려졌던 아이는 Kolk라는 새 이름을 가지고 다시 태어나' ||
        CHR(10) ||
        CHR(10) ||
        '부부의 딸로서 여느 가족과 다를 바 없이 평범하게 자랄 수 있었다.' ||
        CHR(10) ||
        CHR(10) ||
        '그러나 최근 들어' ||
        CHR(10) ||
        CHR(10) ||
        '온 세상 곳곳에서 사람들이 사라지기 시작했다.' ||
        CHR(10) ||
        CHR(10) ||
        '알 수 없는 사건들이 오랜 시간 반복되자,' ||
        CHR(10) ||
        CHR(10) ||
        '심상치 않음을 느낀 부부는 Kolk에게 편지 한 통을 남긴다.',
              'CRAWL', 'OPEN');
DELETE FROM theme_genre WHERE theme_id = (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00005' AND t.title = 'RETURN');
INSERT INTO theme_genre (id, theme_id, genre_id)
SELECT sq_theme_genre.NEXTVAL, t.theme_id, g.genre_id FROM theme t, store s, genre g
 WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00005' AND t.title = 'RETURN' AND g.name IN ('감성');
DELETE FROM time_slot WHERE theme_id = (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00005' AND t.title = 'RETURN');
INSERT INTO time_slot (slot_id, theme_id, day_type, start_time) VALUES (sq_time_slot.NEXTVAL, (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00005' AND t.title = 'RETURN'), 'WEEKDAY', '12:50');
INSERT INTO time_slot (slot_id, theme_id, day_type, start_time) VALUES (sq_time_slot.NEXTVAL, (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00005' AND t.title = 'RETURN'), 'WEEKDAY', '14:00');
INSERT INTO time_slot (slot_id, theme_id, day_type, start_time) VALUES (sq_time_slot.NEXTVAL, (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00005' AND t.title = 'RETURN'), 'WEEKDAY', '15:10');
INSERT INTO time_slot (slot_id, theme_id, day_type, start_time) VALUES (sq_time_slot.NEXTVAL, (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00005' AND t.title = 'RETURN'), 'WEEKDAY', '16:20');
INSERT INTO time_slot (slot_id, theme_id, day_type, start_time) VALUES (sq_time_slot.NEXTVAL, (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00005' AND t.title = 'RETURN'), 'WEEKDAY', '17:40');
INSERT INTO time_slot (slot_id, theme_id, day_type, start_time) VALUES (sq_time_slot.NEXTVAL, (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00005' AND t.title = 'RETURN'), 'WEEKDAY', '18:50');
INSERT INTO time_slot (slot_id, theme_id, day_type, start_time) VALUES (sq_time_slot.NEXTVAL, (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00005' AND t.title = 'RETURN'), 'WEEKDAY', '20:20');
INSERT INTO time_slot (slot_id, theme_id, day_type, start_time) VALUES (sq_time_slot.NEXTVAL, (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00005' AND t.title = 'RETURN'), 'WEEKDAY', '21:30');
INSERT INTO time_slot (slot_id, theme_id, day_type, start_time) VALUES (sq_time_slot.NEXTVAL, (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00005' AND t.title = 'RETURN'), 'WEEKDAY', '22:50');
INSERT INTO time_slot (slot_id, theme_id, day_type, start_time) VALUES (sq_time_slot.NEXTVAL, (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00005' AND t.title = 'RETURN'), 'WEEKEND', '09:50');
INSERT INTO time_slot (slot_id, theme_id, day_type, start_time) VALUES (sq_time_slot.NEXTVAL, (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00005' AND t.title = 'RETURN'), 'WEEKEND', '11:00');
INSERT INTO time_slot (slot_id, theme_id, day_type, start_time) VALUES (sq_time_slot.NEXTVAL, (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00005' AND t.title = 'RETURN'), 'WEEKEND', '12:10');
INSERT INTO time_slot (slot_id, theme_id, day_type, start_time) VALUES (sq_time_slot.NEXTVAL, (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00005' AND t.title = 'RETURN'), 'WEEKEND', '13:20');
INSERT INTO time_slot (slot_id, theme_id, day_type, start_time) VALUES (sq_time_slot.NEXTVAL, (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00005' AND t.title = 'RETURN'), 'WEEKEND', '14:30');
INSERT INTO time_slot (slot_id, theme_id, day_type, start_time) VALUES (sq_time_slot.NEXTVAL, (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00005' AND t.title = 'RETURN'), 'WEEKEND', '15:40');
INSERT INTO time_slot (slot_id, theme_id, day_type, start_time) VALUES (sq_time_slot.NEXTVAL, (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00005' AND t.title = 'RETURN'), 'WEEKEND', '16:50');
INSERT INTO time_slot (slot_id, theme_id, day_type, start_time) VALUES (sq_time_slot.NEXTVAL, (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00005' AND t.title = 'RETURN'), 'WEEKEND', '18:00');
INSERT INTO time_slot (slot_id, theme_id, day_type, start_time) VALUES (sq_time_slot.NEXTVAL, (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00005' AND t.title = 'RETURN'), 'WEEKEND', '19:10');
INSERT INTO time_slot (slot_id, theme_id, day_type, start_time) VALUES (sq_time_slot.NEXTVAL, (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00005' AND t.title = 'RETURN'), 'WEEKEND', '20:20');
INSERT INTO time_slot (slot_id, theme_id, day_type, start_time) VALUES (sq_time_slot.NEXTVAL, (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00005' AND t.title = 'RETURN'), 'WEEKEND', '21:30');
INSERT INTO time_slot (slot_id, theme_id, day_type, start_time) VALUES (sq_time_slot.NEXTVAL, (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00005' AND t.title = 'RETURN'), 'WEEKEND', '22:40');
INSERT INTO time_slot (slot_id, theme_id, day_type, start_time) VALUES (sq_time_slot.NEXTVAL, (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00005' AND t.title = 'RETURN'), 'WEEKEND', '23:50');

-- 「ALBA」
MERGE INTO theme t
USING (SELECT store_id, 'ALBA' AS title FROM store WHERE biz_place_no = '999-02-00005') x
   ON (t.store_id = x.store_id AND t.title = x.title)
 WHEN MATCHED THEN UPDATE SET
      t.difficulty = 4, t.play_minutes = 70, t.min_people = 2, t.max_people = 6, t.price = 25000,
      t.poster_url = 'https://point-nine.com/file/theme/34_a.jpg',
      t.booking_url = 'https://point-nine.com/layout/res/home.php?go=rev.make&s_zizum=5',
      t.description = '이용문의 : insta DM @pointnine_gd /' ||
        CHR(10) ||
        CHR(10) ||
        '빛이 있는 곳엔 어둠도 있는 법.' ||
        CHR(10) ||
        CHR(10) ||
        '구원자의 통치체제에도 반하는 무리가 있었으니' ||
        CHR(10) ||
        CHR(10) ||
        '도심에서 떨어진 지역에 반정부군이 어느새 형성되었다.' ||
        CHR(10) ||
        CHR(10) ||
        '반정부군 소속의 임원 Brian.' ||
        CHR(10) ||
        CHR(10) ||
        '그는 평소에는 도심의 마을에서 의료 목적으로 신체 개조를 해주는' ||
        CHR(10) ||
        CHR(10) ||
        'On & Off Plastic Surgery를 운영하고 있다.' ||
        CHR(10) ||
        CHR(10) ||
        '대외적으로는 정부군과도 협력하며 기술적인 부분을 지원하고 있지만,' ||
        CHR(10) ||
        CHR(10) ||
        '이는 다 첩보활동을 위한 것.' ||
        CHR(10) ||
        CHR(10) ||
        '그는 정부의 여러 비밀정보들을 취합하며 반정부군으로 돌아갈 날을 기다리고 있다.' ||
        CHR(10) ||
        CHR(10) ||
        '그리고 이 가게에서 아르바이트를 하고 있는 Vincent와 Kevin.' ||
        CHR(10) ||
        CHR(10) ||
        '둘은 일을 하다 보니 사장님 Brian이 반정부군 소속임을 알게 되었고,' ||
        CHR(10) ||
        CHR(10) ||
        '반정부군에 들어가고 싶어 넌지시 얘기를 꺼내보지만' ||
        CHR(10) ||
        CHR(10) ||
        '위험한 일이라며 늘 거절당하고 만다.' ||
        CHR(10) ||
        CHR(10) ||
        '오늘도 평소와 다를 것 없는 출근길.' ||
        CHR(10) ||
        CHR(10) ||
        '하지만 웬일인지 사장님에게 연락이 한 통 와있다.'
 WHEN NOT MATCHED THEN INSERT
      (theme_id, store_id, title, difficulty, play_minutes, min_people, max_people, price, poster_url, booking_url, description, source, status)
      VALUES (sq_theme.NEXTVAL, x.store_id, x.title, 4, 70, 2, 6, 25000,
              'https://point-nine.com/file/theme/34_a.jpg',
              'https://point-nine.com/layout/res/home.php?go=rev.make&s_zizum=5',
              '이용문의 : insta DM @pointnine_gd /' ||
        CHR(10) ||
        CHR(10) ||
        '빛이 있는 곳엔 어둠도 있는 법.' ||
        CHR(10) ||
        CHR(10) ||
        '구원자의 통치체제에도 반하는 무리가 있었으니' ||
        CHR(10) ||
        CHR(10) ||
        '도심에서 떨어진 지역에 반정부군이 어느새 형성되었다.' ||
        CHR(10) ||
        CHR(10) ||
        '반정부군 소속의 임원 Brian.' ||
        CHR(10) ||
        CHR(10) ||
        '그는 평소에는 도심의 마을에서 의료 목적으로 신체 개조를 해주는' ||
        CHR(10) ||
        CHR(10) ||
        'On & Off Plastic Surgery를 운영하고 있다.' ||
        CHR(10) ||
        CHR(10) ||
        '대외적으로는 정부군과도 협력하며 기술적인 부분을 지원하고 있지만,' ||
        CHR(10) ||
        CHR(10) ||
        '이는 다 첩보활동을 위한 것.' ||
        CHR(10) ||
        CHR(10) ||
        '그는 정부의 여러 비밀정보들을 취합하며 반정부군으로 돌아갈 날을 기다리고 있다.' ||
        CHR(10) ||
        CHR(10) ||
        '그리고 이 가게에서 아르바이트를 하고 있는 Vincent와 Kevin.' ||
        CHR(10) ||
        CHR(10) ||
        '둘은 일을 하다 보니 사장님 Brian이 반정부군 소속임을 알게 되었고,' ||
        CHR(10) ||
        CHR(10) ||
        '반정부군에 들어가고 싶어 넌지시 얘기를 꺼내보지만' ||
        CHR(10) ||
        CHR(10) ||
        '위험한 일이라며 늘 거절당하고 만다.' ||
        CHR(10) ||
        CHR(10) ||
        '오늘도 평소와 다를 것 없는 출근길.' ||
        CHR(10) ||
        CHR(10) ||
        '하지만 웬일인지 사장님에게 연락이 한 통 와있다.',
              'CRAWL', 'OPEN');
DELETE FROM theme_genre WHERE theme_id = (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00005' AND t.title = 'ALBA');
INSERT INTO theme_genre (id, theme_id, genre_id)
SELECT sq_theme_genre.NEXTVAL, t.theme_id, g.genre_id FROM theme t, store s, genre g
 WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00005' AND t.title = 'ALBA' AND g.name IN ('모험');
DELETE FROM time_slot WHERE theme_id = (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00005' AND t.title = 'ALBA');
INSERT INTO time_slot (slot_id, theme_id, day_type, start_time) VALUES (sq_time_slot.NEXTVAL, (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00005' AND t.title = 'ALBA'), 'WEEKDAY', '13:00');
INSERT INTO time_slot (slot_id, theme_id, day_type, start_time) VALUES (sq_time_slot.NEXTVAL, (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00005' AND t.title = 'ALBA'), 'WEEKDAY', '14:20');
INSERT INTO time_slot (slot_id, theme_id, day_type, start_time) VALUES (sq_time_slot.NEXTVAL, (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00005' AND t.title = 'ALBA'), 'WEEKDAY', '15:40');
INSERT INTO time_slot (slot_id, theme_id, day_type, start_time) VALUES (sq_time_slot.NEXTVAL, (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00005' AND t.title = 'ALBA'), 'WEEKDAY', '17:00');
INSERT INTO time_slot (slot_id, theme_id, day_type, start_time) VALUES (sq_time_slot.NEXTVAL, (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00005' AND t.title = 'ALBA'), 'WEEKDAY', '18:20');
INSERT INTO time_slot (slot_id, theme_id, day_type, start_time) VALUES (sq_time_slot.NEXTVAL, (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00005' AND t.title = 'ALBA'), 'WEEKDAY', '19:40');
INSERT INTO time_slot (slot_id, theme_id, day_type, start_time) VALUES (sq_time_slot.NEXTVAL, (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00005' AND t.title = 'ALBA'), 'WEEKDAY', '21:00');
INSERT INTO time_slot (slot_id, theme_id, day_type, start_time) VALUES (sq_time_slot.NEXTVAL, (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00005' AND t.title = 'ALBA'), 'WEEKDAY', '22:20');
INSERT INTO time_slot (slot_id, theme_id, day_type, start_time) VALUES (sq_time_slot.NEXTVAL, (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00005' AND t.title = 'ALBA'), 'WEEKEND', '10:10');
INSERT INTO time_slot (slot_id, theme_id, day_type, start_time) VALUES (sq_time_slot.NEXTVAL, (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00005' AND t.title = 'ALBA'), 'WEEKEND', '11:30');
INSERT INTO time_slot (slot_id, theme_id, day_type, start_time) VALUES (sq_time_slot.NEXTVAL, (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00005' AND t.title = 'ALBA'), 'WEEKEND', '12:50');
INSERT INTO time_slot (slot_id, theme_id, day_type, start_time) VALUES (sq_time_slot.NEXTVAL, (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00005' AND t.title = 'ALBA'), 'WEEKEND', '14:10');
INSERT INTO time_slot (slot_id, theme_id, day_type, start_time) VALUES (sq_time_slot.NEXTVAL, (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00005' AND t.title = 'ALBA'), 'WEEKEND', '15:30');
INSERT INTO time_slot (slot_id, theme_id, day_type, start_time) VALUES (sq_time_slot.NEXTVAL, (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00005' AND t.title = 'ALBA'), 'WEEKEND', '16:50');
INSERT INTO time_slot (slot_id, theme_id, day_type, start_time) VALUES (sq_time_slot.NEXTVAL, (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00005' AND t.title = 'ALBA'), 'WEEKEND', '18:10');
INSERT INTO time_slot (slot_id, theme_id, day_type, start_time) VALUES (sq_time_slot.NEXTVAL, (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00005' AND t.title = 'ALBA'), 'WEEKEND', '19:30');
INSERT INTO time_slot (slot_id, theme_id, day_type, start_time) VALUES (sq_time_slot.NEXTVAL, (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00005' AND t.title = 'ALBA'), 'WEEKEND', '20:50');
INSERT INTO time_slot (slot_id, theme_id, day_type, start_time) VALUES (sq_time_slot.NEXTVAL, (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00005' AND t.title = 'ALBA'), 'WEEKEND', '22:10');
INSERT INTO time_slot (slot_id, theme_id, day_type, start_time) VALUES (sq_time_slot.NEXTVAL, (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00005' AND t.title = 'ALBA'), 'WEEKEND', '23:30');

-- ==================== [포인트나인] 포인트나인 홍대점  (https://point-nine.com/layout/res/home.php?go=main)
MERGE INTO store s
USING (SELECT '999-02-00006' AS biz_place_no FROM dual) x
   ON (s.biz_place_no = x.biz_place_no)
 WHEN MATCHED THEN UPDATE SET
      s.name = '포인트나인 홍대점', s.area = '홍대', s.address = '서울 마포구 독막로 7길 40, 지하1층', s.phone = '010-2131-0538',
      s.website_url = 'https://point-nine.com/layout/res/home.php?go=main', s.hours = '09:40 ~ 다음 날 01:10 (시간표 기준)',
      s.intro = '포인트나인 홍대점. 공식 홈페이지에서 모은 정보예요. 예약과 정확한 가격은 매장 홈페이지에서 확인해 주세요.', s.lat = NULL, s.lng = NULL
 WHEN NOT MATCHED THEN INSERT
      (store_id, owner_id, name, area, address, phone, website_url, hours, intro, lat, lng, biz_place_no, status, approved_at)
      VALUES (sq_store.NEXTVAL, NULL, '포인트나인 홍대점', '홍대', '서울 마포구 독막로 7길 40, 지하1층', '010-2131-0538',
              'https://point-nine.com/layout/res/home.php?go=main', '09:40 ~ 다음 날 01:10 (시간표 기준)',
              '포인트나인 홍대점. 공식 홈페이지에서 모은 정보예요. 예약과 정확한 가격은 매장 홈페이지에서 확인해 주세요.', NULL, NULL, x.biz_place_no, 'APPROVED', SYSDATE);

-- 「SILENT」
MERGE INTO theme t
USING (SELECT store_id, 'SILENT' AS title FROM store WHERE biz_place_no = '999-02-00006') x
   ON (t.store_id = x.store_id AND t.title = x.title)
 WHEN MATCHED THEN UPDATE SET
      t.difficulty = 3, t.play_minutes = 75, t.min_people = 2, t.max_people = 10, t.price = 25000,
      t.poster_url = 'https://point-nine.com/file/theme/37_a.jpg',
      t.booking_url = 'https://point-nine.com/layout/res/home.php?go=rev.make&s_zizum=6',
      t.description = '나는 보안관이다.' ||
        CHR(10) ||
        CHR(10) ||
        '얼마 전 타지역에 살고 있는 내 친구에게 수상한 편지 한 통을 받았고' ||
        CHR(10) ||
        CHR(10) ||
        '나는 오늘 그 마을을 찾아 가려 한다….' ||
        CHR(10) ||
        CHR(10) ||
        '✓ 몰입이 굉장히 중요한 테마입니다.' ||
        CHR(10) ||
        CHR(10) ||
        '✓ SILENT 배우들의 연출이 들어간 테마입니다.' ||
        CHR(10) ||
        CHR(10) ||
        '✓ 테마 전개에 방해되는 행동은 경고가 주어집니다. 경고 후에도 동일한 행동이 반복될 경우 모든 연출이 삭제됩니다.' ||
        CHR(10) ||
        CHR(10) ||
        '(배우들을 향한 모욕적인 언행, 과도한 장난, 폭력적인 행동, 정해진 룰을 깬 플레이 등)'
 WHEN NOT MATCHED THEN INSERT
      (theme_id, store_id, title, difficulty, play_minutes, min_people, max_people, price, poster_url, booking_url, description, source, status)
      VALUES (sq_theme.NEXTVAL, x.store_id, x.title, 3, 75, 2, 10, 25000,
              'https://point-nine.com/file/theme/37_a.jpg',
              'https://point-nine.com/layout/res/home.php?go=rev.make&s_zizum=6',
              '나는 보안관이다.' ||
        CHR(10) ||
        CHR(10) ||
        '얼마 전 타지역에 살고 있는 내 친구에게 수상한 편지 한 통을 받았고' ||
        CHR(10) ||
        CHR(10) ||
        '나는 오늘 그 마을을 찾아 가려 한다….' ||
        CHR(10) ||
        CHR(10) ||
        '✓ 몰입이 굉장히 중요한 테마입니다.' ||
        CHR(10) ||
        CHR(10) ||
        '✓ SILENT 배우들의 연출이 들어간 테마입니다.' ||
        CHR(10) ||
        CHR(10) ||
        '✓ 테마 전개에 방해되는 행동은 경고가 주어집니다. 경고 후에도 동일한 행동이 반복될 경우 모든 연출이 삭제됩니다.' ||
        CHR(10) ||
        CHR(10) ||
        '(배우들을 향한 모욕적인 언행, 과도한 장난, 폭력적인 행동, 정해진 룰을 깬 플레이 등)',
              'CRAWL', 'OPEN');
DELETE FROM theme_genre WHERE theme_id = (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00006' AND t.title = 'SILENT');
INSERT INTO theme_genre (id, theme_id, genre_id)
SELECT sq_theme_genre.NEXTVAL, t.theme_id, g.genre_id FROM theme t, store s, genre g
 WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00006' AND t.title = 'SILENT' AND g.name IN ('추리', '스릴러');
DELETE FROM time_slot WHERE theme_id = (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00006' AND t.title = 'SILENT');
INSERT INTO time_slot (slot_id, theme_id, day_type, start_time) VALUES (sq_time_slot.NEXTVAL, (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00006' AND t.title = 'SILENT'), 'WEEKDAY', '09:40');
INSERT INTO time_slot (slot_id, theme_id, day_type, start_time) VALUES (sq_time_slot.NEXTVAL, (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00006' AND t.title = 'SILENT'), 'WEEKDAY', '10:30');
INSERT INTO time_slot (slot_id, theme_id, day_type, start_time) VALUES (sq_time_slot.NEXTVAL, (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00006' AND t.title = 'SILENT'), 'WEEKDAY', '11:20');
INSERT INTO time_slot (slot_id, theme_id, day_type, start_time) VALUES (sq_time_slot.NEXTVAL, (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00006' AND t.title = 'SILENT'), 'WEEKDAY', '12:10');
INSERT INTO time_slot (slot_id, theme_id, day_type, start_time) VALUES (sq_time_slot.NEXTVAL, (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00006' AND t.title = 'SILENT'), 'WEEKDAY', '13:00');
INSERT INTO time_slot (slot_id, theme_id, day_type, start_time) VALUES (sq_time_slot.NEXTVAL, (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00006' AND t.title = 'SILENT'), 'WEEKDAY', '13:50');
INSERT INTO time_slot (slot_id, theme_id, day_type, start_time) VALUES (sq_time_slot.NEXTVAL, (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00006' AND t.title = 'SILENT'), 'WEEKDAY', '14:40');
INSERT INTO time_slot (slot_id, theme_id, day_type, start_time) VALUES (sq_time_slot.NEXTVAL, (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00006' AND t.title = 'SILENT'), 'WEEKDAY', '15:30');
INSERT INTO time_slot (slot_id, theme_id, day_type, start_time) VALUES (sq_time_slot.NEXTVAL, (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00006' AND t.title = 'SILENT'), 'WEEKDAY', '16:20');
INSERT INTO time_slot (slot_id, theme_id, day_type, start_time) VALUES (sq_time_slot.NEXTVAL, (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00006' AND t.title = 'SILENT'), 'WEEKDAY', '17:10');
INSERT INTO time_slot (slot_id, theme_id, day_type, start_time) VALUES (sq_time_slot.NEXTVAL, (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00006' AND t.title = 'SILENT'), 'WEEKDAY', '18:00');
INSERT INTO time_slot (slot_id, theme_id, day_type, start_time) VALUES (sq_time_slot.NEXTVAL, (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00006' AND t.title = 'SILENT'), 'WEEKDAY', '18:50');
INSERT INTO time_slot (slot_id, theme_id, day_type, start_time) VALUES (sq_time_slot.NEXTVAL, (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00006' AND t.title = 'SILENT'), 'WEEKDAY', '19:40');
INSERT INTO time_slot (slot_id, theme_id, day_type, start_time) VALUES (sq_time_slot.NEXTVAL, (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00006' AND t.title = 'SILENT'), 'WEEKDAY', '20:30');
INSERT INTO time_slot (slot_id, theme_id, day_type, start_time) VALUES (sq_time_slot.NEXTVAL, (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00006' AND t.title = 'SILENT'), 'WEEKDAY', '21:20');
INSERT INTO time_slot (slot_id, theme_id, day_type, start_time) VALUES (sq_time_slot.NEXTVAL, (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00006' AND t.title = 'SILENT'), 'WEEKDAY', '22:10');
INSERT INTO time_slot (slot_id, theme_id, day_type, start_time) VALUES (sq_time_slot.NEXTVAL, (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00006' AND t.title = 'SILENT'), 'WEEKDAY', '23:00');
INSERT INTO time_slot (slot_id, theme_id, day_type, start_time) VALUES (sq_time_slot.NEXTVAL, (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00006' AND t.title = 'SILENT'), 'WEEKDAY', '23:50');
INSERT INTO time_slot (slot_id, theme_id, day_type, start_time) VALUES (sq_time_slot.NEXTVAL, (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00006' AND t.title = 'SILENT'), 'WEEKEND', '09:40');
INSERT INTO time_slot (slot_id, theme_id, day_type, start_time) VALUES (sq_time_slot.NEXTVAL, (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00006' AND t.title = 'SILENT'), 'WEEKEND', '10:30');
INSERT INTO time_slot (slot_id, theme_id, day_type, start_time) VALUES (sq_time_slot.NEXTVAL, (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00006' AND t.title = 'SILENT'), 'WEEKEND', '11:20');
INSERT INTO time_slot (slot_id, theme_id, day_type, start_time) VALUES (sq_time_slot.NEXTVAL, (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00006' AND t.title = 'SILENT'), 'WEEKEND', '12:10');
INSERT INTO time_slot (slot_id, theme_id, day_type, start_time) VALUES (sq_time_slot.NEXTVAL, (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00006' AND t.title = 'SILENT'), 'WEEKEND', '13:00');
INSERT INTO time_slot (slot_id, theme_id, day_type, start_time) VALUES (sq_time_slot.NEXTVAL, (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00006' AND t.title = 'SILENT'), 'WEEKEND', '13:50');
INSERT INTO time_slot (slot_id, theme_id, day_type, start_time) VALUES (sq_time_slot.NEXTVAL, (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00006' AND t.title = 'SILENT'), 'WEEKEND', '14:40');
INSERT INTO time_slot (slot_id, theme_id, day_type, start_time) VALUES (sq_time_slot.NEXTVAL, (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00006' AND t.title = 'SILENT'), 'WEEKEND', '15:30');
INSERT INTO time_slot (slot_id, theme_id, day_type, start_time) VALUES (sq_time_slot.NEXTVAL, (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00006' AND t.title = 'SILENT'), 'WEEKEND', '16:20');
INSERT INTO time_slot (slot_id, theme_id, day_type, start_time) VALUES (sq_time_slot.NEXTVAL, (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00006' AND t.title = 'SILENT'), 'WEEKEND', '17:10');
INSERT INTO time_slot (slot_id, theme_id, day_type, start_time) VALUES (sq_time_slot.NEXTVAL, (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00006' AND t.title = 'SILENT'), 'WEEKEND', '18:00');
INSERT INTO time_slot (slot_id, theme_id, day_type, start_time) VALUES (sq_time_slot.NEXTVAL, (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00006' AND t.title = 'SILENT'), 'WEEKEND', '18:50');
INSERT INTO time_slot (slot_id, theme_id, day_type, start_time) VALUES (sq_time_slot.NEXTVAL, (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00006' AND t.title = 'SILENT'), 'WEEKEND', '19:40');
INSERT INTO time_slot (slot_id, theme_id, day_type, start_time) VALUES (sq_time_slot.NEXTVAL, (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00006' AND t.title = 'SILENT'), 'WEEKEND', '20:30');
INSERT INTO time_slot (slot_id, theme_id, day_type, start_time) VALUES (sq_time_slot.NEXTVAL, (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00006' AND t.title = 'SILENT'), 'WEEKEND', '21:20');
INSERT INTO time_slot (slot_id, theme_id, day_type, start_time) VALUES (sq_time_slot.NEXTVAL, (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00006' AND t.title = 'SILENT'), 'WEEKEND', '22:10');
INSERT INTO time_slot (slot_id, theme_id, day_type, start_time) VALUES (sq_time_slot.NEXTVAL, (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00006' AND t.title = 'SILENT'), 'WEEKEND', '23:00');
INSERT INTO time_slot (slot_id, theme_id, day_type, start_time) VALUES (sq_time_slot.NEXTVAL, (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00006' AND t.title = 'SILENT'), 'WEEKEND', '23:50');

-- 「LISTEN」
MERGE INTO theme t
USING (SELECT store_id, 'LISTEN' AS title FROM store WHERE biz_place_no = '999-02-00006') x
   ON (t.store_id = x.store_id AND t.title = x.title)
 WHEN MATCHED THEN UPDATE SET
      t.difficulty = 3, t.play_minutes = 75, t.min_people = 2, t.max_people = 10, t.price = 25000,
      t.poster_url = 'https://point-nine.com/file/theme/38_a.jpg',
      t.booking_url = 'https://point-nine.com/layout/res/home.php?go=rev.make&s_zizum=6',
      t.description = '정신을 차려보니 감옥 같은 창고였다.' ||
        CHR(10) ||
        CHR(10) ||
        '✓ 몰입이 굉장히 중요한 테마입니다.' ||
        CHR(10) ||
        CHR(10) ||
        '✓ LISTEN 배우들의 연출이 들어간 테마입니다.' ||
        CHR(10) ||
        CHR(10) ||
        '✓ 테마 전개에 방해되는 행동은 경고가 주어집니다. 경고 후에도 동일한 행동이 반복될 경우 모든 연출이 삭제됩니다.' ||
        CHR(10) ||
        CHR(10) ||
        '(배우들을 향한 모욕적인 언행, 과도한 장난, 폭력적인 행동, 정해진 룰을 깬 플레이 등)'
 WHEN NOT MATCHED THEN INSERT
      (theme_id, store_id, title, difficulty, play_minutes, min_people, max_people, price, poster_url, booking_url, description, source, status)
      VALUES (sq_theme.NEXTVAL, x.store_id, x.title, 3, 75, 2, 10, 25000,
              'https://point-nine.com/file/theme/38_a.jpg',
              'https://point-nine.com/layout/res/home.php?go=rev.make&s_zizum=6',
              '정신을 차려보니 감옥 같은 창고였다.' ||
        CHR(10) ||
        CHR(10) ||
        '✓ 몰입이 굉장히 중요한 테마입니다.' ||
        CHR(10) ||
        CHR(10) ||
        '✓ LISTEN 배우들의 연출이 들어간 테마입니다.' ||
        CHR(10) ||
        CHR(10) ||
        '✓ 테마 전개에 방해되는 행동은 경고가 주어집니다. 경고 후에도 동일한 행동이 반복될 경우 모든 연출이 삭제됩니다.' ||
        CHR(10) ||
        CHR(10) ||
        '(배우들을 향한 모욕적인 언행, 과도한 장난, 폭력적인 행동, 정해진 룰을 깬 플레이 등)',
              'CRAWL', 'OPEN');
DELETE FROM theme_genre WHERE theme_id = (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00006' AND t.title = 'LISTEN');
INSERT INTO theme_genre (id, theme_id, genre_id)
SELECT sq_theme_genre.NEXTVAL, t.theme_id, g.genre_id FROM theme t, store s, genre g
 WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00006' AND t.title = 'LISTEN' AND g.name IN ('스릴러');
DELETE FROM time_slot WHERE theme_id = (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00006' AND t.title = 'LISTEN');
INSERT INTO time_slot (slot_id, theme_id, day_type, start_time) VALUES (sq_time_slot.NEXTVAL, (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00006' AND t.title = 'LISTEN'), 'WEEKDAY', '10:00');
INSERT INTO time_slot (slot_id, theme_id, day_type, start_time) VALUES (sq_time_slot.NEXTVAL, (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00006' AND t.title = 'LISTEN'), 'WEEKDAY', '10:50');
INSERT INTO time_slot (slot_id, theme_id, day_type, start_time) VALUES (sq_time_slot.NEXTVAL, (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00006' AND t.title = 'LISTEN'), 'WEEKDAY', '11:40');
INSERT INTO time_slot (slot_id, theme_id, day_type, start_time) VALUES (sq_time_slot.NEXTVAL, (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00006' AND t.title = 'LISTEN'), 'WEEKDAY', '12:30');
INSERT INTO time_slot (slot_id, theme_id, day_type, start_time) VALUES (sq_time_slot.NEXTVAL, (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00006' AND t.title = 'LISTEN'), 'WEEKDAY', '13:20');
INSERT INTO time_slot (slot_id, theme_id, day_type, start_time) VALUES (sq_time_slot.NEXTVAL, (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00006' AND t.title = 'LISTEN'), 'WEEKDAY', '14:10');
INSERT INTO time_slot (slot_id, theme_id, day_type, start_time) VALUES (sq_time_slot.NEXTVAL, (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00006' AND t.title = 'LISTEN'), 'WEEKDAY', '15:00');
INSERT INTO time_slot (slot_id, theme_id, day_type, start_time) VALUES (sq_time_slot.NEXTVAL, (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00006' AND t.title = 'LISTEN'), 'WEEKDAY', '15:50');
INSERT INTO time_slot (slot_id, theme_id, day_type, start_time) VALUES (sq_time_slot.NEXTVAL, (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00006' AND t.title = 'LISTEN'), 'WEEKDAY', '16:40');
INSERT INTO time_slot (slot_id, theme_id, day_type, start_time) VALUES (sq_time_slot.NEXTVAL, (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00006' AND t.title = 'LISTEN'), 'WEEKDAY', '17:30');
INSERT INTO time_slot (slot_id, theme_id, day_type, start_time) VALUES (sq_time_slot.NEXTVAL, (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00006' AND t.title = 'LISTEN'), 'WEEKDAY', '18:20');
INSERT INTO time_slot (slot_id, theme_id, day_type, start_time) VALUES (sq_time_slot.NEXTVAL, (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00006' AND t.title = 'LISTEN'), 'WEEKDAY', '19:10');
INSERT INTO time_slot (slot_id, theme_id, day_type, start_time) VALUES (sq_time_slot.NEXTVAL, (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00006' AND t.title = 'LISTEN'), 'WEEKDAY', '20:00');
INSERT INTO time_slot (slot_id, theme_id, day_type, start_time) VALUES (sq_time_slot.NEXTVAL, (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00006' AND t.title = 'LISTEN'), 'WEEKDAY', '20:50');
INSERT INTO time_slot (slot_id, theme_id, day_type, start_time) VALUES (sq_time_slot.NEXTVAL, (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00006' AND t.title = 'LISTEN'), 'WEEKDAY', '21:40');
INSERT INTO time_slot (slot_id, theme_id, day_type, start_time) VALUES (sq_time_slot.NEXTVAL, (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00006' AND t.title = 'LISTEN'), 'WEEKDAY', '22:30');
INSERT INTO time_slot (slot_id, theme_id, day_type, start_time) VALUES (sq_time_slot.NEXTVAL, (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00006' AND t.title = 'LISTEN'), 'WEEKDAY', '23:10');
INSERT INTO time_slot (slot_id, theme_id, day_type, start_time) VALUES (sq_time_slot.NEXTVAL, (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00006' AND t.title = 'LISTEN'), 'WEEKDAY', '23:55');
INSERT INTO time_slot (slot_id, theme_id, day_type, start_time) VALUES (sq_time_slot.NEXTVAL, (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00006' AND t.title = 'LISTEN'), 'WEEKEND', '10:00');
INSERT INTO time_slot (slot_id, theme_id, day_type, start_time) VALUES (sq_time_slot.NEXTVAL, (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00006' AND t.title = 'LISTEN'), 'WEEKEND', '10:50');
INSERT INTO time_slot (slot_id, theme_id, day_type, start_time) VALUES (sq_time_slot.NEXTVAL, (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00006' AND t.title = 'LISTEN'), 'WEEKEND', '11:40');
INSERT INTO time_slot (slot_id, theme_id, day_type, start_time) VALUES (sq_time_slot.NEXTVAL, (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00006' AND t.title = 'LISTEN'), 'WEEKEND', '12:30');
INSERT INTO time_slot (slot_id, theme_id, day_type, start_time) VALUES (sq_time_slot.NEXTVAL, (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00006' AND t.title = 'LISTEN'), 'WEEKEND', '13:20');
INSERT INTO time_slot (slot_id, theme_id, day_type, start_time) VALUES (sq_time_slot.NEXTVAL, (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00006' AND t.title = 'LISTEN'), 'WEEKEND', '14:10');
INSERT INTO time_slot (slot_id, theme_id, day_type, start_time) VALUES (sq_time_slot.NEXTVAL, (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00006' AND t.title = 'LISTEN'), 'WEEKEND', '15:00');
INSERT INTO time_slot (slot_id, theme_id, day_type, start_time) VALUES (sq_time_slot.NEXTVAL, (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00006' AND t.title = 'LISTEN'), 'WEEKEND', '15:50');
INSERT INTO time_slot (slot_id, theme_id, day_type, start_time) VALUES (sq_time_slot.NEXTVAL, (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00006' AND t.title = 'LISTEN'), 'WEEKEND', '16:40');
INSERT INTO time_slot (slot_id, theme_id, day_type, start_time) VALUES (sq_time_slot.NEXTVAL, (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00006' AND t.title = 'LISTEN'), 'WEEKEND', '17:30');
INSERT INTO time_slot (slot_id, theme_id, day_type, start_time) VALUES (sq_time_slot.NEXTVAL, (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00006' AND t.title = 'LISTEN'), 'WEEKEND', '18:20');
INSERT INTO time_slot (slot_id, theme_id, day_type, start_time) VALUES (sq_time_slot.NEXTVAL, (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00006' AND t.title = 'LISTEN'), 'WEEKEND', '19:10');
INSERT INTO time_slot (slot_id, theme_id, day_type, start_time) VALUES (sq_time_slot.NEXTVAL, (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00006' AND t.title = 'LISTEN'), 'WEEKEND', '20:00');
INSERT INTO time_slot (slot_id, theme_id, day_type, start_time) VALUES (sq_time_slot.NEXTVAL, (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00006' AND t.title = 'LISTEN'), 'WEEKEND', '20:50');
INSERT INTO time_slot (slot_id, theme_id, day_type, start_time) VALUES (sq_time_slot.NEXTVAL, (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00006' AND t.title = 'LISTEN'), 'WEEKEND', '21:40');
INSERT INTO time_slot (slot_id, theme_id, day_type, start_time) VALUES (sq_time_slot.NEXTVAL, (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00006' AND t.title = 'LISTEN'), 'WEEKEND', '22:30');
INSERT INTO time_slot (slot_id, theme_id, day_type, start_time) VALUES (sq_time_slot.NEXTVAL, (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00006' AND t.title = 'LISTEN'), 'WEEKEND', '23:10');
INSERT INTO time_slot (slot_id, theme_id, day_type, start_time) VALUES (sq_time_slot.NEXTVAL, (SELECT t.theme_id FROM theme t, store s WHERE t.store_id = s.store_id AND s.biz_place_no = '999-02-00006' AND t.title = 'LISTEN'), 'WEEKEND', '23:55');

COMMIT;

-- 확인용 : 크롤링으로 들어온 매장과 테마 수
SELECT s.store_id, s.name, s.area, COUNT(t.theme_id) AS themes
  FROM store s LEFT JOIN theme t ON t.store_id = s.store_id
 WHERE s.biz_place_no LIKE '999-%'
 GROUP BY s.store_id, s.name, s.area
 ORDER BY s.store_id;

-- 크롤링 데이터만 지우기 (찜·후기·파티가 달린 테마가 있으면 그것부터 지워야 함)
-- DELETE FROM time_slot   WHERE theme_id IN (SELECT theme_id FROM theme WHERE store_id IN (SELECT store_id FROM store WHERE biz_place_no LIKE '999-%'));
-- DELETE FROM theme_genre WHERE theme_id IN (SELECT theme_id FROM theme WHERE store_id IN (SELECT store_id FROM store WHERE biz_place_no LIKE '999-%'));
-- DELETE FROM theme       WHERE store_id IN (SELECT store_id FROM store WHERE biz_place_no LIKE '999-%');
-- DELETE FROM store       WHERE biz_place_no LIKE '999-%';
-- COMMIT;

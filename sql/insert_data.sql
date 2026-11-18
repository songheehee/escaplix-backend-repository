-- 방탈출 통합예약 (Escaplix) : 기본 데이터 v3 (hr 계정에서 실행)
-- 테이블 생성 v3 스크립트를 실행한 "직후"에 실행 (빈 테이블 기준)
-- SQL Developer 에서 스크립트 실행(F5)
--  * PK 는 숫자를 직접 넣고, 맨 끝에서 시퀀스를 MAX(id)+1 부터 다시 만든다
--  * 파티 날짜는 SYSDATE 기준이라 언제 실행해도 "앞으로의 파티"가 된다
--  * 별점 집계(theme.review_count / rating_sum)는 트리거가 채우므로 넣지 않는다
--  * 모든 계정 비밀번호 : 1234 (BCrypt $2a$10)


-- 1. 회원 (관리자 1, 사장 3, 손님 6)
INSERT INTO member (member_id, login_id, password, nickname, role) VALUES
  (1, 'admin', '$2a$10$OXTaeyMDsM96N/wANCYIKO9DJBc7lgHpJk.WvM14CJmF7EnUD7q6C', '관리자', 'ADMIN');

INSERT INTO member (member_id, login_id, password, nickname, role, biz_no, biz_file_path) VALUES
  (2, 'owner1', '$2a$10$OXTaeyMDsM96N/wANCYIKO9DJBc7lgHpJk.WvM14CJmF7EnUD7q6C', '미로사장', 'OWNER', '101-11-11111', '/private/biz/owner1.pdf');
INSERT INTO member (member_id, login_id, password, nickname, role, biz_no, biz_file_path) VALUES
  (3, 'owner2', '$2a$10$OXTaeyMDsM96N/wANCYIKO9DJBc7lgHpJk.WvM14CJmF7EnUD7q6C', '타임락사장', 'OWNER', '202-22-22222', '/private/biz/owner2.pdf');
INSERT INTO member (member_id, login_id, password, nickname, role, biz_no, biz_file_path) VALUES
  (4, 'owner3', '$2a$10$OXTaeyMDsM96N/wANCYIKO9DJBc7lgHpJk.WvM14CJmF7EnUD7q6C', '퍼즐사장', 'OWNER', '303-33-33333', '/private/biz/owner3.pdf');

INSERT INTO member (member_id, login_id, password, nickname, role) VALUES (5,  'user1', '$2a$10$OXTaeyMDsM96N/wANCYIKO9DJBc7lgHpJk.WvM14CJmF7EnUD7q6C', '탈출왕',     'USER');
INSERT INTO member (member_id, login_id, password, nickname, role) VALUES (6,  'user2', '$2a$10$OXTaeyMDsM96N/wANCYIKO9DJBc7lgHpJk.WvM14CJmF7EnUD7q6C', '겁쟁이토끼', 'USER');
INSERT INTO member (member_id, login_id, password, nickname, role) VALUES (7,  'user3', '$2a$10$OXTaeyMDsM96N/wANCYIKO9DJBc7lgHpJk.WvM14CJmF7EnUD7q6C', '추리덕후',   'USER');
INSERT INTO member (member_id, login_id, password, nickname, role) VALUES (8,  'user4', '$2a$10$OXTaeyMDsM96N/wANCYIKO9DJBc7lgHpJk.WvM14CJmF7EnUD7q6C', '방린이',     'USER');
INSERT INTO member (member_id, login_id, password, nickname, role) VALUES (9,  'user5', '$2a$10$OXTaeyMDsM96N/wANCYIKO9DJBc7lgHpJk.WvM14CJmF7EnUD7q6C', '힌트요정',   'USER');
INSERT INTO member (member_id, login_id, password, nickname, role) VALUES (10, 'user6', '$2a$10$OXTaeyMDsM96N/wANCYIKO9DJBc7lgHpJk.WvM14CJmF7EnUD7q6C', '노힌트장인', 'USER');


-- 2. 장르
INSERT INTO genre (genre_id, name) VALUES (1, '공포');
INSERT INTO genre (genre_id, name) VALUES (2, '추리');
INSERT INTO genre (genre_id, name) VALUES (3, '스릴러');
INSERT INTO genre (genre_id, name) VALUES (4, '감성');
INSERT INTO genre (genre_id, name) VALUES (5, '코믹');
INSERT INTO genre (genre_id, name) VALUES (6, '판타지');
INSERT INTO genre (genre_id, name) VALUES (7, 'SF');
INSERT INTO genre (genre_id, name) VALUES (8, '모험');


-- 3. 공휴일 (2026 남은 날 + 2027 전체, 대체공휴일 포함)
INSERT INTO holiday (holiday_date, name) VALUES (DATE '2026-12-25', '성탄절');
INSERT INTO holiday (holiday_date, name) VALUES (DATE '2027-01-01', '신정');
INSERT INTO holiday (holiday_date, name) VALUES (DATE '2027-02-06', '설날 연휴');
INSERT INTO holiday (holiday_date, name) VALUES (DATE '2027-02-07', '설날');
INSERT INTO holiday (holiday_date, name) VALUES (DATE '2027-02-08', '설날 연휴');
INSERT INTO holiday (holiday_date, name) VALUES (DATE '2027-02-09', '설날 대체공휴일');
INSERT INTO holiday (holiday_date, name) VALUES (DATE '2027-03-01', '삼일절');
INSERT INTO holiday (holiday_date, name) VALUES (DATE '2027-05-01', '노동절');
INSERT INTO holiday (holiday_date, name) VALUES (DATE '2027-05-03', '노동절 대체공휴일');
INSERT INTO holiday (holiday_date, name) VALUES (DATE '2027-05-05', '어린이날');
INSERT INTO holiday (holiday_date, name) VALUES (DATE '2027-05-13', '부처님오신날');
INSERT INTO holiday (holiday_date, name) VALUES (DATE '2027-06-06', '현충일');
INSERT INTO holiday (holiday_date, name) VALUES (DATE '2027-07-17', '제헌절');
INSERT INTO holiday (holiday_date, name) VALUES (DATE '2027-07-19', '제헌절 대체공휴일');
INSERT INTO holiday (holiday_date, name) VALUES (DATE '2027-08-15', '광복절');
INSERT INTO holiday (holiday_date, name) VALUES (DATE '2027-08-16', '광복절 대체공휴일');
INSERT INTO holiday (holiday_date, name) VALUES (DATE '2027-09-14', '추석 연휴');
INSERT INTO holiday (holiday_date, name) VALUES (DATE '2027-09-15', '추석');
INSERT INTO holiday (holiday_date, name) VALUES (DATE '2027-09-16', '추석 연휴');
INSERT INTO holiday (holiday_date, name) VALUES (DATE '2027-10-03', '개천절');
INSERT INTO holiday (holiday_date, name) VALUES (DATE '2027-10-04', '개천절 대체공휴일');
INSERT INTO holiday (holiday_date, name) VALUES (DATE '2027-10-09', '한글날');
INSERT INTO holiday (holiday_date, name) VALUES (DATE '2027-10-11', '한글날 대체공휴일');
INSERT INTO holiday (holiday_date, name) VALUES (DATE '2027-12-25', '성탄절');
INSERT INTO holiday (holiday_date, name) VALUES (DATE '2027-12-27', '성탄절 대체공휴일');


-- 4. 매장 (승인 4 : 사장 3 + 관리자 직접 등록 1 / 대기 1 / 반려 1)
INSERT INTO store (store_id, owner_id, name, area, address, phone, website_url, hours, intro, photo_url, lat, lng, biz_place_no, biz_place_file_path, status) VALUES
  (1, 2, '미로이스케이프 홍대점', '홍대', '서울 마포구 와우산로 00, 지하 1층', '02-111-1111', 'https://miro-escape.example.com',
   '10:00 ~ 23:00 (연중무휴)', '홍대입구역 9번 출구 도보 5분. 공포와 추리 테마 전문 매장입니다.', '/uploads/stores/common-01.jpg', 37.556100, 126.923500, '110-81-00001', '/private/store/1.pdf', 'APPROVED');
INSERT INTO store (store_id, owner_id, name, area, address, phone, website_url, hours, intro, photo_url, lat, lng, biz_place_no, biz_place_file_path, status) VALUES
  (2, 3, '타임락 강남점', '강남', '서울 강남구 테헤란로 00, 3층', '02-222-2222', 'https://timelock.example.com',
   '11:00 ~ 24:00 (월요일 휴무)', '강남역 11번 출구 바로 앞. 고난도 테마가 많은 매장입니다.', '/uploads/stores/common-05.jpg', 37.498000, 127.027600, '120-81-00002', '/private/store/2.pdf', 'APPROVED');
INSERT INTO store (store_id, owner_id, name, area, address, phone, website_url, hours, intro, photo_url, lat, lng, biz_place_no, biz_place_file_path, status) VALUES
  (3, 4, '퍼즐하우스 건대점', '건대', '서울 광진구 아차산로 00, 2층', '02-333-3333', 'https://puzzlehouse.example.com',
   '12:00 ~ 23:00 (연중무휴)', '건대입구역 2번 출구 도보 3분. 감성 스토리와 SF 테마를 운영합니다.', '/uploads/stores/common-06.jpg', 37.540400, 127.069200, '130-81-00003', '/private/store/3.pdf', 'APPROVED');
-- 관리자가 직접 등록한 매장 (owner_id NULL, 사업장등록증 없음)
INSERT INTO store (store_id, owner_id, name, area, address, phone, website_url, hours, intro, photo_url, lat, lng, biz_place_no, biz_place_file_path, status) VALUES
  (4, NULL, '비밀의방 신촌점', '신촌', '서울 서대문구 연세로 00, 4층', '02-444-4444', 'https://secretroom.example.com',
   '13:00 ~ 22:00 (화요일 휴무)', '신촌역 3번 출구 도보 7분. 처음 하는 분도 즐기기 좋은 매장입니다.', '/uploads/stores/common-04.jpg', 37.555900, 126.936900, '140-81-00004', NULL, 'APPROVED');
-- 승인 대기
INSERT INTO store (store_id, owner_id, name, area, address, phone, website_url, hours, intro, photo_url, lat, lng, biz_place_no, biz_place_file_path, status) VALUES
  (5, 2, '미로이스케이프 강남점', '강남', '서울 강남구 강남대로 00, 5층', '02-555-5555', 'https://miro-escape.example.com/gangnam',
   '10:00 ~ 23:00 (연중무휴)', '미로이스케이프 2호점입니다. 12월 오픈 예정입니다.', '/uploads/stores/common-07.jpg', 37.501200, 127.025900, '150-81-00005', '/private/store/5.pdf', 'PENDING');
-- 반려
INSERT INTO store (store_id, owner_id, name, area, address, phone, website_url, hours, intro, photo_url, lat, lng, biz_place_no, biz_place_file_path, status, reject_reason) VALUES
  (6, 4, '퍼즐하우스 신촌점', '신촌', '서울 서대문구 명물길 00, 2층', '02-666-6666', 'https://puzzlehouse.example.com/sinchon',
   '12:00 ~ 23:00 (연중무휴)', '퍼즐하우스 2호점입니다.', '/uploads/stores/common-03.jpg', 37.557500, 126.938000, '160-81-00006', '/private/store/6.pdf', 'REJECTED',
   '사업장등록증의 주소와 입력한 주소가 다릅니다. 확인 후 다시 신청해 주세요.');


-- 5. 테마 (OPEN 7 / CLOSED 1 / PENDING 1 / REJECTED 1)
INSERT INTO theme (theme_id, store_id, title, description, difficulty, play_minutes, min_people, max_people, price, poster_url, booking_url, source, status) VALUES
  (1, 1, '저주받은 인형의 집', '버려진 인형 공방에 갇혔다. 인형들이 움직이기 전에 빠져나가야 한다.', 4, 60, 2, 4, 23000,
   '/uploads/posters/horror-03.jpg', 'https://miro-escape.example.com/book/1', 'OWNER', 'OPEN');
INSERT INTO theme (theme_id, store_id, title, description, difficulty, play_minutes, min_people, max_people, price, poster_url, booking_url, source, status) VALUES
  (2, 1, '사라진 탐정', '실종된 명탐정의 사무실. 그가 남긴 단서로 마지막 사건을 해결하라.', 3, 70, 2, 5, 22000,
   '/uploads/posters/mystery-01.jpg', 'https://miro-escape.example.com/book/2', 'OWNER', 'OPEN');
INSERT INTO theme (theme_id, store_id, title, description, difficulty, play_minutes, min_people, max_people, price, poster_url, booking_url, source, status) VALUES
  (3, 2, '자정의 병동', '폐쇄된 정신병원 3층. 자정이 되기 전에 탈출하지 못하면 영영 나갈 수 없다.', 5, 75, 2, 4, 25000,
   '/uploads/posters/horror-01.jpg', 'https://timelock.example.com/book/3', 'OWNER', 'OPEN');
INSERT INTO theme (theme_id, store_id, title, description, difficulty, play_minutes, min_people, max_people, price, poster_url, booking_url, source, status) VALUES
  (4, 2, '고양이 탐정 사무소', '생선 도둑을 잡아라! 고양이 탐정과 함께하는 유쾌한 추리.', 2, 60, 2, 6, 20000,
   '/uploads/posters/comic-05.jpg', 'https://timelock.example.com/book/4', 'OWNER', 'OPEN');
INSERT INTO theme (theme_id, store_id, title, description, difficulty, play_minutes, min_people, max_people, price, poster_url, booking_url, source, status) VALUES
  (5, 3, '별을 삼킨 소년', '밤하늘에서 별이 하나씩 사라진다. 소년의 기억을 따라가는 감성 테마.', 3, 70, 2, 4, 22000,
   '/uploads/posters/emotional-07.jpg', 'https://puzzlehouse.example.com/book/5', 'OWNER', 'OPEN');
INSERT INTO theme (theme_id, store_id, title, description, difficulty, play_minutes, min_people, max_people, price, poster_url, booking_url, source, status) VALUES
  (6, 3, '우주정거장 탈출', '산소가 새고 있다. 80분 안에 탈출선을 가동하라.', 4, 80, 3, 6, 24000,
   '/uploads/posters/sf-01.jpg', 'https://puzzlehouse.example.com/book/6', 'OWNER', 'OPEN');
INSERT INTO theme (theme_id, store_id, title, description, difficulty, play_minutes, min_people, max_people, price, poster_url, booking_url, source, status) VALUES
  (7, 4, '해적왕의 보물', '전설의 해적선에 숨겨진 보물 지도를 찾아라. 입문자 추천.', 2, 60, 2, 5, 21000,
   '/uploads/posters/adventure-04.jpg', 'https://secretroom.example.com/book/7', 'ADMIN', 'OPEN');
-- 운영 종료 (크롤링으로 들어온 테마)
INSERT INTO theme (theme_id, store_id, title, description, difficulty, play_minutes, min_people, max_people, price, poster_url, booking_url, source, status) VALUES
  (8, 4, '마지막 기차', '종착역까지 남은 시간 60분. 기차에서 만난 사람들의 이야기.', 3, 60, 2, 4, 21000,
   '/uploads/posters/emotional-05.jpg', 'https://secretroom.example.com/book/8', 'CRAWL', 'CLOSED');
-- 승인 대기
INSERT INTO theme (theme_id, store_id, title, description, difficulty, play_minutes, min_people, max_people, price, poster_url, booking_url, source, status) VALUES
  (9, 1, '심야 라디오', '새벽 3시, 라디오에서 내 이름이 불렸다.', 3, 70, 2, 4, 23000,
   '/uploads/posters/mystery-06.jpg', 'https://miro-escape.example.com/book/9', 'OWNER', 'PENDING');
-- 반려
INSERT INTO theme (theme_id, store_id, title, description, difficulty, play_minutes, min_people, max_people, price, poster_url, booking_url, source, status, reject_reason) VALUES
  (10, 3, '연구소 B-13', '봉인된 연구소에서 실험체가 탈출했다.', 4, 70, 2, 4, 23000,
   '/uploads/posters/sf-06.jpg', 'https://puzzlehouse.example.com/book/10', 'OWNER', 'REJECTED',
   '테마 사진이 다른 매장 이미지와 같습니다. 직접 찍은 사진으로 다시 올려 주세요.');


-- 6. 테마-장르
INSERT INTO theme_genre (id, theme_id, genre_id) VALUES (1,  1, 1);
INSERT INTO theme_genre (id, theme_id, genre_id) VALUES (2,  2, 2);
INSERT INTO theme_genre (id, theme_id, genre_id) VALUES (3,  3, 1);
INSERT INTO theme_genre (id, theme_id, genre_id) VALUES (4,  3, 3);
INSERT INTO theme_genre (id, theme_id, genre_id) VALUES (5,  4, 5);
INSERT INTO theme_genre (id, theme_id, genre_id) VALUES (6,  4, 2);
INSERT INTO theme_genre (id, theme_id, genre_id) VALUES (7,  5, 4);
INSERT INTO theme_genre (id, theme_id, genre_id) VALUES (8,  5, 6);
INSERT INTO theme_genre (id, theme_id, genre_id) VALUES (9,  6, 7);
INSERT INTO theme_genre (id, theme_id, genre_id) VALUES (10, 6, 8);
INSERT INTO theme_genre (id, theme_id, genre_id) VALUES (11, 7, 8);
INSERT INTO theme_genre (id, theme_id, genre_id) VALUES (12, 7, 5);
INSERT INTO theme_genre (id, theme_id, genre_id) VALUES (13, 8, 4);
INSERT INTO theme_genre (id, theme_id, genre_id) VALUES (14, 9, 3);
INSERT INTO theme_genre (id, theme_id, genre_id) VALUES (15, 10, 7);


-- 7. 시간표 : OPEN 테마 7개 × (주중 4 + 주말 6) = 70줄
--    17:00 / 19:00 / 21:00 은 주중·주말 모두 있음 → 파티는 이 시각으로 만든다
INSERT INTO time_slot (slot_id, theme_id, day_type, start_time)
SELECT ROW_NUMBER() OVER (ORDER BY t.theme_id, s.day_type, s.start_time),
       t.theme_id, s.day_type, s.start_time
  FROM theme t
 CROSS JOIN (
        SELECT 'WEEKDAY' AS day_type, '15:00' AS start_time FROM dual
        UNION ALL SELECT 'WEEKDAY', '17:00' FROM dual
        UNION ALL SELECT 'WEEKDAY', '19:00' FROM dual
        UNION ALL SELECT 'WEEKDAY', '21:00' FROM dual
        UNION ALL SELECT 'WEEKEND', '11:00' FROM dual
        UNION ALL SELECT 'WEEKEND', '13:00' FROM dual
        UNION ALL SELECT 'WEEKEND', '15:00' FROM dual
        UNION ALL SELECT 'WEEKEND', '17:00' FROM dual
        UNION ALL SELECT 'WEEKEND', '19:00' FROM dual
        UNION ALL SELECT 'WEEKEND', '21:00' FROM dual
       ) s
 WHERE t.status = 'OPEN';


-- 8. 선호 장르 (손님만)
INSERT INTO member_genre (id, member_id, genre_id) VALUES (1,  5, 1);
INSERT INTO member_genre (id, member_id, genre_id) VALUES (2,  5, 3);
INSERT INTO member_genre (id, member_id, genre_id) VALUES (3,  6, 4);
INSERT INTO member_genre (id, member_id, genre_id) VALUES (4,  6, 5);
INSERT INTO member_genre (id, member_id, genre_id) VALUES (5,  7, 2);
INSERT INTO member_genre (id, member_id, genre_id) VALUES (6,  7, 3);
INSERT INTO member_genre (id, member_id, genre_id) VALUES (7,  8, 5);
INSERT INTO member_genre (id, member_id, genre_id) VALUES (8,  8, 8);
INSERT INTO member_genre (id, member_id, genre_id) VALUES (9,  9, 6);
INSERT INTO member_genre (id, member_id, genre_id) VALUES (10, 9, 4);
INSERT INTO member_genre (id, member_id, genre_id) VALUES (11, 10, 1);
INSERT INTO member_genre (id, member_id, genre_id) VALUES (12, 10, 7);


-- 9. 찜
INSERT INTO wishlist (wish_id, member_id, theme_id) VALUES (1, 5, 1);
INSERT INTO wishlist (wish_id, member_id, theme_id) VALUES (2, 5, 3);
INSERT INTO wishlist (wish_id, member_id, theme_id) VALUES (3, 6, 5);
INSERT INTO wishlist (wish_id, member_id, theme_id) VALUES (4, 7, 2);
INSERT INTO wishlist (wish_id, member_id, theme_id) VALUES (5, 7, 4);
INSERT INTO wishlist (wish_id, member_id, theme_id) VALUES (6, 8, 7);
INSERT INTO wishlist (wish_id, member_id, theme_id) VALUES (7, 9, 6);
INSERT INTO wishlist (wish_id, member_id, theme_id) VALUES (8, 10, 3);


-- 10. 후기 (VISIBLE 13 / HIDDEN 1). 실패(N)면 remain_seconds = 0
INSERT INTO review (review_id, member_id, theme_id, rating, escaped, hint_count, player_count, remain_seconds, played_date, content) VALUES
  (1, 5, 1, 5, 'Y', 1, 2, 754, TRUNC(SYSDATE) - 20, '분위기가 정말 무서워요. 인형 연출이 최고였습니다.');
INSERT INTO review (review_id, member_id, theme_id, rating, escaped, hint_count, player_count, remain_seconds, played_date, content) VALUES
  (2, 7, 1, 4, 'Y', 2, 3, 180, TRUNC(SYSDATE) - 15, '문제 흐름이 깔끔해요. 공포 연출은 생각보다 순한 편.');
INSERT INTO review (review_id, member_id, theme_id, rating, escaped, hint_count, player_count, remain_seconds, played_date, content) VALUES
  (3, 10, 1, 3, 'N', 4, 2, 0, TRUNC(SYSDATE) - 10, '마지막 방이 너무 어려웠어요. 그래도 재밌었습니다.');
INSERT INTO review (review_id, member_id, theme_id, rating, escaped, hint_count, player_count, remain_seconds, played_date, content) VALUES
  (4, 7, 2, 5, 'Y', 0, 2, 1265, TRUNC(SYSDATE) - 30, '추리 좋아하면 무조건 추천. 노힌트 성공!');
INSERT INTO review (review_id, member_id, theme_id, rating, escaped, hint_count, player_count, remain_seconds, played_date, content) VALUES
  (5, 6, 2, 4, 'Y', 3, 4, 300, TRUNC(SYSDATE) - 12, '4명이 딱 적당했어요.');
INSERT INTO review (review_id, member_id, theme_id, rating, escaped, hint_count, player_count, remain_seconds, played_date, content) VALUES
  (6, 5, 3, 5, 'Y', 2, 2, 95, TRUNC(SYSDATE) - 8, '1분 35초 남기고 겨우 탈출. 난이도 5 인정합니다.');
INSERT INTO review (review_id, member_id, theme_id, rating, escaped, hint_count, player_count, remain_seconds, played_date, content) VALUES
  (7, 8, 3, 2, 'N', 5, 3, 0, TRUNC(SYSDATE) - 5, '무서워서 아무것도 못 했어요. 방린이는 비추.');
INSERT INTO review (review_id, member_id, theme_id, rating, escaped, hint_count, player_count, remain_seconds, played_date, content) VALUES
  (8, 6, 5, 5, 'Y', 1, 2, 600, TRUNC(SYSDATE) - 25, '마지막에 울었어요. 스토리가 정말 좋아요.');
INSERT INTO review (review_id, member_id, theme_id, rating, escaped, hint_count, player_count, remain_seconds, played_date, content) VALUES
  (9, 9, 5, 4, 'Y', 2, 2, 420, TRUNC(SYSDATE) - 3, '인테리어가 예뻐요. 문제는 무난한 편.');
INSERT INTO review (review_id, member_id, theme_id, rating, escaped, hint_count, player_count, remain_seconds, played_date, content) VALUES
  (10, 10, 6, 4, 'Y', 1, 4, 1500, TRUNC(SYSDATE) - 18, '장치가 많아서 4명 이상 추천합니다.');
INSERT INTO review (review_id, member_id, theme_id, rating, escaped, hint_count, player_count, remain_seconds, played_date, content) VALUES
  (11, 8, 7, 5, 'Y', 3, 5, 900, TRUNC(SYSDATE) - 7, '첫 방탈출이었는데 너무 재밌었어요!');
INSERT INTO review (review_id, member_id, theme_id, rating, escaped, hint_count, player_count, remain_seconds, played_date, content) VALUES
  (12, 5, 4, 4, 'Y', 1, 2, 800, TRUNC(SYSDATE) - 2, '가볍게 웃으면서 하기 좋아요.');
INSERT INTO review (review_id, member_id, theme_id, rating, escaped, hint_count, player_count, remain_seconds, played_date, content) VALUES
  (13, 9, 8, 4, 'Y', 2, 2, 300, TRUNC(SYSDATE) - 200, '운영 종료라니 아쉽네요.');
-- 신고 처리로 숨긴 후기 (트리거 집계에서 빠짐)
INSERT INTO review (review_id, member_id, theme_id, rating, escaped, hint_count, player_count, remain_seconds, played_date, content, status) VALUES
  (14, 9, 4, 1, 'N', 6, 2, 0, TRUNC(SYSDATE) - 1, '여기 말고 OO역 다른 매장이 훨씬 좋아요. 링크 참고하세요.', 'HIDDEN');


-- 11. 후기 신고
INSERT INTO report (report_id, review_id, reporter_id, reason, status) VALUES
  (1, 14, 5, '후기가 아니라 다른 매장 광고예요.', 'DONE');
INSERT INTO report (report_id, review_id, reporter_id, reason, status) VALUES
  (2, 14, 7, '홍보 글입니다.', 'DONE');
INSERT INTO report (report_id, review_id, reporter_id, reason, status) VALUES
  (3, 7, 10, '테마와 상관없는 비방처럼 보여요.', 'WAIT');


-- 12. 파티 (모집 중 2 / 정원 참 1 / 마감 1 / 삭제 1)
--     play_at 은 17:00 / 19:00 / 21:00 중 하나 (주중·주말 시간표 모두에 있는 시각)
INSERT INTO party (party_id, theme_id, leader_id, title, intro, play_at, deadline_at, max_people, status, created_at) VALUES
  (1, 1, 5, '저주받은 인형의 집 같이 가실 분', '공포 좋아하시는 분 환영! 쫄보도 괜찮아요',
   TRUNC(SYSDATE) + 5 + 19/24, TRUNC(SYSDATE) + 4 + 21/24, 4, 'RECRUITING', SYSDATE - 2);
INSERT INTO party (party_id, theme_id, leader_id, title, intro, play_at, deadline_at, max_people, status, created_at) VALUES
  (2, 3, 7, '자정의 병동 3인 파티', '난이도 5라 경험자 위주로 모아요',
   TRUNC(SYSDATE) + 7 + 19/24, TRUNC(SYSDATE) + 6 + 21/24, 3, 'FULL', SYSDATE - 3);
INSERT INTO party (party_id, theme_id, leader_id, title, intro, play_at, deadline_at, max_people, status, created_at) VALUES
  (3, 6, 9, '우주정거장 탈출 6인 모집', '장치 많은 테마라 인원 많을수록 좋아요',
   TRUNC(SYSDATE) + 10 + 17/24, TRUNC(SYSDATE) + 9 + 21/24, 6, 'RECRUITING', SYSDATE - 1);
INSERT INTO party (party_id, theme_id, leader_id, title, intro, play_at, deadline_at, max_people, status, created_at) VALUES
  (4, 2, 6, '사라진 탐정 퇴근 후 한 판', '추리 초보 환영합니다',
   TRUNC(SYSDATE) - 3 + 19/24, TRUNC(SYSDATE) - 4 + 21/24, 4, 'CLOSED', SYSDATE - 8);
INSERT INTO party (party_id, theme_id, leader_id, title, intro, play_at, deadline_at, max_people, status, created_at) VALUES
  (5, 4, 8, '고양이 탐정 가볍게 하실 분', '일정이 생겨서 취소했어요',
   TRUNC(SYSDATE) + 4 + 21/24, TRUNC(SYSDATE) + 3 + 21/24, 4, 'DELETED', SYSDATE - 4);


-- 13. 파티 참여자 (방장 포함. 3번 파티에 강퇴 1명)
INSERT INTO party_member (id, party_id, member_id, status, joined_at) VALUES (1,  1, 5,  'JOINED', SYSDATE - 2);
INSERT INTO party_member (id, party_id, member_id, status, joined_at) VALUES (2,  1, 6,  'JOINED', SYSDATE - 1);
INSERT INTO party_member (id, party_id, member_id, status, joined_at) VALUES (3,  2, 7,  'JOINED', SYSDATE - 3);
INSERT INTO party_member (id, party_id, member_id, status, joined_at) VALUES (4,  2, 5,  'JOINED', SYSDATE - 2.5);
INSERT INTO party_member (id, party_id, member_id, status, joined_at) VALUES (5,  2, 10, 'JOINED', SYSDATE - 2);
INSERT INTO party_member (id, party_id, member_id, status, joined_at) VALUES (6,  3, 9,  'JOINED', SYSDATE - 1);
INSERT INTO party_member (id, party_id, member_id, status, joined_at) VALUES (7,  3, 8,  'JOINED', SYSDATE - 0.8);
INSERT INTO party_member (id, party_id, member_id, status, joined_at) VALUES (8,  3, 6,  'KICKED', SYSDATE - 0.6);
INSERT INTO party_member (id, party_id, member_id, status, joined_at) VALUES (9,  4, 6,  'JOINED', SYSDATE - 8);
INSERT INTO party_member (id, party_id, member_id, status, joined_at) VALUES (10, 4, 7,  'JOINED', SYSDATE - 7);
INSERT INTO party_member (id, party_id, member_id, status, joined_at) VALUES (11, 5, 8,  'JOINED', SYSDATE - 4);


-- 14. 파티 채팅 (안내 줄 JOIN / KICK 은 content NULL)
INSERT INTO party_message (message_id, party_id, member_id, msg_type, content, created_at) VALUES (1,  1, 5,  'CHAT', '안녕하세요! 예약은 제가 할게요', SYSDATE - 1.9);
INSERT INTO party_message (message_id, party_id, member_id, msg_type, content, created_at) VALUES (2,  1, 6,  'JOIN', NULL, SYSDATE - 1);
INSERT INTO party_message (message_id, party_id, member_id, msg_type, content, created_at) VALUES (3,  1, 6,  'CHAT', '잘 부탁드려요~ 공포는 처음이에요', SYSDATE - 0.9);
INSERT INTO party_message (message_id, party_id, member_id, msg_type, content, created_at) VALUES (4,  2, 5,  'JOIN', NULL, SYSDATE - 2.5);
INSERT INTO party_message (message_id, party_id, member_id, msg_type, content, created_at) VALUES (5,  2, 10, 'JOIN', NULL, SYSDATE - 2);
INSERT INTO party_message (message_id, party_id, member_id, msg_type, content, created_at) VALUES (6,  2, 7,  'CHAT', '인원 다 찼어요! 당일 10분 전에 매장 앞에서 봬요', SYSDATE - 1.9);
INSERT INTO party_message (message_id, party_id, member_id, msg_type, content, created_at) VALUES (7,  3, 8,  'JOIN', NULL, SYSDATE - 0.8);
INSERT INTO party_message (message_id, party_id, member_id, msg_type, content, created_at) VALUES (8,  3, 6,  'JOIN', NULL, SYSDATE - 0.6);
INSERT INTO party_message (message_id, party_id, member_id, msg_type, content, created_at) VALUES (9,  3, 6,  'CHAT', '저는 그냥 구경만 할게요', SYSDATE - 0.55);
INSERT INTO party_message (message_id, party_id, member_id, msg_type, content, created_at) VALUES (10, 3, 6,  'KICK', NULL, SYSDATE - 0.5);
INSERT INTO party_message (message_id, party_id, member_id, msg_type, content, created_at) VALUES (11, 3, 9,  'CHAT', '두 분 더 오시면 바로 예약할게요', SYSDATE - 0.4);
INSERT INTO party_message (message_id, party_id, member_id, msg_type, content, created_at) VALUES (12, 4, 7,  'JOIN', NULL, SYSDATE - 7);
INSERT INTO party_message (message_id, party_id, member_id, msg_type, content, created_at) VALUES (13, 4, 7,  'CHAT', '오늘 재밌었어요! 다음에 또 해요', SYSDATE - 3);

COMMIT;


-- 15. 시퀀스를 MAX(id)+1 부터 다시 만들기 (앱에서 새로 넣을 때 PK 충돌 방지)
DECLARE
    PROCEDURE reset_seq(p_seq VARCHAR2, p_table VARCHAR2, p_col VARCHAR2) IS
        v_next NUMBER;
    BEGIN
        EXECUTE IMMEDIATE 'SELECT NVL(MAX(' || p_col || '), 0) + 1 FROM ' || p_table INTO v_next;
        EXECUTE IMMEDIATE 'DROP SEQUENCE ' || p_seq;
        EXECUTE IMMEDIATE 'CREATE SEQUENCE ' || p_seq || ' START WITH ' || v_next;
    END;
BEGIN
    reset_seq('sq_member',        'member',        'member_id');
    reset_seq('sq_genre',         'genre',         'genre_id');
    reset_seq('sq_store',         'store',         'store_id');
    reset_seq('sq_theme',         'theme',         'theme_id');
    reset_seq('sq_theme_genre',   'theme_genre',   'id');
    reset_seq('sq_time_slot',     'time_slot',     'slot_id');
    reset_seq('sq_wishlist',      'wishlist',      'wish_id');
    reset_seq('sq_review',        'review',        'review_id');
    reset_seq('sq_report',        'report',        'report_id');
    reset_seq('sq_member_genre',  'member_genre',  'id');
    reset_seq('sq_party',         'party',         'party_id');
    reset_seq('sq_party_member',  'party_member',  'id');
    reset_seq('sq_party_message', 'party_message', 'message_id');
END;
/


-- 16. 확인용
-- 테이블별 줄 수 : member 10, genre 8, holiday 25, store 6, theme 10, theme_genre 15, time_slot 70,
--                  member_genre 12, wishlist 8, review 14, report 3, party 5, party_member 11, party_message 13
SELECT 'member' t, COUNT(*) cnt FROM member UNION ALL
SELECT 'store', COUNT(*) FROM store UNION ALL
SELECT 'theme', COUNT(*) FROM theme UNION ALL
SELECT 'time_slot', COUNT(*) FROM time_slot UNION ALL
SELECT 'review', COUNT(*) FROM review UNION ALL
SELECT 'party', COUNT(*) FROM party UNION ALL
SELECT 'party_member', COUNT(*) FROM party_member;

-- 트리거 집계 확인 (기대값 : 1번 3건/12, 2번 2건/9, 3번 2건/7, 4번 1건/4, 5번 2건/9, 6번 1건/4, 7번 1건/5, 8번 1건/4)
SELECT theme_id, title, review_count, rating_sum,
       ROUND(rating_sum / NULLIF(review_count, 0), 1) AS avg_rating
  FROM theme
 ORDER BY theme_id;

-- 파티 현재 인원 (기대값 : 1번 2/4, 2번 3/3, 3번 2/6, 4번 2/4, 5번 1/4)
SELECT p.party_id, p.status, COUNT(pm.id) AS joined, p.max_people
  FROM party p
  LEFT JOIN party_member pm ON pm.party_id = p.party_id AND pm.status = 'JOINED'
 GROUP BY p.party_id, p.status, p.max_people
 ORDER BY p.party_id;

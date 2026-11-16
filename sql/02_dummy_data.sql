-- 방탈출 통합예약 (Escaplix) : 더미 데이터 (01_create_tables.sql 실행 직후에 실행)
--  * 시퀀스가 1부터 시작한다고 보고 FK 번호(1, 2, 3 ...)를 직접 적었다
--    → 01 을 다시 실행해서 시퀀스를 새로 만든 다음에 실행해야 번호가 맞음
--  * 회원 비밀번호는 전부 1234 (BCrypt 해시로 저장. 로그인 테스트할 때 1234 입력)
--  * 시간표·파티 날짜는 SYSDATE 기준이라 언제 실행해도 "내일, 모레 ..." 데이터가 생김
--  * 매장·테마 이름은 전부 지어낸 것. 예약 링크는 example.com (가짜 주소)
--  * 맨 끝에 COMMIT 이 있어야 다른 접속(스프링 서버)에서도 보임

-- 1. 회원 : 1 관리자 / 2~3 사장(점주) / 4~7 손님 / 8 정지된 손님 / 9 사장(본사)
INSERT INTO member (member_id, login_id, password, nickname, role, owner_type, status) VALUES (sq_member.NEXTVAL, 'admin',  '$2a$10$sQuB3Ns0/P2weZA1bJYasO2DtPkHsavuoInHwDd1lplcDjpwfmQQS', '관리자',         'ADMIN', NULL,    'ACTIVE');
INSERT INTO member (member_id, login_id, password, nickname, role, owner_type, status) VALUES (sq_member.NEXTVAL, 'owner1', '$2a$10$sQuB3Ns0/P2weZA1bJYasO2DtPkHsavuoInHwDd1lplcDjpwfmQQS', '미스터리룸홍대', 'OWNER', 'STORE', 'ACTIVE');
INSERT INTO member (member_id, login_id, password, nickname, role, owner_type, status) VALUES (sq_member.NEXTVAL, 'owner2', '$2a$10$sQuB3Ns0/P2weZA1bJYasO2DtPkHsavuoInHwDd1lplcDjpwfmQQS', '열쇠공방사장',   'OWNER', 'STORE', 'ACTIVE');
INSERT INTO member (member_id, login_id, password, nickname, role, owner_type, status) VALUES (sq_member.NEXTVAL, 'user1',  '$2a$10$sQuB3Ns0/P2weZA1bJYasO2DtPkHsavuoInHwDd1lplcDjpwfmQQS', '탈출왕',         'USER',  NULL,    'ACTIVE');
INSERT INTO member (member_id, login_id, password, nickname, role, owner_type, status) VALUES (sq_member.NEXTVAL, 'user2',  '$2a$10$sQuB3Ns0/P2weZA1bJYasO2DtPkHsavuoInHwDd1lplcDjpwfmQQS', '힌트없이',       'USER',  NULL,    'ACTIVE');
INSERT INTO member (member_id, login_id, password, nickname, role, owner_type, status) VALUES (sq_member.NEXTVAL, 'user3',  '$2a$10$sQuB3Ns0/P2weZA1bJYasO2DtPkHsavuoInHwDd1lplcDjpwfmQQS', '쫄보',           'USER',  NULL,    'ACTIVE');
INSERT INTO member (member_id, login_id, password, nickname, role, owner_type, status) VALUES (sq_member.NEXTVAL, 'user4',  '$2a$10$sQuB3Ns0/P2weZA1bJYasO2DtPkHsavuoInHwDd1lplcDjpwfmQQS', '방린이',         'USER',  NULL,    'ACTIVE');
INSERT INTO member (member_id, login_id, password, nickname, role, owner_type, status) VALUES (sq_member.NEXTVAL, 'user5',  '$2a$10$sQuB3Ns0/P2weZA1bJYasO2DtPkHsavuoInHwDd1lplcDjpwfmQQS', '정지된회원',     'USER',  NULL,    'BANNED');
INSERT INTO member (member_id, login_id, password, nickname, role, owner_type, status) VALUES (sq_member.NEXTVAL, 'hq1',    '$2a$10$sQuB3Ns0/P2weZA1bJYasO2DtPkHsavuoInHwDd1lplcDjpwfmQQS', '미스터리룸본사', 'OWNER', 'HQ',    'ACTIVE');

-- 2. 장르 : 1 공포 / 2 추리 / 3 감성 / 4 코믹 / 5 어드벤처 / 6 SF / 7 잠입
INSERT INTO genre (genre_id, name) VALUES (sq_genre.NEXTVAL, '공포');
INSERT INTO genre (genre_id, name) VALUES (sq_genre.NEXTVAL, '추리');
INSERT INTO genre (genre_id, name) VALUES (sq_genre.NEXTVAL, '감성');
INSERT INTO genre (genre_id, name) VALUES (sq_genre.NEXTVAL, '코믹');
INSERT INTO genre (genre_id, name) VALUES (sq_genre.NEXTVAL, '어드벤처');
INSERT INTO genre (genre_id, name) VALUES (sq_genre.NEXTVAL, 'SF');
INSERT INTO genre (genre_id, name) VALUES (sq_genre.NEXTVAL, '잠입');

-- 3. 브랜드와 매장
--    브랜드 1 '미스터리룸' (본사 9번) → 매장 1 (점주 2번), 매장 2 (점주 없음, 본사가 관리)
--    매장 3~4 '열쇠공방' : 일반 매장, 점주 3번이 두 곳 운영
--    매장 5 '어둠상자'  : 크롤링으로 들어온 일반 매장 (점주 없음)
INSERT INTO brand (brand_id, name, hq_id) VALUES (sq_brand.NEXTVAL, '미스터리룸', 9);

INSERT INTO store (store_id, brand_id, owner_id, name, area, address, lat, lng, phone) VALUES (sq_store.NEXTVAL, 1,    2,    '미스터리룸 홍대점', '홍대', '서울 마포구 (예시 주소) 1층', 37.555100, 126.922000, '02-000-0001');
INSERT INTO store (store_id, brand_id, owner_id, name, area, address, lat, lng, phone) VALUES (sq_store.NEXTVAL, 1,    NULL, '미스터리룸 강남점', '강남', '서울 강남구 (예시 주소) 2층', 37.497900, 127.027600, '02-000-0002');
INSERT INTO store (store_id, brand_id, owner_id, name, area, address, lat, lng, phone) VALUES (sq_store.NEXTVAL, NULL, 3,    '열쇠공방 건대점',   '건대', '서울 광진구 (예시 주소) 3층', 37.540400, 127.069200, '02-000-0003');
INSERT INTO store (store_id, brand_id, owner_id, name, area, address, lat, lng, phone) VALUES (sq_store.NEXTVAL, NULL, 3,    '열쇠공방 신촌점',   '신촌', '서울 서대문구 (예시 주소) 지하 1층', 37.559800, 126.942500, '02-000-0004');
INSERT INTO store (store_id, brand_id, owner_id, name, area, address, lat, lng, phone) VALUES (sq_store.NEXTVAL, NULL, NULL, '어둠상자 홍대점',   '홍대', '서울 마포구 (예시 주소) 지하 2층', 37.556300, 126.923600, '02-000-0005');

-- 4. 테마 22개
--    1~20 : 운영 중(OPEN). 단 12번은 운영 종료(CLOSED), 17~20번은 크롤링(CRAWL)
--    21 : 승인 대기(PENDING), 22 : 반려(REJECTED) → 관리자 승인 화면 테스트용
--    컬럼 순서 : 번호, 매장, 이름, 소개, 난이도, 제한시간, 최소인원, 최대인원, 가격, 예약링크, 출처, 상태
INSERT INTO theme (theme_id, store_id, title, description, difficulty, play_minutes, min_people, max_people, price, booking_url, source, status) VALUES (sq_theme.NEXTVAL, 1, '저주받은 인형의 집', '밤마다 자리를 바꾸는 인형들. 인형이 숨긴 단서를 찾아 집을 빠져나가세요.', 4, 70, 2, 4, 25000, 'https://example.com/booking/1', 'OWNER', 'OPEN');
INSERT INTO theme (theme_id, store_id, title, description, difficulty, play_minutes, min_people, max_people, price, booking_url, source, status) VALUES (sq_theme.NEXTVAL, 1, '13번째 배심원', '모두가 유죄라고 할 때, 당신만 다른 증거를 봤습니다.', 3, 60, 2, 5, 23000, 'https://example.com/booking/2', 'OWNER', 'OPEN');
INSERT INTO theme (theme_id, store_id, title, description, difficulty, play_minutes, min_people, max_people, price, booking_url, source, status) VALUES (sq_theme.NEXTVAL, 1, '할머니의 다락방', '할머니가 남긴 상자 속 물건들로 오래된 약속을 찾아가는 이야기.', 2, 60, 2, 4, 22000, 'https://example.com/booking/3', 'OWNER', 'OPEN');
INSERT INTO theme (theme_id, store_id, title, description, difficulty, play_minutes, min_people, max_people, price, booking_url, source, status) VALUES (sq_theme.NEXTVAL, 1, '좀비 연구소', '봉쇄된 연구소. 해독제를 찾기 전에 문이 열리면 끝입니다.', 5, 80, 3, 6, 27000, 'https://example.com/booking/4', 'OWNER', 'OPEN');
INSERT INTO theme (theme_id, store_id, title, description, difficulty, play_minutes, min_people, max_people, price, booking_url, source, status) VALUES (sq_theme.NEXTVAL, 2, '우주정거장 탈출', '산소가 줄어드는 정거장에서 탈출선을 다시 켜세요.', 3, 70, 2, 5, 26000, 'https://example.com/booking/5', 'OWNER', 'OPEN');
INSERT INTO theme (theme_id, store_id, title, description, difficulty, play_minutes, min_people, max_people, price, booking_url, source, status) VALUES (sq_theme.NEXTVAL, 2, '사라진 기억', '눈을 떠 보니 낯선 방. 내가 누구인지부터 알아내야 합니다.', 3, 60, 2, 4, 24000, 'https://example.com/booking/6', 'OWNER', 'OPEN');
INSERT INTO theme (theme_id, store_id, title, description, difficulty, play_minutes, min_people, max_people, price, booking_url, source, status) VALUES (sq_theme.NEXTVAL, 2, '웃음 연구소', '웃음을 잃은 박사를 웃겨야 문이 열립니다. 처음 오는 분께 추천.', 1, 50, 2, 6, 20000, 'https://example.com/booking/7', 'OWNER', 'OPEN');
INSERT INTO theme (theme_id, store_id, title, description, difficulty, play_minutes, min_people, max_people, price, booking_url, source, status) VALUES (sq_theme.NEXTVAL, 2, '금고털이', '경비가 교대하는 70분 동안 금고를 열고 나와야 합니다.', 4, 70, 2, 4, 25000, 'https://example.com/booking/8', 'OWNER', 'OPEN');
INSERT INTO theme (theme_id, store_id, title, description, difficulty, play_minutes, min_people, max_people, price, booking_url, source, status) VALUES (sq_theme.NEXTVAL, 3, '폐병원 304호', '문 닫은 병원의 304호에서만 불이 켜집니다.', 5, 75, 2, 4, 26000, 'https://example.com/booking/9', 'OWNER', 'OPEN');
INSERT INTO theme (theme_id, store_id, title, description, difficulty, play_minutes, min_people, max_people, price, booking_url, source, status) VALUES (sq_theme.NEXTVAL, 3, '명탐정의 마지막 사건', '은퇴한 명탐정이 풀지 못한 마지막 사건을 이어서 풉니다.', 4, 70, 2, 5, 24000, 'https://example.com/booking/10', 'OWNER', 'OPEN');
INSERT INTO theme (theme_id, store_id, title, description, difficulty, play_minutes, min_people, max_people, price, booking_url, source, status) VALUES (sq_theme.NEXTVAL, 3, '시간여행자의 편지', '미래의 내가 보낸 편지. 오늘 안에 답장을 보내야 합니다.', 2, 60, 2, 3, 22000, 'https://example.com/booking/11', 'OWNER', 'OPEN');
INSERT INTO theme (theme_id, store_id, title, description, difficulty, play_minutes, min_people, max_people, price, booking_url, source, status) VALUES (sq_theme.NEXTVAL, 3, '해적선의 보물', '흔들리는 해적선에서 보물 지도를 완성하세요.', 2, 60, 3, 6, 21000, 'https://example.com/booking/12', 'OWNER', 'CLOSED');
INSERT INTO theme (theme_id, store_id, title, description, difficulty, play_minutes, min_people, max_people, price, booking_url, source, status) VALUES (sq_theme.NEXTVAL, 4, '미술관 도난 사건', '전시 마지막 날 밤, 진품과 가품을 바꿔치기해야 합니다.', 3, 60, 2, 4, 23000, 'https://example.com/booking/13', 'OWNER', 'OPEN');
INSERT INTO theme (theme_id, store_id, title, description, difficulty, play_minutes, min_people, max_people, price, booking_url, source, status) VALUES (sq_theme.NEXTVAL, 4, '학교 괴담', '야간 자율학습 중 갑자기 꺼진 불. 무섭지만 웃긴 학교 이야기.', 3, 60, 2, 5, 22000, 'https://example.com/booking/14', 'OWNER', 'OPEN');
INSERT INTO theme (theme_id, store_id, title, description, difficulty, play_minutes, min_people, max_people, price, booking_url, source, status) VALUES (sq_theme.NEXTVAL, 4, '마지막 비행', '기장이 사라진 비행기. 착륙까지 남은 시간은 70분.', 4, 70, 2, 4, 25000, 'https://example.com/booking/15', 'OWNER', 'OPEN');
INSERT INTO theme (theme_id, store_id, title, description, difficulty, play_minutes, min_people, max_people, price, booking_url, source, status) VALUES (sq_theme.NEXTVAL, 4, '첫사랑 우체국', '10년 전 보내지 못한 편지를 찾는 2인 전용 테마.', 1, 50, 2, 2, 22000, 'https://example.com/booking/16', 'OWNER', 'OPEN');
INSERT INTO theme (theme_id, store_id, title, description, difficulty, play_minutes, min_people, max_people, price, booking_url, source, status) VALUES (sq_theme.NEXTVAL, 5, '심야 택시', '새벽 3시에 탄 택시가 이상한 곳으로 달립니다.', 4, 60, 2, 3, 24000, 'https://example.com/booking/17', 'CRAWL', 'OPEN');
INSERT INTO theme (theme_id, store_id, title, description, difficulty, play_minutes, min_people, max_people, price, booking_url, source, status) VALUES (sq_theme.NEXTVAL, 5, '지하 벙커', '지상과 연락이 끊긴 벙커에서 출구 좌표를 찾으세요.', 3, 70, 3, 5, 25000, 'https://example.com/booking/18', 'CRAWL', 'OPEN');
INSERT INTO theme (theme_id, store_id, title, description, difficulty, play_minutes, min_people, max_people, price, booking_url, source, status) VALUES (sq_theme.NEXTVAL, 5, '가면 무도회', '가면 뒤에 숨은 범인을 찾아 무도회장에 잠입합니다.', 4, 70, 2, 4, 25000, 'https://example.com/booking/19', 'CRAWL', 'OPEN');
INSERT INTO theme (theme_id, store_id, title, description, difficulty, play_minutes, min_people, max_people, price, booking_url, source, status) VALUES (sq_theme.NEXTVAL, 5, '고양이 탐정', '사라진 생선 사건을 고양이 탐정과 함께 풉니다.', 1, 50, 2, 4, 20000, 'https://example.com/booking/20', 'CRAWL', 'OPEN');
INSERT INTO theme (theme_id, store_id, title, description, difficulty, play_minutes, min_people, max_people, price, booking_url, source, status) VALUES (sq_theme.NEXTVAL, 1, '마법학교 입학시험', '시험 날 사라진 지팡이를 찾아야 합격합니다.', 2, 60, 2, 5, 23000, 'https://example.com/booking/21', 'OWNER', 'PENDING');
INSERT INTO theme (theme_id, store_id, title, description, difficulty, play_minutes, min_people, max_people, price, booking_url, source, status) VALUES (sq_theme.NEXTVAL, 3, '무인도 생존기', '구조 신호를 보내기 전까지 무인도에서 버티세요.', 3, 70, 2, 4, 24000, 'https://example.com/booking/22', 'OWNER', 'REJECTED');

-- 5. 테마-장르 (테마 하나에 장르 1~2개)
INSERT INTO theme_genre (id, theme_id, genre_id) VALUES (sq_theme_genre.NEXTVAL, 1, 1);
INSERT INTO theme_genre (id, theme_id, genre_id) VALUES (sq_theme_genre.NEXTVAL, 1, 2);
INSERT INTO theme_genre (id, theme_id, genre_id) VALUES (sq_theme_genre.NEXTVAL, 2, 2);
INSERT INTO theme_genre (id, theme_id, genre_id) VALUES (sq_theme_genre.NEXTVAL, 3, 3);
INSERT INTO theme_genre (id, theme_id, genre_id) VALUES (sq_theme_genre.NEXTVAL, 4, 1);
INSERT INTO theme_genre (id, theme_id, genre_id) VALUES (sq_theme_genre.NEXTVAL, 4, 5);
INSERT INTO theme_genre (id, theme_id, genre_id) VALUES (sq_theme_genre.NEXTVAL, 5, 6);
INSERT INTO theme_genre (id, theme_id, genre_id) VALUES (sq_theme_genre.NEXTVAL, 5, 5);
INSERT INTO theme_genre (id, theme_id, genre_id) VALUES (sq_theme_genre.NEXTVAL, 6, 3);
INSERT INTO theme_genre (id, theme_id, genre_id) VALUES (sq_theme_genre.NEXTVAL, 6, 2);
INSERT INTO theme_genre (id, theme_id, genre_id) VALUES (sq_theme_genre.NEXTVAL, 7, 4);
INSERT INTO theme_genre (id, theme_id, genre_id) VALUES (sq_theme_genre.NEXTVAL, 8, 7);
INSERT INTO theme_genre (id, theme_id, genre_id) VALUES (sq_theme_genre.NEXTVAL, 8, 2);
INSERT INTO theme_genre (id, theme_id, genre_id) VALUES (sq_theme_genre.NEXTVAL, 9, 1);
INSERT INTO theme_genre (id, theme_id, genre_id) VALUES (sq_theme_genre.NEXTVAL, 10, 2);
INSERT INTO theme_genre (id, theme_id, genre_id) VALUES (sq_theme_genre.NEXTVAL, 11, 3);
INSERT INTO theme_genre (id, theme_id, genre_id) VALUES (sq_theme_genre.NEXTVAL, 11, 6);
INSERT INTO theme_genre (id, theme_id, genre_id) VALUES (sq_theme_genre.NEXTVAL, 12, 5);
INSERT INTO theme_genre (id, theme_id, genre_id) VALUES (sq_theme_genre.NEXTVAL, 12, 4);
INSERT INTO theme_genre (id, theme_id, genre_id) VALUES (sq_theme_genre.NEXTVAL, 13, 7);
INSERT INTO theme_genre (id, theme_id, genre_id) VALUES (sq_theme_genre.NEXTVAL, 13, 2);
INSERT INTO theme_genre (id, theme_id, genre_id) VALUES (sq_theme_genre.NEXTVAL, 14, 1);
INSERT INTO theme_genre (id, theme_id, genre_id) VALUES (sq_theme_genre.NEXTVAL, 14, 4);
INSERT INTO theme_genre (id, theme_id, genre_id) VALUES (sq_theme_genre.NEXTVAL, 15, 6);
INSERT INTO theme_genre (id, theme_id, genre_id) VALUES (sq_theme_genre.NEXTVAL, 16, 3);
INSERT INTO theme_genre (id, theme_id, genre_id) VALUES (sq_theme_genre.NEXTVAL, 17, 1);
INSERT INTO theme_genre (id, theme_id, genre_id) VALUES (sq_theme_genre.NEXTVAL, 18, 5);
INSERT INTO theme_genre (id, theme_id, genre_id) VALUES (sq_theme_genre.NEXTVAL, 18, 6);
INSERT INTO theme_genre (id, theme_id, genre_id) VALUES (sq_theme_genre.NEXTVAL, 19, 7);
INSERT INTO theme_genre (id, theme_id, genre_id) VALUES (sq_theme_genre.NEXTVAL, 19, 2);
INSERT INTO theme_genre (id, theme_id, genre_id) VALUES (sq_theme_genre.NEXTVAL, 20, 4);
INSERT INTO theme_genre (id, theme_id, genre_id) VALUES (sq_theme_genre.NEXTVAL, 20, 2);
INSERT INTO theme_genre (id, theme_id, genre_id) VALUES (sq_theme_genre.NEXTVAL, 21, 5);
INSERT INTO theme_genre (id, theme_id, genre_id) VALUES (sq_theme_genre.NEXTVAL, 21, 4);
INSERT INTO theme_genre (id, theme_id, genre_id) VALUES (sq_theme_genre.NEXTVAL, 22, 5);

-- 6. 예약 시간표 : 운영 중(OPEN)인 테마마다 오늘부터 7일 × 하루 6타임 (10시 ~ 20시, 2시간 간격)
--    LEVEL 로 0~6 (날짜), 1~6 (타임) 숫자를 만들어서 테마와 전부 짝지어 한 번에 넣는다
--    세 칸 중 한 칸 꼴로 FULL(마감), 나머지는 AVAILABLE
INSERT INTO time_slot (slot_id, theme_id, start_at, status)
SELECT sq_time_slot.NEXTVAL,
       t.theme_id,
       TRUNC(SYSDATE) + d.day_no + (8 + h.hour_no * 2) / 24,
       CASE WHEN MOD(t.theme_id + d.day_no + h.hour_no, 3) = 0 THEN 'FULL' ELSE 'AVAILABLE' END
FROM theme t,
     (SELECT LEVEL - 1 AS day_no FROM dual CONNECT BY LEVEL <= 7) d,
     (SELECT LEVEL AS hour_no FROM dual CONNECT BY LEVEL <= 6) h
WHERE t.status = 'OPEN';

-- 7. 찜
INSERT INTO wishlist (wish_id, member_id, theme_id) VALUES (sq_wishlist.NEXTVAL, 4, 1);
INSERT INTO wishlist (wish_id, member_id, theme_id) VALUES (sq_wishlist.NEXTVAL, 4, 17);
INSERT INTO wishlist (wish_id, member_id, theme_id) VALUES (sq_wishlist.NEXTVAL, 4, 19);
INSERT INTO wishlist (wish_id, member_id, theme_id) VALUES (sq_wishlist.NEXTVAL, 5, 13);
INSERT INTO wishlist (wish_id, member_id, theme_id) VALUES (sq_wishlist.NEXTVAL, 5, 18);
INSERT INTO wishlist (wish_id, member_id, theme_id) VALUES (sq_wishlist.NEXTVAL, 6, 16);
INSERT INTO wishlist (wish_id, member_id, theme_id) VALUES (sq_wishlist.NEXTVAL, 6, 3);
INSERT INTO wishlist (wish_id, member_id, theme_id) VALUES (sq_wishlist.NEXTVAL, 7, 20);
INSERT INTO wishlist (wish_id, member_id, theme_id) VALUES (sq_wishlist.NEXTVAL, 7, 5);

-- 8. 후기 (한 줄 후기가 NULL 이면 별점만 남긴 것)
--    22번은 신고 대기 중, 23번은 광고 글이라 관리자가 숨김(HIDDEN) + 작성자 정지
--    컬럼 순서 : 번호, 회원, 닉네임, 테마, 별점, 탈출, 힌트, 플레이한 날, 한 줄 후기, 상태
INSERT INTO review (review_id, member_id, nickname, theme_id, rating, escaped, hint_count, played_date, content, status) VALUES (sq_review.NEXTVAL, 4, '탈출왕', 2,  5, 'Y', 0, TRUNC(SYSDATE) - 30, '단서가 깔끔하게 이어져서 좋았어요.', 'VISIBLE');
INSERT INTO review (review_id, member_id, nickname, theme_id, rating, escaped, hint_count, played_date, content, status) VALUES (sq_review.NEXTVAL, 4, '탈출왕', 9,  5, 'Y', 1, TRUNC(SYSDATE) - 25, '진짜 무서워요. 공포 좋아하면 무조건 추천.', 'VISIBLE');
INSERT INTO review (review_id, member_id, nickname, theme_id, rating, escaped, hint_count, played_date, content, status) VALUES (sq_review.NEXTVAL, 4, '탈출왕', 10, 4, 'Y', 2, TRUNC(SYSDATE) - 20, NULL, 'VISIBLE');
INSERT INTO review (review_id, member_id, nickname, theme_id, rating, escaped, hint_count, played_date, content, status) VALUES (sq_review.NEXTVAL, 4, '탈출왕', 13, 4, 'N', 3, TRUNC(SYSDATE) - 15, '마지막 장치에서 시간이 모자랐어요.', 'VISIBLE');
INSERT INTO review (review_id, member_id, nickname, theme_id, rating, escaped, hint_count, played_date, content, status) VALUES (sq_review.NEXTVAL, 4, '탈출왕', 5,  3, 'Y', 2, TRUNC(SYSDATE) - 10, '연출은 좋은데 문제가 조금 쉬워요.', 'VISIBLE');
INSERT INTO review (review_id, member_id, nickname, theme_id, rating, escaped, hint_count, played_date, content, status) VALUES (sq_review.NEXTVAL, 4, '탈출왕', 6,  4, 'Y', 1, TRUNC(SYSDATE) - 7,  NULL, 'VISIBLE');
INSERT INTO review (review_id, member_id, nickname, theme_id, rating, escaped, hint_count, played_date, content, status) VALUES (sq_review.NEXTVAL, 5, '힌트없이', 1,  4, 'Y', 0, TRUNC(SYSDATE) - 28, '인형 연출이 소름 돋아요. 추리도 탄탄해요.', 'VISIBLE');
INSERT INTO review (review_id, member_id, nickname, theme_id, rating, escaped, hint_count, played_date, content, status) VALUES (sq_review.NEXTVAL, 5, '힌트없이', 8,  5, 'Y', 0, TRUNC(SYSDATE) - 21, '잠입 테마 중 최고예요.', 'VISIBLE');
INSERT INTO review (review_id, member_id, nickname, theme_id, rating, escaped, hint_count, played_date, content, status) VALUES (sq_review.NEXTVAL, 5, '힌트없이', 15, 4, 'N', 2, TRUNC(SYSDATE) - 14, '비행기 세트가 멋있어요.', 'VISIBLE');
INSERT INTO review (review_id, member_id, nickname, theme_id, rating, escaped, hint_count, played_date, content, status) VALUES (sq_review.NEXTVAL, 5, '힌트없이', 19, 3, 'Y', 1, TRUNC(SYSDATE) - 9,  NULL, 'VISIBLE');
INSERT INTO review (review_id, member_id, nickname, theme_id, rating, escaped, hint_count, played_date, content, status) VALUES (sq_review.NEXTVAL, 5, '힌트없이', 10, 5, 'Y', 0, TRUNC(SYSDATE) - 3,  '명탐정 놀이 제대로 했어요.', 'VISIBLE');
INSERT INTO review (review_id, member_id, nickname, theme_id, rating, escaped, hint_count, played_date, content, status) VALUES (sq_review.NEXTVAL, 5, '힌트없이', 2,  4, 'Y', 1, TRUNC(SYSDATE) - 2,  NULL, 'VISIBLE');
INSERT INTO review (review_id, member_id, nickname, theme_id, rating, escaped, hint_count, played_date, content, status) VALUES (sq_review.NEXTVAL, 6, '쫄보', 3,  5, 'Y', 2, TRUNC(SYSDATE) - 26, '마지막에 눈물 날 뻔했어요.', 'VISIBLE');
INSERT INTO review (review_id, member_id, nickname, theme_id, rating, escaped, hint_count, played_date, content, status) VALUES (sq_review.NEXTVAL, 6, '쫄보', 7,  4, 'Y', 1, TRUNC(SYSDATE) - 19, '친구들이랑 계속 웃었어요.', 'VISIBLE');
INSERT INTO review (review_id, member_id, nickname, theme_id, rating, escaped, hint_count, played_date, content, status) VALUES (sq_review.NEXTVAL, 6, '쫄보', 11, 5, 'Y', 3, TRUNC(SYSDATE) - 12, '편지 연출이 감동이에요.', 'VISIBLE');
INSERT INTO review (review_id, member_id, nickname, theme_id, rating, escaped, hint_count, played_date, content, status) VALUES (sq_review.NEXTVAL, 6, '쫄보', 14, 2, 'N', 5, TRUNC(SYSDATE) - 6,  '무서워서 힌트만 다섯 번 썼어요.', 'VISIBLE');
INSERT INTO review (review_id, member_id, nickname, theme_id, rating, escaped, hint_count, played_date, content, status) VALUES (sq_review.NEXTVAL, 6, '쫄보', 20, 4, 'Y', 1, TRUNC(SYSDATE) - 4,  NULL, 'VISIBLE');
INSERT INTO review (review_id, member_id, nickname, theme_id, rating, escaped, hint_count, played_date, content, status) VALUES (sq_review.NEXTVAL, 6, '쫄보', 16, 5, 'Y', 0, TRUNC(SYSDATE) - 1,  '커플 테마로 강력 추천해요.', 'VISIBLE');
INSERT INTO review (review_id, member_id, nickname, theme_id, rating, escaped, hint_count, played_date, content, status) VALUES (sq_review.NEXTVAL, 7, '방린이', 7,  5, 'Y', 4, TRUNC(SYSDATE) - 18, '첫 방탈출인데 재밌었어요.', 'VISIBLE');
INSERT INTO review (review_id, member_id, nickname, theme_id, rating, escaped, hint_count, played_date, content, status) VALUES (sq_review.NEXTVAL, 7, '방린이', 12, 3, 'N', 4, TRUNC(SYSDATE) - 40, NULL, 'VISIBLE');
INSERT INTO review (review_id, member_id, nickname, theme_id, rating, escaped, hint_count, played_date, content, status) VALUES (sq_review.NEXTVAL, 7, '방린이', 18, 3, 'N', 5, TRUNC(SYSDATE) - 8,  '저한테는 너무 어려웠어요.', 'VISIBLE');
INSERT INTO review (review_id, member_id, nickname, theme_id, rating, escaped, hint_count, played_date, content, status) VALUES (sq_review.NEXTVAL, 7, '방린이', 4,  1, 'N', 6, TRUNC(SYSDATE) - 5,  '돈 아까워요. 직원도 불친절했어요.', 'VISIBLE');
INSERT INTO review (review_id, member_id, nickname, theme_id, rating, escaped, hint_count, played_date, content, status) VALUES (sq_review.NEXTVAL, 8, '정지된회원', 1, 1, 'N', 0, TRUNC(SYSDATE) - 35, '광고) 다른 사이트에서 더 싸게 예약하세요', 'HIDDEN');

-- 9. 후기 신고 : 23번 후기(광고)는 처리 완료, 22번 후기는 대기 중
INSERT INTO report (report_id, review_id, reporter_id, reason, status) VALUES (sq_report.NEXTVAL, 23, 2, '다른 사이트 광고 글입니다.', 'DONE');
INSERT INTO report (report_id, review_id, reporter_id, reason, status) VALUES (sq_report.NEXTVAL, 23, 4, '광고예요.', 'DONE');
INSERT INTO report (report_id, review_id, reporter_id, reason, status) VALUES (sq_report.NEXTVAL, 22, 2, '사실과 다른 내용이 있어요.', 'WAIT');

-- 10. 선호 장르
INSERT INTO member_genre (id, member_id, genre_id) VALUES (sq_member_genre.NEXTVAL, 4, 1);
INSERT INTO member_genre (id, member_id, genre_id) VALUES (sq_member_genre.NEXTVAL, 4, 2);
INSERT INTO member_genre (id, member_id, genre_id) VALUES (sq_member_genre.NEXTVAL, 5, 2);
INSERT INTO member_genre (id, member_id, genre_id) VALUES (sq_member_genre.NEXTVAL, 5, 7);
INSERT INTO member_genre (id, member_id, genre_id) VALUES (sq_member_genre.NEXTVAL, 5, 6);
INSERT INTO member_genre (id, member_id, genre_id) VALUES (sq_member_genre.NEXTVAL, 6, 3);
INSERT INTO member_genre (id, member_id, genre_id) VALUES (sq_member_genre.NEXTVAL, 6, 4);
INSERT INTO member_genre (id, member_id, genre_id) VALUES (sq_member_genre.NEXTVAL, 7, 4);
INSERT INTO member_genre (id, member_id, genre_id) VALUES (sq_member_genre.NEXTVAL, 7, 5);

-- 11. 파티 : 1 모집 중(2/4) / 2 마감(3/3) / 3 끝난 파티(후기 팝업 테스트용) / 4 모집 중(1/3)
INSERT INTO party (party_id, theme_id, leader_id, title, play_at, max_people, status) VALUES (sq_party.NEXTVAL, 1,  4, '인형의 집 같이 가실 분 (추리 좋아하는 분)', TRUNC(SYSDATE) + 2 + 20/24,  4, 'RECRUITING');
INSERT INTO party (party_id, theme_id, leader_id, title, play_at, max_people, status) VALUES (sq_party.NEXTVAL, 7,  7, '방린이끼리 웃음 연구소 가요',             TRUNC(SYSDATE) + 3 + 14/24,  3, 'FULL');
INSERT INTO party (party_id, theme_id, leader_id, title, play_at, max_people, status) VALUES (sq_party.NEXTVAL, 15, 5, '마지막 비행 같이 하실 분',                 TRUNC(SYSDATE) - 14 + 19/24, 4, 'CLOSED');
INSERT INTO party (party_id, theme_id, leader_id, title, play_at, max_people, status) VALUES (sq_party.NEXTVAL, 17, 4, '심야 택시 두 명 더 구해요',                TRUNC(SYSDATE) + 1 + 21/24,  3, 'RECRUITING');

-- 12. 파티 참여자 (방장도 한 줄)
--     3번 파티는 끝났고 4번 회원은 15번 테마 후기가 없음 → 4번 회원 로그인 시 후기 팝업 대상
INSERT INTO party_member (id, party_id, member_id, nickname) VALUES (sq_party_member.NEXTVAL, 1, 4, '탈출왕');
INSERT INTO party_member (id, party_id, member_id, nickname) VALUES (sq_party_member.NEXTVAL, 1, 5, '힌트없이');
INSERT INTO party_member (id, party_id, member_id, nickname) VALUES (sq_party_member.NEXTVAL, 2, 7, '방린이');
INSERT INTO party_member (id, party_id, member_id, nickname) VALUES (sq_party_member.NEXTVAL, 2, 6, '쫄보');
INSERT INTO party_member (id, party_id, member_id, nickname) VALUES (sq_party_member.NEXTVAL, 2, 5, '힌트없이');
INSERT INTO party_member (id, party_id, member_id, nickname) VALUES (sq_party_member.NEXTVAL, 3, 5, '힌트없이');
INSERT INTO party_member (id, party_id, member_id, nickname) VALUES (sq_party_member.NEXTVAL, 3, 4, '탈출왕');
INSERT INTO party_member (id, party_id, member_id, nickname) VALUES (sq_party_member.NEXTVAL, 4, 4, '탈출왕');

-- 13. 파티 채팅 (created_at 을 몇 시간 전으로 넣어서 순서가 보이게)
INSERT INTO party_message (message_id, party_id, member_id, nickname, content, created_at) VALUES (sq_party_message.NEXTVAL, 1, 4, '탈출왕',   '안녕하세요! 모레 저녁 8시 괜찮으세요?',       SYSDATE - 3/24);
INSERT INTO party_message (message_id, party_id, member_id, nickname, content, created_at) VALUES (sq_party_message.NEXTVAL, 1, 5, '힌트없이', '좋아요. 힌트 최대한 안 쓰고 가 봐요.',        SYSDATE - 2/24);
INSERT INTO party_message (message_id, party_id, member_id, nickname, content, created_at) VALUES (sq_party_message.NEXTVAL, 1, 4, '탈출왕',   '두 분만 더 구해지면 바로 예약할게요.',         SYSDATE - 1/24);
INSERT INTO party_message (message_id, party_id, member_id, nickname, content, created_at) VALUES (sq_party_message.NEXTVAL, 2, 7, '방린이',   '다 모였어요! 예약은 제가 할게요.',             SYSDATE - 5/24);
INSERT INTO party_message (message_id, party_id, member_id, nickname, content, created_at) VALUES (sq_party_message.NEXTVAL, 2, 6, '쫄보',     '무서운 거 없는 테마 맞죠?',                    SYSDATE - 4/24);
INSERT INTO party_message (message_id, party_id, member_id, nickname, content, created_at) VALUES (sq_party_message.NEXTVAL, 2, 5, '힌트없이', '코믹 테마라 하나도 안 무서워요.',              SYSDATE - 3/24);

COMMIT;

-- 14. 개수 확인 (회원 9, 장르 7, 브랜드 1, 매장 5, 테마 22, 테마장르 35, 시간표 798, 찜 9,
--     후기 23, 신고 3, 선호장르 9, 파티 4, 참여자 8, 채팅 6)
SELECT 'member' AS table_name, COUNT(*) AS cnt FROM member
UNION ALL SELECT 'genre',         COUNT(*) FROM genre
UNION ALL SELECT 'brand',         COUNT(*) FROM brand
UNION ALL SELECT 'store',         COUNT(*) FROM store
UNION ALL SELECT 'theme',         COUNT(*) FROM theme
UNION ALL SELECT 'theme_genre',   COUNT(*) FROM theme_genre
UNION ALL SELECT 'time_slot',     COUNT(*) FROM time_slot
UNION ALL SELECT 'wishlist',      COUNT(*) FROM wishlist
UNION ALL SELECT 'review',        COUNT(*) FROM review
UNION ALL SELECT 'report',        COUNT(*) FROM report
UNION ALL SELECT 'member_genre',  COUNT(*) FROM member_genre
UNION ALL SELECT 'party',         COUNT(*) FROM party
UNION ALL SELECT 'party_member',  COUNT(*) FROM party_member
UNION ALL SELECT 'party_message', COUNT(*) FROM party_message;

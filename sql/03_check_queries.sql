-- 방탈출 통합예약 : 메뉴별 조회 예시 (02_dummy_data.sql 실행 후)
-- 각자 메뉴의 SQL 을 먼저 여기서 돌려 보고, 결과가 맞으면 MyBatis / JPA 로 옮긴다
-- 숫자로 적힌 회원번호·테마번호는 실제 코드에서는 #{memberId} 처럼 파라미터로 받는다
-- (회원번호는 화면에서 받지 말고 토큰에서 꺼낸다)
-- Oracle 11g 라서 "상위 N개"는 FETCH FIRST 가 아니라 ROWNUM 으로 자른다


-- ===================== B : 테마 탐색 =====================

-- B-1. 메인 목록 : 운영 중인 테마 + 매장 + 평균 별점 + 후기 수 (숨긴 후기는 빼고 계산)
SELECT t.theme_id, t.title, s.name AS store_name, s.area,
       ROUND(AVG(r.rating), 1) AS avg_rating,
       COUNT(r.review_id)      AS review_count
FROM theme t
JOIN store s ON s.store_id = t.store_id
LEFT JOIN review r ON r.theme_id = t.theme_id AND r.status = 'VISIBLE'
WHERE t.status = 'OPEN'
GROUP BY t.theme_id, t.title, s.name, s.area
ORDER BY avg_rating DESC NULLS LAST, t.theme_id;

-- B-2. 필터 : 2명 / 내일 / 예약 가능 / 홍대 / 공포  → 1번, 17번 테마가 나와야 함
SELECT t.theme_id, t.title, s.name AS store_name
FROM theme t
JOIN store s ON s.store_id = t.store_id
WHERE t.status = 'OPEN'
  AND t.min_people <= 2 AND t.max_people >= 2
  AND s.area = '홍대'
  AND EXISTS (SELECT 1
              FROM theme_genre tg JOIN genre g ON g.genre_id = tg.genre_id
              WHERE tg.theme_id = t.theme_id AND g.name = '공포')
  AND EXISTS (SELECT 1
              FROM time_slot ts
              WHERE ts.theme_id = t.theme_id
                AND ts.status = 'AVAILABLE'
                AND ts.start_at >= TRUNC(SYSDATE) + 1
                AND ts.start_at <  TRUNC(SYSDATE) + 2)
ORDER BY t.theme_id;

-- B-3. 상세 : 1번 테마의 장르를 "공포, 추리" 한 줄로
SELECT t.title, LISTAGG(g.name, ', ') WITHIN GROUP (ORDER BY g.genre_id) AS genres
FROM theme t
JOIN theme_genre tg ON tg.theme_id = t.theme_id
JOIN genre g ON g.genre_id = tg.genre_id
WHERE t.theme_id = 1
GROUP BY t.title;

-- B-4. 상세 : 1번 테마의 내일 시간표
SELECT slot_id, TO_CHAR(start_at, 'MM/DD HH24:MI') AS slot_time, status
FROM time_slot
WHERE theme_id = 1
  AND start_at >= TRUNC(SYSDATE) + 1
  AND start_at <  TRUNC(SYSDATE) + 2
ORDER BY start_at;

-- B-5. 내 찜 목록 (4번 회원)
SELECT w.wish_id, t.theme_id, t.title, s.name AS store_name
FROM wishlist w
JOIN theme t ON t.theme_id = w.theme_id
JOIN store s ON s.store_id = t.store_id
WHERE w.member_id = 4
ORDER BY w.created_at DESC;


-- ===================== D : 기록 + 추천 =====================

-- D-1. 1번 테마의 후기 목록 (숨긴 후기는 빼고)
SELECT r.review_id, r.nickname, r.rating, r.escaped, r.hint_count, r.content
FROM review r
WHERE r.theme_id = 1 AND r.status = 'VISIBLE'
ORDER BY r.created_at DESC;

-- D-2. 맞춤 추천 (4번 회원) : 선호 장르 테마 중 아직 기록 안 남긴 것, 평균 별점 높은 순 5개
SELECT *
FROM (SELECT t.theme_id, t.title, ROUND(AVG(r.rating), 1) AS avg_rating
      FROM theme t
      LEFT JOIN review r ON r.theme_id = t.theme_id AND r.status = 'VISIBLE'
      WHERE t.status = 'OPEN'
        AND t.theme_id IN (SELECT tg.theme_id
                           FROM theme_genre tg
                           JOIN member_genre mg ON mg.genre_id = tg.genre_id
                           WHERE mg.member_id = 4)
        AND t.theme_id NOT IN (SELECT theme_id FROM review WHERE member_id = 4)
      GROUP BY t.theme_id, t.title
      ORDER BY avg_rating DESC NULLS LAST, t.theme_id)
WHERE ROWNUM <= 5;

-- D-3. 마이페이지 통계 (4번 회원) : 플레이 수, 탈출 수, 탈출 성공률(%)
SELECT COUNT(*) AS play_count,
       SUM(CASE WHEN escaped = 'Y' THEN 1 ELSE 0 END) AS escape_count,
       ROUND(SUM(CASE WHEN escaped = 'Y' THEN 1 ELSE 0 END) / COUNT(*) * 100) AS escape_rate
FROM review
WHERE member_id = 4;

-- D-4. 후기 팝업 대상 (4번 회원) : 예약 시간이 끝난 파티 중 아직 후기를 안 남긴 테마
--      → 3번 파티(마지막 비행)가 나와야 함
SELECT p.party_id, p.theme_id, t.title, TO_CHAR(p.play_at, 'MM/DD HH24:MI') AS play_time
FROM party p
JOIN party_member pm ON pm.party_id = p.party_id
JOIN theme t ON t.theme_id = p.theme_id
WHERE pm.member_id = 4
  AND p.play_at + t.play_minutes / 1440 < SYSDATE
  AND NOT EXISTS (SELECT 1 FROM review r
                  WHERE r.member_id = pm.member_id AND r.theme_id = p.theme_id);


-- ===================== C : 사장 + 관리 =====================

-- C-1. 관리 매장 목록 : 점주는 owner_id = 나, 본사는 brand.hq_id = 나
--      9번(미스터리룸 본사) → 매장 1, 2 / 2번으로 바꾸면 매장 1 / 3번으로 바꾸면 매장 3, 4
SELECT s.store_id, s.name, b.name AS brand_name
FROM store s
LEFT JOIN brand b ON b.brand_id = s.brand_id
WHERE s.owner_id = 9 OR b.hq_id = 9
ORDER BY s.store_id;

-- C-2. 권한 확인 : 5번 테마(미스터리룸 강남점)를 9번(본사)이 고칠 수 있나?  1 이면 가능
--      9 를 3 으로 바꾸면 0 → 남의 매장 테마라서 403
SELECT COUNT(*) AS can_manage
FROM theme t
JOIN store s ON s.store_id = t.store_id
LEFT JOIN brand b ON b.brand_id = s.brand_id
WHERE t.theme_id = 5 AND (s.owner_id = 9 OR b.hq_id = 9);

-- C-3. 관리자 : 승인 대기 중인 테마 → 21번(마법학교 입학시험)이 나와야 함
SELECT t.theme_id, t.title, s.name AS store_name, t.created_at
FROM theme t
JOIN store s ON s.store_id = t.store_id
WHERE t.status = 'PENDING'
ORDER BY t.created_at;

-- C-4. 관리자 : 처리 대기 중인 신고
SELECT rp.report_id, rp.reason, r.review_id, r.nickname AS writer, r.content AS review_content
FROM report rp
JOIN review r ON r.review_id = rp.review_id
WHERE rp.status = 'WAIT'
ORDER BY rp.created_at;


-- ===================== A : 파티 =====================

-- A-1. 모집 중인 파티 + 현재 인원
SELECT p.party_id, p.title, t.title AS theme_title,
       TO_CHAR(p.play_at, 'MM/DD HH24:MI') AS play_time,
       COUNT(pm.id) || '/' || p.max_people AS people
FROM party p
JOIN theme t ON t.theme_id = p.theme_id
JOIN party_member pm ON pm.party_id = p.party_id
WHERE p.status = 'RECRUITING'
GROUP BY p.party_id, p.title, t.title, p.play_at, p.max_people
ORDER BY p.play_at;

-- A-2. 채팅 : 1번 파티에서 내가 마지막으로 본 메시지(1번) 다음 것만
SELECT message_id, nickname, content, TO_CHAR(created_at, 'HH24:MI') AS sent_time
FROM party_message
WHERE party_id = 1 AND message_id > 1
ORDER BY message_id;


-- ===================== UNIQUE 확인 (직접 해 보기) =====================
-- 4번 회원은 이미 1번 테마를 찜했다. 아래 주석을 풀고 실행하면
-- ORA-00001: unique constraint (HR.WISHLIST_UK) violated 에러가 나야 정상
-- INSERT INTO wishlist (wish_id, member_id, theme_id) VALUES (sq_wishlist.NEXTVAL, 4, 1);

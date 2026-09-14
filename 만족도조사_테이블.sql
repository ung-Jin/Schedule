-- 결과보고 이후 고객 만족도 조사 결과를 담는 테이블 + 대시보드 그래프 확인용 더미데이터
-- SCHEDULE_NO는 일부러 NULL을 허용합니다: 실제 제출(자체 설문 페이지 /survey)은 항상 SCHEDULE_NO를 채워서 저장하지만,
-- 여기 더미데이터는 "작년" 데이터까지 필요해서 실제 존재하지 않는 옛날 AS_SCHEDULE을 억지로 만들지 않고
-- ENGINEER_NO만 연결해서 통계용으로 넣습니다. (SCHEDULE_NO에 FK를 걸지 않은 이유)

CREATE TABLE IF NOT EXISTS SATISFACTION (
    SURVEY_NO INT AUTO_INCREMENT PRIMARY KEY,
    SCHEDULE_NO INT NULL,                  -- 실제 제출 건은 어떤 방문 건인지 연결 (AS_SCHEDULE.SCHEDULE_NO, 소프트 참조)
    ENGINEER_NO INT NOT NULL,              -- 어떤 기사에 대한 평가인지
    SCORE TINYINT NOT NULL,                -- 1~5점
    COMMENT_TEXT VARCHAR(300) NULL,        -- 선택 입력 후기
    CREATED_AT DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT FK_SATISFACTION_ENGINEER FOREIGN KEY (ENGINEER_NO) REFERENCES ENGINEER(ENGINEER_NO)
);

-- ------------------------------------------------------------------
-- 더미데이터: kimgisa(ENGINEER_NO=1) 기준 2025-01 ~ 2026-09, 월 8건씩
-- 뒤로 갈수록(최근 달일수록) 점수가 서서히 올라가는 패턴으로 만들어서
-- "이번달 vs 작년 동월" 비교, "올해 추이" 그래프가 그럴듯하게 보이도록 했습니다.
-- ------------------------------------------------------------------
INSERT INTO SATISFACTION (ENGINEER_NO, SCORE, CREATED_AT)
WITH RECURSIVE idx(i) AS (
    SELECT 0
    UNION ALL
    SELECT i + 1 FROM idx WHERE i < 167   -- 21개월 x 8건 - 1
)
SELECT
    1 AS ENGINEER_NO,
    LEAST(5, GREATEST(1, ROUND(3.5 + (FLOOR(i / 8) * 0.05) + (MOD(i, 3) - 1)))) AS SCORE,
    DATE_ADD(DATE_ADD('2025-01-05 10:00:00', INTERVAL FLOOR(i / 8) MONTH), INTERVAL MOD(i, 8) DAY) AS CREATED_AT
FROM idx;

-- 이번 달(9월) 몇 건은 실제 존재하는 완료된 AS_SCHEDULE(1,2,3,8,9,10)과 연결해서 후기도 같이 넣어둡니다
INSERT INTO SATISFACTION (SCHEDULE_NO, ENGINEER_NO, SCORE, COMMENT_TEXT, CREATED_AT) VALUES
(1, 1, 5, '친절하고 꼼꼼하게 봐주셨어요!', '2026-09-09 13:00:00'),
(2, 1, 4, NULL, '2026-09-10 13:30:00'),
(3, 1, 5, '설명도 잘 해주시고 좋았습니다', '2026-09-10 16:00:00'),
(8, 1, 4, NULL, '2026-09-09 13:10:00'),
(9, 1, 5, '빠른 처리 감사합니다', '2026-09-11 12:00:00'),
(10, 1, 4, NULL, '2026-09-11 17:00:00');

COMMIT;

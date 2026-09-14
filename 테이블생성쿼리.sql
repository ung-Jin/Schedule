-- 1. MEMBER
CREATE TABLE MEMBER (
                        MEM_NO INT AUTO_INCREMENT PRIMARY KEY,
                        MEM_ID VARCHAR(30) NOT NULL UNIQUE,
                        MEM_PW VARCHAR(100) NOT NULL,
                        MEM_NAME VARCHAR(30) NOT NULL,
                        ROLE ENUM('admin', 'repairman', 'user') NOT NULL DEFAULT 'user'
);

-- 2. ENGINEER (MEMBER 참조)
CREATE TABLE ENGINEER (
                          ENGINEER_NO INT AUTO_INCREMENT PRIMARY KEY,     -- 기사 고유번호 (PK)
                          MEM_NO INT NOT NULL,                            -- 회원번호 (MEMBER FK)
                          ENGINEER_NAME VARCHAR(30) NOT NULL,             -- 기사 이름
                          ENGINEER_TEL VARCHAR(20) NOT NULL,              -- 연락처
                          SPECIALTY VARCHAR(50),                          -- 전문 분야
                          WORK_START_TIME TIME,                           -- 근무 시작 시간
                          WORK_END_TIME TIME,                             -- 근무 종료 시간
                          STATUS VARCHAR(20) DEFAULT 'WORK',              -- 근무 상태 (WORK/REST)
                          CONSTRAINT PK_ENGINEER_MEMBER FOREIGN KEY (MEM_NO) REFERENCES MEMBER(MEM_NO)
);

-- 3. REQUEST (독립)
CREATE TABLE AS_SCHEDULE (
                             SCHEDULE_NO INT AUTO_INCREMENT
    , REQUEST_NO INT NOT NULL
    , ENGINEER_NO INT NOT NULL
    , FIX_DATE DATE NOT NULL
    , START_TIME DATETIME NOT NULL
    , END_TIME DATETIME NOT NULL
    , STATUS VARCHAR(20) DEFAULT 'ASSIGNED'
    , CONSTRAINT PK_SCHEDULE PRIMARY KEY (SCHEDULE_NO)
    , CONSTRAINT FK_SCHEDULE_REQUEST FOREIGN KEY (REQUEST_NO) REFERENCES REQUEST (REQUEST_NO)
    , CONSTRAINT FK_SCHEDULE_ENGINEER FOREIGN KEY (ENGINEER_NO) REFERENCES ENGINEER (ENGINEER_NO)
);




CREATE TABLE IF NOT EXISTS AS_RESULT (
                                         RESULT_NO INT AUTO_INCREMENT PRIMARY KEY,
                                         SCHEDULE_NO INT NOT NULL,
                                         PROCESS_DATE DATETIME NOT NULL,
                                         PROCESS_CONTENT VARCHAR(500) NOT NULL,
    RESULT_STATUS VARCHAR(20) NOT NULL DEFAULT '완료',
    IMAGE_PATH VARCHAR(200),
    IMAGE_PATH2 VARCHAR(200),
    CREATED_AT DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (SCHEDULE_NO) REFERENCES AS_SCHEDULE(SCHEDULE_NO)
    );




CREATE TABLE SATISFACTION (
                              SURVEY_NO INT AUTO_INCREMENT PRIMARY KEY,
                              SCHEDULE_NO INT NULL,
                              ENGINEER_NO INT NOT NULL,
                              SCORE_VISIT TINYINT NOT NULL COMMENT '기사 방문 및 시간 준수 만족도 (1~5)',
                              SCORE_KINDNESS TINYINT NOT NULL COMMENT '기사의 친절도 및 응대 만족도 (1~5)',
                              SCORE_RESULT TINYINT NOT NULL COMMENT 'A/S 처리 결과에 대한 전반적인 만족도 (1~5)',
                              COMMENT_TEXT VARCHAR(300) NULL,
                              CREATED_AT DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

                              CONSTRAINT FK_SATISFACTION_ENGINEER
                                  FOREIGN KEY (ENGINEER_NO)
                                      REFERENCES ENGINEER(ENGINEER_NO)
);


-- ######################INSERT 더미데이터##############

-- 멤버
INSERT INTO MEMBER (MEM_NO, MEM_ID, MEM_PW, MEM_NAME, ROLE) VALUES
(1,  'admin',  'asdf', '관리자', 'admin'),

(2,  'eng01', 'asdf', '김기술', 'repairman'),
(3,  'eng02', 'asdf', '이수리', 'repairman'),
(4,  'eng03', 'asdf', '최점검', 'repairman'),
(5,  'eng04', 'asdf', '정출동', 'repairman'),
(6,  'eng05', 'asdf', '한정비', 'repairman'),

(7,  'user01', 'asdf', '홍길동', 'user'),
(8,  'user02', 'asdf', '김철수', 'user'),
(9,  'user03', 'asdf', '이영희', 'user'),
(10, 'user04', 'asdf', '박민수', 'user'),
(11, 'user05', 'asdf', '최지우', 'user'),
(12, 'user06', 'asdf', '정하늘', 'user'),
(13, 'user07', 'asdf', '강나연', 'user'),
(14, 'user08', 'asdf', '조현우', 'user'),
(15, 'user09', 'asdf', '윤서연', 'user'),
(16, 'user10', 'asdf', '임도윤', 'user');

-- ##엔지니어##
INSERT INTO ENGINEER (ENGINEER_NO, MEM_NO, ENGINEER_NAME, ENGINEER_TEL, SPECIALTY, WORK_START_TIME, WORK_END_TIME, STATUS) VALUES
(1, 2, '김기술', '010-1111-2222', '에어컨',       '09:00:00', '18:00:00', 'WORK'),
(2, 3, '이수리', '010-2222-3333', '보일러',       '09:00:00', '18:00:00', 'WORK'),
(3, 4, '최점검', '010-3333-4444', '세탁기',       '09:00:00', '18:00:00', 'WORK'),
(4, 5, '정출동', '010-4444-5555', '에어컨',       '10:00:00', '19:00:00', 'WORK'),
(5, 6, '한정비', '010-5555-6666', '전체',         '09:00:00', '18:00:00', 'REST');


-- ##고장접수##
INSERT INTO REQUEST (REQUEST_NO, CUSTOMER_NAME, CUSTOMER_TEL, CUSTOMER_ADDR, PRODUCT_TYPE, SYMPTOM, WISH_DATE, STATUS, MEM_NO) VALUES
(1,  '홍길동', '010-1000-0001', '울산광역시 중구 태화로 201',           '에어컨',        '냉방이 전혀 안 됨',       '2026-09-15 14:00:00', 'RECEIVED',    7),
(2,  '김철수', '010-1000-0002', '울산광역시 중구 번영로 329',           '보일러',        '온수가 안 나옴',          '2026-09-16 10:00:00', 'RECEIVED',    8),
(3,  '이영희', '010-1000-0003', '울산광역시 남구 삼산로 273',           '스탠드형 에어컨', '혼자 자꾸 넘어짐',        '2026-09-17 13:00:00', 'ASSIGNED',    9),
(4,  '박민수', '010-1000-0004', '울산광역시 남구 돋질로 233',           '에어컨',        '실외기에서 이상한 소리',   '2026-09-18 09:00:00', 'ASSIGNED',    10),
(5,  '최지우', '010-1000-0005', '울산광역시 동구 봉수로 155',           '보일러',        '전원이 안 켜짐',          '2026-09-12 11:00:00', 'COMPLETED',   11),
(6,  '정하늘', '010-1000-0006', '울산광역시 동구 방어진순환도로 1000',  '라디에이터',     '물이 새어 나옴',          '2026-09-19 15:00:00', 'RECEIVED',    12),
(7,  '강나연', '010-1000-0007', '울산광역시 북구 산업로 1426',          '에어컨',        '바람이 미지근함',         '2026-09-20 16:00:00', 'RECEIVED',    13),
(8,  '조현우', '010-1000-0008', '울산광역시 북구 호계로 300',           '보일러',        '에러코드 E03 표시',       '2026-09-14 09:30:00', 'IN_PROGRESS', 14),
(9,  '윤서연', '010-1000-0009', '울산광역시 울주군 범서읍 구영로 100',  '시스템 에어컨',  '전원이 아예 안 들어옴',    '2026-09-26 10:00:00', 'ASSIGNED',    15),
(10, '임도윤', '010-1000-0010', '울산광역시 울주군 언양읍 읍성로 830',  '에어컨',        '필터 청소 후에도 냄새남', '2026-09-22 14:30:00', 'CANCELED',    16);

-- ========================================
-- 4. AS_SCHEDULE (REQUEST의 ASSIGNED/IN_PROGRESS/COMPLETED 건에 대응하는 배정 일정 5건)
-- ========================================
INSERT INTO AS_SCHEDULE (SCHEDULE_NO, REQUEST_NO, ENGINEER_NO, FIX_DATE, START_TIME, END_TIME, STATUS) VALUES
                                                                                                           (1, 3, 1, '2026-09-17', '2026-09-17 13:00:00', '2026-09-17 15:00:00', 'ASSIGNED'),    -- 이영희-스탠드형에어컨-김기술
                                                                                                           (2, 4, 4, '2026-09-18', '2026-09-18 09:00:00', '2026-09-18 11:00:00', 'ASSIGNED'),    -- 박민수-에어컨-정출동
                                                                                                           (3, 5, 2, '2026-09-12', '2026-09-12 11:00:00', '2026-09-12 13:00:00', 'COMPLETED'),   -- 최지우-보일러-이수리
                                                                                                           (4, 8, 2, '2026-09-14', '2026-09-14 09:30:00', '2026-09-14 11:30:00', 'IN_PROGRESS'), -- 조현우-보일러-이수리
                                                                                                           (5, 9, 1, '2026-09-26', '2026-09-26 10:00:00', '2026-09-26 12:00:00', 'ASSIGNED');    -- 윤서연-시스템에어컨-김기술

-- ========================================
-- 5. AS_RESULT (완료된 건(SCHEDULE_NO 3)에 대한 처리 결과 1건)
-- ========================================
INSERT INTO AS_RESULT (RESULT_NO, SCHEDULE_NO, PROCESS_DATE, PROCESS_CONTENT, RESULT_STATUS) VALUES
    (1, 3, '2026-09-12 13:00:00', '보일러 점화플러그 교체 후 정상 작동 확인', '완료');


-- ========================================
-- 6. SATISFACTION (완료 건에 대한 만족도 조사 1건)
-- ========================================
INSERT INTO SATISFACTION (SURVEY_NO, SCHEDULE_NO, ENGINEER_NO, SCORE_VISIT, SCORE_KINDNESS, SCORE_RESULT, COMMENT_TEXT) VALUES
    (1, 3, 2, 5, 5, 4, '친절하고 시간도 잘 지켜주셨어요');


COMMIT;
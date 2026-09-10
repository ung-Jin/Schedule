-- 1. MEMBER (부모 테이블)
CREATE TABLE MEMBER (
                        MEM_NO INT AUTO_INCREMENT PRIMARY KEY,                              -- 회원 고유번호 (PK, 자동증가)
                        MEM_ID VARCHAR(30) NOT NULL UNIQUE,                                 -- 로그인 아이디 (중복 불가)
                        MEM_PW VARCHAR(100) NOT NULL,                                       -- 비밀번호
                        MEM_NAME VARCHAR(30) NOT NULL,                                      -- 회원 이름
                        ROLE ENUM('admin', 'repairman', 'user') NOT NULL DEFAULT 'user'     -- 권한 (admin/repairman/user)
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
                          FOREIGN KEY (MEM_NO) REFERENCES MEMBER(MEM_NO)

);

-- 3. AS_REQUEST (독립)
CREATE TABLE AS_REQUEST (
                            REQUEST_NO INT AUTO_INCREMENT PRIMARY KEY,      -- AS 요청 고유번호 (PK)
                            CUSTOMER_NAME VARCHAR(30) NOT NULL,             -- 고객명
                            CUSTOMER_TEL VARCHAR(20) NOT NULL,              -- 고객 연락처
                            CUSTOMER_ADDR VARCHAR(100) NOT NULL,            -- 고객 주소
                            PRODUCT_TYPE VARCHAR(30),                       -- 제품 종류
                            SYMPTOM VARCHAR(200),                           -- 증상/요청 내용
                            REQUEST_DATE DATETIME DEFAULT CURRENT_TIMESTAMP,-- 접수일시
                            WISH_DATE DATETIME,                             -- 희망 방문일
                            STATUS VARCHAR(20) DEFAULT 'RECEIVED'           -- 상태 (RECEIVED/ASSIGNED/IN_PROGRESS/COMPLETED/CANCELED)
);


USE `project-1`;

-- 관리자 1명
INSERT INTO MEMBER (MEM_ID, MEM_PW, MEM_NAME, ROLE) VALUES
    ('admin1', '1234', '김관리', 'admin');

-- 기사 5명 (repairman)
INSERT INTO MEMBER (MEM_ID, MEM_PW, MEM_NAME, ROLE) VALUES
                                                        ('eng001', '1234', '이성호', 'repairman'),
                                                        ('eng002', '1234', '김민수', 'repairman'),
                                                        ('eng003', '1234', '박지훈', 'repairman'),
                                                        ('eng004', '1234', '정우현', 'repairman'),
                                                        ('eng005', '1234', '최민석', 'repairman');

-- 기사 상세정보 (MEM_NO 2~6 참조)
INSERT INTO ENGINEER (MEM_NO, ENGINEER_NAME, ENGINEER_TEL, SPECIALTY, WORK_START_TIME, WORK_END_TIME, STATUS) VALUES
                                                                                                                  (2, '이성호', '010-1234-5678', '에어컨',        '09:00:00', '18:00:00', 'WORK'),
                                                                                                                  (3, '김민수', '010-2345-6789', '보일러',        '09:00:00', '18:00:00', 'WORK'),
                                                                                                                  (4, '박지훈', '010-3456-7890', '에어컨,보일러', '08:30:00', '17:00:00', 'WORK'),
                                                                                                                  (5, '정우현', '010-4567-8901', '냉난방설비',    '09:00:00', '18:00:00', 'REST'),
                                                                                                                  (6, '최민석', '010-5678-9012', '에어컨',        '10:00:00', '19:00:00', 'WORK');

-- AS 요청 6건 (대시보드 카운트 확인용)
INSERT INTO AS_REQUEST (CUSTOMER_NAME, CUSTOMER_TEL, CUSTOMER_ADDR, PRODUCT_TYPE, SYMPTOM, WISH_DATE, STATUS) VALUES
                                                                                                                  ('이민수', '010-1111-1111', '서울 강남구 테헤란로 123', '에어컨', '냉방이 잘 안됨',    '2026-09-10 10:00:00', 'RECEIVED'),
                                                                                                                  ('김지현', '010-2222-2222', '경기 성남시 분당구',       '보일러', '보일러 작동 안됨',  '2026-09-10 14:00:00', 'IN_PROGRESS'),
                                                                                                                  ('박수현', '010-3333-3333', '인천 남동구',              '에어컨', '실외기 소음',        '2026-09-11 09:00:00', 'ASSIGNED'),
                                                                                                                  ('정하나', '010-4444-4444', '서울 마포구',              '냉난방', '전원이 안 켜짐',     '2026-09-11 15:00:00', 'RECEIVED'),
                                                                                                                  ('최영호', '010-5555-5555', '경기 수원시',              '보일러', '온수가 약함',        '2026-09-09 11:00:00', 'COMPLETED'),
                                                                                                                  ('한지원', '010-6666-6666', '서울 서초구',              '에어컨', '냄새 발생',          '2026-09-08 16:00:00', 'CANCELED');


#AS 기사 스케줄 (ENGINEER, AS_REQUEST 참조)
CREATE TABLE AS_SCHEDULE (
   SCHEDULE_NO INT AUTO_INCREMENT               #스케줄 번호(기본키)
   , REQUEST_NO INT NOT NULL                    #AS 요청 고유번호(외래키)
   , ENGINEER_NO INT NOT NULL                   #기사 고유번호(외래키)
   , START_TIME DATETIME NOT NULL               #시작 예정 시간
   , END_TIME DATETIME NOT NULL                 #종료 예정 시간
   , STATUS VARCHAR(20) DEFAULT 'ASSIGNED'      #진행 상태
   , CONSTRAINT PK_SCHEDULE PRIMARY KEY (SCHEDULE_NO)  # 기본키 제약조건
   , CONSTRAINT FK_SCHEDULE_AS_REQUEST FOREIGN KEY (REQUEST_NO) REFERENCE as_request (REQUEST_NO) #외래키 제약조건
   , CONSTRAINT FK_SCHEDULE_ENGINEER FOREIGN KEY (ENGINEER_NO) REFERENCE ENGINEER (ENGINEER_NO) #외래키 제약조건
);



SELECT * FROM member;
SELECT * FROM engineer;
SELECT * FROM as_request;
--  테이블 생성 쿼리문 더미 데이터 INSERT문은 해당 파일에 작성해주세요!!
-- insert 한 더미 데이터로 공유 해주세요
INSERT INTO member VALUES (1, 'admin', '1234','관리자','admin');
INSERT INTO MEMBER VALUES (2, 'repa1', '1234','기사1','repairman');
INSERT INTO MEMBER VALUES (3, 'user1', '1234','유저1','user');
COMMIT;



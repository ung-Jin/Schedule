--  프로젝트에서 사용할 테이블 생성문 모음 파일
-- Create 한 테이블 쿼리 여기 아래 봍북 해주세요.
CREATE TABLE MEMBER (
    MEM_NO INT AUTO_INCREMENT PRIMARY KEY,
    MEM_ID VARCHAR(30) NOT NULL UNIQUE,
    MEM_PW VARCHAR(100) NOT NULL,
    MEM_NAME VARCHAR(30) NOT NULL,
    ROLE ENUM('admin', 'repairman', 'user') NOT NULL DEFAULT 'user'
);


--  테이블 생성 쿼리문 더미 데이터 INSERT문은 해당 파일에 작성해주세요!!
-- insert 한 더미 데이터로 공유 해주세요
INSERT INTO member VALUES (1, 'admin', '1234','관리자','admin');
INSERT INTO MEMBER VALUES (2, 'repa1', '1234','기사1','repairman');
INSERT INTO MEMBER VALUES (3, 'user1', '1234','유저1','user');
COMMIT;
package com.green.Schedule.engineer.DTO;

import lombok.Data;

//기사 정보 DTO
@Data
public class EngineerDTO {
  //ENGINEER 테이블 컬럼
  private int engineerNo;         //기사 고유번호
  private int memNo;              //회원번호 (MEMBER 참조)
  private String engineerName;    //기사 이름
  private String engineerTel;     //연락처
  private String specialty;       //전문분야
  private String workStartTime;   //근무 시작 시간 (HH:mm)
  private String workEndTime;     //근무 종료 시간
  private String status;          //근무 상태 (WORK/REST)

  //MEMBER 테이블에서 조인해서 가져오는 컬럼
  private String memId;           //로그인 아이디

  //기사 등록 시 사용 (MEMBER + ENGINEER 동시 INSERT용)
  private String memPw;           //로그인 비밀번호
  private String memName;         //회원 이름
}

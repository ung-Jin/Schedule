package com.green.Schedule.dashboard.DTO;

import lombok.Data;

//최근 AS 접수 목록 + 상세 공용 DTO
//배정기사 필드는 AS_SCHEDULE 테이블이 아직 없어서 제외 (팀 완성 후 추가 예정)
@Data
public class RecentAsDTO {
  private int requestNo;          //AS 요청 고유번호 (PK)
  private String customerName;    //고객명
  private String customerTel;     //고객 연락처
  private String customerAddr;    //고객 주소
  private String productType;     //제품 종류
  private String symptom;         //증상/요청내용
  private String requestDate;     //접수일시
  private String wishDate;        //희망 방문일
  private String status;          //상태 (RECEIVED/ASSIGNED/IN_PROGRESS/COMPLETED/CANCELED)
  private String region;          //지역 (주소 앞 두 단어, 목록 표시용)
}

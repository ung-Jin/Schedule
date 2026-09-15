package com.green.Schedule.result.dto;

import lombok.Data;

import java.time.LocalDateTime;

// 관리자 "결과내역" 화면(/pages/admin/sideResult) 전용 - 전체 AS_RESULT 목록을 처리기사 이름과 함께 보여줄 때 씁니다.
// ResultDTO와 달리 engineerName(AS_SCHEDULE -> ENGINEER 조인으로 가져온 값)이 추가로 붙습니다.
@Data
public class ResultListDTO {
  private Long resultNo;
  private Long scheduleNo;
  private LocalDateTime processDate;
  private String processContent;
  private String imagePath;
  private String imagePath2;
  private String noPhotoReason;
  private LocalDateTime createdAt;
  private String engineerName;
}

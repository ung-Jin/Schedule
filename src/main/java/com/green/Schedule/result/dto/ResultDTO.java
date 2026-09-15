package com.green.Schedule.result.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ResultDTO {
  private Long resultNo;
  private Long scheduleNo;
  private LocalDateTime processDate;
  private String processContent;
  private String imagePath;
  private String imagePath2;
  // 처리사진을 하나도 첨부하지 않았을 때 남기는 사유 (선택 항목). 결과보고 화면의 라디오 프리셋 중 하나이거나,
  // "기타"를 골랐을 때 직접 입력한 텍스트가 그대로 들어옵니다. (ResultController.resultReg 참고)
  private String noPhotoReason;
  private LocalDateTime createdAt;
  // 결과보고를 등록한 뒤 내용을 "수정"한 시각. 등록 시점엔 null이고, 수정할 때마다 그 순간으로 갱신됩니다.
  // (처리날짜(processDate)는 최초 처리 시각으로 고정되고 수정해도 안 바뀌므로, 수정 이력은 이 필드로 따로 확인합니다)
  private LocalDateTime updatedAt;
  private String engineerName;

}

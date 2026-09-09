package com.green.Schedule.result.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ResultDTO {
  private Long resultNo;
  private Long scheduleNo;
  private LocalDateTime processDate;
  private String processContent;
  private String resultStatus;
  private String imagePath;
  private LocalDateTime createdAt;
}

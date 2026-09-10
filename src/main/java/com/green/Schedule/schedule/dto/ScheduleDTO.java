package com.green.Schedule.schedule.dto;

import lombok.Data;

import java.sql.Time;
import java.time.LocalDate;

@Data
public class ScheduleDTO {
  private Long scheduleNo;
  private Long requestNo;
  private Long engineerNo;
  private LocalDate fixDate;
  private Time startTime;
  private Time endTime;
  private String status;

}

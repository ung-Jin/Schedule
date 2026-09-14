package com.green.Schedule.schedule.dto;

import com.green.Schedule.engineer.DTO.EngineerDTO;
import com.green.Schedule.request.dto.RequestDTO;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class ScheduleDTO {
  private Long scheduleNo;    //스케줄 번호(기본키)
  private Long requestNo;     //AS 요청 고유번호(외래키)
  private Long engineerNo;    //기사 고유번호(외래키)

  @DateTimeFormat(pattern = "yyyy-MM-dd")
  private LocalDate fixDate;  //일정 배정 날짜

  @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
  private LocalDateTime startTime; //시작 예정 일시 (DB가 DATETIME이라 날짜+시간을 함께 받음)

  @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
  private LocalDateTime endTime;   //종료 예정 일시

  private String status;      //진행 상태
  private EngineerDTO engineerDTO;
  private RequestDTO requestDTO;
}
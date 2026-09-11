package com.green.Schedule.schedule.dto;

import com.green.Schedule.engineer.DTO.EngineerDTO;
import com.green.Schedule.request.dto.RequestDTO;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class ScheduleDTO {
  private Long scheduleNo;    //스케줄 번호(기본키)
  private Long requestNo;     //AS 요청 고유번호(외래키)
  private Long engineerNo;    //기사 고유번호(외래키)
  private LocalDate fixDate;  //일정 배정 날짜
  private LocalTime startTime; //시작 예정 시간
  private LocalTime endTime;   //종료 예정 시간
  private String status;      //진행 상태
  private EngineerDTO engineerDTO;
  private RequestDTO requestDTO;

}

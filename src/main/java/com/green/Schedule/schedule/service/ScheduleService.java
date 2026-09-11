package com.green.Schedule.schedule.service;

import com.green.Schedule.engineer.DTO.EngineerDTO;
import com.green.Schedule.request.dto.RequestDTO;
import com.green.Schedule.schedule.dto.ScheduleDTO;
import com.green.Schedule.schedule.mapper.ScheduleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ScheduleService {
  private final ScheduleMapper scheduleMapper;

  //관리자 AS 일정 관리 페이지
  //AS 기사 전체 일정 조회
  public List<RequestDTO> selectRequestList(){
   return scheduleMapper.selectRequestList();
  }

  //배정 전 AS 기사 일정 조회
  //status가 receive인 일정들 조회
  public List<RequestDTO> selectReceiveRequestList(){
    return scheduleMapper.selectReceiveRequestList();
  }

  //배정된 스케줄 조회
  public List<ScheduleDTO> selectScheduleList(){
    return scheduleMapper.selectScheduleList();
  }

  //AS 기사 배정을 위해 기사 전체 조회
  public List<EngineerDTO> selectEngineerList(){
    return scheduleMapper.selectEngineerList();
  }

  //관리자 AS 일정 배정
  //AS 기사 일정 배정
  public void insertSchedule(ScheduleDTO scheduleDTO){
    scheduleMapper.insertSchedule(scheduleDTO);
  }

  //신청 일정을 접수에서 배정완료로 수정
  //AS 기사 배정된 경우 AS_REQUEST 테이블 STATUS를 배정으로 업데이트
  public void updateReceiveRequest(Long requestNo){
    scheduleMapper.updateReceiveRequest(requestNo);
  }

}

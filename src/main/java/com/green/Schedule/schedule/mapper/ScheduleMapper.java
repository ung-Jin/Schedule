package com.green.Schedule.schedule.mapper;

import com.green.Schedule.engineer.DTO.EngineerDTO;
import com.green.Schedule.request.dto.RequestDTO;
import com.green.Schedule.schedule.dto.ScheduleDTO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ScheduleMapper {

  //관리자 AS 일정 관리 페이지
  //AS 기사 전체 일정 조회 쿼리 실행
  public List<RequestDTO> selectRequestList();

  //배정 전 AS 기사 일정 조회 쿼리 실행
  //status가 receive인 일정들 조회
  public List<RequestDTO> selectReceiveRequestList();

  //배정된 스케줄 조회 쿼리 실행
  public List<ScheduleDTO> selectScheduleList();

  //AS 기사 배정을 위해 기사 전체 조회 쿼리 실행
  public List<EngineerDTO> selectEngineerList();

  //관리자 AS 일정 배정
  //AS 기사 일정 배정 쿼리 실행
  public void insertSchedule(ScheduleDTO scheduleDTO);

  //신청 일정을 접수에서 배정완료로 수정
  //AS 기사 배정된 경우 AS_REQUEST 테이블 STATUS를 배정으로 업데이트 쿼리 실행
  public void updateReceiveRequest(Long requestNo);


}

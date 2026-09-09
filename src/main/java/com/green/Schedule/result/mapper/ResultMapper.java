package com.green.Schedule.result.mapper;

import com.green.Schedule.result.dto.ResultDTO;
import com.green.Schedule.result.dto.ScheduleDetailDTO;
import com.green.Schedule.result.dto.ScheduleTodayDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ResultMapper {
  //오늘 일정 조회
  List<ScheduleTodayDTO> selectToday(@Param("memNo") int memNo);

  //결과보고 화면 - AS 기본정보 조회
  ScheduleDetailDTO selectScheduleDetail(@Param("scheduleNo") long scheduleNo);

  //달력조회


  //기사 결과보고
  void insertResult(ResultDTO resultDTO);

}

package com.green.Schedule.result.service;

import com.green.Schedule.result.dto.ResultDTO;
import com.green.Schedule.result.dto.ScheduleDetailDTO;
import com.green.Schedule.result.dto.ScheduleTodayDTO;
import com.green.Schedule.result.mapper.ResultMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ResultService {
  private final ResultMapper resultMapper;

//  오늘 일정 조회
  public List<ScheduleTodayDTO> selectToday(int memNo){
    return resultMapper.selectToday(memNo);
  }

//  결과보고 화면 - AS 기본정보 조회
  public ScheduleDetailDTO selectScheduleDetail(long scheduleNo){
    return resultMapper.selectScheduleDetail(scheduleNo);
  }

//  as결과보고 삽입
  public void insertResult(ResultDTO resultDTO){
    resultMapper.insertResult(resultDTO);
  }

}

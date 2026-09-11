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

  // 오늘 일정 조회 (대시보드 화면에 뿌려줄 목록)
  public List<ScheduleTodayDTO> selectToday(int memNo){
    return resultMapper.selectToday(memNo);
  }

  // 결과보고 화면 - AS 기본정보 조회 (오른쪽 박스에 채워줄 정보)
  public ScheduleDetailDTO selectScheduleDetail(long scheduleNo){
    return resultMapper.selectScheduleDetail(scheduleNo);
  }

  // 이 일정이 로그인한 기사 본인 것이 맞는지 확인
  // (다른 기사가 남의 scheduleNo를 주소에 직접 넣어서 접근하는 것을 막기 위함)
  public boolean isMySchedule(long scheduleNo, int memNo){
    return resultMapper.countMySchedule(scheduleNo, memNo) > 0;
  }

  // AS결과보고 등록
  public void insertResult(ResultDTO resultDTO){
    resultMapper.insertResult(resultDTO);
  }

  // 대시보드 "진행예정" 버튼 -> AS_REQUEST 상태를 IN_PROGRESS로 변경
  public void startProgress(int requestNo){
    resultMapper.startProgress(requestNo);
  }

  // 결과보고 등록 완료 -> AS_REQUEST 상태를 COMPLETED로 변경
  public void completeRequest(int requestNo){
    resultMapper.completeRequest(requestNo);
  }

}

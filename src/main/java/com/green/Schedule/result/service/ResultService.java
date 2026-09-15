package com.green.Schedule.result.service;

import com.green.Schedule.result.dto.ResultDTO;
import com.green.Schedule.result.dto.ScheduleCalendarDTO;
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

  // 대시보드 "달력" - 이 기사(memNo)가 해당 연/월에 방문하는 일정 목록 (고객명/시간/주소/연락처 포함)
  // (프론트에서 달력 칸에 짧게 뿌려줄 일정 상세 -> as-result-calendar 컨트롤러에서 JSON으로 응답)
  public List<ScheduleCalendarDTO> selectCalender(int memNo, int year, int month){
    return resultMapper.selectCalender(memNo, year, month);
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

  // 결과 등록 후 만족도 문자 발송 - 고객 연락처 조회
  public String selectCustomerTel(int requestNo){
    return resultMapper.selectCustomerTel(requestNo);
  }

  // 결과 등록 후 만족도 문자 발송 - 로그인한 기사의 기사번호 조회
  public int selectEngineerNo(int memNo){
    return resultMapper.selectEngineerNo(memNo);
  }

  public List<ResultDTO> selectResults(){
    return resultMapper.selectResults();
  }
}

package com.green.Schedule.result.mapper;

import com.green.Schedule.result.dto.ResultDTO;
import com.green.Schedule.result.dto.ScheduleCalendarDTO;
import com.green.Schedule.result.dto.ScheduleDetailDTO;
import com.green.Schedule.result.dto.ScheduleTodayDTO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * SQL 내용은 여기가 아니라
 * resources/mapper/result-mapper.xml 에 작성합니다.
 */
@Mapper
public interface ResultMapper {

  // 오늘 일정 조회 (대시보드 화면)
  // memNo: 로그인한 기사의 회원번호(MEMBER.MEM_NO). 이 번호로 ENGINEER를 찾아서
  //        그 기사한테 배정된, 오늘 날짜인 AS_SCHEDULE만 조회합니다.
  // (파라미터가 하나뿐이라 @Param 없이도 xml에서 #{memNo}로 그대로 받을 수 있음)
  List<ScheduleTodayDTO> selectToday(int memNo);

  // 결과보고 화면 - AS 기본정보 조회
  // scheduleNo: 결과를 등록하려는 일정 번호(AS_SCHEDULE.SCHEDULE_NO)
  ScheduleDetailDTO selectScheduleDetail(long scheduleNo);

  // 이 일정(scheduleNo)이 로그인한 기사(memNo) 본인 것이 맞는지 확인 (내 것이면 1, 아니면 0)
  // 다른 기사의 scheduleNo를 주소에 직접 입력해서 남의 일정을 보거나 등록하는 걸 막기 위함
  int countMySchedule(long scheduleNo, int memNo);

  // 대시보드 "달력" - 이 기사(memNo)에게 해당 연/월(year, month)에 배정된 일정 목록
  // (날짜만 주는 게 아니라, 달력 칸에 바로 "고객명/시간/주소/연락처"를 짧게 보여주고
  //  클릭하면 결과보고 화면으로 이동시켜야 해서 일정 하나하나의 상세 정보를 통째로 내려줍니다)
  // (파라미터가 여러 개라도, 다른 다중 파라미터 메서드들처럼 @Param 없이 이름 그대로 xml의 #{memNo}/#{year}/#{month}에 매칭됩니다)
  List<ScheduleCalendarDTO> selectCalender(int memNo, int year, int month);


  // 기사 결과보고 등록 (AS_RESULT INSERT)
  void insertResult(ResultDTO resultDTO);

  // 대시보드 "진행예정" 버튼 -> AS_REQUEST 상태를 IN_PROGRESS로 변경
  void startProgress(int requestNo);

  // 결과보고 등록 완료 -> AS_REQUEST 상태를 COMPLETED로 변경
  void completeRequest(int requestNo);

  // 결과 등록 후 만족도 문자 발송 - 고객 연락처 조회
  String selectCustomerTel(int requestNo);

  // 결과 등록 후 만족도 문자 발송 - 로그인한 기사의 기사번호 조회
  int selectEngineerNo(int memNo);
}

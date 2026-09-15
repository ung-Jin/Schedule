package com.green.Schedule.result.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 대시보드 "이번 달 일정" 달력에서 쓰는 DTO입니다.
 * 하루 전체를 색칠하는 게 아니라, 일정 하나하나를 칸 안에 짧은 정보로 보여주고
 * 클릭하면 바로 그 일정의 결과보고 화면으로 이동시키기 위해 필요한 값만 담습니다.
 *
 * scheduleNo   -> AS_SCHEDULE.SCHEDULE_NO (클릭 시 /as-result-report?scheduleNo=.. 로 이동할 때 씀)
 * customerName -> REQUEST.CUSTOMER_NAME
 * customerTel  -> REQUEST.CUSTOMER_TEL
 * customerAddr -> REQUEST.CUSTOMER_ADDR
 * startTime    -> AS_SCHEDULE.START_TIME
 * endTime      -> AS_SCHEDULE.END_TIME
 * status       -> REQUEST.STATUS (완료(COMPLETED) 건인지 프론트에서 구분해서, 클릭 시
 *                 결과보고 "작성" 화면(/as-result-report) 대신 "조회"(읽기전용) 화면(/as-result-view)으로 보내기 위함)
 */
@Data
public class ScheduleCalendarDTO {
    private int scheduleNo;
    private String customerName;
    private String customerTel;
    private String customerAddr;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status;
}

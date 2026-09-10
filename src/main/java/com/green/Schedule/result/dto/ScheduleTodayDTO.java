package com.green.Schedule.result.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 대시보드(오늘 일정) 화면에서 쓰는 DTO입니다.
 * AS_SCHEDULE 테이블 + AS_REQUEST 테이블을 조인해서 나온 결과 한 줄을 담습니다.
 * (실제 테이블 컬럼이 아니라, mapper의 SELECT문에서 별칭(AS)으로 만든 값들입니다)
 *
 * scheduleNo    -> AS_SCHEDULE.SCHEDULE_NO (일정 번호)
 * requestNo     -> AS_REQUEST.REQUEST_NO   (접수 번호, 진행상태 변경할 때 씀)
 * requestStatus -> AS_REQUEST.STATUS       (RECEIVED/ASSIGNED/IN_PROGRESS/COMPLETED/CANCELED)
 * startTime     -> AS_SCHEDULE.START_TIME  (방문 시작 시각)
 * endTime       -> AS_SCHEDULE.END_TIME    (방문 종료 시각)
 * customerName  -> AS_REQUEST.CUSTOMER_NAME (고객명)
 * symptom       -> AS_REQUEST.SYMPTOM       (고장 증상)
 */
@Data
public class ScheduleTodayDTO {
    private int scheduleNo;
    private int requestNo;
    private String requestStatus;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String customerName;
    private String symptom;
}

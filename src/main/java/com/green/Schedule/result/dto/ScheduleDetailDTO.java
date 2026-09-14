package com.green.Schedule.result.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 결과보고 화면 오른쪽 "AS 기본 정보" 박스에서 쓰는 DTO입니다.
 * 화면에 들어가기 전 기사가 어떤 일정(scheduleNo)의 결과를 등록하려는 건지 알아야 해서,
 * AS_SCHEDULE 테이블 + AS_REQUEST 테이블을 조인해서 접수 정보를 한 번에 가져옵니다.
 *
 * scheduleNo    -> AS_SCHEDULE.SCHEDULE_NO (일정 번호)
 * requestNo     -> AS_REQUEST.REQUEST_NO   (접수 번호)
 * customerName  -> AS_REQUEST.CUSTOMER_NAME (고객명)
 * customerTel   -> AS_REQUEST.CUSTOMER_TEL  (연락처)
 * customerAddr  -> AS_REQUEST.CUSTOMER_ADDR (주소)
 * productType   -> AS_REQUEST.PRODUCT_TYPE  (제품 종류)
 * symptom       -> AS_REQUEST.SYMPTOM       (고장 증상)
 * requestDate   -> AS_REQUEST.REQUEST_DATE  (접수일시 - 고객 요청이 시스템에 접수된 시각)
 * visitStart     -> AS_SCHEDULE.START_TIME   (실제 배정된 방문 시작 시각)
 * visitEnd       -> AS_SCHEDULE.END_TIME     (실제 배정된 방문 종료 시각)
 */
@Data
public class ScheduleDetailDTO {
    private int scheduleNo;
    private int requestNo;
    private String customerName;
    private String customerTel;
    private String customerAddr;
    private String productType;
    private String symptom;
    private LocalDateTime requestDate;
    private LocalDateTime visitStart;
    private LocalDateTime visitEnd;
}

package com.green.Schedule.request.dto;

import lombok.Data;

import java.rmi.StubNotFoundException;

/**
 * REPAIR_REQUEST 테이블 한 행(row)을 담는 DTO입니다.
 *
 * REQUEST_NO     -> requestNo    (자동 증가, DB가 알아서 채워줌)
 * CUSTOMER_NAME  -> customerName (신청자 이름)
 * CUSTOMER_TEL   -> customerTel  (연락처)
 * CUSTOMER_ADDR  -> customerAddr (주소)
 * PRODUCT_TYPE   -> productType  (제품 종류)
 * SYMPTOM        -> symptom      (고장 증상)
 * REQUEST_DATE   -> requestDate  (접수 날짜, yyyy-MM-dd) - 서버에서 자동으로 채움
 * WISH_DATE      -> wishDate     (희망 날짜, yyyy-MM-dd) - 사용자가 입력
 *
 * 날짜를 다루는 여러 자바 타입(Date, LocalDate 등) 대신,
 * 처음 배울 땐 헷갈리기 쉬워서 문자열(String)로 간단하게 다룹니다.
 */
@Data
public class RequestDTO {

    private int requestNo;
    private String customerName;
    private String customerTel;
    private String customerAddr;
    private String productType;
    private String symptom;
    private String requestDate;
    private String wishDate;
    private String status;

}

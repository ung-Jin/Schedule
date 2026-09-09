package com.green.Schedule.result.dto;

import lombok.Data;

import java.time.LocalDateTime;

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
}

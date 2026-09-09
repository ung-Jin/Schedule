package com.green.Schedule.result.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ScheduleTodayDTO {
    private int scheduleNo;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String customerName;
    private String symptom;
}

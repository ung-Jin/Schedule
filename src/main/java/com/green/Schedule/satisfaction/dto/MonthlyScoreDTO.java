package com.green.Schedule.satisfaction.dto;

import lombok.Data;

/**
 * 대시보드 "올해 만족도 추이" 그래프 한 칸(달) 분량 데이터입니다.
 * month    -> 몇 월인지 (1~12)
 * avgScore -> 그 달의 평균 점수 (제출된 설문이 없으면 null)
 * count    -> 그 달의 설문 제출 건수
 */
@Data
public class MonthlyScoreDTO {
    private int month;
    private Double avgScore;
    private int count;
}

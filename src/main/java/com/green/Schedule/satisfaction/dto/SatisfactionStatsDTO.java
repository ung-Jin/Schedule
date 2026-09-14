package com.green.Schedule.satisfaction.dto;

import lombok.Data;

import java.util.List;

/**
 * 대시보드 "고객 만족도" 카드 하나를 통째로 그리는 데 필요한 값을 모아둔 DTO입니다.
 * (여러 쿼리 결과를 SatisfactionService가 조합해서 만듭니다)
 *
 * thisMonth    -> 지금 몇 월인지 (화면에 "9월" 처럼 라벨을 붙이는 용도)
 * thisMonthAvg -> 이번 달 평균 점수 (설문이 하나도 없으면 null)
 * lastYearAvg  -> 작년 같은 달 평균 점수 (설문이 하나도 없으면 null)
 * diff         -> thisMonthAvg - lastYearAvg (둘 다 있을 때만 계산, 아니면 null)
 * monthlyTrend -> 올해 1월~이번 달까지 월별 평균 점수 목록 (막대 그래프용)
 */
@Data
public class SatisfactionStatsDTO {
    private int thisMonth;
    private Double thisMonthAvg;
    private Double lastYearAvg;
    private Double diff;
    private List<MonthlyScoreDTO> monthlyTrend;
}

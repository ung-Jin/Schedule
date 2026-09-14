package com.green.Schedule.satisfaction.service;

import com.green.Schedule.satisfaction.dto.MonthlyScoreDTO;
import com.green.Schedule.satisfaction.dto.SatisfactionDTO;
import com.green.Schedule.satisfaction.dto.SatisfactionStatsDTO;
import com.green.Schedule.satisfaction.mapper.SatisfactionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SatisfactionService {
  private final SatisfactionMapper satisfactionMapper;

  // 만족도 조사 제출 저장 (고객이 /survey에서 별점+후기를 보내면 여기로 옴)
  public void insertSatisfaction(SatisfactionDTO satisfactionDTO) {
    satisfactionMapper.insertSatisfaction(satisfactionDTO);
  }

  // 대시보드 "고객 만족도" 카드에 필요한 값을 한 번에 모아서 돌려줍니다.
  // (이번 달 평균 / 작년 동월 평균 / 증감 / 올해 1월~이번 달 월별 추이)
  public SatisfactionStatsDTO getStats(int engineerNo) {
    LocalDate today = LocalDate.now();
    int year = today.getYear();
    int month = today.getMonthValue();

    Double thisMonthAvg = satisfactionMapper.selectMonthlyAvg(engineerNo, year, month);
    Double lastYearAvg = satisfactionMapper.selectMonthlyAvg(engineerNo, year - 1, month);

    SatisfactionStatsDTO stats = new SatisfactionStatsDTO();
    stats.setThisMonth(month);
    stats.setThisMonthAvg(thisMonthAvg);
    stats.setLastYearAvg(lastYearAvg);
    // 둘 다 데이터가 있을 때만 증감을 계산합니다 (한쪽이라도 없으면 비교 자체가 의미 없어서 null로 둠)
    if (thisMonthAvg != null && lastYearAvg != null) {
      stats.setDiff(Math.round((thisMonthAvg - lastYearAvg) * 10) / 10.0);
    }

    // DB에는 설문이 있었던 달만 돌아오므로, 1월~이번 달까지 빠진 달을 0으로 채워서
    // 그래프에서 막대가 몇 월인지 안 헷갈리게 만듭니다.
    List<MonthlyScoreDTO> rawTrend = satisfactionMapper.selectYearlyTrend(engineerNo, year);
    Map<Integer, MonthlyScoreDTO> byMonth = new HashMap<>();
    for (MonthlyScoreDTO m : rawTrend) {
      byMonth.put(m.getMonth(), m);
    }

    List<MonthlyScoreDTO> trend = new ArrayList<>();
    for (int m = 1; m <= month; m++) {
      MonthlyScoreDTO found = byMonth.get(m);
      if (found != null) {
        trend.add(found);
      } else {
        MonthlyScoreDTO empty = new MonthlyScoreDTO();
        empty.setMonth(m);
        empty.setAvgScore(0.0);
        empty.setCount(0);
        trend.add(empty);
      }
    }
    stats.setMonthlyTrend(trend);

    return stats;
  }
}

package com.green.Schedule.satisfaction.mapper;

import com.green.Schedule.satisfaction.dto.MonthlyScoreDTO;
import com.green.Schedule.satisfaction.dto.SatisfactionDTO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * SQL 내용은 여기가 아니라 resources/mapper/satisfaction-mapper.xml 에 작성합니다.
 */
@Mapper
public interface SatisfactionMapper {

  // 만족도 조사 제출 저장 (/survey POST)
  void insertSatisfaction(SatisfactionDTO satisfactionDTO);

  // 특정 연/월의 평균 점수 (설문이 없으면 null) - "이번 달", "작년 동월" 조회에 둘 다 씀
  Double selectMonthlyAvg(int engineerNo, int year, int month);

  // 해당 연도의 월별 평균 점수 목록 (설문이 있는 달만 돌아옴 - 없는 달은 서비스단에서 0/null로 채움)
  List<MonthlyScoreDTO> selectYearlyTrend(int engineerNo, int year);
}

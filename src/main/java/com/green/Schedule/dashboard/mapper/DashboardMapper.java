package com.green.Schedule.dashboard.mapper;

import com.green.Schedule.dashboard.DTO.DashboardStatDTO;
import com.green.Schedule.dashboard.DTO.RecentAsDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DashboardMapper {

  //상단 카드용 상태별 건수 조회 (한 번의 쿼리로 4개 상태 집계)
  DashboardStatDTO selectStats();

  //최근 AS 접수 목록 조회 (한 페이지에 5건)
  List<RecentAsDTO> selectRecentList(@Param("offset") int offset, @Param("limit") int limit);

  //최근 AS 전체 건수 (페이지네이션 계산용)
  int selectRecentCount();

  //AS 접수 상세 조회 (모달용)
  RecentAsDTO selectRecentDetail(int requestNo);
}

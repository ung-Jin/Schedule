package com.green.Schedule.dashboard.service;

import com.green.Schedule.dashboard.DTO.DashboardStatDTO;
import com.green.Schedule.dashboard.DTO.RecentAsDTO;
import com.green.Schedule.dashboard.mapper.DashboardMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DashboardService {
  private final DashboardMapper dashboardMapper;

  //한 페이지에 보여줄 건수 (요구사항: 5)
  private static final int PAGE_SIZE = 5;

  //상단 상태별 건수 조회
  public DashboardStatDTO getDashboardStats() {
    return dashboardMapper.selectStats();
  }

  //최근 AS 접수 목록 조회 (페이지네이션 포함)
  public Map<String, Object> getRecentAsList(int page) {
    //잘못된 페이지 번호 방어 (0 이하로 들어오면 1페이지로 보정)
    if(page < 1){
      page = 1;
    }

    //MySQL OFFSET 계산 (1페이지=0, 2페이지=5, 3페이지=10 ...)
    int offset = (page - 1) * PAGE_SIZE;

    //전체 건수 조회 + 목록 조회
    int totalCount = dashboardMapper.selectRecentCount();
    List<RecentAsDTO> list = dashboardMapper.selectRecentList(offset, PAGE_SIZE);

    //전체 페이지 수 계산 (13건이면 3페이지, 25건이면 5페이지)
    int totalPages;
    if(totalCount == 0){
      totalPages = 1;
    } else {
      totalPages = (totalCount + PAGE_SIZE - 1) / PAGE_SIZE;
    }

    //응답 데이터 조립 (JSON 응답용)
    Map<String, Object> result = new HashMap<>();
    result.put("list", list);
    result.put("currentPage", page);
    result.put("totalPages", totalPages);
    result.put("totalCount", totalCount);
    return result;
  }

  //AS 접수 상세 조회 (모달용)
  public RecentAsDTO getRecentAsDetail(int requestNo) {
    return dashboardMapper.selectRecentDetail(requestNo);
  }
}

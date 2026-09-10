package com.green.Schedule.dashboard.controller;

import com.green.Schedule.dashboard.DTO.DashboardStatDTO;
import com.green.Schedule.dashboard.DTO.RecentAsDTO;
import com.green.Schedule.dashboard.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController  //비동기
@RequiredArgsConstructor
@RequestMapping("/dashboard-api")
public class DashboardApiController {
  private final DashboardService dashboardService;

  //상태별 건수 조회
  @GetMapping("/stats")
  public DashboardStatDTO getStats() {
    return dashboardService.getDashboardStats();
  }

  //최근 AS 접수 목록 조회 (한 페이지에 5건)
  @GetMapping("/recent")
  public Map<String, Object> getRecent(@RequestParam(value = "page", defaultValue = "1") int page) {
    return dashboardService.getRecentAsList(page);
  }

  //AS 접수 상세 조회 (모달용) - AS요청 고유번호 기준
  @GetMapping("/detail/{requestNo}")
  public RecentAsDTO getDetail(@PathVariable("requestNo") int requestNo) {
    return dashboardService.getRecentAsDetail(requestNo);
  }
}

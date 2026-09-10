package com.green.Schedule.dashboard.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

  //관리자 대시보드 페이지 (실제 데이터는 axios로 별도 조회)
  @GetMapping("/admin/dashboard")
  public String dashboardPage() {
    return "pages/admin2/dashboard";
  }
}

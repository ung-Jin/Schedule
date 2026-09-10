package com.green.Schedule.result.controller;

import com.green.Schedule.result.service.ResultService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class ResultController {
  private final ResultService resultService;

  //a.s기사 대시보드
  @GetMapping("/as-result-dash-board")
  public String asResultDashboard(){
    return "pages/result/result_dashboard";
  }

  //a.s기사 결과보고
  @GetMapping("/as-result-report")
  public String asResultReport(){

    return "pages/result/result_report";
  }

}

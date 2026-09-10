package com.green.Schedule.schedule.controller;

import com.green.Schedule.schedule.service.ScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.sql.Struct;

@Controller
@RequiredArgsConstructor
public class ScheduleController {
  private final ScheduleService scheduleService;

  //관리자 AS 일정 관리 페이지
  @GetMapping("/as-schedule")
  public String asSchedule(){

    return "pages/schedule/admin_schedule";
  }

  //관리자 AS 일정 배정

}

package com.green.Schedule.member.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/pages/admin")
public class EngineerController {

  @GetMapping("/main")
  public String main() {
    return "pages/result/result_dashboard";
  }
}

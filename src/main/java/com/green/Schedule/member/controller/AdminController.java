package com.green.Schedule.member.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/pages/admin")
public class AdminController {

  @GetMapping("/main")
  public String main() {
    return "pages/admin2/dashboard";
  }

  @GetMapping("/management")
  public String management(){
    return "redirect:/admin/engineer";
  }
}

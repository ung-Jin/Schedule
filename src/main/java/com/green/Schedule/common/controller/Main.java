package com.green.Schedule.common.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class Main {

  // 시작페이지를 결정하는 메서드
  @GetMapping("/")
  public String main() {
    return "main";
  }
}
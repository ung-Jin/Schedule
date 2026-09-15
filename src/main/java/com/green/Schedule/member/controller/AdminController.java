package com.green.Schedule.member.controller;

import com.green.Schedule.result.service.ResultService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/pages/admin")
public class AdminController {

  @Autowired
  private ResultService resultService;

  @GetMapping("/main")
  public String main() {
    return "pages/admin2/dashboard";
  }

  @GetMapping("/calendar")
  public String calendar(){
    return "redirect:/as-schedule";
  }

  @GetMapping("/management")
  public String management(){
    return "redirect:/admin/engineer";
  }

  // 관리자 "결과내역" 화면 - 전체 AS 결과보고 목록(처리기사 이름 포함)을 그대로 보여줍니다.
  // (예전엔 "redirect:/sideResult"로 자기 자신한테 리다이렉트만 하고 실제로 받아주는 주소가 없어서
  //  들어가면 아무 일도 안 일어났는데, 이제 pages/admin2/result.html에 목록을 채워서 바로 렌더링합니다)
  @GetMapping("/sideResult")
  public String result(Model model){
    model.addAttribute("resultList", resultService.selectResultList());
    return "pages/admin2/result";
  }
}

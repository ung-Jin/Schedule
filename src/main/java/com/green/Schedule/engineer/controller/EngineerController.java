package com.green.Schedule.engineer.controller;

import com.green.Schedule.engineer.DTO.EngineerDTO;
import com.green.Schedule.engineer.service.EngineerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class EngineerController {
  private final EngineerService engineerService;

  //기사관리
  @GetMapping("/admin/engineer")
  public String engineerListPage(Model model){
    List<EngineerDTO> engineerList = engineerService.engineerList();
    model.addAttribute("engineerList", engineerList);
    return "pages/admin2/engineer_list";
  }
}

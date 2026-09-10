package com.green.Schedule.engineer.controller;

import com.green.Schedule.engineer.DTO.EngineerDTO;
import com.green.Schedule.engineer.service.EngineerService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController  //비동기
@RequiredArgsConstructor
@RequestMapping("/engineer-api")
public class EngineerApiController {
  private final EngineerService engineerService;

  //기사 상세 조회
  @GetMapping("/detail/{engineerNo}")
  public EngineerDTO engineerDetail(@PathVariable("engineerNo") int engineerNo){
    return engineerService.engineerDetail(engineerNo);
  }

  //기사 등록
  @PostMapping("/insert")
  public boolean insertEngineer(@RequestBody EngineerDTO engineerDTO){
    engineerService.insertEngineer(engineerDTO);
    return true;
  }

  //기사 정보 수정
  @PostMapping("/update")
  public boolean updateEngineer(@RequestBody EngineerDTO engineerDTO){
    engineerService.updateEngineer(engineerDTO);
    return true;
  }

  //기사 삭제
  @GetMapping("/delete/{engineerNo}")
  public boolean deleteEngineer(@PathVariable("engineerNo") int engineerNo){
    engineerService.deleteEngineer(engineerNo);
    return true;
  }
}

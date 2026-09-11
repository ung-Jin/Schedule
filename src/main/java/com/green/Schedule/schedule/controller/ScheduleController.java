package com.green.Schedule.schedule.controller;

import com.green.Schedule.engineer.DTO.EngineerDTO;
import com.green.Schedule.request.dto.RequestDTO;
import com.green.Schedule.schedule.dto.ScheduleDTO;
import com.green.Schedule.schedule.service.ScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.sql.Struct;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class ScheduleController {
  private final ScheduleService scheduleService;

  //관리자 AS 일정 관리 페이지
  @GetMapping("/as-schedule")
  public String asSchedule(Model model){

    //AS 기사 전체 일정 조회 후 전달
    List<RequestDTO> requestList = scheduleService.selectRequestList();
    model.addAttribute("requestList", requestList);

    //배정 전 AS 기사 일정 조회 후 전달
    List<RequestDTO> receiveList = scheduleService.selectReceiveRequestList();
    model.addAttribute("receiveList", receiveList);

    //배정된 스케줄 조회 후 전달
    List<ScheduleDTO> scheduleList = scheduleService.selectScheduleList();
    model.addAttribute("scheduleList",scheduleList);

    //AS 기사 전체 조회 후 전달
    List<EngineerDTO> engineerList = scheduleService.selectEngineerList();
    model.addAttribute("engineerList",engineerList);

    //관리자 일정 관리 페이지로 이동
    return "pages/schedule/admin_schedule";
  }

  //관리자 AS 일정 배정
  @PostMapping("/assign-schedule")
  public String assignSchedule(ScheduleDTO scheduleDTO){

    //AS 기사 일정 배정
    scheduleService.insertSchedule(scheduleDTO);

    //신청 일정을 접수에서 배정완료로 수정
    //AS_REQUEST 테이블 STATUS를 배정으로 업데이트
    Long requestNo = scheduleDTO.getRequestNo();
    scheduleService.updateReceiveRequest(requestNo);

    //관리자 AS 일정 관리 페이지로 다시 이동
    return "redirect:/as-schedule";
  }


}

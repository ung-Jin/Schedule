package com.green.Schedule.result.controller;

import com.green.Schedule.member.dto.MemberDTO;
import com.green.Schedule.result.dto.ResultDTO;
import com.green.Schedule.result.service.ResultService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class ResultController {
  private final ResultService resultService;

  private static final Path UPLOAD_DIR = Paths.get("uploads", "result");

  //a.s기사 대시보드
  @GetMapping("/as-result-dash-board")
  public String asResultDashboard(HttpSession session, Model model){
    MemberDTO loginMember = (MemberDTO) session.getAttribute("loginMember");
    if (loginMember == null) {
      return "redirect:/member/login";
    }

    model.addAttribute("todayList", resultService.selectToday(loginMember.getMemNo()));

    return "pages/result/result_dashboard";
  }

  //a.s기사 결과보고
  @GetMapping("/as-result-report")
  public String asResultReport(@RequestParam(value = "scheduleNo", required = false) Long scheduleNo,
                                HttpSession session, Model model){
    if (session.getAttribute("loginMember") == null) {
      return "redirect:/member/login";
    }

    if (scheduleNo != null) {
      model.addAttribute("scheduleInfo", resultService.selectScheduleDetail(scheduleNo));
    }

    return "pages/result/result_report";
  }

  //a.s기사 결과보고 등록
  @PostMapping("/result/reg")
  public String resultReg(@ModelAttribute ResultDTO resultDTO,
                           @RequestParam(value = "resultImage", required = false) MultipartFile resultImage,
                           HttpSession session) throws IOException {
    if (session.getAttribute("loginMember") == null) {
      return "redirect:/member/login";
    }

    if (resultImage != null && !resultImage.isEmpty()) {
      Files.createDirectories(UPLOAD_DIR);

      String ext = "";
      String originalName = resultImage.getOriginalFilename();
      if (originalName != null && originalName.contains(".")) {
        ext = originalName.substring(originalName.lastIndexOf("."));
      }
      String savedName = UUID.randomUUID() + ext;

      Path savedPath = UPLOAD_DIR.resolve(savedName);
      resultImage.transferTo(savedPath);

      resultDTO.setImagePath("/uploads/result/" + savedName);
    }

    resultService.insertResult(resultDTO);

    return "redirect:/as-result-dash-board";
  }

}

package com.green.Schedule.satisfaction.controller;

import com.green.Schedule.engineer.DTO.EngineerDTO;
import com.green.Schedule.engineer.service.EngineerService;
import com.green.Schedule.satisfaction.dto.SatisfactionDTO;
import com.green.Schedule.satisfaction.service.SatisfactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * 결과 등록 후 고객에게 문자로 보내는 만족도 조사 페이지입니다.
 * 로그인한 직원이 아니라 문자를 받은 "고객"이 들어오는 화면이라 로그인 검사 대상에서 제외되어 있습니다
 * (WebConfig의 LoginCheckInterceptor excludePathPatterns에 "/survey/**" 추가해둠).
 */
@Controller
@RequiredArgsConstructor
public class SatisfactionController {
  private final SatisfactionService satisfactionService;
  private final EngineerService engineerService;

  // 만족도 조사 폼 (문자 속 링크로 진입: /survey?scheduleNo=..&engineerNo=..)
  @GetMapping("/survey")
  public String surveyForm(SatisfactionDTO satisfactionDTO, Model model) {
    // 링크가 잘못되었거나(engineerNo가 없거나) 이미 지난 링크를 다시 눌러도 에러 화면 대신
    // "링크가 올바르지 않습니다" 안내만 보여주고 조용히 끝냅니다.
    EngineerDTO engineer = satisfactionDTO.getEngineerNo() > 0
        ? engineerService.engineerDetail(satisfactionDTO.getEngineerNo())
        : null;

    if (engineer == null) {
      model.addAttribute("invalidLink", true);
      return "pages/satisfaction/survey";
    }

    model.addAttribute("engineerName", engineer.getEngineerName());
    model.addAttribute("scheduleNo", satisfactionDTO.getScheduleNo());
    model.addAttribute("engineerNo", satisfactionDTO.getEngineerNo());
    return "pages/satisfaction/survey";
  }

  // 만족도 조사 제출
  @PostMapping("/survey")
  public String surveySubmit(@ModelAttribute SatisfactionDTO satisfactionDTO, Model model) {
    // 항목 3개 중 하나라도 1~5 범위를 벗어나면(직접 URL을 조작한 경우 등) 저장하지 않고 폼으로 돌려보냄
    if (!isValidScore(satisfactionDTO.getScoreVisit())
        || !isValidScore(satisfactionDTO.getScoreKindness())
        || !isValidScore(satisfactionDTO.getScoreResult())
        || satisfactionDTO.getEngineerNo() <= 0) {
      model.addAttribute("invalidLink", true);
      return "pages/satisfaction/survey";
    }

    satisfactionService.insertSatisfaction(satisfactionDTO);

    model.addAttribute("submitted", true);
    return "pages/satisfaction/survey";
  }

  private boolean isValidScore(int score) {
    return score >= 1 && score <= 5;
  }
}

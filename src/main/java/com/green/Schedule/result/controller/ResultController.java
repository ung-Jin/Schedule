package com.green.Schedule.result.controller;

import com.green.Schedule.member.dto.MemberDTO;
import com.green.Schedule.request.dto.RequestDTO;
import com.green.Schedule.result.dto.ResultDTO;
import com.green.Schedule.result.dto.ScheduleCalendarDTO;
import com.green.Schedule.result.service.ResultService;
import com.green.Schedule.result.util.UploadUtil;
import com.green.Schedule.satisfaction.service.SatisfactionService;
import com.solapi.sdk.SolapiClient;
import com.solapi.sdk.message.exception.SolapiMessageNotReceivedException;
import com.solapi.sdk.message.model.Message;
import com.solapi.sdk.message.service.DefaultMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpSession;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class ResultController {
  private final ResultService resultService;
  private final SatisfactionService satisfactionService;
  // 처리사진 업로드는 UploadUtil이 전담 (application.yaml의 file.upload.dir 경로 사용)
  private final UploadUtil uploadUtil;

  // 만족도 조사 문자 링크에 쓸 "고객이 실제로 열 수 있는" 공개 주소.
  // 기사님이 localhost로 개발/테스트하시든 무엇으로 접속하시든 상관없이,
  // 고객한테 나가는 링크는 항상 이 설정값 하나로 고정됩니다 (application.yaml의 app.public-base-url).
  @Value("${app.public-base-url:http://localhost:8080}")
  private String publicBaseUrl;

  // a/s기사 대시보드, 로그인 된 상태에서만 보이게 세팅
  @GetMapping("/as-result-dash-board")

  public String asResultDashboard(HttpSession session, Model model){
    MemberDTO loginMember = getRepairmanOrNull(session);
    if (loginMember == null) {
      return "redirect:/member/login";
    }
    // 로그인한 기사의 회원번호(memNo)로 오늘 일정만 조회해서 화면에 전달
    model.addAttribute("todayList", resultService.selectToday(loginMember.getMemNo()));
    // "고객 만족도" 카드 제목에 쓸 이름 ("OOO기사님 고객만족도 현황")
    model.addAttribute("memName", loginMember.getMemName());

    // "오늘의 일정" 아래 "고객 만족도" 카드용 통계 (이번 달/작년 동월 비교 + 올해 추이)
    // memNo가 아니라 engineerNo 기준으로 집계해야 해서 먼저 engineerNo를 구합니다.
    int engineerNo = resultService.selectEngineerNo(loginMember.getMemNo());
    model.addAttribute("satisfactionStats", satisfactionService.getStats(engineerNo));

    return "pages/result/result_dashboard";
  }

  // 대시보드 "이번 달 일정" 달력 - FullCalendar가 보여주는 달이 바뀔 때마다 이 주소를 다시 불러서
  // 그 연/월에 로그인한 기사에게 배정된 일정(고객명/시간/주소/연락처)을 JSON으로 받아갑니다. (result_dashboard.js 참고)
  @GetMapping("/as-result-calendar")
  @ResponseBody
  public List<ScheduleCalendarDTO> asResultCalendar(@RequestParam int year, @RequestParam int month, HttpSession session){
    MemberDTO loginMember = getRepairmanOrNull(session);
    if (loginMember == null) {
      return List.of();
    }
    return resultService.selectCalender(loginMember.getMemNo(), year, month);
  }

  // a/s기사 결과보고 화면, 대시보드에서 결과등록 누르면 이쪽으로 옴
  @GetMapping("/as-result-report")
  public String asResultReport(@RequestParam(value = "scheduleNo", required = false) Long scheduleNo,
                                HttpSession session, Model model){
    MemberDTO loginMember = getRepairmanOrNull(session);
    if (loginMember == null) {
      return "redirect:/member/login";
    }
    // scheduleNo가 넘어왔으면, 그게 내(로그인한 기사) 일정이 맞는지 먼저 확인하고
    // 맞을 때만 화면 오른쪽 "AS 기본 정보"에 채울 데이터를 조회
    // (다른 기사의 scheduleNo를 주소에 직접 넣어서 남의 정보를 보는 것을 막기 위함)
    if (scheduleNo != null) {
      if (!resultService.isMySchedule(scheduleNo, loginMember.getMemNo())) {
        return "redirect:/as-result-dash-board";
      }
      model.addAttribute("scheduleInfo", resultService.selectScheduleDetail(scheduleNo));
    }
    return "pages/result/result_report";
  }

  // 대시보드 "진행예정" 버튼 처리
  // 버튼을 누르면 AS_REQUEST 상태가 RECEIVED/ASSIGNED -> IN_PROGRESS(진행중)로 바뀝니다.
  @PostMapping("/as-result/start")
  public String startProgress(@RequestParam("requestNo") int requestNo,
                               @RequestParam("scheduleNo") long scheduleNo,
                               HttpSession session){
    MemberDTO loginMember = getRepairmanOrNull(session);
    if (loginMember == null) {
      return "redirect:/member/login";
    }
    if (!resultService.isMySchedule(scheduleNo, loginMember.getMemNo())) {
      return "redirect:/as-result-dash-board";
    }

    resultService.startProgress(requestNo);

    return "redirect:/as-result-dash-board";
  }

  // a/s기사 결과보고 등록 처리 (처리사진은 최대 2장까지 첨부 가능)
  @PostMapping("/result/reg")
  public String resultReg(@ModelAttribute ResultDTO resultDTO,
                           @RequestParam("requestNo") int requestNo,
                           @RequestParam(value = "resultImage", required = false) MultipartFile resultImage,
                           @RequestParam(value = "resultImage2", required = false) MultipartFile resultImage2,
                           HttpSession session) {
    MemberDTO loginMember = getRepairmanOrNull(session);
    if (loginMember == null) {
      return "redirect:/member/login";
    }
    // 내 일정이 맞는지 확인 (남의 scheduleNo로 결과를 등록해버리는 것을 막기 위함)
    if (resultDTO.getScheduleNo() == null
        || !resultService.isMySchedule(resultDTO.getScheduleNo(), loginMember.getMemNo())) {
      return "redirect:/as-result-dash-board";
    }

    // 사진 1, 사진 2 각각 저장하고 DB에 넣을 경로를 DTO에 담아줌
    resultDTO.setImagePath(uploadUtil.fileUpload(resultImage));
    resultDTO.setImagePath2(uploadUtil.fileUpload(resultImage2));

    resultService.insertResult(resultDTO);

    // 결과 등록까지 끝났으니 AS_REQUEST 상태를 IN_PROGRESS(진행중) -> COMPLETED(완료)로 변경
    resultService.completeRequest(requestNo);

    // 등록 끝나면 다시 대시보드로 이동
    //return "redirect:/as-result-dash-board";

    //결과보고를 등록하면 문자 발송하러 이동 (문자 내용 만들 때 필요한 requestNo/scheduleNo를 같이 넘겨줌)
    return "redirect:/to-msg?requestNo=" + requestNo
        + "&scheduleNo=" + resultDTO.getScheduleNo()
        + "&memNo=" + loginMember.getMemNo();
  }

  // 결과 등록 후 고객한테 만족도 조사 문자 발송
  // resultReg()에서 "redirect:/to-msg?requestNo=..&scheduleNo=..&memNo=.." 로 넘어올 때
  // requestNo/scheduleNo는 RequestDTO에, memNo는 MemberDTO에 각각 자동으로 바인딩됩니다.
  // (RequestDTO/MemberDTO에 있는 다른 필드들은 쿼리 파라미터로 안 넘어와서 전부 기본값(null/0)입니다.)
  @GetMapping("/to-msg")
  public String sendMsg(RequestDTO requestDTO, MemberDTO memberDTO,
                         @RequestParam(value = "scheduleNo", required = false) Long scheduleNo){
    DefaultMessageService messageService =  SolapiClient.INSTANCE.createInstance("NCSKUPDBEGMQEN9B", "VQTCMTT3VPSVPMRUAPBOD4JORBQTJ7VC");

    // 문자를 받을 사람 = 이번에 처리한 AS 건의 고객 연락처
    // requestDTO.getCustomerTel()은 위 주석처럼 쿼리 파라미터로 안 넘어와서 항상 비어있으므로 쓰면 안 되고,
    // requestNo로 REQUEST 테이블을 직접 조회해서 실제 연락처를 가져와야 합니다.
    String customerTel = resultService.selectCustomerTel(requestDTO.getRequestNo());

    // 만족도 조사 링크에 붙일 기사번호
    // memberDTO.getMemNo()는 로그인한 "회원"의 번호(MEMBER.MEM_NO)일 뿐, ENGINEER.ENGINEER_NO와는 다른 값이라
    // 그대로 쓰면 안 되고, MEM_NO로 ENGINEER 테이블을 조회해서 진짜 기사번호로 변환해줘야 합니다.
    int engineerNo = resultService.selectEngineerNo(memberDTO.getMemNo());

    // 만족도 조사는 구글폼이 아니라 우리 서버가 직접 받도록 자체 페이지(/survey)로 링크를 겁니다.
    // (구글폼 응답은 우리 DB로 안 들어와서 대시보드 만족도 통계에 실시간으로 반영할 수 없었음)
    // 예전엔 이 요청이 들어온 주소(request.getServerName() 등)를 그대로 썼는데, 그러면 기사님이
    // localhost로 접속해서 결과 등록을 누르면 고객한테도 localhost 링크가 나가버리는 문제가 있었습니다.
    // 기사님이 어떤 주소로 접속했는지와, 고객이 받을 주소는 서로 다른 문제라서 분리했습니다.
    // -> publicBaseUrl(application.yaml의 app.public-base-url)은 고정값으로, 터널/배포 주소가 바뀌면
    //    그 설정 한 줄만 바꾸면 되고 코드는 안 건드려도 됩니다.
    String surveyUrl = publicBaseUrl + "/survey?scheduleNo=" + scheduleNo + "&engineerNo=" + engineerNo;

    // Message 패키지가 중복될 경우 com.solapi.sdk.message.model.Message로 치환하여 주세요
    Message message = new Message();
    message.setFrom("01099365962");
    message.setTo(customerTel);
    message.setText("아래의 링크를 클릭하세요.\n\n만족도 조사 링크\n" + surveyUrl);

    try {
      // send 메소드로 ArrayList<Message> 객체를 넣어도 동작합니다!
      messageService.send(message);
    } catch (SolapiMessageNotReceivedException exception) {
      // 발송에 실패한 메시지 목록을 확인할 수 있습니다!
      System.out.println(exception.getFailedMessageList());
      System.out.println(exception.getMessage());
    } catch (Exception exception) {
      System.out.println(exception.getMessage());
    }

    return "redirect:/as-result-dash-board";
  }

  // 로그인 여부 + role이 "repairman"인지 같이 확인하는 메서드
  // 여기 화면들은 전부 기사(repairman) 전용이라, 로그인 안 했거나 다른 role이면 null을 돌려줌
  private MemberDTO getRepairmanOrNull(HttpSession session) {
    MemberDTO loginMember = (MemberDTO) session.getAttribute("loginMember");
    if (loginMember == null) {
      return null;
    }
    if (!"repairman".equals(loginMember.getRole())) {
      return null;
    }
    return loginMember;
  }

}

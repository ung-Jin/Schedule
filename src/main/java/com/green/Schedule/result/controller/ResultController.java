package com.green.Schedule.result.controller;

import com.green.Schedule.member.dto.MemberDTO;
import com.green.Schedule.result.dto.ResultDTO;
import com.green.Schedule.result.service.ResultService;
import com.green.Schedule.result.util.UploadUtil;
import com.solapi.sdk.SolapiClient;
import com.solapi.sdk.message.exception.SolapiMessageNotReceivedException;
import com.solapi.sdk.message.model.Message;
import com.solapi.sdk.message.service.DefaultMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpSession;

@Controller
@RequiredArgsConstructor
public class ResultController {
  private final ResultService resultService;
  // 처리사진 업로드는 UploadUtil이 전담 (application.yaml의 file.upload.dir 경로 사용)
  private final UploadUtil uploadUtil;

  // a/s기사 대시보드, 로그인 된 상태에서만 보이게 세팅
  @GetMapping("/as-result-dash-board")

  public String asResultDashboard(HttpSession session, Model model){
    MemberDTO loginMember = getRepairmanOrNull(session);
    if (loginMember == null) {
      return "redirect:/member/login";
    }
    // 로그인한 기사의 회원번호(memNo)로 오늘 일정만 조회해서 화면에 전달
    model.addAttribute("todayList", resultService.selectToday(loginMember.getMemNo()));

    return "pages/result/result_dashboard";
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

    //결과보고를 등록하면 문자 발송하러 이동
    return "redirect:/to-msg";
  }

  @GetMapping("/to-msg")
  public String sendMsg(){
    DefaultMessageService messageService =  SolapiClient.INSTANCE.createInstance("NCSKUPDBEGMQEN9B", "VQTCMTT3VPSVPMRUAPBOD4JORBQTJ7VC");

    // Message 패키지가 중복될 경우 com.solapi.sdk.message.model.Message로 치환하여 주세요
    Message message = new Message();
    message.setFrom("01099365962");
    message.setTo("01030587733");
    message.setText("아래의 링크를 클릭하세요.\n\n만족도 조사 링크\nhttps://docs.google.com/forms/d/e/1FAIpQLSesxXDcKtEWoUDW1eQCOUr-bQNgYbwZkGvOX3RkxF50EZFE7w/viewform?usp=publish-editor");

    try {
      System.out.println(111);
      // send 메소드로 ArrayList<Message> 객체를 넣어도 동작합니다!
      messageService.send(message);
    } catch (SolapiMessageNotReceivedException exception) {
      // 발에 실패한 메시지 목록을 확인할 수 있습니다!
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

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

  // 결과보고 화면 "사진 미첨부 사유" 라디오 프리셋. 여기 없는 값이면(=기타를 골라 직접 입력한 경우) 화면에서
  // "기타" 라디오를 선택하고 그 텍스트를 옆 입력칸에 채워줍니다 (asResultReport 참고).
  private static final java.util.Set<String> NO_PHOTO_REASON_PRESETS = java.util.Set.of(
      "단순 설정/원격 조치로 사진이 필요 없음",
      "고객이 촬영을 원하지 않음",
      "현장 여건상 촬영이 어려움(조명/장소 등)",
      "촬영을 깜빡함"
  );

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

    // 카드 상태(진행예정/결과보고/완료)를 화면(Thymeleaf)에서 "배정된 방문 시작 시각(startTime)이 지났는지"로
    // 직접 판단하기 위한 기준 시각. (버튼을 눌러야 진행중으로 바뀌던 예전 방식 대신, 방문을 시작할 시각이 지나면
    // 자동으로 "결과보고" 상태로 바뀌도록 함 - 종료 시각이 아니라 시작 시각 기준)
    model.addAttribute("now", java.time.LocalDateTime.now());

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
    // "사진 미첨부 사유" 라디오 기본값 - scheduleNo가 없거나(= 신규 진입) 등록된 결과가 없으면 그냥 기본값(기타 아님/빈 텍스트) 사용
    model.addAttribute("noPhotoReasonIsEtc", false);
    model.addAttribute("noPhotoReasonEtcValue", "");

    // scheduleNo가 넘어왔으면, 그게 내(로그인한 기사) 일정이 맞는지 먼저 확인하고
    // 맞을 때만 화면 오른쪽 "AS 기본 정보"에 채울 데이터를 조회
    // (다른 기사의 scheduleNo를 주소에 직접 넣어서 남의 정보를 보는 것을 막기 위함)
    if (scheduleNo != null) {
      if (!resultService.isMySchedule(scheduleNo, loginMember.getMemNo())) {
        return "redirect:/as-result-dash-board";
      }
      model.addAttribute("scheduleInfo", resultService.selectScheduleDetail(scheduleNo));
      // 이미 등록된 결과가 있으면(= 결과보고 조회 화면의 "수정" 버튼을 눌러서 들어온 경우) 폼에 기존 값을 채워주기 위해 같이 조회.
      // 없으면 null이 내려가고, 화면(result_report.html)은 그냥 평소대로 빈 폼(등록 모드)을 보여줍니다.
      ResultDTO resultInfo = resultService.selectResultByScheduleNo(scheduleNo);
      model.addAttribute("resultInfo", resultInfo);

      // "사진 미첨부 사유" 라디오 - 기존 값이 프리셋 중 하나가 아니면 "기타"로 간주해서, 그 라디오를 선택
      // 상태로 + 텍스트를 그대로 옆 입력칸에 채워 보여줍니다.
      boolean noPhotoReasonIsEtc = resultInfo != null && resultInfo.getNoPhotoReason() != null
          && !NO_PHOTO_REASON_PRESETS.contains(resultInfo.getNoPhotoReason());
      model.addAttribute("noPhotoReasonIsEtc", noPhotoReasonIsEtc);
      model.addAttribute("noPhotoReasonEtcValue", noPhotoReasonIsEtc ? resultInfo.getNoPhotoReason() : "");
    }
    // 처리날짜는 더 이상 기사가 화면에서 직접 입력/수정하는 값이 아닙니다(resultReg 참고 - 서버가 자동으로 채움).
    // 그래서 화면에는 "등록 시점" 혹은 기존에 등록됐던 처리날짜를 읽기전용으로 보여주기만 합니다.
    return "pages/result/result_report";
  }

  // a/s기사 결과보고 "조회" 화면 (읽기전용) - 대시보드에서 완료(COMPLETED)된 일정의 카드/달력을 클릭하면 이쪽으로 옴
  // (결과보고 "작성" 화면(/as-result-report)과 달리, 폼이 아니라 이미 등록된 처리 결과를 그대로 보여만 줌)
  @GetMapping("/as-result-view")
  public String asResultView(@RequestParam("scheduleNo") long scheduleNo,
                              HttpSession session, Model model){
    MemberDTO loginMember = getRepairmanOrNull(session);
    if (loginMember == null) {
      return "redirect:/member/login";
    }
    // 결과보고 작성 화면과 마찬가지로, 남의 일정을 주소로 직접 조회하는 것을 막기 위해 본인 일정인지 먼저 확인
    if (!resultService.isMySchedule(scheduleNo, loginMember.getMemNo())) {
      return "redirect:/as-result-dash-board";
    }
    model.addAttribute("scheduleInfo", resultService.selectScheduleDetail(scheduleNo));
    // 아직 결과가 등록되지 않은 일정(예: 완료 전 상태에서 주소를 직접 넣어 들어온 경우)이면 null이 내려가고,
    // 화면(result_view.html)에서 null 여부로 "등록된 결과보고 내용이 없습니다" 안내를 보여줌
    model.addAttribute("resultInfo", resultService.selectResultByScheduleNo(scheduleNo));
    return "pages/result/result_view";
  }

  // a/s기사 결과보고 등록 처리 (처리사진은 최대 2장까지 첨부 가능)
  @PostMapping("/result/reg")
  public String resultReg(@ModelAttribute ResultDTO resultDTO,
                           @RequestParam("requestNo") int requestNo,
                           @RequestParam(value = "resultImage", required = false) MultipartFile resultImage,
                           @RequestParam(value = "resultImage2", required = false) MultipartFile resultImage2,
                           // "기타" 라디오를 골랐을 때 옆 텍스트칸에 직접 입력한 사유. name="noPhotoReasonEtc"라
                           // ResultDTO 필드가 아니라서 @ModelAttribute로는 안 잡히고 따로 받아야 함
                           @RequestParam(value = "noPhotoReasonEtc", required = false) String noPhotoReasonEtc,
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

    // 라디오 name="noPhotoReason"은 ResultDTO.noPhotoReason과 이름이 같아서 @ModelAttribute가 이미 채워줬지만,
    // "기타"를 골랐으면 그 값이 "ETC"라는 표식 문자열로 들어와 있으므로, 실제로는 옆 텍스트칸 값으로 바꿔치기합니다.
    if ("ETC".equals(resultDTO.getNoPhotoReason())) {
      resultDTO.setNoPhotoReason(noPhotoReasonEtc);
    }

    // 이미 등록된 결과가 있는지 확인 - 있으면 "수정"(UPDATE), 없으면 새로 "등록"(INSERT)
    ResultDTO existing = resultService.selectResultByScheduleNo(resultDTO.getScheduleNo());

    // 처리날짜(processDate)는 이제 화면에서 기사가 직접 입력하는 값이 아니라 서버가 자동으로 채웁니다.
    // - 신규 등록: 지금 이 순간(등록하는 시점)을 처리날짜로 고정
    // - 수정: 처리날짜는 최초 등록 시점 값 그대로 유지(안 바뀜)하고, 대신 "수정일시"만 지금 시각으로 새로 기록
    if (existing != null) {
      resultDTO.setProcessDate(existing.getProcessDate());
      resultDTO.setUpdatedAt(java.time.LocalDateTime.now());
    } else {
      resultDTO.setProcessDate(java.time.LocalDateTime.now());
    }

    // 사진은 필수 항목이 아니라서, 새 파일을 고르지 않았으면(fileUpload가 null을 돌려줌)
    // 수정 화면에 미리 보여줬던 기존 사진 경로를 그대로 유지합니다. (새로 등록하는 경우엔 existing이 없으니 그냥 null)
    String newImagePath = uploadUtil.fileUpload(resultImage);
    resultDTO.setImagePath(newImagePath != null ? newImagePath : (existing != null ? existing.getImagePath() : null));

    String newImagePath2 = uploadUtil.fileUpload(resultImage2);
    resultDTO.setImagePath2(newImagePath2 != null ? newImagePath2 : (existing != null ? existing.getImagePath2() : null));

    if (existing != null) {
      resultService.updateResult(resultDTO);
    } else {
      resultService.insertResult(resultDTO);
    }

    // AS_REQUEST 상태를 COMPLETED(완료)로 변경 (이미 완료 상태였어도 그대로 유지되는 것뿐이라 수정 때도 그냥 호출)
    // (대시보드에서는 이제 이 값 하나로 카드가 "완료" 상태로 바뀝니다 - result_dashboard.html 참고)
    resultService.completeRequest(requestNo);

    // 수정인 경우엔 만족도 조사 문자를 다시 보낼 필요가 없으니, 바로 조회(읽기전용) 화면으로 이동
    if (existing != null) {
      return "redirect:/as-result-view?scheduleNo=" + resultDTO.getScheduleNo();
    }

    //결과보고를 새로 등록한 경우에만 문자 발송하러 이동 (문자 내용 만들 때 필요한 requestNo/scheduleNo를 같이 넘겨줌)
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

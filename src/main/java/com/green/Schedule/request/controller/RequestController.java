package com.green.Schedule.request.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.green.Schedule.member.dto.MemberDTO;
import com.green.Schedule.request.dto.RequestDTO;
import com.green.Schedule.request.service.RequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import jakarta.servlet.http.HttpSession;


@Controller
@RequestMapping("/request")
public class RequestController {

    @Autowired
    private RequestService requestService;

    /**
     * A/S 요청 신청 화면 (로그인한 회원만 접근 가능)
     * 주소 예: /request/write
     */
    @GetMapping("/write")
    public String writeForm(HttpSession session) {

        // 로그인 안 한 상태면 로그인 화면으로 돌려보냄
        if (session.getAttribute("loginMember") == null) {
            return "redirect:/member/login";
        }

        // 화면(write.html)에서 이름을 미리 채워줄 때 필요한 로그인 정보는
        // ${session.loginMember} 로 템플릿에서 바로 꺼내 쓸 수 있어서
        // 여기서 따로 Model에 담지 않아도 됩니다.
        return "request/write";
    }

    /**
     * A/S 요청 등록 처리
     */
    @PostMapping("/write")
    public String write(@ModelAttribute RequestDTO requestDTO, HttpSession session) {

        MemberDTO loginMember = (MemberDTO) session.getAttribute("loginMember");
        if (loginMember == null) {
            return "redirect:/member/login";
        }

        // "내 신청내역" 조회를 위해, 신청한 회원의 번호를 같이 저장합니다.
        requestDTO.setMemNo(loginMember.getMemNo());
        requestDTO.setStatus("RECEIVED");
        requestService.save(requestDTO);

        // "내용 확인" 단계 화면을 따로 만들지 않고,
        // 접수 완료 화면 하나로 자연스럽게 이어지도록 처리합니다.
        return "redirect:/request/complete";
    }

    /**
     * 접수 완료 화면
     */
    @GetMapping("/complete")
    public String complete(HttpSession session) {

        if (session.getAttribute("loginMember") == null) {
            return "redirect:/member/login";
        }

        return "request/complete";
    }

    /**
     *  내 신청내역 목록
     * 주소 예: /request/list
     * 로그인한 회원(memNo) 기준으로 본인이 신청한 내역만 보여줍니다.
     */
    @GetMapping("/list")
    public String list(HttpSession session, Model model) {

        MemberDTO loginMember = (MemberDTO) session.getAttribute("loginMember");
        if (loginMember == null) {
            return "redirect:/member/login";
        }

        List<RequestDTO> requestList = requestService.getMyRequestList(loginMember.getMemNo());
        model.addAttribute("requestList", requestList);

        return "request/list";
    }

    /**
     * 신청 상세 내역
     * 주소 예: /request/detail/3
     * 본인이 신청한 내역이 맞는지(memNo 확인)까지 서비스에서 같이 확인합니다.
     */
    @GetMapping("/detail/{requestNo}")
    public String detail(@PathVariable("requestNo") int requestNo, HttpSession session, Model model) {

        MemberDTO loginMember = (MemberDTO) session.getAttribute("loginMember");
        if (loginMember == null) {
            return "redirect:/member/login";
        }

        RequestDTO requestDetail = requestService.getRequestDetail(requestNo, loginMember.getMemNo());

        // 존재하지 않는 요청번호이거나, 본인이 신청한 내역이 아니면 목록으로 돌려보냅니다.
        if (requestDetail == null) {
            return "redirect:/request/list";
        }

        model.addAttribute("requestDetail", requestDetail);

        return "request/detail";
    }

    /**
     * 중복 신청 확인 (비동기 / AJAX 전용)
     * 화면에서 fetch()로 이 주소를 호출하면 JSON으로 결과를 돌려줍니다.
     * 예: /request/checkDuplicate?customerTel=010-1234-5678&wishDate=2026-09-15
     */
    @GetMapping("/checkDuplicate")
    @ResponseBody
    public Map<String, Object> checkDuplicate(@RequestParam("customerTel") String customerTel,
                                              @RequestParam("wishDate") String wishDate) {

        boolean duplicate = requestService.isDuplicate(customerTel, wishDate);

        Map<String, Object> result = new HashMap<>();
        result.put("duplicate", duplicate);

        return result;
    }

}

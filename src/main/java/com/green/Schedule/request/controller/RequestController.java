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


    @GetMapping("/write")
    public String writeForm(HttpSession session) {

        // 로그인 안 한 상태면 로그인 화면으로 돌려보냄
        if (session.getAttribute("loginMember") == null) {
            return "redirect:/member/login";
        }

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


    //본인이 신청한 내역
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


    //상세 신청내역
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

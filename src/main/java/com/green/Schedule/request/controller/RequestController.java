package com.green.Schedule.request.controller;

import java.util.HashMap;
import java.util.Map;

import com.green.Schedule.request.dto.RequestDTO;
import com.green.Schedule.request.service.RequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
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

        if (session.getAttribute("loginMember") == null) {
            return "redirect:/member/login";
        }

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

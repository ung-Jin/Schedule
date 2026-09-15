package com.green.Schedule.member.controller;

import com.green.Schedule.member.dto.MemberDTO;
import com.green.Schedule.member.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/member")
public class MemberController {

    @Autowired
    private MemberService memberService;

    @GetMapping("/login")
    public String loginForm(HttpSession session) {

        MemberDTO loginMember = (MemberDTO) session.getAttribute("loginMember");

        if (loginMember != null) {
            return redirectByRole(loginMember.getRole());
        }


        return "member/login";
    }



    // 로그인
    @PostMapping("/login")
    public String login(@RequestParam("memId") String memId,
                        @RequestParam("memPw") String memPw,
                        HttpSession session,
                        Model model) {

        MemberDTO loginMember = memberService.login(memId, memPw);

        // 로그인 실패 -> 에러 메시지와 함께 다시 로그인 화면으로
        if (loginMember == null) {
            model.addAttribute("loginError", "아이디 또는 비밀번호가 일치하지 않습니다.");
            return "member/login";
        }

        // 로그인 성공 -> 세션에 로그인한 회원 정보를 저장
        session.setAttribute("loginMember", loginMember);

        // 첫 화면에서 로그인한 회원의 역할에 맞는 업무 바로가기를 제공합니다.
        return "redirect:/";
    }



    //로그아웃
    @GetMapping("/logout")
    public String logout(HttpSession session) {

        session.invalidate();

        return "redirect:/";
    }

    //회원가입 하러 가는 페이지
    @GetMapping("/join")
    public String joinForm(HttpSession session) {

        MemberDTO loginMember = (MemberDTO) session.getAttribute("loginMember");

        if (loginMember != null) {
            return redirectByRole(loginMember.getRole());
        }

        return "member/join";
    }


    //회원가입처리
    @PostMapping("/join")
    public String join(@RequestParam("memId") String memId,
                       @RequestParam("memPw") String memPw,
                       @RequestParam("memPwConfirm") String memPwConfirm,
                       @RequestParam("memName") String memName,
                       Model model) {

        // 비밀번호 / 비밀번호 확인이 다르면 다시 가입 화면으로
        if (!memPw.equals(memPwConfirm)) {
            model.addAttribute("joinError", "비밀번호가 일치하지 않습니다.");
            model.addAttribute("memId", memId);
            model.addAttribute("memName", memName);
            return "member/join";
        }

        // 이미 있는 아이디면 다시 가입 화면으로
        if (memberService.isIdDuplicate(memId)) {
            model.addAttribute("joinError", "이미 사용 중인 아이디입니다.");
            model.addAttribute("memName", memName);
            return "member/join";
        }

        MemberDTO memberDTO = new MemberDTO();
        memberDTO.setMemId(memId);
        memberDTO.setMemPw(memPw);
        memberDTO.setMemName(memName);
        // role은 세팅하지 않습니다 - MEMBER 테이블의 DEFAULT 'user'가 자동으로 채워줍니다.

        memberService.join(memberDTO);

        // 가입 완료 -> 로그인 화면으로 이동
        return "redirect:/member/login";
    }


    private String redirectByRole(String role) {
        if ("admin".equals(role)) {
            return "redirect:/pages/admin/main";
        }
        else if ("repairman".equals(role)) {
            return "redirect:/pages/engineer/main";
        }
        else {
            return "redirect:/pages/user/main";
        }
    }




}


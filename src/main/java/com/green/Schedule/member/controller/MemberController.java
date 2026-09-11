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

    /**
     * 로그인 화면 보여주기 (GET 방식)
     * 주소 예: /member/login
     *
     * 이미 로그인이 되어있는 상태로 이 주소에 다시 들어오면(예: 로그인 성공 후 뒤로가기)
     * 로그인 폼을 또 보여주지 않고, 바로 원래 가는 화면으로 돌려보냅니다.
     */
    @GetMapping("/login")
    public String loginForm(HttpSession session) {

        MemberDTO loginMember = (MemberDTO) session.getAttribute("loginMember");

        if (loginMember != null) {
            return redirectByRole(loginMember.getRole());
        }


        return "member/login";
    }

    /**
     * 로그인 처리 (POST 방식)
     * login.html의 <form action="/member/login" method="post"> 에서 넘어옵니다.
     */
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
        // (이후 다른 페이지에서 session.getAttribute("loginMember")로 꺼내 쓸 수 있습니다.)
        session.setAttribute("loginMember", loginMember);

        // role 값에 따라 이동할 첫 화면을 다르게 이동시킵니다.
        return redirectByRole(loginMember.getRole());
    }


    /* 로그아웃
     * 주소 예: /member/logout      */

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        // 세션에 저장된 정보를 모두 지웁니다.
        session.invalidate();
        // 어느 화면에서 로그아웃을 누르든 메인(첫) 화면으로 이동시킵니다.
        return "redirect:/";
    }

    /**
     * 회원가입 화면 (GET)
     * 주소 예: /member/join
     * 이미 로그인된 상태라면 가입 화면 대신 원래 화면(role별 홈)으로 보냅니다.
     */
    @GetMapping("/join")
    public String joinForm(HttpSession session) {

        MemberDTO loginMember = (MemberDTO) session.getAttribute("loginMember");
        if (loginMember != null) {
            return redirectByRole(loginMember.getRole());
        }

        return "member/join";
    }

    /**
     *  회원가입 처리 (POST)
     * join.html의 <form action="/member/join" method="post"> 에서 넘어옵니다.
     * 화면에는 role 입력을 아예 두지 않고, 여기서 가입하는 사람은 전부 user로만 가입됩니다.
     * (관리자는 기본 쿼리로, 기사는 별도의 기사등록 기능으로 만들 예정이라 여기서는 다루지 않습니다.)
     */
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
        } else if ("repairman".equals(role)) {
            return "redirect:/pages/engineer/main";
        } else {
            return "redirect:/pages/user/main";
        }
    }




}


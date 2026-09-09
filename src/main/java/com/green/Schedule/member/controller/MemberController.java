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

    /**
     * 로그아웃
     * 주소 예: /member/logout
     */
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        // 세션에 저장된 정보를 모두 지웁니다.
        session.invalidate();
        return "redirect:/member/login";
    }


    /**
     * role 값에 따라 이동할 주소를 정해주는 메서드
     * (로그인 성공 직후, 그리고 로그인 상태로 로그인 화면에 다시 들어왔을 때
     *  똑같은 분기 코드를 두 번 쓰지 않기 위해 따로 뺐습니다.)
     */
    private String redirectByRole(String role) {
        if ("admin".equals(role)) {
            return "redirect:/pages/admin/main";
        } else if ("repairman".equals(role)) {
            return "redirect:/pages/engineer/main";
        } else {
            return "redirect:/pages/user/main";
        }
    }


    // =========================
    // 사용자 메인
    // =========================
    @Controller
    @RequestMapping("/pages/user")
    public class UserController {

        @GetMapping("/main")
        public String main() {
            return "pages/user/main";
        }
    }


    // =========================
    // 관리자 메인
    // =========================
    @Controller
    @RequestMapping("/pages/admin")
    public class AdminController {

        @GetMapping("/main")
        public String main() {
            return "pages/admin/main";
        }
    }


    // =========================
    // 기사 메인
    // =========================
    @Controller
    @RequestMapping("/pages/engineer")
    public class EngineerController {

        @GetMapping("/main")
        public String main() {
            return "pages/engineer/main";
        }
    }
}

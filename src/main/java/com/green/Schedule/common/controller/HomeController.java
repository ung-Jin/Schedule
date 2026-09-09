package com.green.Schedule.common.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 메인(랜딩) 페이지 / 서비스소개 / 주요기능 페이지를 보여주는 컨트롤러입니다.
 * 로그인 관련 요청은 이미 만들어둔 MemberController가 처리합니다.
 */
@Controller
public class HomeController {


    @GetMapping("/")
    public String home() {
        // templates/FirstPage.html 을 보여줍니다.
        return "FirstPage";
    }

    @GetMapping("/service-intro")
    public String serviceIntro() {
        // templates/service-intro.html 을 보여줍니다.
        return "service-intro";
    }

    @GetMapping("/features")
    public String features() {
        // templates/features.html 을 보여줍니다.
        return "features";
    }

}

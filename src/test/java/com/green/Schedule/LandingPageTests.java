package com.green.Schedule;

import com.green.Schedule.member.dto.MemberDTO;
import com.green.Schedule.member.service.MemberService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
class LandingPageTests {
    @Autowired WebApplicationContext context;
    @MockitoBean MemberService memberService;
    MockMvc mvc;

    @BeforeEach
    void setup() {
        mvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    private MemberDTO member(String role) {
        MemberDTO member = new MemberDTO();
        member.setRole(role);
        member.setMemName("테스트 회원");
        return member;
    }

    @Test
    void guestSeesFiveCardsLeadingOnlyToLogin() throws Exception {
        mvc.perform(get("/"))
            .andExpect(status().isOk())
            .andExpect(xpath("//a[@class='menu-card']").nodeCount(5))
            .andExpect(xpath("//a[@class='menu-card' and @href='/member/login']").nodeCount(5))
            .andExpect(xpath("//div[@data-menu-role]").nodeCount(1))
            .andExpect(xpath("//a[contains(@class,'more-link')]").doesNotExist());
    }

    @Test
    void adminSeesOnlyThreeAdminDestinations() throws Exception {
        mvc.perform(get("/").sessionAttr("loginMember", member("admin")))
            .andExpect(status().isOk())
            .andExpect(xpath("//a[@class='menu-card']").nodeCount(3))
            .andExpect(xpath("//div[@data-menu-role]").nodeCount(1))
            .andExpect(xpath("//div[@data-menu-role='admin']/a[@href='/pages/admin/main']").exists())
            .andExpect(xpath("//div[@data-menu-role='admin']/a[@href='/pages/admin/calendar']").exists())
            .andExpect(xpath("//div[@data-menu-role='admin']/a[@href='/pages/admin/management']").exists());
    }

    @Test
    void repairmanSeesOnlyTheirDashboard() throws Exception {
        mvc.perform(get("/").sessionAttr("loginMember", member("repairman")))
            .andExpect(status().isOk())
            .andExpect(xpath("//a[@class='menu-card']").nodeCount(1))
            .andExpect(xpath("//div[@data-menu-role]").nodeCount(1))
            .andExpect(xpath("//div[@data-menu-role='repairman']/a[@href='/pages/engineer/main']").exists());
    }

    @Test
    void userSeesRequestHistoryAndNewRequest() throws Exception {
        mvc.perform(get("/").sessionAttr("loginMember", member("user")))
            .andExpect(status().isOk())
            .andExpect(xpath("//a[@class='menu-card']").nodeCount(2))
            .andExpect(xpath("//div[@data-menu-role]").nodeCount(1))
            .andExpect(xpath("//div[@data-menu-role='user']/a[@href='/request/list']").exists())
            .andExpect(xpath("//div[@data-menu-role='user']/a[@href='/request/write']").exists());
    }

    @Test
    void successfulLoginReturnsToRoleAwareLandingPage() throws Exception {
        MemberDTO member = member("admin");
        when(memberService.login("test", "test-password")).thenReturn(member);
        mvc.perform(post("/member/login").param("memId", "test").param("memPw", "test-password"))
            .andExpect(redirectedUrl("/"))
            .andExpect(request().sessionAttribute("loginMember", member));
    }
}

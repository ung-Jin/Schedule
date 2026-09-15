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

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import org.springframework.test.web.servlet.ResultMatcher;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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

    // Inspect rendered HTML rather than parsing HTML5 void elements as XML.
    private ResultMatcher menus(String role, List<String> destinations) {
        return result -> {
            String html = result.getResponse().getContentAsString();
            List<String> links = new ArrayList<>();
            var anchors = Pattern.compile("<a\\b[^>]*class=\"menu-card\"[^>]*>").matcher(html);
            while (anchors.find()) {
                var href = Pattern.compile("href=\"([^\"]*)\"").matcher(anchors.group());
                if (href.find()) links.add(href.group(1));
            }
            assertEquals(destinations, links);
            var roles = Pattern.compile("data-menu-role=\"([^\"]*)\"").matcher(html);
            List<String> visibleRoles = new ArrayList<>();
            while (roles.find()) visibleRoles.add(roles.group(1));
            assertEquals(List.of(role), visibleRoles);
            assertFalse(html.contains("more-link"));
        };
    }

    @Test
    void guestSeesFiveCardsLeadingOnlyToLogin() throws Exception {
        mvc.perform(get("/"))
            .andExpect(status().isOk())
            .andExpect(menus("guest", java.util.Collections.nCopies(5, "/member/login")));
    }

    @Test
    void adminSeesOnlyThreeAdminDestinations() throws Exception {
        mvc.perform(get("/").sessionAttr("loginMember", member("admin")))
            .andExpect(status().isOk())
            .andExpect(menus("admin", List.of("/pages/admin/main", "/pages/admin/calendar", "/pages/admin/management")));
    }

    @Test
    void repairmanSeesOnlyTheirDashboard() throws Exception {
        mvc.perform(get("/").sessionAttr("loginMember", member("repairman")))
            .andExpect(status().isOk())
            .andExpect(menus("repairman", List.of("/pages/engineer/main")));
    }

    @Test
    void userSeesRequestHistoryAndNewRequest() throws Exception {
        mvc.perform(get("/").sessionAttr("loginMember", member("user")))
            .andExpect(status().isOk())
            .andExpect(menus("user", List.of("/request/list", "/request/write")));
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

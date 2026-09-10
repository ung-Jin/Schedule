package com.green.Schedule.common.interceptor;

import com.green.Schedule.member.dto.MemberDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 로그인 여부 / 역할(role)별 접근 권한을 한 곳에서 검사하는 인터셉터.
 *
 * 지금까지는 컨트롤러마다 "session.getAttribute("loginMember") == null" 체크를
 * 따로따로 넣거나(혹은 아예 빠뜨려서) 화면마다 동작이 달랐습니다.
 * (예: /pages/admin/main, /pages/user/main 등은 로그인 없이 URL만 알면 그냥 들어가짐)
 *
 * 이 인터셉터가 하는 일:
 *  1) 로그인이 필요한 화면인데 세션에 로그인 정보가 없으면 -> 로그인 화면으로 이동
 *  2) 로그인은 했지만 자기 role의 화면이 아닌 곳(예: user가 /pages/admin/** 접근)에 들어오면
 *     -> 자기 role의 홈 화면으로 돌려보냄
 *  3) 로그인 이후 화면은 브라우저가 캐시하지 못하도록 헤더를 세팅.
 *     이게 없으면 로그아웃 후 "뒤로가기"를 눌렀을 때 서버에 다시 물어보지 않고
 *     브라우저에 캐시된(로그인 되어 있던 시절의) 화면이 그대로 보이는 문제가 생김.
 */
public class LoginCheckInterceptor implements HandlerInterceptor {

    // { URL 접두사, 그 URL을 쓸 수 있는 role }
    private static final String[][] ROLE_PATH = {
            {"/pages/admin", "admin"},
            {"/admin", "admin"},
            {"/engineer-api", "admin"},
            {"/pages/engineer", "repairman"},
            {"/as-result", "repairman"},
            {"/pages/user", "user"},
    };

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        // 로그인 이후에 보여주는 화면들은 브라우저/뒤로가기 캐시에 남지 않도록 막는다.
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);

        HttpSession session = request.getSession(false);
        MemberDTO loginMember = (session == null) ? null : (MemberDTO) session.getAttribute("loginMember");
        String uri = request.getRequestURI();

        boolean isAjax = "XMLHttpRequest".equals(request.getHeader("X-Requested-With"));

        if (loginMember == null) {
            if (isAjax || uri.startsWith("/engineer-api")) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "로그인이 필요합니다.");
            } else {
                response.sendRedirect(request.getContextPath() + "/member/login");
            }
            return false;
        }

        for (String[] rule : ROLE_PATH) {
            String prefix = rule[0];
            String requiredRole = rule[1];
            if (uri.startsWith(prefix) && !requiredRole.equals(loginMember.getRole())) {
                if (isAjax || uri.startsWith("/engineer-api")) {
                    response.sendError(HttpServletResponse.SC_FORBIDDEN, "접근 권한이 없습니다.");
                } else {
                    response.sendRedirect(request.getContextPath() + homeOf(loginMember.getRole()));
                }
                return false;
            }
        }

        return true;
    }

    private String homeOf(String role) {
        if ("admin".equals(role)) {
            return "/pages/admin/main";
        } else if ("repairman".equals(role)) {
            return "/pages/engineer/main";
        } else {
            return "/pages/user/main";
        }
    }
}

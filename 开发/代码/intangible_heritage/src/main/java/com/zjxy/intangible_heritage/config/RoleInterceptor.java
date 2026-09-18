package com.zjxy.intangible_heritage.config;

import com.zjxy.intangible_heritage.entity.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 角色守卫拦截器：
 *   /custom/apply       需要 USER
 *   /custom/myApply     需要 USER
 *   /custom/myCraft     需要 CRAFTSMAN
 *   /custom/accept|reject|finish  双方均可（具体业务层再校验是否当事人）
 *   /custom/chat|messages          双方均可
 *   /admin/**         需要 ADMIN
 */
public class RoleInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String uri = request.getRequestURI();
        User loginUser = (User) request.getSession().getAttribute("loginUser");

        // 未登录
        if (loginUser == null) {
            response.sendRedirect("/login");
            return false;
        }
        String role = loginUser.getRole();

        // 仅 USER
        if (uri.equals("/custom/apply") || uri.equals("/custom/myApply")) {
            if (!"USER".equals(role)) {
                response.sendRedirect("/");
                return false;
            }
        }
        // 仅 CRAFTSMAN
        if (uri.equals("/custom/myCraft")) {
            if (!"CRAFTSMAN".equals(role)) {
                response.sendRedirect("/");
                return false;
            }
        }
        // 仅 ADMIN
        if (uri.startsWith("/admin")) {
            if (!"ADMIN".equals(role)) {
                response.sendRedirect("/");
                return false;
            }
        }
        return true;
    }
}

package com.cupk.config;

import com.cupk.common.UserContext;
import com.cupk.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Set;

/**
 * JWT 认证过滤器 —— 校验 Token 并注入用户上下文
 */
@Component
@RequiredArgsConstructor
public class JwtAuthFilter implements Filter {

    private final JwtUtil jwtUtil;

    /** 无需登录即可访问的路径 */
    private static final Set<String> PUBLIC_PATHS = Set.of(
            "/api/auth/login",
            "/api/auth/register",
            "/api/auth/send-code",
            "/api/auth/reset-password"
    );

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        String path = req.getRequestURI();

        // 放行公开接口
        if (PUBLIC_PATHS.contains(path)) {
            chain.doFilter(request, response);
            return;
        }

        // 放行 OPTIONS 预检请求
        if ("OPTIONS".equalsIgnoreCase(req.getMethod())) {
            chain.doFilter(request, response);
            return;
        }

        // 提取 Token
        String header = req.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            res.setStatus(401);
            res.setContentType("application/json;charset=UTF-8");
            res.getWriter().write("{\"code\":401,\"message\":\"未登录\"}");
            return;
        }

        String token = header.substring(7);

        // 校验 Token
        try {
            Claims claims = jwtUtil.parseToken(token);
            Integer userId = Integer.valueOf(claims.getId());
            String username = claims.getSubject();
            String role = (String) claims.get("role");

            UserContext.set(userId, username, role);
            chain.doFilter(request, response);
        } catch (Exception e) {
            res.setStatus(401);
            res.setContentType("application/json;charset=UTF-8");
            res.getWriter().write("{\"code\":401,\"message\":\"Token 无效或已过期\"}");
        } finally {
            UserContext.clear();
        }
    }
}

package com.rongproject.JavaSprint5_2LibrarySystem.security;

import com.rongproject.JavaSprint5_2LibrarySystem.services.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 图书馆项目 - JWT 认证过滤器
 * Jules Fix: Added deep diagnostic logging for authority verification.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    private final CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            // 1. 从请求头 Header 中获取 Token
            String jwt = parseJwt(request);

            // 2. 校验 Token 是否有效
            if (jwt != null && jwtUtils.validateToken(jwt)) {
                String username = jwtUtils.getUsernameFromToken(jwt);

                // 3. 根据用户名加载用户详情 (UserDetails)
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                // --- JULES DIAGNOSTIC START ---
                // English Comment: Print authentication details to verify role mapping (e.g., [ROLE_ADMIN])
                System.out.println(">>> [JULES JWT CHECK] Request URL: " + request.getRequestURI());
                System.out.println(">>> [JULES JWT CHECK] Authenticated User: " + username);
                System.out.println(">>> [JULES JWT CHECK] Assigned Authorities: " + userDetails.getAuthorities());
                // --- JULES DIAGNOSTIC END ---

                // 4. 创建认证令牌，并存入 SecurityContext 上下文
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authentication);
            } else if (jwt == null && !request.getRequestURI().contains("/api/auth")) {
                // Jules: 记录非登录接口但未携带 Token 的请求
                System.out.println(">>> [JULES JWT CHECK] No JWT found for protected resource: " + request.getRequestURI());
            }
        } catch (Exception e) {
            // Jules: 捕获并记录认证过程中的异常
            System.err.println(">>> [JULES JWT ERROR] Cannot set user authentication: " + e.getMessage());
            logger.error("Cannot set user authentication: {}", e);
        }

        // 5. 继续执行后面的过滤器链
        filterChain.doFilter(request, response);
    }

    // 私有辅助方法：从 Authorization Header 提取 Token
    private String parseJwt(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");
        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            return headerAuth.substring(7); // 去掉 "Bearer " 前缀
        }
        return null;
    }
}
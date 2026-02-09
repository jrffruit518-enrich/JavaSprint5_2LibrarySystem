package com.rongproject.JavaSprint5_2LibrarySystem.configs;

import com.rongproject.JavaSprint5_2LibrarySystem.configs.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 1. 启用并配置 CORS (跨域)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // 2. 禁用 CSRF（因为 JWT 不需要 Session）
                .csrf(csrf -> csrf.disable())

                // 3. 设置为无状态模式
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 4. 配置权限规则
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/books", "/api/books/{id}").permitAll()
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/h2-console/**").permitAll()
                        .anyRequest().authenticated()
                )

                // 5. 匿名用户配置
                .anonymous(anonymous -> anonymous
                        .principal("guestUser")
                        .authorities("ROLE_ANONYMOUS")
                )

                // 6. 允许 H2 Console 使用 Frame
                .headers(headers -> headers.frameOptions(f -> f.disable()));

        // 7. 将 JWT 过滤器放在用户名密码过滤器之前
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // --- 新增：跨域策略定义 ---
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // 允许的前端来源（这里填你前端跑起来的地址，如 http://localhost:5173）
        configuration.setAllowedOrigins(List.of("http://localhost:5173", "http://127.0.0.1:5173"));

        // 允许的 HTTP 方法
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));

        // 允许的 Header 头部信息
        configuration.setAllowedHeaders(List.of("*"));

        // 允许携带 Cookie (如果需要的话)
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // 对所有 API 路径生效
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
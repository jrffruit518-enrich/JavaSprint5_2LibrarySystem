package com.rongproject.JavaSprint5_2LibrarySystem.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

    @Configuration
    @EnableWebSecurity
    @EnableMethodSecurity
    public class SecurityConfig {

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
            http
                    .csrf(csrf -> csrf.disable()) // 示例：开发阶段禁用
                    .authorizeHttpRequests(auth -> auth
                            // 1. 允许所有人（包括匿名访客）访问的路径
                            .requestMatchers("/api/books", "/api/books/{id}").permitAll()
                            .requestMatchers("/api/auth/**").permitAll()
                            .requestMatchers("/h2-console/**").permitAll()
                            // 2. 其他所有请求都需要登录（认证后即可，不限角色）
                            .anyRequest().authenticated()
                    )
                    // 3. 启用匿名认证（默认就是开启的，但显式配置更清晰）
                    .anonymous(anonymous -> anonymous
                            .principal("guestUser")
                            .authorities("ROLE_ANONYMOUS")
                    )
                    .headers(headers -> headers.frameOptions(f -> f.disable())); // 允许 H2 控制台

            return http.build();
        }
    }

package com.rongproject.JavaSprint5_2LibrarySystem.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

    @Configuration
    @EnableWebSecurity
    public class SecurityConfig {
        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
            http
                    .csrf(csrf -> csrf.disable()) // 前后端分离通常关闭这个
                    .authorizeHttpRequests(auth -> auth
                            .requestMatchers("/api/public/**").permitAll() // 所有人都能看的
                            .requestMatchers("/api/admin/**").hasRole("ADMIN") // 只有管理员能进
                            .anyRequest().authenticated() // 剩下的都要登录
                    );
            return http.build();
        }
    }

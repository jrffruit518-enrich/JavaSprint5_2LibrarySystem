package com.rongproject.JavaSprint5_2LibrarySystem.security;

import com.rongproject.JavaSprint5_2LibrarySystem.entities.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

/**
 * 图书馆项目 - Custom UserDetails
 * Jules Fix: Fixed boolean getter naming and fulfilled UserDetails interface.
 */
public class CustomUserDetails implements UserDetails {

    @Getter
    private final Long id;
    private final String username;
    private final String password;
    private final boolean enabled;
    private final Collection<? extends GrantedAuthority> authorities;

    public CustomUserDetails(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.password = user.getPassword();

        // Jules Fix: Lombok generates 'isEnabled()' for boolean fields, not 'getEnabled()'
        this.enabled = user.isEnabled();

        // Jules Fix: Directly use "ROLE_ADMIN" from enum
        this.authorities = Collections.singletonList(
                new SimpleGrantedAuthority(user.getUserRole().name())
        );
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    // --- 以下是 UserDetails 接口必须实现的方法，否则会报错 ---

    @Override
    public boolean isAccountNonExpired() {
        return true; // 账号永不过期
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // 账号未锁定
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // 凭证永不过期
    }

    @Override
    public boolean isEnabled() {
        return enabled; // 使用数据库中的状态
    }
}
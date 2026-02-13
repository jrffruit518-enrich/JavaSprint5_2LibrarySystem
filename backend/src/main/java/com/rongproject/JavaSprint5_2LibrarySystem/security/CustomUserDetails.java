package com.rongproject.JavaSprint5_2LibrarySystem.security;

import com.rongproject.JavaSprint5_2LibrarySystem.entities.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

public class CustomUserDetails implements UserDetails {
    @Getter // 必须确保有这个 Getter，Controller 才能拿到 id
    private final Long id;
    private final String username;
    private final String password;
    private final Collection<? extends GrantedAuthority> authorities;

    public CustomUserDetails(User user) {
        this.id = user.getId(); // 修复点：确保这里拿到了 MySQL 里的 Long ID
        this.username = user.getUsername();
        this.password = user.getPassword();
        // 确保角色前缀匹配 ROLE_
        String roleName = user.getUserRole().name();
        String finalRole = roleName.startsWith("ROLE_") ? roleName : "ROLE_" + roleName;
        this.authorities = Collections.singletonList(new SimpleGrantedAuthority(finalRole));
    }

    @Override public Collection<? extends GrantedAuthority> getAuthorities() { return authorities; }
    @Override public String getPassword() { return password; }
    @Override public String getUsername() { return username; }
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return true; }
}
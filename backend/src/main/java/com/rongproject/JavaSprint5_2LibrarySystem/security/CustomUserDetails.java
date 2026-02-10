package com.rongproject.JavaSprint5_2LibrarySystem.security;

import com.rongproject.JavaSprint5_2LibrarySystem.entities.User;
import lombok.Getter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import java.util.Collections;

// English Comment: Custom wrapper to hold our database User ID within the Security context
@Getter
public class CustomUserDetails extends org.springframework.security.core.userdetails.User {
    private final Long id;

    public CustomUserDetails(User user) {
        super(user.getUsername(), user.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority(user.getUserRole().name())));
        this.id = user.getId();
    }
}

package com.rongproject.JavaSprint5_2LibrarySystem.entities;


import com.rongproject.JavaSprint5_2LibrarySystem.enums.UserRole;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole userRole; // e.g., "ROLE_USER", "ROLE_ADMIN"

    @Builder.Default
    private boolean enabled = true;

    // English Comment: If true, only an admin can unlock this account.
// System automation will skip users with manualLock = true.
    @Column(nullable = false)
    private boolean manualLock = false;

    private String avatarUrl;
}

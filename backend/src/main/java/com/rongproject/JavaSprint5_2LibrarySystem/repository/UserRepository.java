package com.rongproject.JavaSprint5_2LibrarySystem.repository;

import com.rongproject.JavaSprint5_2LibrarySystem.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
}

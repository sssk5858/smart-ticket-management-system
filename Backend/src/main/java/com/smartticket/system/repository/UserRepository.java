package com.smartticket.system.repository;

import com.smartticket.system.model.Role;
import com.smartticket.system.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    List<User> findByRoleAndIsActiveTrue(Role role);
    long countByRole(Role role);
}

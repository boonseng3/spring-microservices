package com.obs.microservices.repo;

import com.obs.microservices.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepo extends JpaRepository<Role, Long> {
    Optional<Role> findByRole(String role);
}

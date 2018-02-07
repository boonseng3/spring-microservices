package com.obs.microservices.repo;

import com.obs.microservices.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PermissionRepo extends JpaRepository<Permission, Integer> {
    Optional<Permission> findByName(String name);
    Optional<Permission> findById(Integer id);
}

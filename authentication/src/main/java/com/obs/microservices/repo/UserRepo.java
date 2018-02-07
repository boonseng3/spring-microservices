package com.obs.microservices.repo;

import com.obs.microservices.entity.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepo extends JpaRepository<User, Long> {

    @EntityGraph(attributePaths = "roles", type = EntityGraph.EntityGraphType.FETCH)
    Optional<User> findByUsername(String username);
}

package com.obs.microservices.repo;

import com.obs.microservices.entity.Profile;
import com.obs.microservices.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProfileRepo extends JpaRepository<Profile, Long> {

    Optional<Profile> findByUser(User user);
}

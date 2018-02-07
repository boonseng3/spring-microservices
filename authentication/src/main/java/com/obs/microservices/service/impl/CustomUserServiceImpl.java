package com.obs.microservices.service.impl;

import com.obs.microservices.entity.User;
import com.obs.microservices.repo.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CustomUserServiceImpl implements UserDetailsService {
    @Autowired
    private UserRepo userRepo;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepo.findByUsername(username).<RuntimeException>orElseThrow(() -> {
            throw new UsernameNotFoundException("Record not found.");
        });
        List<SimpleGrantedAuthority> roles = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.getRole()))
                .collect(Collectors.toList());
        return new org.springframework.security.core.userdetails.User(user.getUsername(),
                user.getPassword(), roles);

    }
}

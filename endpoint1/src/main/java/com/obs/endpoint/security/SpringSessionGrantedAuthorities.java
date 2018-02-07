package com.obs.endpoint.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.GrantedAuthoritiesContainer;

import java.util.Collection;

public class SpringSessionGrantedAuthorities implements GrantedAuthoritiesContainer {
    private Collection<? extends GrantedAuthority> authorities;

    @Override
    public Collection<? extends GrantedAuthority> getGrantedAuthorities() {
        return authorities;
    }

    public SpringSessionGrantedAuthorities setAuthorities(Collection<? extends GrantedAuthority> authorities) {
        this.authorities = authorities;
        return this;
    }
}

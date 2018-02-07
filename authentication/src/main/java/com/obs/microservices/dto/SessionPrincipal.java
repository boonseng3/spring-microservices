package com.obs.microservices.dto;

import java.io.Serializable;
import java.util.List;

public class SessionPrincipal implements Serializable {
    private String username;
    private List<String> authorities;

    public String getUsername() {
        return username;
    }

    public SessionPrincipal setUsername(String username) {
        this.username = username;
        return this;
    }

    public List<String> getAuthorities() {
        return authorities;
    }

    public SessionPrincipal setAuthorities(List<String> authorities) {
        this.authorities = authorities;
        return this;
    }
}

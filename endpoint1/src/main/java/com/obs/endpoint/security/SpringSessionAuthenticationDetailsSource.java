package com.obs.endpoint.security;

import com.obs.endpoint.config.dto.SessionPrincipal;
import com.obs.endpoint.service.AuthenticationClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationDetailsSource;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;

public class SpringSessionAuthenticationDetailsSource implements AuthenticationDetailsSource<HttpServletRequest, SpringSessionGrantedAuthorities> {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private AuthenticationClient authenticationClient;

    public AuthenticationClient getAuthenticationClient() {
        return authenticationClient;
    }

    public SpringSessionAuthenticationDetailsSource setAuthenticationClient(AuthenticationClient authenticationClient) {
        this.authenticationClient = authenticationClient;
        return this;
    }

    @Override
    public SpringSessionGrantedAuthorities buildDetails(HttpServletRequest request) {
        // TODO How to avoid multiple call to get username and authorities
        String token = request.getHeader("x-auth-token");

        SessionPrincipal sessionPrincipal = authenticationClient.getSessionPrincipal(token);
        List<SimpleGrantedAuthority> authorities = sessionPrincipal.getAuthorities()
                .stream().map(s -> new SimpleGrantedAuthority(s))
                .collect(Collectors.toList());
        return new SpringSessionGrantedAuthorities().setAuthorities(authorities);
    }
}

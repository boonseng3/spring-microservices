package com.obs.microservices.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.util.StringUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

public class LoginAuthenticationFilter extends AbstractAuthenticationProcessingFilter {
    private static final Logger logger = LoggerFactory.getLogger(LoginAuthenticationFilter.class);
    public static final String HEADER_SECURITY_USERNAME = "X-Auth-Username";
    public static final String HEADER_SECURITY_PASSWORD = "X-Auth-password";
    public static final String PARAM_USERNAME = "username";
    public static final String PARAM_PASSWORD = "password";

    public LoginAuthenticationFilter(String defaultFilterProcessesUrl) {
        super(defaultFilterProcessesUrl);
        super.setRequiresAuthenticationRequestMatcher(new AntPathRequestMatcher(defaultFilterProcessesUrl));
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException, IOException, ServletException {
        String username = Optional.ofNullable(request.getHeader(HEADER_SECURITY_USERNAME))
                .orElseGet(() -> request.getParameter(PARAM_USERNAME));
        String password = Optional.ofNullable(request.getHeader(HEADER_SECURITY_PASSWORD))
                .orElseGet(() -> request.getParameter(PARAM_PASSWORD));

        if (!StringUtils.hasText(username) || !StringUtils.hasText(password)) {
            throw new BadCredentialsException("Missing credentials.");
        }

        UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(username, password);
        return this.getAuthenticationManager().authenticate(authRequest);
    }
}

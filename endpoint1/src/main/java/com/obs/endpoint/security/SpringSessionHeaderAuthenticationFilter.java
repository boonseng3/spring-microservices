package com.obs.endpoint.security;

import com.obs.endpoint.config.dto.SessionPrincipal;
import com.obs.endpoint.service.AuthenticationClient;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedCredentialsNotFoundException;
import org.springframework.util.Assert;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;

public class SpringSessionHeaderAuthenticationFilter extends
        AbstractPreAuthenticatedProcessingFilter {
    private String principalRequestHeader = "x-auth-token";
    private String credentialsRequestHeader;
    private boolean exceptionIfHeaderMissing = true;

    private AuthenticationClient authenticationClient;

    public AuthenticationClient getAuthenticationClient() {
        return authenticationClient;
    }

    public SpringSessionHeaderAuthenticationFilter setAuthenticationClient(AuthenticationClient authenticationClient) {
        this.authenticationClient = authenticationClient;
        return this;
    }

    /**
     * Read and returns the header named by {@code principalRequestHeader} from the
     * request.
     *
     * @throws PreAuthenticatedCredentialsNotFoundException if the header is missing and
     *                                                      {@code exceptionIfHeaderMissing} is set to {@code true}.
     */
    protected Object getPreAuthenticatedPrincipal(HttpServletRequest request) {
        // TODO How to avoid multiple call to get username and authorities

        String token = request.getHeader("x-auth-token");

        SessionPrincipal sessionPrincipal = authenticationClient.getSessionPrincipal(token);
        String principal = sessionPrincipal.getUsername();

        if (principal == null && exceptionIfHeaderMissing) {
            throw new PreAuthenticatedCredentialsNotFoundException(principalRequestHeader
                    + " header not found in request.");
        }

        List<SimpleGrantedAuthority> authorities =sessionPrincipal.getAuthorities()
                .stream().map(s -> new SimpleGrantedAuthority(s))
                .collect(Collectors.toList());

        return principal;
    }

    /**
     * Credentials aren't usually applicable, but if a {@code credentialsRequestHeader} is
     * set, this will be read and used as the credentials value. Otherwise a dummy value
     * will be used.
     */
    protected Object getPreAuthenticatedCredentials(HttpServletRequest request) {
        if (credentialsRequestHeader != null) {
            return request.getHeader(credentialsRequestHeader);
        }

        return "N/A";
    }

    public void setPrincipalRequestHeader(String principalRequestHeader) {
        Assert.hasText(principalRequestHeader,
                "principalRequestHeader must not be empty or null");
        this.principalRequestHeader = principalRequestHeader;
    }

    public void setCredentialsRequestHeader(String credentialsRequestHeader) {
        Assert.hasText(credentialsRequestHeader,
                "credentialsRequestHeader must not be empty or null");
        this.credentialsRequestHeader = credentialsRequestHeader;
    }

    /**
     * Defines whether an exception should be raised if the principal header is missing.
     * Defaults to {@code true}.
     *
     * @param exceptionIfHeaderMissing set to {@code false} to override the default
     *                                 behaviour and allow the request to proceed if no header is found.
     */
    public void setExceptionIfHeaderMissing(boolean exceptionIfHeaderMissing) {
        this.exceptionIfHeaderMissing = exceptionIfHeaderMissing;
    }
}

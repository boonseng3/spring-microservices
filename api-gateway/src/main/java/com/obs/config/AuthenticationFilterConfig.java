package com.obs.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "application.authentication-filter")
public class AuthenticationFilterConfig {
    private List<String> ignoredUrls;

    public List<String> getIgnoredUrls() {
        return ignoredUrls;
    }

    public AuthenticationFilterConfig setIgnoredUrls(List<String> ignoredUrls) {
        this.ignoredUrls = ignoredUrls;
        return this;
    }
}

package com.obs;

import org.junit.Test;
import org.springframework.util.AntPathMatcher;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

public class EndpointTest {
    @Test
    public void test(){
        AntPathMatcher antPathMatcher = new AntPathMatcher();
        boolean result = antPathMatcher.match("/endpoint1/api/v1/*","/endpoint1/api/v1/a");
        assertThat(result).isTrue();

        result = antPathMatcher.match("/endpoint1/api/v1/**","/endpoint1/api/v1/a/b/c");
        assertThat(result).isTrue();

        result = antPathMatcher.match("/endpoint1/api/v1/**","/endpoint1/api/v1/");
        assertThat(result).isTrue();

        result = antPathMatcher.match("/endpoint1/api/v1/**","/endpoint1/api/v1");
        assertThat(result).isTrue();

        result = antPathMatcher.match("/endpoint1/api/v1/a*","/endpoint1/api/v1/abc");
        assertThat(result).isTrue();
    }


}

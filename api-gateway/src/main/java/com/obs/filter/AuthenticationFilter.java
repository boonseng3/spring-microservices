package com.obs.filter;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.obs.config.AuthenticationFilterConfig;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;

import javax.servlet.http.HttpServletRequest;

import java.util.List;

import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.PRE_DECORATION_FILTER_ORDER;
import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.PRE_TYPE;

@Component
public class AuthenticationFilter extends ZuulFilter {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    AntPathMatcher antPathMatcher = new AntPathMatcher();

    @Autowired
    AuthenticationFilterConfig authenticationFilterConfig;

    @Override
    public String filterType() {
        return PRE_TYPE;
    }

    @Override
    public int filterOrder() {
        return PRE_DECORATION_FILTER_ORDER - 1;
    }

    @Override
    public boolean shouldFilter() {
        return true;
    }

    @Override
    public Object run() {
        RequestContext ctx = RequestContext.getCurrentContext();
        HttpServletRequest request = ctx.getRequest();
        String uri = request.getRequestURI();
        logger.debug("request uri: {}", uri);

        boolean ignored = authenticationFilterConfig.getIgnoredUrls().stream()
                .anyMatch(s -> antPathMatcher.match(s, uri));
        if(!ignored){
            if(StringUtils.isBlank(request.getHeader("X-Auth-Token"))){
                ctx.setResponseStatusCode(HttpStatus.UNAUTHORIZED.value());
                ctx.setSendZuulResponse(false);
                logger.debug("Request uri {} does not have authentication header.", uri);
            } else {
                // verify with authentication server
            }
        }
        return null;
    }
}

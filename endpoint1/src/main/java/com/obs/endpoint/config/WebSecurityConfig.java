package com.obs.endpoint.config;

import com.obs.endpoint.security.*;
import com.obs.endpoint.service.AuthenticationClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.ehcache.EhCacheManagerFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.acls.AclPermissionEvaluator;
import org.springframework.security.acls.domain.*;
import org.springframework.security.acls.model.AclCache;
import org.springframework.security.acls.model.AclService;
import org.springframework.security.acls.model.PermissionGrantingStrategy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationProvider;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedGrantedAuthoritiesUserDetailsService;
import org.springframework.security.web.authentication.preauth.RequestHeaderAuthenticationFilter;
import org.springframework.web.client.RestTemplate;

import java.beans.PropertyVetoException;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());


    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private AuthenticationClient authenticationClient;

    @Override
    public void configure(WebSecurity web) throws Exception {
        web
                .ignoring()
                // disable authentication checking for these url
                .antMatchers("/api/v1/commands", "/api/v1/notification", "/api/v1/echo", "/api/v1/info");
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .csrf().disable()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authorizeRequests()
//                .antMatchers("/api/v1/commands", "/api/v1/notification", "/api/v1/echo", "/api/v1/info").permitAll()
                .anyRequest().authenticated()
        ;
        http.addFilterBefore(requestHeader1AuthenticationFilter(authenticationManager(), restTemplate), RequestHeaderAuthenticationFilter.class);
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth)
            throws Exception {
        auth.authenticationProvider(preauthAuthProvider());
    }

    @Bean
    public PreAuthenticatedAuthenticationProvider preauthAuthProvider() {
        PreAuthenticatedAuthenticationProvider preauthAuthProvider = new PreAuthenticatedAuthenticationProvider();
        preauthAuthProvider.setPreAuthenticatedUserDetailsService(new PreAuthenticatedGrantedAuthoritiesUserDetailsService());
        return preauthAuthProvider;
    }

    public SpringSessionHeaderAuthenticationFilter requestHeader1AuthenticationFilter(
            final AuthenticationManager authenticationManager, final RestTemplate restTemplate) {
        SpringSessionHeaderAuthenticationFilter filter = new SpringSessionHeaderAuthenticationFilter()
                .setAuthenticationClient(authenticationClient);
        filter.setAuthenticationManager(authenticationManager);
        SpringSessionAuthenticationDetailsSource springSessionAuthenticationDetailsSource = new SpringSessionAuthenticationDetailsSource()
                .setAuthenticationClient(authenticationClient);
        filter.setAuthenticationDetailsSource(springSessionAuthenticationDetailsSource);
        return filter;
    }

    @Bean
    public ExternalAclLookupStrategy lookupStrategy() throws PropertyVetoException {
        ExternalAclLookupStrategy lookupStrategy = new ExternalAclLookupStrategy();
        return lookupStrategy;
    }

    @Bean
    public AclService aclService() throws PropertyVetoException {
        final ExternalAclService externalAclService = new ExternalAclService(lookupStrategy());
        return externalAclService;
    }


    @Bean
    public AclAuthorizationStrategy aclAuthorizationStrategy() {
        return new AclAuthorizationStrategyImpl(
                new SimpleGrantedAuthority("ROLE_ADMIN"),
                new SimpleGrantedAuthority("ROLE_ADMIN"),
                new SimpleGrantedAuthority("ROLE_ADMIN"));
    }

    @Bean
    public AuditLogger auditLogger() {
        return new ConsoleAuditLogger();
    }

    @Bean
    public PermissionGrantingStrategy grantingStrategy() {
        PermissionGrantingStrategy bean = new DefaultPermissionGrantingStrategy(auditLogger());
        return bean;
    }

    @Bean
    public AclCache aclCache() {
        // TODO: How to handle distribute cache if need to scale
        return new EhCacheBasedAclCache(ehCacheManagerFactoryBean().getObject().getCache("aclCache"), grantingStrategy(), aclAuthorizationStrategy());
    }


    @Bean
    public EhCacheManagerFactoryBean ehCacheManagerFactoryBean() {
        EhCacheManagerFactoryBean cacheManagerFactoryBean = new EhCacheManagerFactoryBean();

        cacheManagerFactoryBean.setConfigLocation(new ClassPathResource("ehcache.xml"));
        cacheManagerFactoryBean.setShared(true);
        return cacheManagerFactoryBean;
    }

    @Bean
    public PermissionEvaluator permissionEvaluator() throws PropertyVetoException {
        AclPermissionEvaluator bean = new AclPermissionEvaluator(aclService());
        bean.setPermissionFactory(externalPermissionFactory());
        return bean;
    }

    @Bean
    public ExternalPermissionFactory externalPermissionFactory() {
        ExternalPermissionFactory bean = new ExternalPermissionFactory();

        return bean;
    }
}

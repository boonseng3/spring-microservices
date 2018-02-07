package com.obs.microservices.config;

import com.obs.microservices.security.CustomPermissionFactory;
import com.obs.microservices.security.LoginAuthenticationFilter;
import com.obs.microservices.security.UsernamePasswordAuthenticationFailureHandler;
import com.obs.microservices.security.UsernamePasswordAuthenticationSuccessHandler;
import com.obs.microservices.service.impl.CustomUserServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.ehcache.EhCacheManagerFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.acls.AclPermissionEvaluator;
import org.springframework.security.acls.domain.*;
import org.springframework.security.acls.jdbc.BasicLookupStrategy;
import org.springframework.security.acls.jdbc.JdbcMutableAclService;
import org.springframework.security.acls.model.AclCache;
import org.springframework.security.acls.model.MutableAclService;
import org.springframework.security.acls.model.PermissionGrantingStrategy;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.beans.PropertyVetoException;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    @Value("${application.login.url}")
    private String authenticationUrl;

    @Autowired
    private CustomUserServiceImpl userDetailsService;

    @Override
    protected void configure(AuthenticationManagerBuilder auth)
            throws Exception {
        auth.authenticationProvider(authenticationProvider());
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Override
    public void configure(WebSecurity web) throws Exception {
        web
                .ignoring()
                // disable authentication checking for these url
                .antMatchers("/api/v1/permissions", "/api/v1/acl");
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .csrf().disable()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                .and()
                .authorizeRequests()
                .anyRequest().authenticated()
                .and()
                .anonymous().disable()

                .exceptionHandling().authenticationEntryPoint(authenticationEntryPoint());
        http.addFilterBefore(loginAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);
        ;
    }

    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint() {
        return (request, response, authException) -> {
            logger.debug("Authentication failure", authException);
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");

        };
    }

    public LoginAuthenticationFilter loginAuthenticationFilter() throws Exception {
        LoginAuthenticationFilter filter = new LoginAuthenticationFilter(authenticationUrl);
        filter.setAuthenticationManager(authenticationManagerBean());
        filter.setAuthenticationSuccessHandler(usernamePasswordAuthenticationSuccessHandler());
        filter.setAuthenticationFailureHandler(usernamePasswordAuthenticationFailureHandler());
        return filter;
    }

    @Bean
    public UsernamePasswordAuthenticationSuccessHandler usernamePasswordAuthenticationSuccessHandler() {
        return new UsernamePasswordAuthenticationSuccessHandler();
    }

    @Bean
    public UsernamePasswordAuthenticationFailureHandler usernamePasswordAuthenticationFailureHandler() {
        return new UsernamePasswordAuthenticationFailureHandler();
    }


    // ACL
    @Bean
    public MutableAclService aclService(@Autowired DataSource dataSource) throws PropertyVetoException {
        final JdbcMutableAclService mutableAclService = new JdbcMutableAclService(dataSource, lookupStrategy(dataSource), aclCache());
        mutableAclService.setClassIdentityQuery("SELECT LAST_INSERT_ID()");
        mutableAclService.setSidIdentityQuery("SELECT LAST_INSERT_ID()");
        return mutableAclService;
    }

    @Bean
    public BasicLookupStrategy lookupStrategy(@Autowired DataSource dataSource) throws PropertyVetoException {
        BasicLookupStrategy basicLookupStrategy = new BasicLookupStrategy(dataSource, aclCache(), aclAuthorizationStrategy(), grantingStrategy());
        basicLookupStrategy.setPermissionFactory(customPermissionFactory());
        return basicLookupStrategy;
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
    public AclAuthorizationStrategy aclAuthorizationStrategy() {
        return new AclAuthorizationStrategyImpl(
                new SimpleGrantedAuthority("ROLE_ADMIN"),
                new SimpleGrantedAuthority("ROLE_ADMIN"),
                new SimpleGrantedAuthority("ROLE_ADMIN"));
    }

    @Bean
    public PermissionGrantingStrategy grantingStrategy() {
        PermissionGrantingStrategy bean = new DefaultPermissionGrantingStrategy(auditLogger());
        return bean;
    }

    @Bean
    public AuditLogger auditLogger() {
        return new ConsoleAuditLogger();
    }

    @Bean
    public PermissionEvaluator permissionEvaluator(MutableAclService aclService) {
        AclPermissionEvaluator bean = new AclPermissionEvaluator(aclService);
        bean.setPermissionFactory(customPermissionFactory());
        return bean;
    }

    @Bean
    public PermissionFactory customPermissionFactory() {
        return new CustomPermissionFactory();
    }


}

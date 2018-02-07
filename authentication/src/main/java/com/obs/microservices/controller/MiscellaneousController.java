package com.obs.microservices.controller;

import com.obs.microservices.dto.Profile;
import com.obs.microservices.dto.SessionPrincipal;
import com.obs.microservices.repo.ProfileRepo;
import com.obs.microservices.repo.UserRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.acls.domain.AclAuthorizationStrategy;
import org.springframework.security.acls.model.PermissionGrantingStrategy;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.*;

@RestController
public class MiscellaneousController {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private UserRepo userRepo;
    @Autowired
    private ProfileRepo profileRepo;

    @Autowired
    AclAuthorizationStrategy aclAuthorizationStrategy;
    @Autowired
    PermissionGrantingStrategy grantingStrategy;

    @RequestMapping("/api/v1/session")
    public Map<String, Object> updateSession(HttpSession session, @RequestParam String currentValue) {
        Map<String, Object> sessionMap = new HashMap<>();

        Object value = session.getAttribute("previousValue");
        session.setAttribute("previousValue", currentValue);
        sessionMap.put("previousValue", value);
        sessionMap.put("currentValue", currentValue);

        return sessionMap;
    }

    /**
     * User is allowed to access their own profile.
     * Admin is allowed to access all user profile.
     *
     * @param id
     * @return
     */
    @PreAuthorize("hasPermission(#id, 'com.obs.dto.Profile', 'GET_PROFILE') || hasPermission(0, 'com.obs.dto.Profile', 'GET_PROFILE')")
    @GetMapping("/profile/{id}")
    public Profile profile(@PathVariable Long id) {
        return Optional.ofNullable(profileRepo.findOne(id))
                .map(profile1 -> new Profile().setName(profile1.getFirstName()
                        + (StringUtils.hasText(profile1.getLastName()) ? " " + profile1.getLastName() : "")))
                .<RuntimeException>orElseThrow(() -> {
                    throw new RuntimeException("Profile record not found.");
                });
    }

    @GetMapping("/api/v1/session_principal")
    public SessionPrincipal principal(HttpSession session) {
        List<String> authorities = new ArrayList<>();
        SessionPrincipal sessionPrincipal = new SessionPrincipal().setAuthorities(authorities);

        if (logger.isDebugEnabled()) {
            Enumeration<String> names = session.getAttributeNames();
            while (names.hasMoreElements()) {
                String name = names.nextElement();
                logger.debug("{} = {}", name, session.getAttribute(name));
            }
        }

        SecurityContext springSecurityContext = (SecurityContext) session.getAttribute("SPRING_SECURITY_CONTEXT");
        UserDetails userDetails = (UserDetails) springSecurityContext.getAuthentication().getPrincipal();
        String username = userDetails.getUsername();
        sessionPrincipal.setUsername(username);

        logger.debug("username: {}", username);
        springSecurityContext.getAuthentication().getAuthorities().forEach(grantedAuthoritiy -> {
            sessionPrincipal.getAuthorities().add(grantedAuthoritiy.getAuthority());
            logger.debug("authority: {}", grantedAuthoritiy.getAuthority());
        });
        return sessionPrincipal;
    }


}

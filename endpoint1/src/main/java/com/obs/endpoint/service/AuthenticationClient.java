package com.obs.endpoint.service;

import com.obs.endpoint.config.dto.SessionPrincipal;
import com.obs.microservices.CustomPermission;
import com.obs.microservices.dto.AclRequest;
import com.obs.microservices.dto.AclResponse;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@FeignClient("authentication-server")
public interface AuthenticationClient {
    @RequestMapping(method = RequestMethod.GET, value = "/api/v1/session_principal")
    SessionPrincipal getSessionPrincipal(@RequestHeader("X-Auth-Token") String token);

    @RequestMapping(method = RequestMethod.POST, value = "/api/v1/acl", consumes = MediaType.APPLICATION_JSON_VALUE)
    AclResponse[] getAcl(AclRequest aclRequest);

    @RequestMapping(method = RequestMethod.POST, value = "/api/v1/permissions", consumes = MediaType.APPLICATION_JSON_VALUE)
    CustomPermission[] getPermission();
}

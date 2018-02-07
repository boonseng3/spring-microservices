package com.obs.microservices.controller;

import com.obs.microservices.CustomPermission;
import com.obs.microservices.SpringAclUtil;
import com.obs.microservices.dto.AclRequest;
import com.obs.microservices.dto.AclResponse;
import com.obs.microservices.repo.PermissionRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.acls.domain.AclImpl;
import org.springframework.security.acls.domain.ObjectIdentityImpl;
import org.springframework.security.acls.domain.PrincipalSid;
import org.springframework.security.acls.model.*;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.util.*;
import java.util.stream.Collectors;

@RestController
public class AclController {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    private AclService aclService;

    @Autowired
    private PermissionRepo permissionRepo;

    @RequestMapping("/api/v1/acl")
    public List<AclResponse> aclForSessionPrincipal1(HttpSession session, @RequestBody AclRequest<Long> body) {
        // if sid is provided, search based on the sid else based on the current username
        List<Sid> sids = Optional.ofNullable(body.getSid())
                .orElseGet(() -> {
                    SecurityContext springSecurityContext = (SecurityContext) session.getAttribute("SPRING_SECURITY_CONTEXT");
                    UserDetails userDetails = (UserDetails) springSecurityContext.getAuthentication().getPrincipal();
                    return Arrays.asList(userDetails.getUsername());
                })
                .stream().map(s -> new PrincipalSid(s))
                .collect(Collectors.toList());

        if (body.getOid().size() > 1) {
            List<ObjectIdentity> oids = body.getOid().stream()
                    .map(dto -> SpringAclUtil.map(dto))
                    .collect(Collectors.toList());

            try {
                Map<ObjectIdentity, Acl> acls = aclService.readAclsById(oids, sids);

                return acls.entrySet().stream()
                        .map(objectIdentityAclEntry -> new AclResponse()
                                .setObjectIdentity(SpringAclUtil.map(objectIdentityAclEntry.getKey()))
                                .setAcl(SpringAclUtil.map((AclImpl) objectIdentityAclEntry.getValue())))
                        .collect(Collectors.toList());
            } catch (NotFoundException e) {
                logger.debug("No Acl information", e);
            }
            return Collections.EMPTY_LIST;


        } else {
            ObjectIdentity oid = SpringAclUtil.map(body.getOid().get(0));
            List<AclResponse> result = new ArrayList<>(1);
            try {
                result.add(new AclResponse()
                        .setObjectIdentity(SpringAclUtil.map(oid))
                        .setAcl(SpringAclUtil.map((AclImpl) aclService.readAclById(oid, sids))));

            } catch (NotFoundException e) {
                logger.debug("No Acl information", e);
            }
            return result;
        }
    }

    @RequestMapping("/api/v1/permissions")
    public List<CustomPermission> permissions(@RequestParam Optional<Integer> id, @RequestParam Optional<String> name) {

        return id
                .map(integer -> permissionRepo.findById(integer).<RuntimeException>orElseThrow(() -> new RuntimeException("Id not found")))
                .map(permission -> Arrays.asList(new CustomPermission(permission.getId(), permission.getName())))
                .orElseGet(() ->
                        name
                                .map(s -> permissionRepo.findByName(s).<RuntimeException>orElseThrow(() -> new RuntimeException("Name not found")))
                                .map(permission -> Arrays.asList(new CustomPermission(permission.getId(), permission.getName())))
                                .orElseGet(() -> permissionRepo.findAll().stream()
                                        .map(permission -> new CustomPermission(permission.getId(), permission.getName()))
                                        .collect(Collectors.toList()))
                );


    }
}

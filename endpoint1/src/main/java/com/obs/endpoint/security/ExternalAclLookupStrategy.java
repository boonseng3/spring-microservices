package com.obs.endpoint.security;

import com.obs.endpoint.service.AuthenticationClient;
import com.obs.microservices.SpringAclUtil;
import com.obs.microservices.dto.AclRequest;
import com.obs.microservices.dto.AclResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.acls.domain.AclAuthorizationStrategy;
import org.springframework.security.acls.domain.PrincipalSid;
import org.springframework.security.acls.jdbc.LookupStrategy;
import org.springframework.security.acls.model.Acl;
import org.springframework.security.acls.model.ObjectIdentity;
import org.springframework.security.acls.model.PermissionGrantingStrategy;
import org.springframework.security.acls.model.Sid;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ExternalAclLookupStrategy implements LookupStrategy {
    @Autowired
    private AuthenticationClient authenticationClient;
    @Autowired
    AclAuthorizationStrategy aclAuthorizationStrategy;

    @Autowired
    PermissionGrantingStrategy grantingStrategy;
    @Autowired
    ExternalPermissionFactory permissionFactory;


    @Override
    public Map<ObjectIdentity, Acl> readAclsById(List<ObjectIdentity> objects, List<Sid> sids) {
        // how to authenticate at here
//        String token ="";
        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

        AclRequest aclRequest = new AclRequest<>();
        aclRequest.setOid(objects.stream().map(SpringAclUtil::map)
                .collect(Collectors.toList()));
        aclRequest.setSid(sids.stream()
                .filter(sid -> sid instanceof PrincipalSid)
                .map(sid -> ((PrincipalSid) sid).getPrincipal())
                .collect(Collectors.toList()));


        AclResponse<Long>[] response = authenticationClient.getAcl(aclRequest);
        Map<ObjectIdentity, Acl> aclMap = Arrays.asList(response).stream()
                .collect(Collectors.toMap(o -> SpringAclUtil.map(o.getObjectIdentity()),
                        v -> {
                            try {
                                return SpringAclUtil.map(v.getAcl(), aclAuthorizationStrategy, grantingStrategy);
                            } catch (IllegalAccessException e) {
                                e.printStackTrace();
                                throw new RuntimeException(e);
                            }
                        }));
        return aclMap;
    }
}

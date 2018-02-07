package com.obs.microservices;

import com.obs.microservices.dto.*;
import org.springframework.security.acls.domain.*;
import org.springframework.security.acls.model.*;
import org.springframework.security.util.FieldUtils;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.List;
import java.util.stream.Collectors;

public class SpringAclUtil {
    private final static Field fieldAces = FieldUtils.getField(AclImpl.class, "aces");

    static {
        fieldAces.setAccessible(true);
    }

    public static ObjectIdentity map(ObjectIdentityDto<? extends Serializable> object) {
        return new ObjectIdentityImpl(object.getType(), object.getIdentifier());
    }

    public static ObjectIdentityDto<? extends Serializable> map(ObjectIdentity object) {
        return new ObjectIdentityDto().setType(object.getType()).setIdentifier(object.getIdentifier());
    }

    public static Sid map(SidDto object) {
        return new PrincipalSid(object.getPrincipal());
    }

    public static SidDto map(PrincipalSid object) {
        return new SidDto().setPrincipal(object.getPrincipal());
    }

    public static Permission map(PermissionDto object) {
        return new Permission() {
            @Override
            public int getMask() {
                return object.getMask();
            }

            @Override
            public String getPattern() {
                return object.getPattern();
            }
        };
    }

    public static PermissionDto map(Permission object) {
        return new PermissionDto().setMask(object.getMask()).setPattern(object.getPattern());
    }

    public static AccessControlEntry map(AccessControlEntryDto object, Acl acl) {
        return new AccessControlEntryImpl((Serializable) object.getId(),
                acl,
                map(object.getSid()),
                map(object.getPermission()),
                object.isGranting(),
                object.isAuditSuccess(),
                object.isAuditFailure());
    }

    public static AccessControlEntryDto<Long> map(AccessControlEntry object) {
        return new AccessControlEntryDto<>()
                .setId(object.getId())
                .setSid(map((PrincipalSid) object.getSid()))
                .setPermission(map(object.getPermission()))
                .setGranting(object.isGranting());
    }


    public static Acl map(AclDto<Long> object, AclAuthorizationStrategy aclAuthorizationStrategy, PermissionGrantingStrategy grantingStrategy) throws IllegalAccessException {
        if (object == null) {
            return null;
        }
        Acl acl = new AclImpl(map(object.getObjectIdentity()), (Serializable) object.getId(), aclAuthorizationStrategy,
                grantingStrategy, map(object.getParentAcl(), aclAuthorizationStrategy, grantingStrategy), null, object.isEntriesInheriting(),
                map(object.getOwner()));

        List<AccessControlEntry> aces = object.getEntries().stream()
                .map(longAccessControlEntryDto -> map(longAccessControlEntryDto, acl))
                .collect(Collectors.toList());
        fieldAces.set(acl, aces);


        return acl;
    }

    public static AclDto<Long> map(AclImpl object) {
        if (object == null) {
            return null;
        }

        AclDto<Long> acl = new AclDto()
                .setParentAcl(map((AclImpl) object.getParentAcl()))
                .setId((Long) object.getId())
                .setObjectIdentity(map(object.getObjectIdentity()))
                .setOwner(map((PrincipalSid) object.getOwner()))
                .setEntriesInheriting(object.isEntriesInheriting());


        List<AccessControlEntryDto<Long>> aces = object.getEntries()
                .stream().map(accessControlEntry -> map(accessControlEntry))
                .collect(Collectors.toList());

        acl.setEntries(aces);


        return acl;
    }
}

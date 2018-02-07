package com.obs.microservices.dto;

import java.io.Serializable;

public class AclResponse<T extends Serializable> {
    private ObjectIdentityDto<T> objectIdentity;
    private AclDto<T> acl;

    public ObjectIdentityDto<T> getObjectIdentity() {
        return objectIdentity;
    }

    public AclResponse setObjectIdentity(ObjectIdentityDto<T> objectIdentity) {
        this.objectIdentity = objectIdentity;
        return this;
    }

    public AclDto<T> getAcl() {
        return acl;
    }

    public AclResponse setAcl(AclDto<T> acl) {
        this.acl = acl;
        return this;
    }
}
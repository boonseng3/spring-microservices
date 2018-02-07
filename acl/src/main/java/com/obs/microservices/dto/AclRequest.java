package com.obs.microservices.dto;

import java.io.Serializable;
import java.util.List;

public class AclRequest<T extends Serializable> {
    private List<ObjectIdentityDto<T>> oid;
    private List<String> sid;

    public List<ObjectIdentityDto<T>> getOid() {
        return oid;
    }

    public AclRequest setOid(List<ObjectIdentityDto<T>> oid) {
        this.oid = oid;
        return this;
    }

    public List<String> getSid() {
        return sid;
    }

    public AclRequest setSid(List<String> sid) {
        this.sid = sid;
        return this;
    }
}
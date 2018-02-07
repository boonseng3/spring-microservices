package com.obs.microservices.dto;

import java.io.Serializable;
import java.util.List;

public class AclDto<T extends Serializable> {
    private AclDto<T> parentAcl;
    private List<AccessControlEntryDto<T>> entries;
    private ObjectIdentityDto<T> objectIdentity;
    private T id;
    private SidDto owner; // OwnershipAcl
    private boolean entriesInheriting = true;

    public AclDto<T> getParentAcl() {
        return parentAcl;
    }

    public AclDto<T> setParentAcl(AclDto<T> parentAcl) {
        this.parentAcl = parentAcl;
        return this;
    }

    public List<AccessControlEntryDto<T>> getEntries() {
        return entries;
    }

    public AclDto setEntries(List<AccessControlEntryDto<T>> entries) {
        this.entries = entries;
        return this;
    }

    public ObjectIdentityDto<T> getObjectIdentity() {
        return objectIdentity;
    }

    public AclDto setObjectIdentity(ObjectIdentityDto<T> objectIdentity) {
        this.objectIdentity = objectIdentity;
        return this;
    }

    public T getId() {
        return id;
    }

    public AclDto setId(T id) {
        this.id = id;
        return this;
    }

    public SidDto getOwner() {
        return owner;
    }

    public AclDto setOwner(SidDto owner) {
        this.owner = owner;
        return this;
    }

    public boolean isEntriesInheriting() {
        return entriesInheriting;
    }

    public AclDto setEntriesInheriting(boolean entriesInheriting) {
        this.entriesInheriting = entriesInheriting;
        return this;
    }
}

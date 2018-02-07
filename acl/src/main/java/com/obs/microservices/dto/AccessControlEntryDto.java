package com.obs.microservices.dto;

import java.io.Serializable;

public class AccessControlEntryDto<T extends Serializable> {
    private PermissionDto permission;
    private T id;
    private SidDto sid;
    private boolean auditFailure = false;
    private boolean auditSuccess = false;
    private boolean granting;

    public PermissionDto getPermission() {
        return permission;
    }

    public AccessControlEntryDto setPermission(PermissionDto permission) {
        this.permission = permission;
        return this;
    }

    public T getId() {
        return id;
    }

    public AccessControlEntryDto setId(T id) {
        this.id = id;
        return this;
    }

    public boolean isAuditFailure() {
        return auditFailure;
    }

    public AccessControlEntryDto setAuditFailure(boolean auditFailure) {
        this.auditFailure = auditFailure;
        return this;
    }

    public boolean isAuditSuccess() {
        return auditSuccess;
    }

    public AccessControlEntryDto setAuditSuccess(boolean auditSuccess) {
        this.auditSuccess = auditSuccess;
        return this;
    }

    public boolean isGranting() {
        return granting;
    }

    public AccessControlEntryDto setGranting(boolean granting) {
        this.granting = granting;
        return this;
    }

    public SidDto getSid() {
        return sid;
    }

    public AccessControlEntryDto setSid(SidDto sid) {
        this.sid = sid;
        return this;
    }

}

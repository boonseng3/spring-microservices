package com.obs.microservices;

import org.springframework.security.acls.model.Permission;

public class CustomPermission implements Permission {

    private int permissionId;
    private String permission;

    public CustomPermission(int permissionId, String permission) {
        this.permissionId = permissionId;
        this.permission = permission;
    }

    @Override
    public int getMask() {
        return permissionId;
    }

    @Override
    public String getPattern() {
        return permission;
    }
}

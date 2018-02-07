package com.obs.microservices.security;

import com.obs.microservices.CustomPermission;
import com.obs.microservices.repo.PermissionRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.acls.domain.PermissionFactory;
import org.springframework.security.acls.model.Permission;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


public class CustomPermissionFactory implements PermissionFactory {
    @Autowired
    private PermissionRepo permissionRepo;

    @Override
    public Permission buildFromMask(int mask) {
        return Optional.ofNullable(permissionRepo.findOne(Integer.valueOf(mask)))
                .map(permission -> new CustomPermission(permission.getId(), permission.getName()))
                .<IllegalStateException>orElseThrow(() -> {
                    throw new IllegalStateException("Mask '" + mask
                            + "' does not have a corresponding Permission");
                });
    }

    @Override
    public Permission buildFromName(String name) {
        return permissionRepo.findByName(name)
                .map(permission -> new CustomPermission(permission.getId(), permission.getName()))
                .<IllegalStateException>orElseThrow(() -> {
                    throw new IllegalStateException("Mask '" + name
                            + "' does not have a corresponding Permission");
                });
    }

    @Override
    public List<Permission> buildFromNames(List<String> names) {
        if ((names == null) || (names.size() == 0)) {
            return Collections.emptyList();
        }
        return names.stream()
                .map(s -> buildFromName(s))
                .collect(Collectors.toList());
    }
}

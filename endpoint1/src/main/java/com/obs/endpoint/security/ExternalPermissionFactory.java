package com.obs.endpoint.security;

import com.obs.endpoint.service.AuthenticationClient;
import com.obs.microservices.CustomPermission;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.security.acls.domain.BasePermission;
import org.springframework.security.acls.domain.CumulativePermission;
import org.springframework.security.acls.domain.PermissionFactory;
import org.springframework.security.acls.model.Permission;
import org.springframework.util.Assert;

import java.lang.reflect.Field;
import java.util.*;

public class ExternalPermissionFactory implements PermissionFactory {
    private final Map<Integer, Permission> registeredPermissionsByInteger = new HashMap<Integer, Permission>();
    private final Map<String, Permission> registeredPermissionsByName = new HashMap<String, Permission>();

    @Autowired
    private AuthenticationClient authenticationClient;

    /**
     * Registers the <tt>Permission</tt> fields from the <tt>BasePermission</tt> class.
     */
    public ExternalPermissionFactory() {
//        registerPublicPermissions(CustomPermission.class);

    }

    /**
     * Registers the public static fields of type {@link Permission} for a give class.
     * <p>
     * These permissions will be registered under the name of the field. See
     * {@link BasePermission} for an example.
     *
     * @param clazz a {@link Permission} class with public static fields to register
     */
    public void registerPublicPermissions(Class<? extends Permission> clazz) {
        Assert.notNull(clazz, "Class required");

        Field[] fields = clazz.getFields();

        for (Field field : fields) {
            try {
                Object fieldValue = field.get(null);

                if (Permission.class.isAssignableFrom(fieldValue.getClass())) {
                    // Found a Permission static field
                    Permission perm = (Permission) fieldValue;
                    String permissionName = field.getName();

                    registerPermission(perm, permissionName);
                }
            } catch (Exception ignore) {
            }
        }
    }

    public void registerPermission(Permission perm, String permissionName) {
        Assert.notNull(perm, "Permission required");
        Assert.hasText(permissionName, "Permission name required");

        Integer mask = Integer.valueOf(perm.getMask());

        // Ensure no existing Permission uses this integer or code
        Assert.isTrue(!registeredPermissionsByInteger.containsKey(mask),
                "An existing Permission already provides mask " + mask);
        Assert.isTrue(!registeredPermissionsByName.containsKey(permissionName),
                "An existing Permission already provides name '" + permissionName + "'");

        // Register the new Permission
        registeredPermissionsByInteger.put(mask, perm);
        registeredPermissionsByName.put(permissionName, perm);
    }

    public Permission buildFromMask(int mask) {
        if (registeredPermissionsByInteger.containsKey(Integer.valueOf(mask))) {
            // The requested mask has an exact match against a statically-defined
            // Permission, so return it
            return registeredPermissionsByInteger.get(Integer.valueOf(mask));
        }

        // To get this far, we have to use a CumulativePermission
        CumulativePermission permission = new CumulativePermission();

        for (int i = 0; i < 32; i++) {
            int permissionToCheck = 1 << i;

            if ((mask & permissionToCheck) == permissionToCheck) {
                Permission p = registeredPermissionsByInteger.get(Integer
                        .valueOf(permissionToCheck));

                if (p == null) {
                    throw new IllegalStateException("Mask '" + permissionToCheck
                            + "' does not have a corresponding static Permission");
                }
                permission.set(p);
            }
        }

        return permission;
    }

    public Permission buildFromName(String name) {
        Permission p = registeredPermissionsByName.get(name);
        if (p == null) {
            HttpHeaders requestHeaders = new HttpHeaders();


            CustomPermission[] response = authenticationClient.getPermission();
            Arrays.asList(response)
                    .forEach(customPermission -> {
                        registerPermission(customPermission, customPermission.getPattern());
                    });
            p = registeredPermissionsByName.get(name);
        }
        if (p == null) {
            throw new IllegalArgumentException("Unknown permission '" + name + "'");
        }

        return p;
    }

    public List<Permission> buildFromNames(List<String> names) {
        if ((names == null) || (names.size() == 0)) {
            return Collections.emptyList();
        }

        List<Permission> permissions = new ArrayList<Permission>(names.size());

        for (String name : names) {
            permissions.add(buildFromName(name));
        }

        return permissions;
    }
}

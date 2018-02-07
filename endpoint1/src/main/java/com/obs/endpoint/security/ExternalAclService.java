package com.obs.endpoint.security;

import org.springframework.security.acls.jdbc.LookupStrategy;
import org.springframework.security.acls.model.*;
import org.springframework.util.Assert;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class ExternalAclService implements AclService {
    private final LookupStrategy lookupStrategy;


    public ExternalAclService(LookupStrategy lookupStrategy) {
        this.lookupStrategy = lookupStrategy;
    }

    /**
     * Locates all object identities that use the specified parent. This is useful for
     * administration tools.
     *
     * @param parentIdentity to locate children of
     * @return the children (or <tt>null</tt> if none were found)
     */
    @Override
    public List<ObjectIdentity> findChildren(ObjectIdentity parentIdentity) {
        throw new UnsupportedOperationException();
    }

    /**
     * Same as {@link #readAclsById(List)} except it returns only a single Acl.
     * <p>
     * This method should not be called as it does not leverage the underlying
     * implementation's potential ability to filter <tt>Acl</tt> entries based on a
     * {@link Sid} parameter.
     * </p>
     *
     * @param object to locate an {@link Acl} for
     * @return the {@link Acl} for the requested {@link ObjectIdentity} (never
     * <tt>null</tt>)
     * @throws NotFoundException if an {@link Acl} was not found for the requested
     *                           {@link ObjectIdentity}
     */
    @Override
    public Acl readAclById(ObjectIdentity object) throws NotFoundException {
        return readAclById(object, null);
    }

    /**
     * Same as {@link #readAclsById(List, List)} except it returns only a single Acl.
     *
     * @param object to locate an {@link Acl} for
     * @param sids   the security identities for which {@link Acl} information is required
     *               (may be <tt>null</tt> to denote all entries)
     * @return the {@link Acl} for the requested {@link ObjectIdentity} (never
     * <tt>null</tt>)
     * @throws NotFoundException if an {@link Acl} was not found for the requested
     *                           {@link ObjectIdentity}
     */
    @Override
    public Acl readAclById(ObjectIdentity object, List<Sid> sids) throws NotFoundException {
        Map<ObjectIdentity, Acl> map = readAclsById(Arrays.asList(object), sids);
        Assert.isTrue(map.containsKey(object),
                "There should have been an Acl entry for ObjectIdentity " + object);

        return (Acl) map.get(object);
    }

    /**
     * Obtains all the <tt>Acl</tt>s that apply for the passed <tt>Object</tt>s.
     * <p>
     * The returned map is keyed on the passed objects, with the values being the
     * <tt>Acl</tt> instances. Any unknown objects will not have a map key.
     * </p>
     *
     * @param objects the objects to find {@link Acl} information for
     * @return a map with exactly one element for each {@link ObjectIdentity} passed as an
     * argument (never <tt>null</tt>)
     * @throws NotFoundException if an {@link Acl} was not found for each requested
     *                           {@link ObjectIdentity}
     */
    @Override
    public Map<ObjectIdentity, Acl> readAclsById(List<ObjectIdentity> objects) throws NotFoundException {
        return readAclsById(objects, null);
    }

    /**
     * Obtains all the <tt>Acl</tt>s that apply for the passed <tt>Object</tt>s, but only
     * for the security identifies passed.
     * <p>
     * Implementations <em>MAY</em> provide a subset of the ACLs via this method although
     * this is NOT a requirement. This is intended to allow performance optimisations
     * within implementations. Callers should therefore use this method in preference to
     * the alternative overloaded version which does not have performance optimisation
     * opportunities.
     * </p>
     * <p>
     * The returned map is keyed on the passed objects, with the values being the
     * <tt>Acl</tt> instances. Any unknown objects (or objects for which the interested
     * <tt>Sid</tt>s do not have entries) will not have a map key.
     * </p>
     *
     * @param objects the objects to find {@link Acl} information for
     * @param sids    the security identities for which {@link Acl} information is required
     *                (may be <tt>null</tt> to denote all entries)
     * @return a map with exactly one element for each {@link ObjectIdentity} passed as an
     * argument (never <tt>null</tt>)
     * @throws NotFoundException if an {@link Acl} was not found for each requested
     *                           {@link ObjectIdentity}
     */
    @Override
    public Map<ObjectIdentity, Acl> readAclsById(List<ObjectIdentity> objects, List<Sid> sids) throws NotFoundException {
        Map<ObjectIdentity, Acl> result = lookupStrategy.readAclsById(objects, sids);

        // Check every requested object identity was found (throw NotFoundException if
        // needed)
        for (ObjectIdentity oid : objects) {
            if (!result.containsKey(oid)) {
                throw new NotFoundException(
                        "Unable to find ACL information for object identity '" + oid
                                + "'");
            }
        }

        return result;
    }
}

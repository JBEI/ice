package org.jbei.ice.lib.dao.hibernate;

import org.jbei.ice.lib.access.RemotePermission;

/**
 * @author Hector Plahar
 */
public class RemotePermissionDAO extends HibernateRepository<RemotePermission> {

    @Override
    public RemotePermission get(long id) {
        return super.get(RemotePermission.class, id);
    }
}

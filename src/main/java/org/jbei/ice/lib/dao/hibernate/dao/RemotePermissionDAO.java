package org.jbei.ice.lib.dao.hibernate.dao;

import org.jbei.ice.lib.access.RemotePermission;
import org.jbei.ice.lib.dao.hibernate.HibernateRepository;

/**
 * @author Hector Plahar
 */
public class RemotePermissionDAO extends HibernateRepository<RemotePermission> {

    @Override
    public RemotePermission get(long id) {
        return super.get(RemotePermission.class, id);
    }
}

package org.jbei.ice.storage.hibernate.dao;

import org.jbei.ice.storage.hibernate.HibernateRepository;
import org.jbei.ice.storage.model.RemotePermission;

/**
 * @author Hector Plahar
 */
public class RemotePermissionDAO extends HibernateRepository<RemotePermission> {

    @Override
    public RemotePermission get(long id) {
        return super.get(RemotePermission.class, id);
    }
}

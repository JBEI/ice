package org.jbei.ice.storage.hibernate.dao;

import org.hibernate.criterion.Restrictions;
import org.jbei.ice.storage.DAOException;
import org.jbei.ice.storage.hibernate.HibernateRepository;
import org.jbei.ice.storage.model.Folder;
import org.jbei.ice.storage.model.RemotePartner;
import org.jbei.ice.storage.model.RemoteShareModel;

import java.util.List;

/**
 * @author Hector Plahar
 */
@SuppressWarnings("unchecked")
public class RemoteShareModelDAO extends HibernateRepository<RemoteShareModel> {

    @Override
    public RemoteShareModel get(long id) {
        return super.get(RemoteShareModel.class, id);
    }

    public RemoteShareModel get(String userId, RemotePartner remotePartner, Folder folder) throws DAOException {
        return (RemoteShareModel) currentSession().createCriteria(RemoteShareModel.class)
                .createAlias("permission", "permission")
                .createAlias("client", "client")
                .add(Restrictions.eq("permission.folder", folder))
                .add(Restrictions.eq("client.remotePartner", remotePartner))
                .add(Restrictions.eq("client.email", userId))
                .uniqueResult();
    }

    public List<RemoteShareModel> getByFolder(Folder folder) throws DAOException {
        return currentSession().createCriteria(RemoteShareModel.class)
                .createAlias("permission", "permission")
                .add(Restrictions.eq("permission.folder", folder))
                .list();
    }
}

package org.jbei.ice.storage.hibernate.dao;

import org.hibernate.HibernateException;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.storage.DAOException;
import org.jbei.ice.storage.hibernate.HibernateRepository;
import org.jbei.ice.storage.model.*;

import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Root;
import java.util.List;

/**
 * @author Hector Plahar
 */
public class RemoteShareModelDAO extends HibernateRepository<RemoteShareModel> {

    @Override
    public RemoteShareModel get(long id) {
        return super.get(RemoteShareModel.class, id);
    }

    public RemoteShareModel get(String userId, RemotePartner remotePartner, Folder folder) {
        try {
            CriteriaQuery<RemoteShareModel> query = getBuilder().createQuery(RemoteShareModel.class);
            Root<RemoteShareModel> from = query.from(RemoteShareModel.class);
            Join<RemoteShareModel, Permission> permission = from.join("permission");
            Join<RemoteShareModel, RemoteClientModel> client = from.join("client");
            query.where(
                    getBuilder().equal(permission.get("folder"), folder),
                    getBuilder().equal(client.get("remotePartner"), remotePartner),
                    getBuilder().equal(client.get("email"), userId)
            );
            return currentSession().createQuery(query).uniqueResult();
        } catch (HibernateException he) {
            Logger.error(he);
            throw new DAOException(he);
        }
    }

    public List<RemoteShareModel> getByFolder(Folder folder) {
        try {
            CriteriaQuery<RemoteShareModel> query = getBuilder().createQuery(RemoteShareModel.class);
            Root<RemoteShareModel> from = query.from(RemoteShareModel.class);
            Join<RemoteShareModel, Permission> permission = from.join("permission");
            query.where(getBuilder().equal(permission.get("folder"), folder));
            return currentSession().createQuery(query).list();
        } catch (HibernateException he) {
            Logger.error(he);
            throw new DAOException(he);
        }
    }
}

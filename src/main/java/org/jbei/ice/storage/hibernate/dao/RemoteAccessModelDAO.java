package org.jbei.ice.storage.hibernate.dao;

import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Root;
import org.jbei.ice.logging.Logger;
import org.jbei.ice.storage.DAOException;
import org.jbei.ice.storage.hibernate.HibernateRepository;
import org.jbei.ice.storage.model.AccountModel;
import org.jbei.ice.storage.model.Folder;
import org.jbei.ice.storage.model.Permission;
import org.jbei.ice.storage.model.RemoteAccessModel;

/**
 * @author Hector Plahar
 */
public class RemoteAccessModelDAO extends HibernateRepository<RemoteAccessModel> {

    @Override
    public RemoteAccessModel get(long id) {
        return super.get(RemoteAccessModel.class, id);
    }

    public RemoteAccessModel getByFolder(AccountModel account, Folder folder) {
        try {
            CriteriaQuery<RemoteAccessModel> query = getBuilder().createQuery(RemoteAccessModel.class);
            Root<RemoteAccessModel> from = query.from(RemoteAccessModel.class);
            Join<RemoteAccessModel, Permission> permission = from.join("permission");
            query.where(
                    getBuilder().equal(permission.get("folder"), folder),
                    getBuilder().equal(permission.get("account"), account)
            );
            return currentSession().createQuery(query).uniqueResult();
        } catch (Exception he) {
            Logger.error(he);
            throw new DAOException(he);
        }
    }
}

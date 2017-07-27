package org.jbei.ice.storage.hibernate.dao;

import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.storage.DAOException;
import org.jbei.ice.storage.hibernate.HibernateRepository;
import org.jbei.ice.storage.model.Account;
import org.jbei.ice.storage.model.Folder;
import org.jbei.ice.storage.model.Permission;
import org.jbei.ice.storage.model.RemoteAccessModel;

import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Root;
import java.util.List;

/**
 * @author Hector Plahar
 */
public class RemoteAccessModelDAO extends HibernateRepository<RemoteAccessModel> {

    @Override
    public RemoteAccessModel get(long id) {
        return super.get(RemoteAccessModel.class, id);
    }

    public RemoteAccessModel getByFolder(Account account, Folder folder) {
        try {
            CriteriaQuery<RemoteAccessModel> query = getBuilder().createQuery(RemoteAccessModel.class);
            Root<RemoteAccessModel> from = query.from(RemoteAccessModel.class);
            Join<RemoteAccessModel, Permission> permission = from.join("permission");
            query.where(
                    getBuilder().equal(permission.get("folder"), folder),
                    getBuilder().equal(permission.get("account"), account)
            );
            List<RemoteAccessModel> result = currentSession().createQuery(query).list();
            if (result.size() > 1) {
                Logger.warn("Found " + result.size() + " access models for folder " + folder.getId());
                return result.get(0);
            }
            return null;
        } catch (Exception he) {
            Logger.error(he);
            throw new DAOException(he);
        }
    }
}

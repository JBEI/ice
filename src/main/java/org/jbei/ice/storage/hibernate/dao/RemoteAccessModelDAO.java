package org.jbei.ice.storage.hibernate.dao;

import org.hibernate.criterion.Restrictions;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.storage.hibernate.HibernateRepository;
import org.jbei.ice.storage.model.Account;
import org.jbei.ice.storage.model.Folder;
import org.jbei.ice.storage.model.RemoteAccessModel;

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
        List list = currentSession().createCriteria(RemoteAccessModel.class)
                .createAlias("permission", "permission")
                .add(Restrictions.eq("permission.folder", folder))
                .add(Restrictions.eq("permission.account", account))
                .list();
        if (!list.isEmpty()) {
            if (list.size() > 1)
                Logger.warn("Found " + list.size() + " access models for folder " + folder.getId());
            return (RemoteAccessModel) list.get(0);
        }
        return null;
    }
}

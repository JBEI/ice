package org.jbei.ice.lib.permissions;

import org.jbei.ice.lib.account.model.Account;
import org.jbei.ice.lib.dao.DAOException;
import org.jbei.ice.lib.entry.model.Entry;
import org.jbei.ice.lib.permissions.model.ReadUser;
import org.jbei.ice.lib.permissions.model.WriteUser;
import org.jbei.ice.server.dao.hibernate.HibernateRepository;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

/**
 * @author Hector Plahar
 */
public class PermissionDAO extends HibernateRepository {

    /**
     * Check if the given {@link Account} has read permission to the given {@link Entry}.
     *
     * @param entry Entry to query on.
     * @return True if given Account has read permission to the given Entry.
     */
    public boolean isReadUserAccount(Account account, Entry entry) throws DAOException {
        Session session = newSession();
        Criteria criteria = session.createCriteria(ReadUser.class)
                                   .add(Restrictions.eq("account", account))
                                   .add(Restrictions.eq("entry", entry));

        Object result = criteria.uniqueResult();
        return result != null;
    }

    public boolean isWriteUserAccount(Account account, Entry entry) throws DAOException {
        Session session = newSession();
        Criteria criteria = session.createCriteria(WriteUser.class)
                                   .add(Restrictions.eq("account", account))
                                   .add(Restrictions.eq("entry", entry));

        Object result = criteria.uniqueResult();
        return result != null;
    }
}

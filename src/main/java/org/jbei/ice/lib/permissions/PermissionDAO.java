package org.jbei.ice.lib.permissions;

import org.jbei.ice.lib.account.model.Account;
import org.jbei.ice.lib.dao.DAOException;
import org.jbei.ice.lib.entry.model.Entry;
import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.lib.permissions.model.ReadUser;
import org.jbei.ice.lib.permissions.model.WriteUser;
import org.jbei.ice.server.dao.hibernate.HibernateRepository;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Example;

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
        ReadUser readUser = new ReadUser(entry, account);

        try {
            return session.createCriteria(ReadUser.class)
                          .add(Example.create(readUser))
                          .list()
                          .size() >= 1;
        } catch (HibernateException he) {
            Logger.error(he);
            throw new DAOException(he);
        }
    }

    public boolean isWriteUserAccount(Account account, Entry entry) throws DAOException {
        Session session = newSession();
        WriteUser writeUser = new WriteUser(entry, account);

        try {
            return session.createCriteria(WriteUser.class)
                          .add(Example.create(writeUser))
                          .list()
                          .size() >= 1;
        } catch (HibernateException he) {
            Logger.error(he);
            throw new DAOException(he);
        }
    }
}

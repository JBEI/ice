package org.jbei.ice.lib.permissions;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jbei.ice.lib.account.model.Account;
import org.jbei.ice.lib.dao.DAOException;
import org.jbei.ice.lib.dao.hibernate.HibernateRepository;
import org.jbei.ice.lib.entry.model.Entry;
import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.lib.permissions.model.ReadUser;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

/**
 * @author Hector Plahar
 */
public class ReadUserDAO extends HibernateRepository<ReadUser> {

    /**
     * Set read permissions for specified user {@link org.jbei.ice.lib.account.model.Account}s to the given {@link
     * org.jbei.ice.lib.entry.model.Entry}.
     * <p/>
     * This method creates new {@link org.jbei.ice.lib.permissions.model.ReadUser} objects using the given {@link
     * org.jbei.ice.lib.account.model.Account}s.
     *
     * @param entry    Entry to give read permission to.
     * @param accounts Accounts to give read permission to.
     * @throws org.jbei.ice.lib.dao.DAOException
     *
     */
    public void setReadUser(Entry entry, Set<Account> accounts) throws DAOException {
        String queryString = "delete ReadUser readUser where readUser.entry = :entry";
        Session session = newSession();

        try {
            session.getTransaction().begin();
            Query query = session.createQuery(queryString);
            query.setEntity("entry", entry);
            query.executeUpdate();
            session.getTransaction().commit();
            for (Account account : accounts) {
                ReadUser readUser = new ReadUser(entry, account);
                super.saveOrUpdate(readUser);
            }
        } catch (HibernateException e) {
            session.getTransaction().rollback();
            String msg = "Could not set Read User to " + entry.getRecordId();
            throw new DAOException(msg, e);
        } finally {
            closeSession(session);
        }
    }

    public void removeReadUser(Entry entry, Account account) throws DAOException {
        String queryString = "delete ReadUser readUser where readUser.entry = :entry and readUser.account = :account";
        Session session = newSession();

        try {
            session.getTransaction().begin();
            Query query = session.createQuery(queryString);
            query.setEntity("entry", entry);
            query.setEntity("account", account);
            query.executeUpdate();
            session.getTransaction().commit();
        } catch (HibernateException e) {
            session.getTransaction().rollback();
            String msg = "Could not remove read user \"" + account.getEmail() + "\" for entry \""
                    + entry.getId() + "\"";
            throw new DAOException(msg, e);
        } finally {
            closeSession(session);
        }
    }

    /**
     * Retrieve {@link Account}s with read permissions set for the specified {@link Entry}.
     *
     * @param entry Entry to get ReadUsers about.
     * @return Set of Accounts with read permission for the given Entry.
     * @throws DAOException
     */
    @SuppressWarnings("unchecked")
    public Set<Account> getReadUsers(Entry entry) throws DAOException {
        Session session = newSession();

        try {
            Criteria criteria = session.createCriteria(ReadUser.class)
                                       .add(Restrictions.eq("entry", entry))
                                       .setProjection(Projections.property("account"));
            List list = criteria.list();
            return new HashSet<Account>(list);

        } catch (HibernateException he) {
            Logger.error(he);
            throw new DAOException(he);
        }
    }
}

package org.jbei.ice.lib.permissions;

import org.jbei.ice.lib.account.model.Account;
import org.jbei.ice.lib.dao.DAOException;
import org.jbei.ice.lib.dao.hibernate.HibernateRepository;
import org.jbei.ice.lib.entry.model.Entry;
import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.lib.permissions.model.ReadUser;
import org.jbei.ice.lib.permissions.model.WriteUser;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Example;
import org.hibernate.criterion.Projections;

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
        Example example = Example.create(new ReadUser(entry, account));
        return createCriteriaQuery(ReadUser.class, example);
    }

    public boolean isWriteUserAccount(Account account, Entry entry) throws DAOException {
        Example example = Example.create(new WriteUser(entry, account));
        return createCriteriaQuery(WriteUser.class, example);
    }

    protected boolean createCriteriaQuery(Class<?> c, Example example) throws DAOException {
        Session session = newSession();

        try {
            Criteria criteria = session.createCriteria(c)
                                       .add(example)
                                       .setProjection(Projections.rowCount());
            Integer integer = (Integer) criteria.uniqueResult();
            return integer == 1;
        } catch (HibernateException he) {
            Logger.error(he);
            throw new DAOException(he);
        }
    }
}

package org.jbei.ice.lib.permissions;

import java.util.ArrayList;

import org.jbei.ice.lib.account.model.Account;
import org.jbei.ice.lib.dao.DAOException;
import org.jbei.ice.lib.dao.hibernate.HibernateRepository;
import org.jbei.ice.lib.entry.model.Entry;
import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.lib.permissions.model.Permission;
import org.jbei.ice.lib.permissions.model.ReadGroup;
import org.jbei.ice.lib.permissions.model.ReadUser;
import org.jbei.ice.lib.permissions.model.WriteGroup;
import org.jbei.ice.lib.permissions.model.WriteUser;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Example;
import org.hibernate.criterion.Projections;

/**
 * @author Hector Plahar
 */
public class PermissionDAO extends HibernateRepository<Permission> {

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

    public void upgradePermissions() throws DAOException {
        Session session = newSession();
        session.getTransaction().begin();

        try {

            Logger.info("Upgrading permissions....please wait");
            // convert read group
            Query query = session.createQuery("from " + ReadGroup.class.getName());
            ArrayList<ReadGroup> results = new ArrayList<ReadGroup>(query.list());
            for (ReadGroup readGroup : results) {
                Permission permission = new Permission();
                permission.setGroup(readGroup.getGroup());
                permission.setCanRead(true);
                permission.setEntry(readGroup.getEntry());
                session.save(permission);
                session.delete(readGroup);
            }

            // convert read user
            query = session.createQuery("from " + ReadUser.class.getName());
            ArrayList<ReadUser> readUsers = new ArrayList<ReadUser>(query.list());
            for (ReadUser readUser : readUsers) {
                Permission permission = new Permission();
                permission.setAccount(readUser.getAccount());
                permission.setCanRead(true);
                permission.setEntry(readUser.getEntry());
                session.save(permission);
                session.delete(readUser);
            }

            // convert write group
            query = session.createQuery("from " + WriteGroup.class.getName());
            ArrayList<WriteGroup> writeGroups = new ArrayList<WriteGroup>(query.list());
            for (WriteGroup writeGroup : writeGroups) {
                Permission permission = new Permission();
                permission.setGroup(writeGroup.getGroup());
                permission.setCanWrite(true);
                permission.setEntry(writeGroup.getEntry());
                session.save(permission);
                session.delete(writeGroup);
            }

            // convert write user
            query = session.createQuery("from " + WriteUser.class.getName());
            ArrayList<WriteUser> writeUsers = new ArrayList<WriteUser>(query.list());
            for (WriteUser writeUser : writeUsers) {
                Permission permission = new Permission();
                permission.setAccount(writeUser.getAccount());
                permission.setCanWrite(true);
                permission.setEntry(writeUser.getEntry());
                session.save(permission);
                session.delete(writeUser);
            }
            Logger.info("Permissions upgrade complete");
            session.getTransaction().commit();
        } catch (HibernateException he) {
            session.getTransaction().rollback();
            throw new DAOException(he);
        } finally {
            closeSession(session);
        }
    }
}

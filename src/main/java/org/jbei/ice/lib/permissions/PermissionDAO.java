package org.jbei.ice.lib.permissions;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jbei.ice.lib.account.model.Account;
import org.jbei.ice.lib.dao.DAOException;
import org.jbei.ice.lib.dao.hibernate.HibernateRepository;
import org.jbei.ice.lib.entry.model.Entry;
import org.jbei.ice.lib.group.Group;
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
import org.hibernate.criterion.Restrictions;

/**
 * @author Hector Plahar
 */
class PermissionDAO extends HibernateRepository<Permission> {

    public boolean hasAccountPermission(Entry entry, Account account, boolean canWrite, boolean canRead)
            throws DAOException {
        Permission permission = new Permission();
        permission.setAccount(account);
        permission.setEntry(entry);
        permission.setCanRead(canRead);
        permission.setCanWrite(canWrite);
        Example example = Example.create(permission);
        return createCriteriaQuery(example);
    }

    public boolean hasGroupPermission(Entry entry, Group group, boolean canWrite, boolean canRead)
            throws DAOException {
        Permission permission = new Permission();
        permission.setEntry(entry);
        permission.setGroup(group);
        permission.setCanWrite(canWrite);
        permission.setCanRead(canRead);
        Example example = Example.create(permission);
        return createCriteriaQuery(example);

    }

    public void addGroupPermission(Entry entry, Set<Group> groups, boolean canWrite, boolean canRead)
            throws DAOException {
        Session session = newSession();
        try {
            session.getTransaction().begin();
            for (Group group : groups) {
                Permission permission = new Permission();
                permission.setEntry(entry);
                permission.setGroup(group);
                permission.setCanRead(canRead);
                permission.setCanWrite(canWrite);
                session.save(permission);
            }
            session.getTransaction().commit();
        } catch (HibernateException e) {
            session.getTransaction().rollback();
            throw new DAOException("dbSave failed!", e);
        } catch (Exception e1) {
            session.getTransaction().rollback();
            Logger.error(e1);
            throw new DAOException("Unknown database exception ", e1);
        } finally {
            closeSession(session);
        }
    }

    public void removeGroupPermission(Entry entry, Set<Group> groups, boolean canWrite, boolean canRead)
            throws DAOException {
        Session session = newSession();
        try {
            session.getTransaction().begin();
            for (Group group : groups) {
                Permission permission =
                        (Permission) session.createCriteria(Permission.class)
                                            .add(Restrictions.eq("can_write", Boolean.valueOf(canWrite)))
                                            .add(Restrictions.eq("can_read", Boolean.valueOf(canRead)))
                                            .add(Restrictions.eq("group_id", group))
                                            .add(Restrictions.eq("entry_id", entry)).uniqueResult();
                session.delete(permission);
            }
            session.getTransaction().commit();
        } catch (HibernateException he) {
            Logger.error(he);
            session.getTransaction().rollback();
        } finally {
            closeSession(session);
        }
    }

    public void addAccountPermission(Entry entry, Set<Account> accounts, boolean canWrite, boolean canRead)
            throws DAOException {
        Session session = newSession();
        try {
            session.getTransaction().begin();
            for (Account account : accounts) {
                Permission permission = new Permission();
                permission.setEntry(entry);
                permission.setAccount(account);
                permission.setCanRead(canRead);
                permission.setCanWrite(canWrite);
                session.save(permission);
            }
            session.getTransaction().commit();
        } catch (HibernateException e) {
            session.getTransaction().rollback();
            throw new DAOException("dbSave failed!", e);
        } catch (Exception e1) {
            session.getTransaction().rollback();
            Logger.error(e1);
            throw new DAOException("Unknown database exception ", e1);
        } finally {
            closeSession(session);
        }
    }

    public void removeAccountPermission(Entry entry, Set<Account> accounts, boolean canWrite, boolean canRead)
            throws DAOException {
        Session session = newSession();
        try {
            session.getTransaction().begin();
            for (Account account : accounts) {
                Permission permission =
                        (Permission) session.createCriteria(Permission.class)
                                            .add(Restrictions.eq("can_write", Boolean.valueOf(canWrite)))
                                            .add(Restrictions.eq("can_read", Boolean.valueOf(canRead)))
                                            .add(Restrictions.eq("account_id", account))
                                            .add(Restrictions.eq("entry_id", entry)).uniqueResult();
                session.delete(permission);
            }
            session.getTransaction().commit();
        } catch (HibernateException he) {
            Logger.error(he);
            session.getTransaction().rollback();
        } finally {
            closeSession(session);
        }
    }

    protected boolean createCriteriaQuery(Example example) throws DAOException {
        Session session = newSession();

        try {
            Criteria criteria = session.createCriteria(Permission.class)
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

    public Set<Account> retrieveAccountPermissions(boolean canWrite, boolean canRead) throws DAOException {
        Session session = newSession();
        try {
            List list = session.createCriteria(Permission.class)
                               .add(Restrictions.eq("can_write", Boolean.valueOf(canWrite)))
                               .add(Restrictions.eq("can_read", Boolean.valueOf(canRead)))
                               .setProjection(Projections.property("account_id"))
                               .list();

            return new HashSet<Account>(list);
        } catch (HibernateException he) {
            Logger.error(he);
            throw new DAOException(he);
        } finally {
            closeSession(session);
        }
    }

    public Set<Group> retrieveGroupPermissions(boolean canWrite, boolean canRead) throws DAOException {
        Session session = newSession();
        try {
            List list = session.createCriteria(Permission.class)
                               .add(Restrictions.eq("can_write", Boolean.valueOf(canWrite)))
                               .add(Restrictions.eq("can_read", Boolean.valueOf(canRead)))
                               .setProjection(Projections.property("account_id"))
                               .list();
            return new HashSet<Group>(list);
        } catch (HibernateException he) {
            Logger.error(he);
            throw new DAOException(he);
        } finally {
            closeSession(session);
        }
    }
}

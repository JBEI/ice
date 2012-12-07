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
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

/**
 * @author Hector Plahar
 */
class PermissionDAO extends HibernateRepository<Permission> {

    public boolean hasAccountPermission(Entry entry, Account account, boolean canWrite, boolean canRead)
            throws DAOException {
        Session session = currentSession();

        try {
            Criteria criteria = session.createCriteria(Permission.class)
                                       .add(Restrictions.eq("canWrite", Boolean.valueOf(canWrite)))
                                       .add(Restrictions.eq("canRead", Boolean.valueOf(canRead)))
                                       .add(Restrictions.eq("account", account))
                                       .add(Restrictions.isNull("folder"))
                                       .add(Restrictions.isNull("group"))
                                       .add(Restrictions.eq("entry", entry))
                                       .setProjection(Projections.rowCount());

            Number integer = (Number) criteria.uniqueResult();
            return integer.intValue() == 1;
        } catch (HibernateException he) {
            Logger.error(he);
            throw new DAOException(he);
        } finally {
            closeSession();
        }
    }

    public boolean hasGroupPermission(Entry entry, Set<Group> groups, boolean canWrite, boolean canRead)
            throws DAOException {

        Session session = currentSession();

        try {
            Criteria criteria = session.createCriteria(Permission.class)
                                       .add(Restrictions.eq("canWrite", Boolean.valueOf(canWrite)))
                                       .add(Restrictions.eq("canRead", Boolean.valueOf(canRead)))
                                       .add(Restrictions.in("group", groups))
                                       .add(Restrictions.isNull("folder"))
                                       .add(Restrictions.isNull("account"))
                                       .add(Restrictions.eq("entry", entry))
                                       .setProjection(Projections.rowCount());

            Number integer = (Number) criteria.uniqueResult();
            return integer.intValue() == 1;
        } catch (HibernateException he) {
            Logger.error(he);
            throw new DAOException(he);
        } catch (Exception e) {
            Logger.error(e);
            throw new DAOException(e);
        } finally {
            closeSession();
        }
    }

    public void addGroupPermission(Entry entry, Set<Group> groups, boolean canWrite, boolean canRead)
            throws DAOException {
        Session session = currentSession();
        try {
            for (Group group : groups) {
                Permission permission = new Permission();
                permission.setEntry(entry);
                permission.setGroup(group);
                permission.setCanRead(canRead);
                permission.setCanWrite(canWrite);
                session.save(permission);
            }
        } catch (HibernateException e) {
            throw new DAOException("dbSave failed!", e);
        } catch (Exception e1) {
            Logger.error(e1);
            throw new DAOException("Unknown database exception ", e1);
        } finally {
            closeSession();
        }
    }

    public void removeGroupPermission(Entry entry, Set<Group> groups, boolean canWrite, boolean canRead)
            throws DAOException {
        Session session = currentSession();
        try {
            for (Group group : groups) {
                Permission permission =
                        (Permission) session.createCriteria(Permission.class)
                                            .add(Restrictions.eq("canWrite", Boolean.valueOf(canWrite)))
                                            .add(Restrictions.eq("canRead", Boolean.valueOf(canRead)))
                                            .add(Restrictions.eq("group", group))
                                            .add(Restrictions.eq("entry", entry)).uniqueResult();
                session.delete(permission);
            }
        } catch (HibernateException he) {
            Logger.error(he);
        } finally {
            closeSession();
        }
    }

    public void addAccountPermission(Entry entry, Set<Account> accounts, boolean canWrite, boolean canRead)
            throws DAOException {
        Session session = currentSession();
        try {
            for (Account account : accounts) {
                Permission permission = new Permission();
                permission.setEntry(entry);
                permission.setAccount(account);
                permission.setCanRead(canRead);
                permission.setCanWrite(canWrite);
                session.save(permission);
            }
        } catch (HibernateException e) {
            throw new DAOException("dbSave failed!", e);
        } catch (Exception e1) {
            Logger.error(e1);
            throw new DAOException("Unknown database exception ", e1);
        } finally {
            closeSession();
        }
    }

    public void removeAccountPermission(Entry entry, Set<Account> accounts, boolean canWrite, boolean canRead)
            throws DAOException {
        Session session = currentSession();
        try {
            for (Account account : accounts) {
                Permission permission =
                        (Permission) session.createCriteria(Permission.class)
                                            .add(Restrictions.eq("canWrite", Boolean.valueOf(canWrite)))
                                            .add(Restrictions.eq("canRead", Boolean.valueOf(canRead)))
                                            .add(Restrictions.eq("account", account))
                                            .add(Restrictions.eq("entry", entry)).uniqueResult();
                session.delete(permission);
            }
        } catch (HibernateException he) {
            Logger.error(he);
        } finally {
            closeSession();
        }
    }

//    protected boolean createCriteriaQuery(Example example) throws DAOException {
//        Session session = currentSession();
//
//        try {
//            Criteria criteria = session.createCriteria(Permission.class)
//                                       .add(example)
//                                       .setProjection(Projections.rowCount());
//            Number integer = (Number) criteria.uniqueResult();
//            return integer.intValue() == 1;
//        } catch (HibernateException he) {
//            Logger.error(he);
//            throw new DAOException(he);
//        }
//    }

    public void upgradePermissions() throws DAOException {
        Session session = currentSession();
        try {
            Logger.info("Upgrading permissions....please wait");
            // convert read group
            Query query = session.createQuery("from " + ReadGroup.class.getName());
            ArrayList<ReadGroup> results = new ArrayList<ReadGroup>(query.list());
            for (ReadGroup readGroup : results) {
                Permission permission = new Permission();
                permission.setGroup(readGroup.getGroup());
                permission.setCanRead(true);
                Entry entry = readGroup.getEntry();
                permission.setEntry(entry);
                entry.getPermissions().add(permission);
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
                Entry entry = readUser.getEntry();
                permission.setEntry(entry);
                entry.getPermissions().add(permission);
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
                Entry entry = writeGroup.getEntry();
                permission.setEntry(entry);
                entry.getPermissions().add(permission);
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
                Entry entry = writeUser.getEntry();
                permission.setEntry(entry);
                entry.getPermissions().add(permission);
                session.save(permission);
                session.delete(writeUser);
            }
            Logger.info("Permissions upgrade complete");
        } catch (HibernateException he) {
            throw new DAOException(he);
        }
    }

    public Set<Account> retrieveAccountPermissions(Entry entry, boolean canWrite, boolean canRead) throws DAOException {
        Session session = currentSession();
        try {
            List list = session.createCriteria(Permission.class)
                               .add(Restrictions.eq("canWrite", Boolean.valueOf(canWrite)))
                               .add(Restrictions.eq("canRead", Boolean.valueOf(canRead)))
                               .add(Restrictions.eq("entry", entry))
                               .setProjection(Projections.property("account"))
                               .add(Restrictions.isNotNull("account"))
                               .list();

            return new HashSet<Account>(list);
        } catch (HibernateException he) {
            Logger.error(he);
            throw new DAOException(he);
        } finally {
            closeSession();
        }
    }

    public Set<Group> retrieveGroupPermissions(Entry entry, boolean canWrite, boolean canRead) throws DAOException {
        Session session = currentSession();
        try {
            List list = session.createCriteria(Permission.class)
                               .add(Restrictions.eq("entry", entry))
                               .add(Restrictions.eq("canWrite", Boolean.valueOf(canWrite)))
                               .add(Restrictions.eq("canRead", Boolean.valueOf(canRead)))
                               .setProjection(Projections.property("group"))
                               .add(Restrictions.isNotNull("group"))
                               .list();
            return new HashSet<Group>(list);
        } catch (HibernateException he) {
            Logger.error(he);
            throw new DAOException(he);
        } finally {
            closeSession();
        }
    }
}

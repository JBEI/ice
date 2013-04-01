package org.jbei.ice.lib.permissions;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.jbei.ice.lib.account.model.Account;
import org.jbei.ice.lib.dao.DAOException;
import org.jbei.ice.lib.dao.hibernate.HibernateRepository;
import org.jbei.ice.lib.entry.model.Entry;
import org.jbei.ice.lib.folder.Folder;
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
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

/**
 * @author Hector Plahar
 */
class PermissionDAO extends HibernateRepository<Permission> {

    public boolean hasPermission(Entry entry, Folder folder, Account account, Group group, boolean canRead,
            boolean canWrite) throws DAOException {
        try {
            Session session = currentSession();
            Criteria criteria = session.createCriteria(Permission.class)
                                       .add(Restrictions.eq("canWrite", Boolean.valueOf(canWrite)))
                                       .add(Restrictions.eq("canRead", Boolean.valueOf(canRead)));

            if (group == null)
                criteria.add(Restrictions.isNull("group"));
            else
                criteria.add(Restrictions.eq("group", group));

            if (folder == null)
                criteria.add(Restrictions.isNull("folder"));
            else
                criteria.add(Restrictions.eq("folder", folder));

            if (account == null)
                criteria.add(Restrictions.isNull("account"));
            else
                criteria.add(Restrictions.eq("account", account));

            if (entry == null)
                criteria.add(Restrictions.isNull("entry"));
            else
                criteria.add(Restrictions.eq("entry", entry));

            criteria.setProjection(Projections.rowCount());
            Number integer = (Number) criteria.uniqueResult();
            return integer.intValue() == 1;
        } catch (Exception e) {
            Logger.error(e);
            throw new DAOException(e);
        }
    }

    // to avoid ambiguous call name clashes when collections are null
    public boolean hasPermissionMulti(Entry entry, Set<Folder> folders, Account account, Set<Group> groups,
            boolean canRead, boolean canWrite) throws DAOException {
        try {
            Session session = currentSession();
            Criteria criteria = session.createCriteria(Permission.class)
                                       .add(Restrictions.eq("canWrite", Boolean.valueOf(canWrite)))
                                       .add(Restrictions.eq("canRead", Boolean.valueOf(canRead)));

            if (groups == null || groups.isEmpty())
                criteria.add(Restrictions.isNull("group"));
            else
                criteria.add(Restrictions.in("group", groups));

            if (folders == null || folders.isEmpty())
                criteria.add(Restrictions.isNull("folder"));
            else
                criteria.add(Restrictions.in("folder", folders));

            if (account == null)
                criteria.add(Restrictions.isNull("account"));
            else
                criteria.add(Restrictions.eq("account", account));

            if (entry == null)
                criteria.add(Restrictions.isNull("entry"));
            else
                criteria.add(Restrictions.eq("entry", entry));

            criteria.setProjection(Projections.rowCount());
            Number integer = (Number) criteria.uniqueResult();
            return integer.intValue() == 1;
        } catch (Exception e) {
            Logger.error(e);
            throw new DAOException(e);
        }
    }

    public Permission retrievePermission(Entry entry, Folder folder, Account account, Group group, boolean canRead,
            boolean canWrite) throws DAOException {
        try {
            Session session = currentSession();
            Criteria criteria = session.createCriteria(Permission.class)
                                       .add(Restrictions.eq("canWrite", Boolean.valueOf(canWrite)))
                                       .add(Restrictions.eq("canRead", Boolean.valueOf(canRead)));

            if (group == null)
                criteria.add(Restrictions.isNull("group"));
            else
                criteria.add(Restrictions.eq("group", group));

            if (folder == null)
                criteria.add(Restrictions.isNull("folder"));
            else
                criteria.add(Restrictions.eq("folder", folder));

            if (account == null)
                criteria.add(Restrictions.isNull("account"));
            else
                criteria.add(Restrictions.eq("account", account));

            if (entry == null)
                criteria.add(Restrictions.isNull("entry"));
            else
                criteria.add(Restrictions.eq("entry", entry));

            return (Permission) criteria.uniqueResult();
        } catch (HibernateException e) {
            Logger.error(e);
            throw new DAOException(e);
        }
    }

    public void removePermission(Entry entry, Folder folder, Account account, Group group, boolean canRead,
            boolean canWrite) throws DAOException {
        Permission permission = retrievePermission(entry, folder, account, group, canRead, canWrite);
        if (permission == null)
            return;

        try {
            delete(permission);
        } catch (HibernateException he) {
            Logger.error(he);
            throw new DAOException(he);
        }
    }

    @Deprecated
    // this will be removed in the next release
    public void upgradePermissions() throws DAOException {
        Session session = currentSession();
        try {
            // convert read group
            Query query = session.createQuery("from " + ReadGroup.class.getName());
            Iterator iterator = query.iterate();
            int i = 0;

            while (iterator.hasNext()) {
                ReadGroup readGroup = (ReadGroup) iterator.next();
                i += 1;
                Permission permission = new Permission();
                permission.setGroup(readGroup.getGroup());
                permission.setCanRead(true);
                Entry entry = readGroup.getEntry();
                permission.setEntry(entry);
                entry.getPermissions().add(permission);
                session.save(permission);
                session.delete(readGroup);
                if (i % 20 == 0) {
                    session.flush();
                    session.clear();
                }
            }

            // convert read user
            query = session.createQuery("from " + ReadUser.class.getName());
            i = 0;
            ArrayList<ReadUser> readUsers = new ArrayList<ReadUser>(query.list());
            Logger.info("Read User list: " + readUsers.size());
            for (ReadUser readUser : readUsers) {
                i += 1;
                Permission permission = new Permission();
                permission.setAccount(readUser.getAccount());
                permission.setCanRead(true);
                Entry entry = readUser.getEntry();
                permission.setEntry(entry);
                entry.getPermissions().add(permission);
                session.save(permission);
                session.delete(readUser);
                if (i % 20 == 0) {
                    session.flush();
                    session.clear();
                }
            }

            // convert write group
            query = session.createQuery("from " + WriteGroup.class.getName());
            ArrayList<WriteGroup> writeGroups = new ArrayList<WriteGroup>(query.list());
            Logger.info("Write Group list: " + writeGroups.size());
            for (WriteGroup writeGroup : writeGroups) {
                i += 1;
                Permission permission = new Permission();
                permission.setGroup(writeGroup.getGroup());
                permission.setCanWrite(true);
                Entry entry = writeGroup.getEntry();
                permission.setEntry(entry);
                entry.getPermissions().add(permission);
                session.save(permission);
                session.delete(writeGroup);
                if (i % 20 == 0) {
                    session.flush();
                    session.clear();
                }
            }

            // convert write user
            query = session.createQuery("from " + WriteUser.class.getName());
            ArrayList<WriteUser> writeUsers = new ArrayList<WriteUser>(query.list());
            Logger.info("Write User list: " + writeUsers.size());
            for (WriteUser writeUser : writeUsers) {
                i += 1;
                Permission permission = new Permission();
                permission.setAccount(writeUser.getAccount());
                permission.setCanWrite(true);
                Entry entry = writeUser.getEntry();
                permission.setEntry(entry);
                entry.getPermissions().add(permission);
                session.save(permission);
                session.delete(writeUser);
                if (i % 20 == 0) {
                    session.flush();
                    session.clear();
                }
            }
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
        }
    }

    public Set<Account> retrieveAccountPermissions(Folder folder, boolean canWrite, boolean canRead)
            throws DAOException {
        Session session = currentSession();
        try {
            List list = session.createCriteria(Permission.class)
                               .add(Restrictions.eq("canWrite", Boolean.valueOf(canWrite)))
                               .add(Restrictions.eq("canRead", Boolean.valueOf(canRead)))
                               .add(Restrictions.eq("folder", folder))
                               .setProjection(Projections.property("account"))
                               .add(Restrictions.isNotNull("account"))
                               .list();

            return new HashSet<Account>(list);
        } catch (HibernateException he) {
            Logger.error(he);
            throw new DAOException(he);
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
        }
    }

    public Set<Group> retrieveGroupPermissions(Folder folder, boolean canWrite, boolean canRead) throws DAOException {
        Session session = currentSession();
        try {
            List list = session.createCriteria(Permission.class)
                               .add(Restrictions.eq("folder", folder))
                               .add(Restrictions.eq("canWrite", Boolean.valueOf(canWrite)))
                               .add(Restrictions.eq("canRead", Boolean.valueOf(canRead)))
                               .setProjection(Projections.property("group"))
                               .add(Restrictions.isNotNull("group"))
                               .list();
            return new HashSet<Group>(list);
        } catch (HibernateException he) {
            Logger.error(he);
            throw new DAOException(he);
        }
    }

    public int clearPermissions(Entry entry) throws DAOException {
        Session session = currentSession();
        Query query = session.createQuery("delete " + Permission.class.getName() + " where entry = :entry");
        query.setParameter("entry", entry);
        try {
            return query.executeUpdate();
        } catch (HibernateException he) {
            Logger.error(he);
            throw new DAOException(he);
        }
    }

    public int clearPermissions(Folder folder) throws DAOException {
        Session session = currentSession();
        Query query = session.createQuery("delete " + Permission.class.getName() + " where folder = :folder");
        query.setParameter("folder", folder);
        try {
            return query.executeUpdate();
        } catch (HibernateException he) {
            Logger.error(he);
            throw new DAOException(he);
        }
    }

    public int clearPermissions(Group group) throws DAOException {
        Session session = currentSession();
        Query query = session.createQuery("delete " + Permission.class.getName() + " where group = :group");
        query.setParameter("group", group);
        try {
            return query.executeUpdate();
        } catch (HibernateException he) {
            Logger.error(he);
            throw new DAOException(he);
        }
    }

    public Set<Folder> retrieveFolderPermissions(Account account, Set<Group> accountGroups) throws DAOException {
        // can read or can write
        Criterion criterion = Restrictions.disjunction()
                                          .add(Restrictions.eq("canWrite", true))
                                          .add(Restrictions.eq("canRead", true));
        Session session = currentSession();
        try {
            List list = session.createCriteria(Permission.class)
                               .add(Restrictions.isNull("entry"))
                               .add(Restrictions.disjunction()
                                                .add(Restrictions.in("group", accountGroups))
                                                .add(Restrictions.eq("account", account)))
                               .add(criterion)
                               .setProjection(Projections.property("folder"))
                               .add(Restrictions.isNotNull("folder"))
                               .list();
            return new HashSet<Folder>(list);
        } catch (HibernateException he) {
            Logger.error(he);
            throw new DAOException(he);
        }
    }
}

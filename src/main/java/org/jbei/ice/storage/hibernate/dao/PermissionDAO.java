package org.jbei.ice.storage.hibernate.dao;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.*;
import org.hibernate.sql.JoinType;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.dto.entry.Visibility;
import org.jbei.ice.storage.DAOException;
import org.jbei.ice.storage.hibernate.HibernateRepository;
import org.jbei.ice.storage.model.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * {@link Permission} data accessor Object
 *
 * @author Hector Plahar
 */
@SuppressWarnings("unchecked")
public class PermissionDAO extends HibernateRepository<Permission> {

    public boolean hasPermission(Entry entry, Folder folder, BulkUpload upload, Account account, Group group,
                                 boolean canRead, boolean canWrite) {
        try {
            Session session = currentSession();
            Criteria criteria = session.createCriteria(Permission.class)
                    .add(Restrictions.eq("canWrite", canWrite))
                    .add(Restrictions.eq("canRead", canRead));

            if (group == null)
                criteria.add(Restrictions.isNull("group"));
            else
                criteria.add(Restrictions.eq("group", group));

            if (folder == null)
                criteria.add(Restrictions.isNull("folder"));
            else
                criteria.add(Restrictions.eq("folder", folder));

            if (upload == null)
                criteria.add(Restrictions.isNull("upload"));
            else
                criteria.add(Restrictions.eq("upload", upload));

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
            return integer.intValue() != 0;
        } catch (Exception e) {
            Logger.error(e);
            throw new DAOException(e);
        }
    }

    // to avoid ambiguous call name clashes when collections are null
    public boolean hasPermissionMulti(Entry entry, Set<Folder> folders, Account account, Set<Group> groups,
                                      boolean canRead, boolean canWrite) {
        try {
            Criteria criteria = currentSession().createCriteria(Permission.class)
                    .add(Restrictions.eq("canWrite", canWrite))
                    .add(Restrictions.eq("canRead", canRead));

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
            return integer.intValue() != 0;
        } catch (Exception e) {
            Logger.error(e);
            throw new DAOException(e);
        }
    }

    public Permission retrievePermission(Entry entry, Folder folder, BulkUpload upload, Account account, Group group,
                                         boolean canRead, boolean canWrite) {
        try {
            Criteria criteria = createPermissionCriteria(entry, folder, upload, account, group, canRead, canWrite);
            List list = criteria.list();
            if (list == null || list.isEmpty())
                return null;
            if (list.size() > 1)
                Logger.error("permission query did not return unique result. returning first result");

            return (Permission) list.get(0);
        } catch (HibernateException e) {
            Logger.error(e);
            throw new DAOException(e);
        }
    }

    protected Criteria createPermissionCriteria(Entry entry, Folder folder, BulkUpload upload, Account account,
                                                Group group, boolean canRead, boolean canWrite) {
        Session session = currentSession();
        Criteria criteria = session.createCriteria(Permission.class)
                .add(Restrictions.eq("canWrite", canWrite))
                .add(Restrictions.eq("canRead", canRead));

        if (group == null)
            criteria.add(Restrictions.isNull("group"));
        else
            criteria.add(Restrictions.eq("group", group));

        if (folder == null)
            criteria.add(Restrictions.isNull("folder"));
        else
            criteria.add(Restrictions.eq("folder", folder));

        if (upload == null)
            criteria.add(Restrictions.isNull("upload"));
        else
            criteria.add(Restrictions.eq("upload", upload));

        if (account == null)
            criteria.add(Restrictions.isNull("account"));
        else
            criteria.add(Restrictions.eq("account", account));

        if (entry == null)
            criteria.add(Restrictions.isNull("entry"));
        else
            criteria.add(Restrictions.eq("entry", entry));
        return criteria;
    }

    public void removePermission(Entry entry, Folder folder, BulkUpload upload, Account account, Group group,
                                 boolean canRead, boolean canWrite) {
        Criteria criteria = createPermissionCriteria(entry, folder, upload, account, group, canRead, canWrite);
        List list = criteria.list();
        if (list == null || list.isEmpty())
            return;

        for (Object object : list) {
            Permission permission = (Permission) object;
            try {
                delete(permission);
            } catch (HibernateException he) {
                Logger.error(he);
                throw new DAOException(he);
            }
        }
    }

    public Set<Permission> getEntryPermissions(Entry entry) {
        try {
            Criteria criteria = currentSession().createCriteria(Permission.class)
                    .add(Restrictions.eq("entry", entry));
            return new HashSet<>(criteria.list());
        } catch (HibernateException he) {
            Logger.error(he);
            throw new DAOException(he);
        }
    }

    public Set<Permission> getFolderPermissions(Folder folder) throws DAOException {
        try {
            Criteria criteria = currentSession().createCriteria(Permission.class)
                    .add(Restrictions.eq("folder", folder));
            return new HashSet<>(criteria.list());
        } catch (HibernateException he) {
            Logger.error(he);
            throw new DAOException(he);
        }
    }

    public Set<Account> retrieveAccountPermissions(Folder folder, boolean canWrite, boolean canRead) {
        Session session = currentSession();
        try {
            List list = session.createCriteria(Permission.class)
                    .add(Restrictions.eq("canWrite", canWrite))
                    .add(Restrictions.eq("canRead", canRead))
                    .add(Restrictions.eq("folder", folder))
                    .setProjection(Projections.property("account"))
                    .add(Restrictions.isNotNull("account"))
                    .list();

            return new HashSet<>(list);
        } catch (HibernateException he) {
            Logger.error(he);
            throw new DAOException(he);
        }
    }

    public boolean hasSetWriteFolderPermission(Folder folder, Account account) {
        List list = currentSession().createCriteria(Permission.class).add(Restrictions.eq("folder", folder))
                .add(Restrictions.eq("account", account))
                .add(Restrictions.eq("canWrite", true))
                .add(Restrictions.isNull("entry")).list();
        return list != null && !list.isEmpty();
    }

    public Set<Group> retrieveGroupPermissions(Folder folder, boolean canWrite, boolean canRead) throws DAOException {
        Session session = currentSession();
        try {
            List list = session.createCriteria(Permission.class)
                    .add(Restrictions.eq("folder", folder))
                    .add(Restrictions.eq("canWrite", canWrite))
                    .add(Restrictions.eq("canRead", canRead))
                    .setProjection(Projections.property("group"))
                    .add(Restrictions.isNotNull("group"))
                    .list();
            return new HashSet<>(list);
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
            Disjunction disjunction = Restrictions.disjunction();
            if (!accountGroups.isEmpty())
                disjunction.add(Restrictions.in("group", accountGroups));
            disjunction.add(Restrictions.eq("account", account));

            List list = session.createCriteria(Permission.class)
                    .add(Restrictions.isNull("entry"))
                    .add(disjunction)
                    .add(criterion)
                    .setProjection(Projections.property("folder"))
                    .add(Restrictions.isNotNull("folder"))
                    .list();
            return new HashSet<>(list);
        } catch (HibernateException he) {
            Logger.error(he);
            throw new DAOException(he);
        }
    }

    public Set<Folder> getFolders(Group group) {
        Criterion criterion = Restrictions.disjunction()
                .add(Restrictions.eq("canWrite", true))
                .add(Restrictions.eq("canRead", true));
        Session session = currentSession();
        try {
            List list = session.createCriteria(Permission.class)
                    .add(Restrictions.isNull("entry"))
                    .add(Restrictions.eq("group", group))
                    .add(criterion)
                    .add(Restrictions.isNotNull("folder"))
                    .setProjection(Projections.property("folder"))
                    .list();
            return new HashSet<>(list);
        } catch (HibernateException he) {
            Logger.error(he);
            throw new DAOException(he);
        }
    }

    /**
     * Filters the given list, removing those that the specified account does not have read privileges on
     *
     * @param account account to filter entries by
     * @param groups  groups that this account belongs to
     * @param entries list of entry ids to filter
     * @return filtered list such that specified account have read privileges on entries contained in it
     */
    public List<Long> getCanReadEntries(Account account, Set<Group> groups, List<Long> entries) {
        Criteria criteria = currentSession().createCriteria(Permission.class);
        Disjunction disjunction = Restrictions.disjunction();
        disjunction.add(Restrictions.eq("account", account));
        disjunction.add(Restrictions.eq("entry.ownerEmail", account.getEmail()));

        if (!groups.isEmpty()) {
            disjunction.add(Restrictions.in("group", groups));
        }

        criteria.createAlias("entry", "entry", JoinType.LEFT_OUTER_JOIN)
                .add(Restrictions.in("entry.id", entries))
                .add(Restrictions.eq("entry.visibility", Visibility.OK.getValue()));

        criteria.add(disjunction);

        return criteria.setProjection(
                Projections.distinct(Projections.property("entry.id")))
                .list();
    }

    /**
     * Determines if the specified account has write privileges on the entries passed on the parameter
     *
     * @param account user account
     * @param groups  groups that the account belongs to
     * @param entries list of entry Ids to check
     * @return true if the user has write privileges on <b>all</b> the entries specified in the parameter
     */
    public boolean canWrite(Account account, Set<Group> groups, List<Long> entries) {
        Criteria criteria = currentSession().createCriteria(Permission.class);
        criteria.add(Restrictions.in("entry.id", entries));
        criteria.add(Restrictions.or(Restrictions.eq("entry.ownerEmail", account.getEmail())));

        Disjunction disjunction = Restrictions.disjunction();

        if (!groups.isEmpty()) {
            disjunction.add(Restrictions.in("group", groups));
        }

        disjunction.add(Restrictions.eq("account", account));
        criteria.createAlias("entry", "entry", JoinType.LEFT_OUTER_JOIN);

        LogicalExpression logicalExpression = Restrictions.and(disjunction, Restrictions.eq("canWrite", true));

        criteria.add(logicalExpression);
        criteria.setProjection(Projections.countDistinct("entry.id"));
        Number number = (Number) criteria.uniqueResult();
        return number.intValue() == entries.size();
    }

    @Override
    public Permission get(long id) {
        return super.get(Permission.class, id);
    }
}

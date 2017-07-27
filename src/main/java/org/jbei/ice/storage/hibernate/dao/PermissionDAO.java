package org.jbei.ice.storage.hibernate.dao;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.dto.entry.Visibility;
import org.jbei.ice.storage.DAOException;
import org.jbei.ice.storage.DataModel;
import org.jbei.ice.storage.hibernate.HibernateRepository;
import org.jbei.ice.storage.model.*;

import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * {@link Permission} data accessor Object
 *
 * @author Hector Plahar
 */
public class PermissionDAO extends HibernateRepository<Permission> {

    private Predicate getPredicate(Root<Permission> root, String path, Object object) {
        if (object == null)
            return getBuilder().isNull(root.get(path));
        return getBuilder().equal(root.get(path), object);
    }

    private Predicate getPredicateForCollection(Root<Permission> root, String path, Collection<? extends DataModel> objects) {
        if (objects == null || objects.isEmpty())
            return getBuilder().isNull(root.get(path));
        return root.get(path).in(objects);
    }

    public boolean hasPermission(Entry entry, Folder folder, BulkUpload upload, Account account, Group group,
                                 boolean canRead, boolean canWrite) {
        try {
            CriteriaQuery<Long> query = getBuilder().createQuery(Long.class);
            Root<Permission> from = query.from(Permission.class);

            List<Predicate> predicates = new ArrayList<>();

            predicates.add(getBuilder().equal(from.get("canWrite"), canWrite));
            predicates.add(getBuilder().equal(from.get("canRead"), canRead));
            predicates.add(getPredicate(from, "group", group));
            predicates.add(getPredicate(from, "folder", folder));
            predicates.add(getPredicate(from, "upload", upload));
            predicates.add(getPredicate(from, "account", account));
            predicates.add(getPredicate(from, "entry", entry));

            query.select(getBuilder().countDistinct(from.get("id")));
            query.where(predicates.toArray(new Predicate[predicates.size()]));
            return currentSession().createQuery(query).setMaxResults(1).uniqueResult() > 0;
        } catch (Exception e) {
            Logger.error(e);
            throw new DAOException(e);
        }
    }

    // to avoid ambiguous call name clashes when collections are null
    public boolean hasPermissionMulti(Entry entry, Set<Folder> folders, Account account, List<Group> groups,
                                      boolean canRead, boolean canWrite) {
        try {
            CriteriaQuery<Long> query = getBuilder().createQuery(Long.class);
            Root<Permission> from = query.from(Permission.class);

            List<Predicate> predicates = new ArrayList<>();

            predicates.add(getBuilder().equal(from.get("canWrite"), canWrite));
            predicates.add(getBuilder().equal(from.get("canRead"), canRead));
            predicates.add(getPredicateForCollection(from, "group", groups));
            predicates.add(getPredicateForCollection(from, "folder", folders));
            predicates.add(getPredicate(from, "account", account));
            predicates.add(getPredicate(from, "entry", entry));

            query.select(getBuilder().countDistinct(from.get("id")));
            query.where(predicates.toArray(new Predicate[predicates.size()]));
            return currentSession().createQuery(query).setMaxResults(1).uniqueResult() > 0;
        } catch (Exception e) {
            Logger.error(e);
            throw new DAOException(e);
        }
    }

    public Permission retrievePermission(Entry entry, Folder folder, BulkUpload upload, Account account, Group group,
                                         boolean canRead, boolean canWrite) {
        try {
            Query<Permission> query = createPermissionQuery(entry, folder, upload, account, group, canRead, canWrite);
            List<Permission> result = query.list();
            if (result == null || result.isEmpty())
                return null;
            if (result.size() > 1)
                Logger.error("permission query did not return unique result. returning first result");

            return result.get(0);
        } catch (HibernateException e) {
            Logger.error(e);
            throw new DAOException(e);
        }
    }

    protected Query<Permission> createPermissionQuery(Entry entry, Folder folder, BulkUpload upload,
                                                      Account account, Group group, boolean canRead,
                                                      boolean canWrite) {
        CriteriaQuery<Permission> query = getBuilder().createQuery(Permission.class);
        Root<Permission> from = query.from(Permission.class);

        List<Predicate> predicates = new ArrayList<>();

        predicates.add(getBuilder().equal(from.get("canWrite"), canWrite));
        predicates.add(getBuilder().equal(from.get("canRead"), canRead));
        predicates.add(getPredicate(from, "group", group));
        predicates.add(getPredicate(from, "folder", folder));
        predicates.add(getPredicate(from, "account", account));
        predicates.add(getPredicate(from, "entry", entry));
        predicates.add(getPredicate(from, "upload", upload));
        query.where(predicates.toArray(new Predicate[predicates.size()]));
        return currentSession().createQuery(query);
    }

    public int removePermission(Entry entry, Folder folder, BulkUpload upload, Account account, Group group,
                                boolean canRead, boolean canWrite) {
        try {
            CriteriaDelete<Permission> delete = getBuilder().createCriteriaDelete(Permission.class);
            Root<Permission> from = delete.from(Permission.class);
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(getBuilder().equal(from.get("canWrite"), canWrite));
            predicates.add(getBuilder().equal(from.get("canRead"), canRead));
            predicates.add(getPredicate(from, "group", group));
            predicates.add(getPredicate(from, "folder", folder));
            predicates.add(getPredicate(from, "account", account));
            predicates.add(getPredicate(from, "entry", entry));
            predicates.add(getPredicate(from, "upload", upload));

            delete.where(predicates.toArray(new Predicate[predicates.size()]));
            return currentSession().createQuery(delete).executeUpdate();
        } catch (HibernateException he) {
            Logger.error(he);
            throw new DAOException(he);
        }
    }

    public List<Permission> getEntryPermissions(Entry entry) {
        try {
            CriteriaQuery<Permission> query = getBuilder().createQuery(Permission.class);
            Root<Permission> from = query.from(Permission.class);
            query.where(getBuilder().equal(from.get("entry"), entry));
            return currentSession().createQuery(query).list();
        } catch (HibernateException he) {
            Logger.error(he);
            throw new DAOException(he);
        }
    }

    public List<Permission> getFolderPermissions(Folder folder) {
        try {
            CriteriaQuery<Permission> query = getBuilder().createQuery(Permission.class);
            Root<Permission> from = query.from(Permission.class);
            query.where(getBuilder().equal(from.get("folder"), folder));
            return currentSession().createQuery(query).list();
        } catch (HibernateException he) {
            Logger.error(he);
            throw new DAOException(he);
        }
    }

    public List<Account> retrieveAccountPermissions(Folder folder, boolean canWrite, boolean canRead) {
        try {
            CriteriaQuery<Account> query = getBuilder().createQuery(Account.class);
            Root<Permission> from = query.from(Permission.class);
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(getBuilder().equal(from.get("canWrite"), canWrite));
            predicates.add(getBuilder().equal(from.get("canRead"), canRead));
            predicates.add(getBuilder().equal(from.get("folder"), folder));
            predicates.add(getBuilder().isNotNull(from.get("account")));
            query.select(from.get("account")).where(predicates.toArray(new Predicate[predicates.size()]));
            return currentSession().createQuery(query).list();
        } catch (HibernateException he) {
            Logger.error(he);
            throw new DAOException(he);
        }
    }

    public boolean hasSetWriteFolderPermission(Folder folder, Account account) {
        try {
            CriteriaQuery<Long> query = getBuilder().createQuery(Long.class);
            Root<Permission> from = query.from(Permission.class);
            query.select(getBuilder().countDistinct(from.get("id")));
            query.where(
                    getBuilder().isNull(from.get("entry")),
                    getBuilder().equal(from.get("folder"), folder),
                    getBuilder().equal(from.get("account"), account));
            return currentSession().createQuery(query).setMaxResults(1).uniqueResult() > 0;
        } catch (HibernateException he) {
            Logger.error(he);
            throw new DAOException(he);
        }
    }

    public List<Group> retrieveGroupPermissions(Folder folder, boolean canWrite, boolean canRead) {
        try {
            CriteriaQuery<Group> query = getBuilder().createQuery(Group.class);
            Root<Permission> from = query.from(Permission.class);
            query.select(from.get("group"));
            query.where(
                    getBuilder().isNull(from.get("group")),
                    getBuilder().equal(from.get("folder"), folder),
                    getBuilder().equal(from.get("canRead"), canRead),
                    getBuilder().equal(from.get("canWrite"), canWrite));
            return currentSession().createQuery(query).list();
        } catch (HibernateException he) {
            Logger.error(he);
            throw new DAOException(he);
        }
    }

    public int clearPermissions(Folder folder) {
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

    public int clearPermissions(Group group) {
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

    public List<Folder> retrieveFolderPermissions(Account account, Set<Group> accountGroups) {
        // can read or can write
        try {
            CriteriaQuery<Folder> query = getBuilder().createQuery(Folder.class);
            Root<Permission> from = query.from(Permission.class);
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(getBuilder().or(
                    getBuilder().equal(from.get("canWrite"), true),
                    getBuilder().equal(from.get("canRead"), true)));
            predicates.add(getBuilder().isNull(from.get("entry")));
            predicates.add(getBuilder().isNotNull(from.get("folder")));
            if (accountGroups.isEmpty()) {
                predicates.add(getBuilder().equal(from.get("account"), account));
            } else {
                predicates.add(getBuilder().or(
                        getBuilder().equal(from.get("account"), account),
                        from.get("group").in(accountGroups)
                ));
            }

            query.select(from.get("folder"));
            query.where(predicates.toArray(new Predicate[predicates.size()]));
            return currentSession().createQuery(query).list();
        } catch (HibernateException he) {
            Logger.error(he);
            throw new DAOException(he);
        }
    }

    public List<Folder> getFolders(Group group) {
        try {
            CriteriaQuery<Folder> query = getBuilder().createQuery(Folder.class);
            Root<Permission> from = query.from(Permission.class);
            query.select(from.get("folder"));
            query.where(
                    getBuilder().or(
                            getBuilder().equal(from.get("canWrite"), true),
                            getBuilder().equal(from.get("canRead"), true)),
                    getBuilder().isNull(from.get("entry")),
                    getBuilder().equal(from.get("group"), group)
            );
            return currentSession().createQuery(query).list();
        } catch (HibernateException he) {
            Logger.error(he);
            throw new DAOException(he);
        }
    }

    /**
     * Filters the given list, removing those that the specified account does not have read privileges on
     * // todo : this doesn't check permissions from folder (get list of all folders entries are in)
     *
     * @param account account to filter entries by
     * @param groups  groups that this account belongs to
     * @param entries list of entry ids to filter
     * @return filtered list such that specified account have read privileges on entries contained in it
     */
    public List<Long> getCanReadEntries(Account account, List<Group> groups, List<Long> entries) {
        try {
            CriteriaQuery<Long> query = getBuilder().createQuery(Long.class);
            Root<Permission> from = query.from(Permission.class);
            Join<Permission, Entry> entry = from.join("entry", JoinType.LEFT);
            Predicate predicate = getBuilder().or(
                    getBuilder().equal(from.get("account"), account),
                    getBuilder().equal(entry.get("ownerEmail"), account.getEmail())
            );
            if (!groups.isEmpty())
                predicate.getExpressions().add(from.get("group").in(groups));
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(predicate);
            predicates.add(entry.get("id").in(entries));
            predicates.add(entry.get("visibility").in(Visibility.OK.getValue(), Visibility.PENDING.getValue()));
            query.select(entry.get("id")).distinct(true);
            query.where(predicates.toArray(new Predicate[predicates.size()]));
            return currentSession().createQuery(query).list();
        } catch (HibernateException he) {
            Logger.error(he);
            throw new DAOException(he);
        }
    }

    @Override
    public Permission get(long id) {
        return super.get(Permission.class, id);
    }
}

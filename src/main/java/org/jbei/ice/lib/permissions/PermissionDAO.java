package org.jbei.ice.lib.permissions;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.lib.account.model.Account;
import org.jbei.ice.lib.dao.DAOException;
import org.jbei.ice.lib.entry.model.Entry;
import org.jbei.ice.lib.group.GroupController;
import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.lib.models.Group;
import org.jbei.ice.server.dao.hibernate.HibernateRepository;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;

/**
 * DAO to manipulate {@link org.jbei.ice.lib.permissions.model.ReadGroup}, {@link org.jbei.ice.lib.permissions.model
 * .ReadUser}, {@link org.jbei.ice.lib.permissions.model.WriteGroup}
 * & {@link org.jbei.ice.lib.permissions.model.WriteUser} objects in the database
 *
 * @author Hector Plahar, Timothy Ham, Zinovii Dmytriv
 */
public class PermissionDAO extends HibernateRepository {

    /**
     * Check if the given {@link Account} has read permission to the given {@link Entry}.
     *
     * @param entry Entry to query on.
     * @return True if given Account has read permission to the given Entry.
     */
    public List<Long> getEntryReadAccounts(Entry entry) throws DAOException {
        String queryString = "select readUser.account.id from ReadUser as readUser where readUser.entry = :entry";
        Session session = newSession();
        Query query = session.createQuery(queryString);
        query.setEntity("entry", entry);
        List<Long> accounts = null;
        try {
            accounts = new ArrayList<Long>(query.list());
        } catch (HibernateException e) {
            throw new DAOException(e);
        } finally {
            if (session.isOpen()) {
                session.close();
            }
        }

        return accounts;
    }

    public List<Long> getEntryWriteAccounts(Entry entry) throws DAOException {
        String queryString = "select writeUser.account.id from WriteUser as writeUser where writeUser.entry = :entry";
        Session session = newSession();
        Query query = session.createQuery(queryString);
        query.setEntity("entry", entry);
        List<Long> accounts = null;
        try {
            accounts = new ArrayList<Long>(query.list());
        } catch (HibernateException e) {
            throw new DAOException(e);
        } finally {
            if (session.isOpen()) {
                session.close();
            }
        }

        return accounts;
    }

    /**
     * Check if the given {@link Account} has read permission to the given {@link Entry} by
     * comparing the permissible {@link Group} hierarchy with groups the user belongs to.
     *
     * @param entry   Entry to query on.
     * @param account Account to query on.
     * @return True if the Account has read permission to the Entry.
     */
    @SuppressWarnings("unchecked")
    public boolean groupHasReadPermission(Entry entry, Account account) throws DAOException {
        boolean result = false;
        String queryString = "select readGroup.group.id from ReadGroup as readGroup where readGroup.entry = :entry";
        Session session = newSession();
        List<Long> readGroups = null;
        try {
            Query query = session.createQuery(queryString);
            query.setEntity("entry", entry);
            readGroups = query.list();
        } catch (HibernateException e) {
            throw e;
        } finally {
            if (session.isOpen()) {
                session.close();
            }
        }
        Set<Long> accountGroups = getAllAccountGroups(account);

        accountGroups.retainAll(new HashSet<Long>(readGroups));
        if (accountGroups.size() > 0) {
            result = true;
        }

        return result;
    }

    /**
     * Check if the given {@link Account} has write permission to the specified {@link Entry} by
     * comparing the permissible {@link Group} hierarchy with groups the user belongs to.
     * *
     *
     * @param entry   Entry to query on.
     * @param account Account to query on.
     * @return True if the Account has write permission to the Entry.
     */
    @SuppressWarnings("unchecked")
    protected boolean groupHasWritePermission(Entry entry, Account account) throws DAOException {
        boolean result = false;
        String queryString = "select writeGroup.group.id from WriteGroup as writeGroup where writeGroup.entry = :entry";
        Session session = newSession();
        Query query = session.createQuery(queryString);
        query.setEntity("entry", entry);
        List<Long> readGroups = null;
        try {
            readGroups = query.list();
        } catch (HibernateException e) {
            throw e;
        } finally {
            if (session.isOpen()) {
                session.close();
            }
        }

        Set<Long> accountGroups = getAllAccountGroups(account);

        accountGroups.retainAll(new HashSet<Long>(readGroups));
        if (accountGroups.size() > 0) {
            result = true;
        }

        return result;
    }

    /**
     * Retrieve all parent {@link Group}s of a given group.
     *
     * @param group    Group to query on.
     * @param groupIds optional set of group ids. Can be empty.
     * @return Set of Parent group ids.
     */
    protected HashSet<Long> getParentGroups(Group group, HashSet<Long> groupIds) throws DAOException {
        if (groupIds.contains(group.getId())) {
            return groupIds;
        } else {
            groupIds.add(group.getId());
            Group parentGroup = group.getParent();
            if (parentGroup != null) {
                getParentGroups(parentGroup, groupIds);
            }
        }

        return groupIds;
    }

    /**
     * Retrieve all parent {@link Group}s of a given {@link Account}.
     *
     * @param account Account to query on.
     * @return Set of Group ids.
     */
    protected Set<Long> getAllAccountGroups(Account account) throws DAOException {
        HashSet<Long> accountGroups = new HashSet<Long>();

        for (Group group : account.getGroups()) {
            accountGroups = getParentGroups(group, accountGroups);
        }

        // Everyone belongs to the everyone group
        GroupController controller = new GroupController();
        try {
            Group everybodyGroup = controller.createOrRetrievePublicGroup();
            accountGroups.add(everybodyGroup.getId());
        } catch (ControllerException e) {
            Logger.warn("could not get everybody group: " + e.toString());
        }
        return accountGroups;
    }
}

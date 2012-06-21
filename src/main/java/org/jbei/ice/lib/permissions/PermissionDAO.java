package org.jbei.ice.lib.permissions;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.lib.account.model.Account;
import org.jbei.ice.lib.dao.DAOException;
import org.jbei.ice.lib.entry.model.Entry;
import org.jbei.ice.lib.group.GroupController;
import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.lib.models.Group;
import org.jbei.ice.lib.permissions.model.ReadGroup;
import org.jbei.ice.lib.permissions.model.ReadUser;
import org.jbei.ice.lib.permissions.model.WriteGroup;
import org.jbei.ice.lib.permissions.model.WriteUser;
import org.jbei.ice.server.dao.hibernate.HibernateRepository;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * DAO to manipulate {@link org.jbei.ice.lib.permissions.model.ReadGroup}, {@link org.jbei.ice.lib.permissions.model
 * .ReadUser}, {@link org.jbei.ice.lib.permissions.model.WriteGroup}
 * & {@link org.jbei.ice.lib.permissions.model.WriteUser} objects in the database
 *
 * @author Hector Plahar, Timothy Ham, Zinovii Dmytriv
 */
public class PermissionDAO extends HibernateRepository {

    /**
     * Set read permissions for specified user {@link Account}s to the given {@link Entry}.
     * <p/>
     * This method creates new {@link org.jbei.ice.lib.permissions.model.ReadUser} objects using the given {@link
     * Account}s.
     *
     * @param entry    Entry to give read permission to.
     * @param accounts Accounts to give read permission to.
     * @throws DAOException
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
            if (session != null)
                session.close();
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
            if (session != null)
                session.close();
        }
    }

    public void removeReadGroup(Entry entry, Group group) throws DAOException {
        String queryString = "delete  ReadGroup readGroup where readGroup.entry = :entry and readGroup.group = :group";
        Session session = newSession();

        try {
            session.getTransaction().begin();
            Query query = session.createQuery(queryString);
            query.setEntity("entry", entry);
            query.setEntity("group", group);
            query.executeUpdate();
            session.getTransaction().commit();
        } catch (HibernateException e) {
            session.getTransaction().rollback();
            String msg = "Could not remove read group \"" + group.getLabel() + "\" for entry \""
                    + entry.getId() + "\"";
            throw new DAOException(msg, e);
        } finally {
            if (session != null)
                session.close();
        }
    }

    public void removeWriteUser(Entry entry, Account account) throws DAOException {
        String queryString = "delete WriteUser writeUser where writeUser.entry = :entry and writeUser.account = " +
                ":account";
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
            String msg = "Could not remove write user \"" + account.getEmail() + "\" for entry \""
                    + entry.getId() + "\"";
            throw new DAOException(msg, e);
        } finally {
            if (session != null)
                session.close();
        }
    }

    public void removeWriteGroup(Entry entry, Group group) throws DAOException {
        String queryString = "delete  WriteGroup writeGroup where writeGroup.entry = :entry and writeGroup.group = " +
                ":group";
        Session session = newSession();

        try {
            session.getTransaction().begin();
            Query query = session.createQuery(queryString);
            query.setEntity("entry", entry);
            query.setEntity("group", group);
            query.executeUpdate();
            session.getTransaction().commit();
        } catch (HibernateException e) {
            session.getTransaction().rollback();
            String msg = "Could not remove write group \"" + group.getLabel() + "\" for entry \""
                    + entry.getId() + "\"";
            throw new DAOException(msg, e);
        } finally {
            if (session != null)
                session.close();
        }
    }

    /**
     * Add read permission for the specified {@link Account} to the specified {@link Entry}.
     * <p/>
     * This method adds a new {@link ReadUser} object to the database..
     *
     * @param entry
     * @param account
     * @throws DAOException
     */
    public void addReadUser(Entry entry, Account account) throws DAOException {
        Set<Account> accounts = getReadUser(entry);
        boolean alreadyAdded = false;
        for (Account oldAccount : accounts) {
            if (oldAccount.getId() == account.getId()) {
                alreadyAdded = true;
                break;
            }
        }
        if (alreadyAdded == false) {
            accounts.add(account);
            setReadUser(entry, accounts);
        }
    }

    /**
     * Retrieve {@link Account}s with read permissions set for the specified {@link Entry}.
     *
     * @param entry Entry to get ReadUsers about.
     * @return Set of Accounts with read permission for the given Entry.
     * @throws DAOException
     */
    public Set<Account> getReadUser(Entry entry) throws DAOException {
        Session session = newSession();
        try {
            String queryString = "select readUser.account from ReadUser readUser where readUser.entry = :entry";
            Query query = session.createQuery(queryString);
            query.setEntity("entry", entry);

            @SuppressWarnings("unchecked")
            HashSet<Account> result = new HashSet<Account>(query.list());
            return result;

        } catch (Exception e) {
            e.printStackTrace();
            String msg = "Could not read ReadUser of " + entry.getRecordId();
            Logger.error(msg, e);
            throw new DAOException(msg, e);
        } finally {
            if (session.isOpen()) {
                session.close();
            }
        }
    }

    /**
     * Set read permissions for specified user {@link Group}s to the given {@link Entry}.
     * <p/>
     * This method creates new {@link org.jbei.ice.lib.permissions.model.ReadGroup} objects using the given {@link
     * Group}s.
     *
     * @param entry  Entry to give permission to.
     * @param groups Groups to give read permission to.
     * @throws DAOException
     */
    public void setReadGroup(Entry entry, Set<Group> groups) throws DAOException {
        String queryString = "delete  ReadGroup readGroup where readGroup.entry = :entry";
        Session session = newSession();
        session.getTransaction().begin();
        Query query = session.createQuery(queryString);
        query.setEntity("entry", entry);
        query.executeUpdate();
        session.getTransaction().commit();
        try {
            for (Group group : groups) {
                ReadGroup readGroup = new ReadGroup(entry, group);
                super.saveOrUpdate(readGroup);
            }
        } catch (Exception e) {
            session.getTransaction().rollback();
            String msg = "Could not set Read Group of " + entry.getRecordId();
            throw new DAOException(msg, e);
        } finally {
            if (session.isOpen()) {
                session.close();
            }
        }
    }

    /**
     * Add read permission for the specified {@link Group} to the specified {@link Entry}.
     * <p/>
     * This method adds a new {@link ReadGroup} object to the database..
     *
     * @param entry Entry to give read permission to.
     * @param group Group to give read permission to.
     * @throws DAOException
     */
    public void addReadGroup(Entry entry, Group group) throws DAOException {
        Set<Group> groups = getReadGroup(entry);
        boolean alreadyAdded = false;
        for (Group existingGroup : groups) {
            if (existingGroup.getId() == group.getId()) {
                alreadyAdded = true;
                break;
            }
        }
        if (alreadyAdded == false) {
            groups.add(group);
            setReadGroup(entry, groups);
        }
    }

    /**
     * Retrieve {@link Group}s with read permissions set for the specified {@link Entry}.
     *
     * @param entry Entry to query on.
     * @return Set of Groups.
     * @throws DAOException
     */
    public Set<Group> getReadGroup(Entry entry) throws DAOException {
        Session session = newSession();
        try {
            String queryString = "select readGroup.group from ReadGroup readGroup where readGroup.entry = :entry";

            Query query = session.createQuery(queryString);
            query.setEntity("entry", entry);

            @SuppressWarnings("unchecked")
            HashSet<Group> result = new HashSet<Group>(query.list());
            return result;

        } catch (Exception e) {
            e.printStackTrace();
            String msg = "Could not get Read Group of " + entry.getRecordId();
            Logger.error(msg, e);
            throw new DAOException(msg, e);
        } finally {
            if (session.isOpen()) {
                session.close();
            }
        }
    }

    /**
     * Set write permissions for specified user {@link Account}s to the given {@link Entry}.
     * <p/>
     * This method creates new {@link org.jbei.ice.lib.permissions.model.WriteUser} objects using the given {@link
     * Account}s.
     *
     * @param entry    Entry to give permission to.
     * @param accounts Accounts to give write permission to.
     * @throws DAOException
     */
    public void setWriteUser(Entry entry, Set<Account> accounts) throws DAOException {
        String queryString = "delete  WriteUser writeUser where writeUser.entry = :entry";

        Session session = newSession();
        try {
            Query query = session.createQuery(queryString);
            query.setEntity("entry", entry);
            session.getTransaction().begin();
            query.executeUpdate();
            session.getTransaction().commit();
            for (Account account : accounts) {
                WriteUser writeUser = new WriteUser(entry, account);
                super.saveOrUpdate(writeUser);
            }
        } catch (HibernateException e) {
            session.getTransaction().rollback();
            String msg = "Could not set Write User of " + entry.getRecordId();
            throw new DAOException(msg, e);
        } finally {
            if (session.isOpen()) {
                session.close();
            }
        }
    }

    /**
     * Add write permission for the specified {@link Account} to the specified {@link Entry}.
     * <p/>
     * This method adds a new {@link WriteUser} object to the database..
     *
     * @param entry   Entry to give write permission to.
     * @param account Account to give write permission to.
     * @throws DAOException
     */
    public void addWriteUser(Entry entry, Account account) throws DAOException {
        Set<Account> accounts = getWriteUser(entry);
        boolean alreadyAdded = false;
        for (Account existingAccount : accounts) {
            if (existingAccount.getId() == account.getId()) {
                alreadyAdded = true;
                break;
            }
        }
        if (alreadyAdded == false) {
            accounts.add(account);
            setWriteUser(entry, accounts);
        }
    }

    /**
     * Retrieve {@link Account}s with write permissions set for the specified {@link Entry}.
     *
     * @param entry entry to query on.
     * @return Set of Accounts.
     * @throws DAOException
     */
    public Set<Account> getWriteUser(Entry entry) throws DAOException {
        Session session = newSession();
        try {
            String queryString = "select writeUser.account from WriteUser writeUser where writeUser.entry = :entry";
            Query query = session.createQuery(queryString);
            query.setEntity("entry", entry);

            @SuppressWarnings("unchecked")
            HashSet<Account> result = new HashSet<Account>(query.list());
            return result;

        } catch (Exception e) {
            String msg = "Could not get Write User of " + entry.getRecordId();
            Logger.error(msg, e);
            throw new DAOException(msg, e);
        } finally {
            if (session.isOpen()) {
                session.close();
            }
        }

    }

    /**
     * Set write permissions for specified user {@link Group}s to the given {@link Entry}.
     * <p/>
     * This method creates new {@link org.jbei.ice.lib.permissions.model.WriteGroup} objects using the given {@link
     * Group}s.
     *
     * @param entry  Entry to give permission to.
     * @param groups Groups to give write permission to.
     * @throws DAOException
     */
    public void setWriteGroup(Entry entry, Set<Group> groups) throws DAOException {
        String queryString = "delete  WriteGroup writeGroup where writeGroup.entry = :entry";
        Session session = newSession();

        try {
            session.getTransaction().begin();
            Query query = session.createQuery(queryString);
            query.setEntity("entry", entry);
            query.executeUpdate();
            session.getTransaction().commit();

            for (Group group : groups) {
                WriteGroup writeGroup = new WriteGroup(entry, group);
                super.saveOrUpdate(writeGroup);
            }
        } catch (Exception e) {
            session.getTransaction().rollback();
            String msg = "Could not set WriteGroup of " + entry.getRecordId();
            throw new DAOException(msg, e);
        } finally {
            if (session != null)
                session.close();
        }
    }

    /**
     * Add write permission for the specified {@link Group} to the specified {@link Entry}.
     * <p/>
     * This method adds a new {@link WriteGroup} object to the database..
     *
     * @param entry Entry to give write permission to.
     * @param group Group to give write permission to.
     * @throws DAOException
     */
    public void addWriteGroup(Entry entry, Group group) throws DAOException {
        Set<Group> groups = getWriteGroup(entry);
        boolean alreadyAdded = false;
        for (Group existingGroup : groups) {
            if (existingGroup.getId() == group.getId()) {
                alreadyAdded = true;
                break;
            }
        }
        if (alreadyAdded == false) {
            groups.add(group);
            setWriteGroup(entry, groups);
        }
    }

    /**
     * Retrieve {@link Group}s with write permissions set for the specified {@link Entry}.
     *
     * @param entry Entry to query on.
     * @return Set of Groups.
     * @throws DAOException
     */
    public Set<Group> getWriteGroup(Entry entry) throws DAOException {
        Session session = newSession();
        try {
            String queryString = "select writeGroup.group from WriteGroup writeGroup where writeGroup.entry = :entry";

            Query query = session.createQuery(queryString);
            query.setEntity("entry", entry);

            @SuppressWarnings("unchecked")
            HashSet<Group> result = new HashSet<Group>(query.list());
            return result;

        } catch (Exception e) {
            String msg = "Could not get Write Group of " + entry.getRecordId();
            throw new DAOException(msg, e);
        } finally {
            if (session.isOpen()) {
                session.close();
            }
        }
    }

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

    public List<Long> getEntryReadGroupIds(Entry entry) throws DAOException {
        String queryString = "select readGroup.group.id from " + ReadGroup.class.getName() + " as readGroup where " +
                "readGroup.entry = :entry";
        Session session = newSession();
        Query query = session.createQuery(queryString);
        query.setEntity("entry", entry);
        List<Long> groupIds = null;
        try {
            groupIds = new ArrayList<Long>(query.list());
        } catch (HibernateException e) {
            throw new DAOException(e);
        } finally {
            if (session.isOpen()) {
                session.close();
            }
        }

        return groupIds;
    }

    /**
     * Check if the given {@link Account} has read permission to the specified {@link Entry}.
     *
     * @param entryId id of Entry to query on.
     * @param account Account to query on.
     * @return True if given Account has read permission to the specified Entry.
     */
    @SuppressWarnings("unchecked")
    protected boolean userHasReadPermission(long entryId, Account account) throws DAOException {
        boolean result = false;

        String queryString1 = "select count(id) from Entry as entry where entry.ownerEmail = '"
                + account.getEmail() + "' AND " + " entry.id = " + entryId;
        Session session = newSession();
        Long numberOfEntries = null;
        try {
            Query query1 = session.createQuery(queryString1);

            numberOfEntries = (Long) query1.uniqueResult();
        } catch (HibernateException e) {
            throw e;
        } finally {
            if (session.isOpen()) {
                session.close();
            }
        }
        if (numberOfEntries > 0) {
            result = true;
        }

        if (!result) {
            String queryString2 = "select readUser.account.id from ReadUser as readUser where readUser.entry.id = "
                    + entryId;
            session = newSession();
            Query query2 = session.createQuery(queryString2);
            List<Long> accounts = null;
            try {
                accounts = query2.list();
            } catch (HibernateException e) {
                throw e;
            } finally {

                if (session.isOpen()) {
                    session.close();
                }
            }

            if (accounts.contains(account.getId())) {
                result = true;
            }
        }

        return result;
    }

    /**
     * Check if the given {@link Account} has read permission to the specified {@link Entry}.
     *
     * @param recordId id of Entry to query on.
     * @param account  Account to query on.
     * @return True if given Account has read permission to the specified Entry.
     */
    @SuppressWarnings("unchecked")
    protected boolean userHasReadPermission(String recordId, Account account) throws DAOException {
        boolean result = false;

        String queryString1 = "select count(id) from Entry as entry where entry.ownerEmail = '"
                + account.getEmail() + "' AND " + " entry.recordId = '" + recordId + "'";
        Session session = newSession();
        Long numberOfEntries = null;
        try {
            Query query1 = session.createQuery(queryString1);

            numberOfEntries = (Long) query1.uniqueResult();
        } catch (HibernateException e) {
            throw e;
        } finally {
            if (session.isOpen()) {
                session.close();
            }
        }
        if (numberOfEntries > 0) {
            result = true;
        }

        if (!result) {
            String queryString2 = "select readUser.account.id from ReadUser as readUser where readUser.entry.recordId" +
                    " = '"
                    + recordId + "'";
            session = newSession();
            Query query2 = session.createQuery(queryString2);
            List<Long> accounts = null;
            try {
                accounts = query2.list();
            } catch (HibernateException e) {
                throw e;
            } finally {

                if (session.isOpen()) {
                    session.close();
                }
            }

            if (accounts.contains(account.getId())) {
                result = true;
            }
        }

        return result;
    }

    /**
     * Check if the given {@link Account} has write permission to the given {@link Entry}.
     *
     * @param entry   Entry to query on.
     * @param account Account to query on.
     * @return True if given Account has write permission to the given Entry.
     */
    protected boolean userHasWritePermission(Entry entry, Account account) throws DAOException {
        boolean result = false;
        String queryString = "select writeUser.account.id from WriteUser as writeUser where writeUser.entry = :entry";

        Session session = newSession();
        try {
            Query query = session.createQuery(queryString);

            if (query != null) {
                query.setEntity("entry", entry);

                @SuppressWarnings("unchecked")
                List<Long> accounts = query.list();
                if (account.getEmail().equals(entry.getOwnerEmail())) {
                    result = true;
                } else if (accounts.contains(account.getId())) {
                    result = true;
                }
            }
        } catch (HibernateException e) {
            throw e;
        } finally {
            if (session.isOpen()) {
                session.close();
            }
        }
        return result;
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
     * Check if the given {@link Account} has read permission to the specified {@link Entry} by
     * comparing the permissible {@link Group} hierarchy with groups the user belongs to.
     *
     * @param entryId id of the Entry.
     * @param account Account to be queried.
     * @return True if the Account has read permission to the Entry.
     */
    @SuppressWarnings("unchecked")
    protected boolean groupHasReadPermission(long entryId, Account account) throws DAOException {
        boolean result = false;

        String queryString = "select readGroup.group.id from ReadGroup as readGroup where readGroup.entry.id = "
                + entryId;
        Session session = newSession();
        List<Long> readGroups = null;
        try {
            Query query = session.createQuery(queryString);
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
     * Check if the given {@link Account} has read permission to the specified {@link Entry} by
     * comparing the permissible {@link Group} hierarchy with groups the user belongs to.
     *
     * @param recordId recordId of Entry.
     * @param account  Account to query on.
     * @return True if the Account has read permission to the Entry.
     */
    @SuppressWarnings("unchecked")
    protected boolean groupHasReadPermission(String recordId, Account account) throws DAOException {
        boolean result = false;

        String queryString = "select readGroup.group.id from ReadGroup as readGroup where readGroup.entry.recordId = '"
                + recordId + "'";
        Session session = newSession();
        List<Long> readGroups = null;
        try {
            Query query = session.createQuery(queryString);
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

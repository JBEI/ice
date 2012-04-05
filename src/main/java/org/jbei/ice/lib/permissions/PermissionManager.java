package org.jbei.ice.lib.permissions;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.jbei.ice.lib.dao.DAO;
import org.jbei.ice.lib.dao.DAOException;
import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.lib.managers.AccountManager;
import org.jbei.ice.lib.managers.EntryManager;
import org.jbei.ice.lib.managers.GroupManager;
import org.jbei.ice.lib.managers.ManagerException;
import org.jbei.ice.lib.models.Account;
import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.lib.models.Group;
import org.jbei.ice.web.IceSession;

/**
 * Manager to manipulate Permissions.
 * 
 * @author Timothy Ham, Zinovii Dmytriv
 * 
 */
public class PermissionManager {

    /**
     * Check if the logged in user has read permission to the given {@link Entry} by entryId.
     * 
     * @param entryId
     *            id of the Entry.
     * @return True if user has read permission to the specified entry.
     */
    public static boolean hasReadPermission(long entryId) {
        return hasReadPermission(entryId, IceSession.get().getAccount());
    }

    /**
     * Check if the {@link Account} associated with the given sessionKey has read permission to the
     * given {@link Entry} by recordId.
     * 
     * @param recordId
     *            recordId of the Entry.
     * @param sessionKey
     *            session key.
     * @return True if the given session account has read permission to the specified entry.
     */
    public static boolean hasReadPermission(String recordId, String sessionKey) {
        boolean result = false;

        Account account = null;
        try {
            account = AccountManager.getAccountByAuthToken(sessionKey);

            if (account != null) {
                result = hasReadPermission(recordId, account)
                        || groupHasReadPermission(recordId, account);
            }
        } catch (ManagerException e) {
            // if lookup fails, doesn't have permission
            String msg = "manager exception during permission lookup: " + e.toString();
            Logger.warn(msg);
        }

        return result;
    }

    /**
     * Check if the {@link Account} has read permission to the specified {@link Entry} by entryId.
     * 
     * @param recordId
     *            recordId of the Entry.
     * @param account
     *            Account
     * @return True if the given Account has read permission to the specified Entry.
     */
    public static boolean hasReadPermission(String recordId, Account account) {
        boolean result = false;

        if (recordId != null && !recordId.isEmpty() && account != null) {
            try {
                if (AccountManager.isModerator(account)) {
                    result = true;
                } else {
                    result = groupHasReadPermission(recordId, account)
                            || userHasReadPermission(recordId, account);
                }
            } catch (ManagerException e) {
                e.printStackTrace();
            }
        }

        return result;
    }

    /**
     * Check if the {@link Account} has read permission to the specified {@link Entry}.
     * 
     * @param entryId
     *            id of the specified Entry.
     * @param account
     *            Account
     * @return True if given Account has read permission to the specified Entry.
     */
    public static boolean hasReadPermission(long entryId, Account account) {
        boolean result = false;

        if (entryId > 0 && account != null) {
            try {
                if (AccountManager.isModerator(account)) {
                    result = true;
                } else {
                    result = groupHasReadPermission(entryId, account)
                            || userHasReadPermission(entryId, account);
                }
            } catch (ManagerException e) {
                e.printStackTrace();
            }
        }

        return result;
    }

    /**
     * Check if the current {@link Account} has write permission to the specified {@link Entry}.
     * 
     * @param entryId
     *            id of the specified Entry.
     * @return True if current user has write permission to the specified Entry.
     */
    public static boolean hasWritePermission(long entryId) {
        boolean result = false;
        Entry entry;

        try {
            entry = EntryManager.get(entryId);
            if (entry != null) {
                result = hasWritePermission(entry);

            }
        } catch (ManagerException e) {
            String msg = "manager exception during permission lookup: " + e.toString();
            Logger.warn(msg);
        }
        return result;
    }

    /**
     * Check if the {@link Account} associated with the given session key has write permission to
     * the specified {@link Entry}.
     * 
     * @param entryId
     *            id of the Entry
     * @param sessionKey
     *            session key
     * @return True if the user has write permission.
     */
    public static boolean hasWritePermission(long entryId, String sessionKey) {
        boolean result = false;
        Entry entry;
        try {
            Account account = AccountManager.getAccountByAuthToken(sessionKey);
            if (account != null) {
                entry = EntryManager.get(entryId);
                if (entry != null) {
                    result = hasWritePermission(entry, account);
                }
            }
        } catch (ManagerException e) {
            String msg = "manager exception during permission lookup: " + e.toString();
            Logger.warn(msg);
        }
        return result;
    }

    /**
     * Check if the {@link Account} associated with the given session key has write permission to
     * the specified {@link Entry}.
     * 
     * @param recordId
     *            recordId of the Entry.
     * @param account
     *            Account to be queried.
     * @return True if the account has write permission to the specified Entry.
     */
    public static boolean hasWritePermission(String recordId, Account account) {
        boolean result = false;
        Entry entry;

        try {
            entry = EntryManager.getByRecordId(recordId);

            if (entry != null) {
                result = hasWritePermission(entry, account);
            }
        } catch (ManagerException e) {
            // if lookup fails, doesn't have permission
            String msg = "manager exception during permission lookup: " + e.toString();
            Logger.warn(msg);
        }

        return result;
    }

    /**
     * Check if the {@link Account} associated with the given session key has write permission to
     * the specified {@link Entry}.
     * 
     * @param entryId
     *            id of the Entry.
     * @param account
     *            Account to be queried.
     * @return True if the account has write permission to the specified Entry.
     */
    public static boolean hasWritePermission(long entryId, Account account) {
        boolean result = false;
        Entry entry;

        try {
            entry = EntryManager.get(entryId);

            if (entry != null) {
                result = hasWritePermission(entry, account);
            }
        } catch (ManagerException e) {
            // if lookup fails, doesn't have permission
            String msg = "manager exception during permission lookup: " + e.toString();
            Logger.warn(msg);
        }

        return result;
    }

    /**
     * Checks if the current {@link Account} logged in has read permission to the given
     * {@link Entry}.
     * 
     * @param entry
     *            Entry to be queried.
     * @return True if the current user has read permission.
     */
    public static boolean hasReadPermission(Entry entry) {
        return hasReadPermission(entry, IceSession.get().getAccount());
    }

    public static boolean hasReadPermission(Entry entry, Account account) {
        boolean result = false;
        if (entry != null && account != null) {
            try {
                if (AccountManager.isModerator(account)) {
                    result = true;
                } else {
                    result = userHasReadPermission(entry, account)
                            | groupHasReadPermission(entry, account);
                }
            } catch (ManagerException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    /**
     * Checks if the current {@link Account} logged in has write permission to the given
     * {@link Entry}.
     * 
     * @param entry
     *            Entry to be queried.
     * @return True if the current user has wrte permission.
     */
    public static boolean hasWritePermission(Entry entry) {
        boolean result = false;

        Account account;
        account = IceSession.get().getAccount();

        if (entry != null && account != null) {
            try {
                if (AccountManager.isModerator(account)) {
                    result = true;
                } else {
                    result = userHasWritePermission(entry, account)
                            | groupHasWritePermission(entry, account);
                }
            } catch (ManagerException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    /**
     * Checks if the given {@link Account} has write permission to the given {@link Entry}.
     * 
     * @param entry
     *            Entry to be queried.
     * @param account
     *            Account to be queried.
     * @return True if the given account has write permission to the given Entry.
     */
    public static boolean hasWritePermission(Entry entry, Account account) {
        boolean result = false;
        if (entry != null && account != null) {
            try {
                if (AccountManager.isModerator(account)) {
                    result = true;
                } else {
                    result = userHasWritePermission(entry, account)
                            | groupHasWritePermission(entry, account);
                }
            } catch (ManagerException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    /**
     * Set read permissions for specified user {@link Account}s to the given {@link Entry}.
     * <p>
     * This method creates new {@link ReadUser} objects using the given {@link Account}s.
     * 
     * @param entry
     *            Entry to give read permission to.
     * @param accounts
     *            Accounts to give read permission to.
     * @throws ManagerException
     */
    public static void setReadUser(Entry entry, Set<Account> accounts) throws ManagerException {
        String queryString = "delete  ReadUser readUser where readUser.entry = :entry";
        Session session = DAO.newSession();

        try {
            session.getTransaction().begin();
            Query query = session.createQuery(queryString);
            query.setEntity("entry", entry);
            query.executeUpdate();
            session.getTransaction().commit();
            for (Account account : accounts) {
                ReadUser readUser = new ReadUser(entry, account);
                DAO.save(readUser);
            }
        } catch (HibernateException e) {
            session.getTransaction().rollback();
            String msg = "Could not set Read User to " + entry.getRecordId();
            throw new ManagerException(msg, e);
        } catch (DAOException e) {
            throw new ManagerException(e);
        }
    }

    public static void removeReadUser(Entry entry, Account account) throws ManagerException {
        String queryString = "delete  ReadUser readUser where readUser.entry = :entry and readUser.account = :account";
        Session session = DAO.newSession();

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
            throw new ManagerException(msg, e);
        } finally {
            if (session != null)
                session.disconnect();
        }
    }

    public static void removeReadGroup(Entry entry, Group group) throws ManagerException {
        String queryString = "delete  ReadGroup readGroup where readGroup.entry = :entry and readGroup.group = :group";
        Session session = DAO.newSession();

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
            throw new ManagerException(msg, e);
        } finally {
            if (session != null)
                session.disconnect();
        }
    }

    public static void removeWriteUser(Entry entry, Account account) throws ManagerException {
        String queryString = "delete  WriteUser writeUser where writeUser.entry = :entry and writeUser.account = :account";
        Session session = DAO.newSession();

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
            throw new ManagerException(msg, e);
        } finally {
            if (session != null)
                session.disconnect();
        }
    }

    public static void removeWriteGroup(Entry entry, Group group) throws ManagerException {
        String queryString = "delete  WriteGroup writeGroup where writeGroup.entry = :entry and writeGroup.group = :group";
        Session session = DAO.newSession();

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
            throw new ManagerException(msg, e);
        } finally {
            if (session != null)
                session.disconnect();
        }
    }

    /**
     * Add read permission for the specified {@link Account} to the specified {@link Entry}.
     * <p>
     * This method adds a new {@link ReadUser} object to the database..
     * 
     * @param entry
     * @param account
     * @throws ManagerException
     */
    public static void addReadUser(Entry entry, Account account) throws ManagerException {
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
     * @param entry
     *            Entry to get ReadUsers about.
     * @return Set of Accounts with read permission for the given Entry.
     * @throws ManagerException
     */
    public static Set<Account> getReadUser(Entry entry) throws ManagerException {
        Session session = DAO.newSession();
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
            throw new ManagerException(msg, e);
        } finally {
            if (session.isOpen()) {
                session.close();
            }
        }
    }

    /**
     * Set read permissions for specified user {@link Group}s to the given {@link Entry}.
     * <p>
     * This method creates new {@link ReadGroup} objects using the given {@link Group}s.
     * 
     * @param entry
     *            Entry to give permission to.
     * @param groups
     *            Groups to give read permission to.
     * @throws ManagerException
     */
    public static void setReadGroup(Entry entry, Set<Group> groups) throws ManagerException {
        String queryString = "delete  ReadGroup readGroup where readGroup.entry = :entry";
        Session session = DAO.newSession();
        session.getTransaction().begin();
        Query query = session.createQuery(queryString);
        query.setEntity("entry", entry);
        query.executeUpdate();
        session.getTransaction().commit();
        try {
            for (Group group : groups) {
                ReadGroup readGroup = new ReadGroup(entry, group);
                DAO.save(readGroup);
            }
        } catch (Exception e) {
            session.getTransaction().rollback();
            String msg = "Could not set Read Group of " + entry.getRecordId();
            throw new ManagerException(msg, e);
        } finally {
            if (session.isOpen()) {
                session.close();
            }
        }
    }

    /**
     * Add read permission for the specified {@link Group} to the specified {@link Entry}.
     * <p>
     * This method adds a new {@link ReadGroup} object to the database..
     * 
     * @param entry
     *            Entry to give read permission to.
     * @param group
     *            Group to give read permission to.
     * @throws ManagerException
     */
    public static void addReadGroup(Entry entry, Group group) throws ManagerException {
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
     * @param entry
     *            Entry to query on.
     * @return Set of Groups.
     * @throws ManagerException
     */
    public static Set<Group> getReadGroup(Entry entry) throws ManagerException {
        Session session = DAO.newSession();
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
            throw new ManagerException(msg, e);
        } finally {
            if (session.isOpen()) {
                session.close();
            }
        }
    }

    /**
     * Set write permissions for specified user {@link Account}s to the given {@link Entry}.
     * <p>
     * This method creates new {@link WriteUser} objects using the given {@link Account}s.
     * 
     * @param entry
     *            Entry to give permission to.
     * @param accounts
     *            Accounts to give write permission to.
     * @throws ManagerException
     */
    public static void setWriteUser(Entry entry, Set<Account> accounts) throws ManagerException {
        String queryString = "delete  WriteUser writeUser where writeUser.entry = :entry";

        Session session = DAO.newSession();
        try {
            Query query = session.createQuery(queryString);
            query.setEntity("entry", entry);
            session.getTransaction().begin();
            query.executeUpdate();
            session.getTransaction().commit();
            for (Account account : accounts) {
                WriteUser writeUser = new WriteUser(entry, account);
                DAO.save(writeUser);
            }
        } catch (HibernateException e) {
            session.getTransaction().rollback();
            String msg = "Could not set Write User of " + entry.getRecordId();
            throw new ManagerException(msg, e);
        } catch (DAOException e) {
            throw new ManagerException(e);
        } finally {
            if (session.isOpen()) {
                session.close();
            }
        }
    }

    /**
     * Add write permission for the specified {@link Account} to the specified {@link Entry}.
     * <p>
     * This method adds a new {@link WriteUser} object to the database..
     * 
     * @param entry
     *            Entry to give write permission to.
     * @param account
     *            Account to give write permission to.
     * @throws ManagerException
     */
    public static void addWriteUser(Entry entry, Account account) throws ManagerException {
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
     * @param entry
     *            entry to query on.
     * @return Set of Accounts.
     * @throws ManagerException
     */
    public static Set<Account> getWriteUser(Entry entry) throws ManagerException {
        Session session = DAO.newSession();
        try {
            String queryString = "select writeUser.account from WriteUser writeUser where writeUser.entry = :entry";
            Query query = session.createQuery(queryString);
            query.setEntity("entry", entry);

            @SuppressWarnings("unchecked")
            HashSet<Account> result = new HashSet<Account>(query.list());
            return result;

        } catch (Exception e) {
            e.printStackTrace();
            String msg = "Could not get Write User of " + entry.getRecordId();
            Logger.error(msg, e);
            throw new ManagerException(msg, e);
        } finally {
            if (session.isOpen()) {
                session.close();
            }
        }

    }

    /**
     * Set write permissions for specified user {@link Group}s to the given {@link Entry}.
     * <p>
     * This method creates new {@link WriteGroup} objects using the given {@link Group}s.
     * 
     * @param entry
     *            Entry to give permission to.
     * @param groups
     *            Groups to give write permission to.
     * @throws ManagerException
     */
    public static void setWriteGroup(Entry entry, Set<Group> groups) throws ManagerException {
        String queryString = "delete  WriteGroup writeGroup where writeGroup.entry = :entry";
        Session session = DAO.newSession();

        try {
            session.getTransaction().begin();
            Query query = session.createQuery(queryString);
            query.setEntity("entry", entry);
            query.executeUpdate();
            session.getTransaction().commit();

            for (Group group : groups) {
                WriteGroup writeGroup = new WriteGroup(entry, group);
                DAO.save(writeGroup);
            }
        } catch (Exception e) {
            session.getTransaction().rollback();
            String msg = "Could not set WriteGroup of " + entry.getRecordId();
            throw new ManagerException(msg, e);
        }
    }

    /**
     * Add write permission for the specified {@link Group} to the specified {@link Entry}.
     * <p>
     * This method adds a new {@link WriteGroup} object to the database..
     * 
     * @param entry
     *            Entry to give write permission to.
     * @param group
     *            Group to give write permission to.
     * @throws ManagerException
     */
    public static void addWriteGroup(Entry entry, Group group) throws ManagerException {
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
     * @param entry
     *            Entry to query on.
     * @return Set of Groups.
     * @throws ManagerException
     */
    public static Set<Group> getWriteGroup(Entry entry) throws ManagerException {
        Session session = DAO.newSession();
        try {
            String queryString = "select writeGroup.group from WriteGroup writeGroup where writeGroup.entry = :entry";

            Query query = session.createQuery(queryString);
            query.setEntity("entry", entry);

            @SuppressWarnings("unchecked")
            HashSet<Group> result = new HashSet<Group>(query.list());
            return result;

        } catch (Exception e) {
            String msg = "Could not get Write Group of " + entry.getRecordId();
            throw new ManagerException(msg, e);
        } finally {
            if (session.isOpen()) {
                session.close();
            }
        }
    }

    /**
     * Check if the given {@link Account} has read permission to the given {@link Entry}.
     * 
     * @param entry
     *            Entry to query on.
     * @param account
     *            Account to query on.
     * @return True if given Account has read permission to the given Entry.
     */
    @SuppressWarnings("unchecked")
    protected static boolean userHasReadPermission(Entry entry, Account account) {
        boolean result = false;
        String queryString = "select readUser.account.id from ReadUser as readUser where readUser.entry = :entry";
        Session session = DAO.newSession();
        Query query = session.createQuery(queryString);
        query.setEntity("entry", entry);
        List<Long> accounts = null;
        try {
            accounts = query.list();
        } catch (HibernateException e) {
            throw e;
        } finally {
            if (session.isOpen()) {
                session.close();
            }
        }
        if (account.getEmail().equals(entry.getOwnerEmail())) {
            result = true;
        } else if (accounts.contains(account.getId())) {
            result = true;
        }
        return result;
    }

    /**
     * Check if the given {@link Account} has read permission to the specified {@link Entry}.
     * 
     * @param entryId
     *            id of Entry to query on.
     * @param account
     *            Account to query on.
     * @return True if given Account has read permission to the specified Entry.
     */
    @SuppressWarnings("unchecked")
    protected static boolean userHasReadPermission(long entryId, Account account) {
        boolean result = false;

        String queryString1 = "select count(id) from Entry as entry where entry.ownerEmail = '"
                + account.getEmail() + "' AND " + " entry.id = " + entryId;
        Session session = DAO.newSession();
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
            session = DAO.newSession();
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
     * @param recordId
     *            id of Entry to query on.
     * @param account
     *            Account to query on.
     * @return True if given Account has read permission to the specified Entry.
     */
    @SuppressWarnings("unchecked")
    protected static boolean userHasReadPermission(String recordId, Account account) {
        boolean result = false;

        String queryString1 = "select count(id) from Entry as entry where entry.ownerEmail = '"
                + account.getEmail() + "' AND " + " entry.recordId = '" + recordId + "'";
        Session session = DAO.newSession();
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
            String queryString2 = "select readUser.account.id from ReadUser as readUser where readUser.entry.recordId = '"
                    + recordId + "'";
            session = DAO.newSession();
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
     * @param entry
     *            Entry to query on.
     * @param account
     *            Account to query on.
     * @return True if given Account has write permission to the given Entry.
     */
    protected static boolean userHasWritePermission(Entry entry, Account account) {
        boolean result = false;
        String queryString = "select writeUser.account.id from WriteUser as writeUser where writeUser.entry = :entry";

        Session session = DAO.newSession();
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
     * @param entry
     *            Entry to query on.
     * @param account
     *            Account to query on.
     * @return True if the Account has read permission to the Entry.
     */
    @SuppressWarnings("unchecked")
    protected static boolean groupHasReadPermission(Entry entry, Account account) {
        boolean result = false;
        String queryString = "select readGroup.group.id from ReadGroup as readGroup where readGroup.entry = :entry";
        Session session = DAO.newSession();
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
     * @param entryId
     *            id of the Entry.
     * @param account
     *            Account to be queried.
     * @return True if the Account has read permission to the Entry.
     */
    @SuppressWarnings("unchecked")
    protected static boolean groupHasReadPermission(long entryId, Account account) {
        boolean result = false;

        String queryString = "select readGroup.group.id from ReadGroup as readGroup where readGroup.entry.id = "
                + entryId;
        Session session = DAO.newSession();
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
     * @param recordId
     *            recordId of Entry.
     * @param account
     *            Account to query on.
     * @return True if the Account has read permission to the Entry.
     */
    @SuppressWarnings("unchecked")
    protected static boolean groupHasReadPermission(String recordId, Account account) {
        boolean result = false;

        String queryString = "select readGroup.group.id from ReadGroup as readGroup where readGroup.entry.recordId = '"
                + recordId + "'";
        Session session = DAO.newSession();
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
     * @param entry
     *            Entry to query on.
     * @param account
     *            Account to query on.
     * @return True if the Account has write permission to the Entry.
     */
    @SuppressWarnings("unchecked")
    protected static boolean groupHasWritePermission(Entry entry, Account account) {
        boolean result = false;
        String queryString = "select writeGroup.group.id from WriteGroup as writeGroup where writeGroup.entry = :entry";
        Session session = DAO.newSession();
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
     * @param group
     *            Group to query on.
     * @param groupIds
     *            optional set of group ids. Can be empty.
     * @return Set of Parent group ids.
     */
    protected static HashSet<Long> getParentGroups(Group group, HashSet<Long> groupIds) {
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
     * @param account
     *            Account to query on.
     * @return Set of Group ids.
     */
    protected static Set<Long> getAllAccountGroups(Account account) {
        HashSet<Long> accountGroups = new HashSet<Long>();

        for (Group group : account.getGroups()) {
            accountGroups = getParentGroups(group, accountGroups);
        }

        // Everyone belongs to the everyone group
        try {
            Group everybodyGroup = GroupManager.getEverybodyGroup();
            accountGroups.add(everybodyGroup.getId());
        } catch (ManagerException e) {
            Logger.warn("could not get everybody group: " + e.toString());
        }
        return accountGroups;
    }

}

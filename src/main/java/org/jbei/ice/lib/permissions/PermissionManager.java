package org.jbei.ice.lib.permissions;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.Query;
import org.hibernate.Transaction;
import org.jbei.ice.lib.dao.DAO;
import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.lib.managers.AccountManager;
import org.jbei.ice.lib.managers.EntryManager;
import org.jbei.ice.lib.managers.GroupManager;
import org.jbei.ice.lib.managers.ManagerException;
import org.jbei.ice.lib.models.Account;
import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.lib.models.Group;
import org.jbei.ice.web.IceSession;

public class PermissionManager {

    // convenience method that wraps actual method
    public static boolean hasReadPermission(int entryId) {
        return hasReadPermission(entryId, IceSession.get().getAccount());
    }

    public static boolean hasReadPermission(int entryId, String sessionKey) {
        boolean result = false;

        Account account = null;
        try {
            account = AccountManager.getAccountByAuthToken(sessionKey);

            if (account != null) {
                result = hasReadPermission(entryId, account)
                        | groupHasReadPermission(entryId, account);
            }
        } catch (ManagerException e) {
            // if lookup fails, doesn't have permission
            String msg = "manager exception during permission lookup: " + e.toString();
            Logger.warn(msg);
        }

        return result;
    }

    public static boolean hasReadPermission(int entryId, Account account) {
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

    // convenience method that wraps actual method
    public static boolean hasWritePermission(int entryId) {
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

    public static boolean hasWritePermission(int entryId, String sessionKey) {
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

    public static boolean hasWritePermission(int entryId, Account account) {
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

    public static void setReadUser(Entry entry, Set<Account> accounts) throws ManagerException {
        String queryString = "delete  ReadUser readUser where readUser.entry = :entry";
        Transaction tx = DAO.getSession().beginTransaction();
        Query query = DAO.getSession().createQuery(queryString);
        query.setEntity("entry", entry);
        query.executeUpdate();
        tx.commit();
        try {
            for (Account account : accounts) {
                ReadUser readUser = new ReadUser(entry, account);
                DAO.save(readUser);
            }
        } catch (Exception e) {
            e.printStackTrace();
            String msg = "Could not set Read User to " + entry.getRecordId();
            Logger.error(msg, e);
            throw new ManagerException(msg, e);
        }
    }

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

    public static Set<Account> getReadUser(Entry entry) throws ManagerException {
        try {
            String queryString = "select readUser.account from ReadUser readUser where readUser.entry = :entry";

            Query query = DAO.getSession().createQuery(queryString);
            query.setEntity("entry", entry);

            @SuppressWarnings("unchecked")
            HashSet<Account> result = new HashSet<Account>(query.list());
            return result;

        } catch (Exception e) {
            e.printStackTrace();
            String msg = "Could not read ReadUser of " + entry.getRecordId();
            Logger.error(msg, e);
            throw new ManagerException(msg, e);
        }
    }

    public static void setReadGroup(Entry entry, Set<Group> groups) throws ManagerException {
        String queryString = "delete  ReadGroup readGroup where readGroup.entry = :entry";
        Transaction tx = DAO.getSession().beginTransaction();
        Query query = DAO.getSession().createQuery(queryString);
        query.setEntity("entry", entry);
        query.executeUpdate();
        tx.commit();
        try {
            for (Group group : groups) {
                ReadGroup readGroup = new ReadGroup(entry, group);
                DAO.save(readGroup);
            }
        } catch (Exception e) {
            e.printStackTrace();
            String msg = "Could not set Read Group of " + entry.getRecordId();
            Logger.error(msg, e);
            throw new ManagerException(msg, e);
        }
    }

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

    public static Set<Group> getReadGroup(Entry entry) throws ManagerException {
        try {
            String queryString = "select readGroup.group from ReadGroup readGroup where readGroup.entry = :entry";
            Query query = DAO.getSession().createQuery(queryString);
            query.setEntity("entry", entry);

            @SuppressWarnings("unchecked")
            HashSet<Group> result = new HashSet<Group>(query.list());
            return result;

        } catch (Exception e) {
            e.printStackTrace();
            String msg = "Could not get Read Group of " + entry.getRecordId();
            Logger.error(msg, e);
            throw new ManagerException(msg, e);
        }
    }

    public static void setWriteUser(Entry entry, Set<Account> accounts) throws ManagerException {
        String queryString = "delete  WriteUser writeUser where writeUser.entry = :entry";
        Transaction tx = DAO.getSession().beginTransaction();
        Query query = DAO.getSession().createQuery(queryString);
        query.setEntity("entry", entry);
        query.executeUpdate();
        tx.commit();
        try {
            for (Account account : accounts) {
                WriteUser writeUser = new WriteUser(entry, account);
                DAO.save(writeUser);
            }
        } catch (Exception e) {
            e.printStackTrace();
            String msg = "Could not set Write User of " + entry.getRecordId();
            Logger.error(msg, e);
            throw new ManagerException(msg, e);
        }
    }

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

    public static Set<Account> getWriteUser(Entry entry) throws ManagerException {
        try {
            String queryString = "select writeUser.account from WriteUser writeUser where writeUser.entry = :entry";
            Query query = DAO.getSession().createQuery(queryString);
            query.setEntity("entry", entry);

            @SuppressWarnings("unchecked")
            HashSet<Account> result = new HashSet<Account>(query.list());
            return result;

        } catch (Exception e) {
            e.printStackTrace();
            String msg = "Could not get Write User of " + entry.getRecordId();
            Logger.error(msg, e);
            throw new ManagerException(msg, e);
        }
    }

    public static void setWriteGroup(Entry entry, Set<Group> groups) throws ManagerException {
        String queryString = "delete  WriteGroup writeGroup where writeGroup.entry = :entry";
        Transaction tx = DAO.getSession().beginTransaction();
        Query query = DAO.getSession().createQuery(queryString);
        query.setEntity("entry", entry);
        query.executeUpdate();
        tx.commit();
        try {
            for (Group group : groups) {
                WriteGroup writeGroup = new WriteGroup(entry, group);
                DAO.save(writeGroup);
            }
        } catch (Exception e) {
            e.printStackTrace();
            String msg = "Could not set WriteGroup of " + entry.getRecordId();
            Logger.error(msg, e);
            throw new ManagerException(msg, e);
        }
    }

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

    public static Set<Group> getWriteGroup(Entry entry) throws ManagerException {
        try {
            String queryString = "select writeGroup.group from WriteGroup writeGroup where writeGroup.entry = :entry";

            Query query = DAO.getSession().createQuery(queryString);
            query.setEntity("entry", entry);

            @SuppressWarnings("unchecked")
            HashSet<Group> result = new HashSet<Group>(query.list());
            return result;

        } catch (Exception e) {
            e.printStackTrace();
            String msg = "Could not get Write Group of " + entry.getRecordId();
            Logger.error(msg, e);
            throw new ManagerException(msg, e);
        }
    }

    protected static boolean userHasReadPermission(Entry entry, Account account) {
        boolean result = false;
        String queryString = "select readUser.account.id from ReadUser as readUser where readUser.entry = :entry";
        Query query = DAO.getSession().createQuery(queryString);
        query.setEntity("entry", entry);

        @SuppressWarnings("unchecked")
        List<Integer> accounts = query.list();
        if (account.getEmail().equals(entry.getOwnerEmail())) {
            result = true;
        } else if (accounts.contains(account.getId())) {
            result = true;
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    protected static boolean userHasReadPermission(int entryId, Account account) {
        boolean result = false;

        String queryString1 = "select count(id) from Entry as entry where entry.ownerEmail = '"
                + account.getEmail() + "' AND " + " entry.id = " + entryId;
        Query query1 = DAO.getSession().createQuery(queryString1);

        Long numberOfEntries = (Long) query1.uniqueResult();

        if (numberOfEntries > 0) {
            result = true;
        }

        if (!result) {
            String queryString2 = "select readUser.account.id from ReadUser as readUser where readUser.entry.id = "
                    + entryId;
            Query query2 = DAO.getSession().createQuery(queryString2);
            //query.setEntity("entryId", entryId);

            List<Integer> accounts = query2.list();
            if (accounts.contains(account.getId())) {
                result = true;
            }
        }

        return result;
    }

    protected static boolean userHasWritePermission(Entry entry, Account account) {
        boolean result = false;
        String queryString = "select writeUser.account.id from WriteUser as writeUser where writeUser.entry = :entry";
        Query query = null;
        try {
            query = DAO.getSession().createQuery(queryString);
        } catch (Exception e) {
            Logger.warn(e.toString());
        }
        if (query != null) {
            query.setEntity("entry", entry);

            @SuppressWarnings("unchecked")
            List<Integer> accounts = query.list();
            if (account.getEmail().equals(entry.getOwnerEmail())) {
                result = true;
            } else if (accounts.contains(account.getId())) {
                result = true;
            }
        }
        return result;
    }

    protected static boolean groupHasReadPermission(Entry entry, Account account) {
        boolean result = false;
        String queryString = "select readGroup.group.id from ReadGroup as readGroup where readGroup.entry = :entry";
        Query query = DAO.getSession().createQuery(queryString);
        query.setEntity("entry", entry);

        @SuppressWarnings("unchecked")
        List<Integer> readGroups = query.list();

        Set<Integer> accountGroups = getAllAccountGroups(account);

        accountGroups.retainAll(new HashSet<Integer>(readGroups));
        if (accountGroups.size() > 0) {
            result = true;
        }

        return result;
    }

    protected static boolean groupHasReadPermission(int entryId, Account account) {
        boolean result = false;

        String queryString = "select readGroup.group.id from ReadGroup as readGroup where readGroup.entry.id = "
                + entryId;
        Query query = DAO.getSession().createQuery(queryString);

        @SuppressWarnings("unchecked")
        List<Integer> readGroups = query.list();

        Set<Integer> accountGroups = getAllAccountGroups(account);

        accountGroups.retainAll(new HashSet<Integer>(readGroups));
        if (accountGroups.size() > 0) {
            result = true;
        }

        return result;
    }

    protected static boolean groupHasWritePermission(Entry entry, Account account) {
        boolean result = false;
        String queryString = "select writeGroup.group.id from WriteGroup as writeGroup where writeGroup.entry = :entry";
        Query query = DAO.getSession().createQuery(queryString);
        query.setEntity("entry", entry);

        @SuppressWarnings("unchecked")
        List<Integer> readGroups = query.list();

        Set<Integer> accountGroups = getAllAccountGroups(account);

        accountGroups.retainAll(new HashSet<Integer>(readGroups));
        if (accountGroups.size() > 0) {
            result = true;
        }

        return result;
    }

    protected static HashSet<Integer> getParentGroups(Group group, HashSet<Integer> groupIds) {
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

    protected static Set<Integer> getAllAccountGroups(Account account) {
        HashSet<Integer> accountGroups = new HashSet<Integer>();

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

    public static void main(String[] args) {

    }
}

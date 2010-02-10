package org.jbei.ice.lib.permissions;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.Query;
import org.hibernate.Transaction;
import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.lib.managers.AccountManager;
import org.jbei.ice.lib.managers.EntryManager;
import org.jbei.ice.lib.managers.GroupManager;
import org.jbei.ice.lib.managers.HibernateHelper;
import org.jbei.ice.lib.managers.Manager;
import org.jbei.ice.lib.managers.ManagerException;
import org.jbei.ice.lib.models.Account;
import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.lib.models.Group;

public class PermissionManager extends Manager {

    // convenience method that wraps actual method
    public static boolean hasReadPermission(int entryId, String sessionKey) {
        boolean result = false;
        Entry entry;
        Account account;
        try {
            entry = EntryManager.get(entryId);
            if (entry != null) {
                account = AccountManager.getAccountByAuthToken(sessionKey);
                result = hasReadPermission(entry, account);
            }
        } catch (ManagerException e) {
            // if lookup fails, doesn't have permission
            String msg = "manager exception during permission lookup: " + e.toString();
            Logger.warn(msg);
        }
        return result;
    }

    // convenience method that wraps actual method
    public static boolean hasWritePermission(int entryId, String sessionKey) {
        boolean result = false;
        Entry entry;
        Account account;
        try {
            entry = EntryManager.get(entryId);
            if (entry != null) {
                account = AccountManager.getAccountByAuthToken(sessionKey);
                result = hasWritePermission(entry, account);
            }
        } catch (ManagerException e) {
            String msg = "manager exception during permission lookup: " + e.toString();
            Logger.warn(msg);
        }
        return result;
    }

    public static boolean hasReadPermission(Entry entry, Account account) {
        boolean result = false;
        result = userHasReadPermission(entry, account) | groupHasReadPermission(entry, account);
        return result;
    }

    public static boolean hasWritePermission(Entry entry, Account account) {
        boolean result = userHasWritePermission(entry, account)
                | groupHasWritePermission(entry, account);
        return result;
    }

    public static void setReadUser(Entry entry, Set<Account> accounts) throws ManagerException {
        String queryString = "delete  ReadUser readUser where readUser.entry = :entry";
        Transaction tx = session.beginTransaction();
        Query query = session.createQuery(queryString);
        query.setEntity("entry", entry);
        query.executeUpdate();
        tx.commit();
        try {
            for (Account account : accounts) {
                ReadUser readUser = new ReadUser(entry, account);
                dbSave(readUser);
            }
        } catch (Exception e) {
            e.printStackTrace();
            String msg = "Could not set Read User to " + entry.getRecordId();
            Logger.error(msg);
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

            Query query = session.createQuery(queryString);
            query.setEntity("entry", entry);

            @SuppressWarnings("unchecked")
            HashSet<Account> result = new HashSet<Account>(query.list());
            return result;

        } catch (Exception e) {
            e.printStackTrace();
            String msg = "Could not read ReadUser of " + entry.getRecordId();
            Logger.error(msg);
            throw new ManagerException(msg, e);
        }
    }

    public static void setReadGroup(Entry entry, Set<Group> groups) throws ManagerException {
        String queryString = "delete  ReadGroup readGroup where readGroup.entry = :entry";
        Transaction tx = session.beginTransaction();
        Query query = session.createQuery(queryString);
        query.setEntity("entry", entry);
        query.executeUpdate();
        tx.commit();
        try {
            for (Group group : groups) {
                ReadGroup readGroup = new ReadGroup(entry, group);
                dbSave(readGroup);
            }
        } catch (Exception e) {
            e.printStackTrace();
            String msg = "Could not set Read Group of " + entry.getRecordId();
            Logger.error(msg);
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
            Query query = session.createQuery(queryString);
            query.setEntity("entry", entry);

            @SuppressWarnings("unchecked")
            HashSet<Group> result = new HashSet<Group>(query.list());
            return result;

        } catch (Exception e) {
            e.printStackTrace();
            String msg = "Could not get Read Group of " + entry.getRecordId();
            Logger.error(msg);
            throw new ManagerException(msg, e);
        }
    }

    public static void setWriteUser(Entry entry, Set<Account> accounts) throws ManagerException {
        String queryString = "delete  WriteUser writeUser where writeUser.entry = :entry";
        Transaction tx = session.beginTransaction();
        Query query = session.createQuery(queryString);
        query.setEntity("entry", entry);
        query.executeUpdate();
        tx.commit();
        try {
            for (Account account : accounts) {
                WriteUser writeUser = new WriteUser(entry, account);
                dbSave(writeUser);
            }
        } catch (Exception e) {
            e.printStackTrace();
            String msg = "Could not set Write User of " + entry.getRecordId();
            Logger.error(msg);
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
            Query query = session.createQuery(queryString);
            query.setEntity("entry", entry);

            @SuppressWarnings("unchecked")
            HashSet<Account> result = new HashSet<Account>(query.list());
            return result;

        } catch (Exception e) {
            e.printStackTrace();
            String msg = "Could not get Write User of " + entry.getRecordId();
            Logger.error(msg);
            throw new ManagerException(msg, e);
        }
    }

    public static void setWriteGroup(Entry entry, Set<Group> groups) throws ManagerException {
        String queryString = "delete  WriteGroup writeGroup where writeGroup.entry = :entry";
        Transaction tx = session.beginTransaction();
        Query query = session.createQuery(queryString);
        query.setEntity("entry", entry);
        query.executeUpdate();
        tx.commit();
        try {
            for (Group group : groups) {
                WriteGroup writeGroup = new WriteGroup(entry, group);
                dbSave(writeGroup);
            }
        } catch (Exception e) {
            e.printStackTrace();
            String msg = "Could not set WriteGroup of " + entry.getRecordId();
            Logger.error(msg);
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

            Query query = session.createQuery(queryString);
            query.setEntity("entry", entry);

            @SuppressWarnings("unchecked")
            HashSet<Group> result = new HashSet<Group>(query.list());
            return result;

        } catch (Exception e) {
            e.printStackTrace();
            String msg = "Could not get Write Group of " + entry.getRecordId();
            Logger.error(msg);
            throw new ManagerException(msg, e);
        }
    }

    protected static boolean userHasReadPermission(Entry entry, Account account) {
        boolean result = false;
        String queryString = "select readUser.account.id from ReadUser as readUser where readUser.entry = :entry";
        Query query = HibernateHelper.getSession().createQuery(queryString);
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

    protected static boolean userHasWritePermission(Entry entry, Account account) {
        boolean result = false;
        String queryString = "select writeUser.account.id from WriteUser as writeUser where writeUser.entry = :entry";
        Query query = null;
        try {
            query = HibernateHelper.getSession().createQuery(queryString);
        } catch (Exception e) {
            e.printStackTrace();
        }
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

    protected static boolean groupHasReadPermission(Entry entry, Account account) {
        boolean result = false;
        String queryString = "select readGroup.group.id from ReadGroup as readGroup where readGroup.entry = :entry";
        Query query = HibernateHelper.getSession().createQuery(queryString);
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

    protected static boolean groupHasWritePermission(Entry entry, Account account) {
        boolean result = false;
        String queryString = "select writeGroup.group.id from WriteGroup as writeGroup where writeGroup.entry = :entry";
        Query query = HibernateHelper.getSession().createQuery(queryString);
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

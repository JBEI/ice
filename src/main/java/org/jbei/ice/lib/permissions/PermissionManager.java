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
import org.jbei.ice.lib.utils.PopulateInitialDatabase;

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
        /* If entry has no group or user read permissions, that is, nobody has 
         * read permissions because permissions are never set, the default 
         * is to allow read for everyone. 
         */

        if (entryNeedsDefaultPermission(entry)) {
            result = true;
        } else {
            result = userHasReadPermission(entry, account) | groupHasReadPermission(entry, account);
        }
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

        return accountGroups;
    }

    /**
     * Determines if any sort of permission was set for entry
     * 
     * @param entry
     * @return
     * @throws ManagerException
     */
    protected static boolean entryNeedsDefaultPermission(Entry entry) {

        boolean result = false;

        int entriesGroup;
        int entriesUser;

        String queryString = "from ReadUser readUser where readUser.entry = :entry";
        Query query = session.createQuery(queryString);
        query.setEntity("entry", entry);
        entriesUser = query.list().size();
        queryString = "from ReadGroup readGroup where readGroup.entry = :entry";
        query.setEntity("entry", entry);
        entriesGroup = query.list().size();

        if (entriesUser == 0 && entriesGroup == 0) {
            result = true;

            try {
                Group defaultGroup = GroupManager.get(PopulateInitialDatabase.everyoneGroup);
                if (defaultGroup == null) {
                    defaultGroup = PopulateInitialDatabase.createFirstGroup();
                }
                HashSet<Group> tempGroups = new HashSet<Group>();
                tempGroups.add(defaultGroup);
                PermissionManager.setReadGroup(entry, tempGroups);
            } catch (ManagerException e) {
                String msg = "Could not get default everyone group: " + e.toString();
                Logger.error(msg);
            }
            Logger.info("Default Permission supplied");
        }
        return result;
    }

    public static void main(String[] args) {
        try {
            /*
            HashSet<Account> users = new HashSet<Account>();

            users.add(AccountManager.getById(1));
            users.add(AccountManager.getById(2));
            users.add(AccountManager.getById(3));
            
            Entry entry = EntryManager.get(1);
            
            for (Account user: users) {
            	ReadUser readUsers = new ReadUser(entry, user);
            	dbSave(readUsers);
            }
            
            users = new HashSet<Account>();
            users.add(AccountManager.getById(1));
            users.add(AccountManager.getById(2));
            users.add(AccountManager.getById(4));
            
            for (Account user: users) {
            	WriteUser writeUsers = new WriteUser(entry, user);
            	dbSave(writeUsers);
            }
            
            */

            /*
            Entry entry = EntryManager.get(1);
            Account account = AccountManager.getById(3);
            Boolean result = hasReadPermission(entry, account);
            System.out.println("Has read permission: " + result.toString());
            
            result = hasWritePermission(entry, account);
            System.out.println("Has write permission: " + result.toString());
            */

            /*
            Entry entry2 = EntryManager.get(2);
            Group group = GroupManager.get(551);
            
            ReadGroup readGroup = new ReadGroup(entry2, group);
            readGroup = (ReadGroup) dbSave(readGroup);
            
            WriteGroup writeGroup = new WriteGroup(entry2, group);
            writeGroup = (WriteGroup) dbSave(writeGroup);
            */

            /*
            Account account = AccountManager.getById(2);
            Group group = GroupManager.get(551);
            HashSet<Group> groups = new HashSet<Group>();
            groups.add(group);
            
            account.setGroups(groups);
            account = (Account) AccountManager.save(account);
            */
            /*
            Entry entry = EntryManager.get(2);
            Account account = AccountManager.getById(2);
            Boolean result = groupHasReadPermission(entry, account);
            System.out.println("account has group read permission: " + result.toString());

            result = groupHasWritePermission(entry, account);
            System.out.println("account has group write permission: " + result.toString());
            */

            // create some dummy groups for testing
            // g1 -- G2 -- G4
            //				 -- G5
            //		-- G3 -- G6
            /*
            Group g1 = GroupManager.create("dummy1", "Group 1", "dummy group 1", null);
            Group g2 = GroupManager.create("dummy2", "Group 2", "dummy group 2", g1);
            Group g3 = GroupManager.create("dummy3", "Group 3", "dummy group 3", g1);
            Group g4 = GroupManager.create("dummy4", "Group 4", "dummy group 4", g2);
            Group g5 = GroupManager.create("dummy5", "Group 5", "dummy group 5", g2);
            Group g6 = GroupManager.create("dummy6", "Group 6", "dummy group 6", g3);
            */

            // set some users to groups
            /*
            Account u1 = AccountManager.getById(1);
            Account u2 = AccountManager.getById(2);
            Account u3 = AccountManager.getById(3);
            Account u4 = AccountManager.getById(4);
            
            HashSet<Group> groups = new HashSet<Group>();
            groups.add(GroupManager.get(15)); //g4
            u1.setGroups(groups);
            AccountManager.save(u1);
            
            groups = new HashSet<Group>();
            groups.add(GroupManager.get(15)); //g4
            u2.setGroups(groups);
            AccountManager.save(u2);
            
            groups = new HashSet<Group>();
            groups.add(GroupManager.get(17)); //g6
            u3.setGroups(groups);
            AccountManager.save(u3);
            
            groups = new HashSet<Group>();
            groups.add(GroupManager.get(16)); //g5
            groups.add(GroupManager.get(14)); //g3
            u4.setGroups(groups);
            AccountManager.save(u4);
            */

            //test group hierarchy
            /*
            Account u1 = AccountManager.getById(1);
            Account u2 = AccountManager.getById(2);
            Account u3 = AccountManager.getById(3);
            Account u4 = AccountManager.getById(4);
            
            System.out.println("u1: " + getAllAccountGroups(u1).toString());
            System.out.println("u2: " + getAllAccountGroups(u2).toString());
            System.out.println("u3: " + getAllAccountGroups(u3).toString());
            System.out.println("u4: " + getAllAccountGroups(u4).toString());
            */

            //set groups to entry3
            /*
            Entry entry3 = EntryManager.get(3);
            
            HashSet<Group> groups = new HashSet<Group>();
            groups.add(GroupManager.get(15));
            groups.add(GroupManager.get(14));
            setReadGroup(entry3, groups);
            */

            //set groups to entry4
            /*
            Entry entry4 = EntryManager.get(4);
            HashSet<Group> groups = new HashSet<Group>();
            groups.add(GroupManager.get(14));
            setReadGroup(entry4, groups);
            */

            //test user permission via groups
            Account u1 = AccountManager.get(1);
            Account u2 = AccountManager.get(2);
            Account u3 = AccountManager.get(3);
            Account u4 = AccountManager.get(4);

            //Entry entry3 = EntryManager.get(3);
            Entry entry4 = EntryManager.get(4);
            /*
            System.out.println("u1 can read e3: " + hasReadPermission(entry3, u1));
            System.out.println("u2 can read e3: " + hasReadPermission(entry3, u2));
            System.out.println("u3 can read e3: " + hasReadPermission(entry3, u3));
            System.out.println("u4 can read e3: " + hasReadPermission(entry3, u4));
            */
            System.out.println("u1 can read e4: " + hasReadPermission(entry4, u1));
            System.out.println("u2 can read e4: " + hasReadPermission(entry4, u2));
            System.out.println("u3 can read e4: " + hasReadPermission(entry4, u3));
            System.out.println("u4 can read e4: " + hasReadPermission(entry4, u4));

        } catch (ManagerException e) {
            e.printStackTrace();
        }

    }

}

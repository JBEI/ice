package org.jbei.ice.lib.permissions;

import java.util.Set;

import org.jbei.ice.lib.managers.ManagerException;
import org.jbei.ice.lib.models.Account;
import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.lib.models.Group;

/**
 * Wrap {@link PermissionManager} methods to check if the user has permission to view or modify
 * permissions.
 * 
 * @author Timothy Ham, Zinovii Dmytriv
 * 
 */
public class AuthenticatedPermissionManager {
    /**
     * Retrieve {@link Account}s with read permissions set for the specified {@link Entry}.
     * 
     * @param entry
     *            - Entry to get ReadUsers about.
     * @return Set of Accounts with read permission for the given Entry.
     * @throws ManagerException
     * @throws PermissionException
     */
    public static Set<Account> getReadUser(Entry entry) throws ManagerException,
            PermissionException {
        if (PermissionManager.hasReadPermission(entry.getId())) {
            return PermissionManager.getReadUser(entry);
        } else {
            throw new PermissionException("Read Permission not permitted");
        }
    }

    /**
     * Retrieve {@link Account}s with write permissions set for the specified {@link Entry}.
     * 
     * @param entry
     *            - entry to query on.
     * @return Set of Accounts.
     * @throws ManagerException
     * @throws PermissionException
     */
    public static Set<Account> getWriteUser(Entry entry) throws ManagerException,
            PermissionException {
        if (PermissionManager.hasReadPermission(entry.getId())) {
            return PermissionManager.getWriteUser(entry);
        } else {
            throw new PermissionException("Read Permission not permitted");
        }
    }

    /**
     * Retrieve {@link Group}s with read permissions set for the specified {@link Entry}.
     * 
     * @param entry
     *            - Entry to query on.
     * @return Set of Groups.
     * @throws ManagerException
     * @throws PermissionException
     */
    public static Set<Group> getReadGroup(Entry entry) throws ManagerException, PermissionException {
        if (PermissionManager.hasReadPermission(entry.getId())) {
            return PermissionManager.getReadGroup(entry);
        } else {
            throw new PermissionException("Read Permission not permitted");
        }
    }

    /**
     * Retrieve {@link Group}s with write permissions set for the specified {@link Entry}.
     * 
     * @param entry
     *            - Entry to query on.
     * @return Set of Groups.
     * @throws ManagerException
     * @throws PermissionException
     */
    public static Set<Group> getWriteGroup(Entry entry) throws ManagerException,
            PermissionException {
        if (PermissionManager.hasReadPermission(entry.getId())) {
            return PermissionManager.getWriteGroup(entry);
        } else {
            throw new PermissionException("Read Permission not permitted");
        }
    }

    /**
     * Set read permissions for specified user {@link Account}s to the given {@link Entry}.
     * <p>
     * This method creates new {@link ReadUser} objects using the given {@link Account}s.
     * 
     * @param entry
     *            - Entry to give read permission to.
     * @param accounts
     *            - Accounts to give read permission to.
     * @throws ManagerException
     * @throws PermissionException
     */
    public static void setReadUser(Entry entry, Set<Account> accounts) throws ManagerException,
            PermissionException {
        if (PermissionManager.hasWritePermission(entry.getId())) {
            PermissionManager.setReadUser(entry, accounts);
        } else {
            throw new PermissionException("Write Permission not permitted");
        }
    }

    /**
     * Set write permissions for specified user {@link Account}s to the given {@link Entry}.
     * <p>
     * This method creates new {@link WriteUser} objects using the given {@link Account}s.
     * 
     * @param entry
     *            - Entry to give permission to.
     * @param accounts
     *            - Accounts to give write permission to.
     * @throws ManagerException
     * @throws PermissionException
     */
    public static void setWriteUser(Entry entry, Set<Account> accounts) throws ManagerException,
            PermissionException {
        if (PermissionManager.hasWritePermission(entry.getId())) {
            PermissionManager.setWriteUser(entry, accounts);
        } else {
            throw new PermissionException("Write Permission not permitted");
        }
    }

    /**
     * Set read permissions for specified user {@link Group}s to the given {@link Entry}.
     * <p>
     * This method creates new {@link ReadGroup} objects using the given {@link Group}s.
     * 
     * @param entry
     *            - Entry to give permission to.
     * @param groups
     *            - Groups to give read permission to.
     * @throws ManagerException
     * @throws PermissionException
     */
    public static void setReadGroup(Entry entry, Set<Group> groups) throws ManagerException,
            PermissionException {
        if (PermissionManager.hasWritePermission(entry.getId())) {
            PermissionManager.setReadGroup(entry, groups);
        } else {
            throw new PermissionException("Write Permission not permitted");
        }
    }

    /**
     * Set write permissions for specified user {@link Group}s to the given {@link Entry}.
     * <p>
     * This method creates new {@link WriteGroup} objects using the given {@link Group}s.
     * 
     * @param entry
     *            - Entry to give permission to.
     * @param groups
     *            - Groups to give write permission to.
     * @throws ManagerException
     * @throws PermissionException
     */
    public static void setWriteGroup(Entry entry, Set<Group> groups) throws ManagerException,
            PermissionException {
        if (PermissionManager.hasWritePermission(entry.getId())) {
            PermissionManager.setWriteGroup(entry, groups);
        } else {
            throw new PermissionException("Write Permission not permitted");
        }
    }
}

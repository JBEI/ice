package org.jbei.ice.lib.permissions;

import java.util.Set;

import org.jbei.ice.lib.managers.ManagerException;
import org.jbei.ice.lib.models.Account;
import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.lib.models.Group;

public class AuthenticatedPermissionManager {
    public static Set<Account> getReadUser(Entry entry, String sessionKey) throws ManagerException,
            PermissionException {
        if (PermissionManager.hasReadPermission(entry.getId(), sessionKey)) {
            return PermissionManager.getReadUser(entry);
        } else {
            throw new PermissionException("Read Permission not permitted");
        }
    }

    public static Set<Account> getWriteUser(Entry entry, String sessionKey)
            throws ManagerException, PermissionException {
        if (PermissionManager.hasReadPermission(entry.getId(), sessionKey)) {
            return PermissionManager.getWriteUser(entry);
        } else {
            throw new PermissionException("Read Permission not permitted");
        }
    }

    public static Set<Group> getReadGroup(Entry entry, String sessionKey) throws ManagerException,
            PermissionException {
        if (PermissionManager.hasReadPermission(entry.getId(), sessionKey)) {
            return PermissionManager.getReadGroup(entry);
        } else {
            throw new PermissionException("Read Permission not permitted");
        }
    }

    public static Set<Group> getWriteGroup(Entry entry, String sessionKey) throws ManagerException,
            PermissionException {
        if (PermissionManager.hasReadPermission(entry.getId(), sessionKey)) {
            return PermissionManager.getWriteGroup(entry);
        } else {
            throw new PermissionException("Read Permission not permitted");
        }
    }

    public static void setReadUser(Entry entry, Set<Account> accounts, String sessionKey)
            throws ManagerException, PermissionException {
        if (PermissionManager.hasWritePermission(entry.getId(), sessionKey)) {
            PermissionManager.setReadUser(entry, accounts);
        } else {
            throw new PermissionException("Write Permission not permitted");
        }
    }

    public static void setWriteUser(Entry entry, Set<Account> accounts, String sessionKey)
            throws ManagerException, PermissionException {
        if (PermissionManager.hasWritePermission(entry.getId(), sessionKey)) {
            PermissionManager.setWriteUser(entry, accounts);
        } else {
            throw new PermissionException("Write Permission not permitted");
        }
    }

    public static void setReadGroup(Entry entry, Set<Group> groups, String sessionKey)
            throws ManagerException, PermissionException {
        if (PermissionManager.hasWritePermission(entry.getId(), sessionKey)) {
            PermissionManager.setReadGroup(entry, groups);
        } else {
            throw new PermissionException("Write Permission not permitted");
        }
    }

    public static void setWriteGroup(Entry entry, Set<Group> groups, String sessionKey)
            throws ManagerException, PermissionException {
        if (PermissionManager.hasWritePermission(entry.getId(), sessionKey)) {
            PermissionManager.setWriteGroup(entry, groups);
        } else {
            throw new PermissionException("Write Permission not permitted");
        }
    }
}

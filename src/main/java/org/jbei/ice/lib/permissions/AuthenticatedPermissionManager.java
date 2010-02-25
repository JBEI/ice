package org.jbei.ice.lib.permissions;

import java.util.Set;

import org.jbei.ice.lib.managers.ManagerException;
import org.jbei.ice.lib.models.Account;
import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.lib.models.Group;

public class AuthenticatedPermissionManager {
    public static Set<Account> getReadUser(Entry entry) throws ManagerException,
            PermissionException {
        if (PermissionManager.hasReadPermission(entry.getId())) {
            return PermissionManager.getReadUser(entry);
        } else {
            throw new PermissionException("Read Permission not permitted");
        }
    }

    public static Set<Account> getWriteUser(Entry entry) throws ManagerException,
            PermissionException {
        if (PermissionManager.hasReadPermission(entry.getId())) {
            return PermissionManager.getWriteUser(entry);
        } else {
            throw new PermissionException("Read Permission not permitted");
        }
    }

    public static Set<Group> getReadGroup(Entry entry) throws ManagerException, PermissionException {
        if (PermissionManager.hasReadPermission(entry.getId())) {
            return PermissionManager.getReadGroup(entry);
        } else {
            throw new PermissionException("Read Permission not permitted");
        }
    }

    public static Set<Group> getWriteGroup(Entry entry) throws ManagerException,
            PermissionException {
        if (PermissionManager.hasReadPermission(entry.getId())) {
            return PermissionManager.getWriteGroup(entry);
        } else {
            throw new PermissionException("Read Permission not permitted");
        }
    }

    public static void setReadUser(Entry entry, Set<Account> accounts) throws ManagerException,
            PermissionException {
        if (PermissionManager.hasWritePermission(entry.getId())) {
            PermissionManager.setReadUser(entry, accounts);
        } else {
            throw new PermissionException("Write Permission not permitted");
        }
    }

    public static void setWriteUser(Entry entry, Set<Account> accounts) throws ManagerException,
            PermissionException {
        if (PermissionManager.hasWritePermission(entry.getId())) {
            PermissionManager.setWriteUser(entry, accounts);
        } else {
            throw new PermissionException("Write Permission not permitted");
        }
    }

    public static void setReadGroup(Entry entry, Set<Group> groups) throws ManagerException,
            PermissionException {
        if (PermissionManager.hasWritePermission(entry.getId())) {
            PermissionManager.setReadGroup(entry, groups);
        } else {
            throw new PermissionException("Write Permission not permitted");
        }
    }

    public static void setWriteGroup(Entry entry, Set<Group> groups) throws ManagerException,
            PermissionException {
        if (PermissionManager.hasWritePermission(entry.getId())) {
            PermissionManager.setWriteGroup(entry, groups);
        } else {
            throw new PermissionException("Write Permission not permitted");
        }
    }
}

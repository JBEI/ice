package org.jbei.ice.lib.permissions;

import java.util.Set;

import org.jbei.ice.controllers.common.Controller;
import org.jbei.ice.controllers.permissionVerifiers.EntryPermissionVerifier;
import org.jbei.ice.lib.managers.ManagerException;
import org.jbei.ice.lib.models.Account;
import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.lib.models.Group;

public class PermissionsController extends Controller {

    public PermissionsController(Account account) {
        super(account, new EntryPermissionVerifier());
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
     * @throws PermissionException
     */
    public void setReadGroup(Entry entry, Set<Group> groups) throws ManagerException,
            PermissionException {
        if (PermissionManager.hasWritePermission(entry.getId(), super.getAccount())) {
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
     *            Entry to give permission to.
     * @param groups
     *            Groups to give write permission to.
     * @throws ManagerException
     * @throws PermissionException
     */
    public void setWriteGroup(Entry entry, Set<Group> groups) throws ManagerException,
            PermissionException {
        if (PermissionManager.hasWritePermission(entry.getId(), super.getAccount())) {
            PermissionManager.setWriteGroup(entry, groups);
        } else {
            throw new PermissionException("Write Permission not permitted");
        }
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
     * @throws PermissionException
     */
    public void setReadUser(Entry entry, Set<Account> accounts) throws ManagerException,
            PermissionException {
        if (PermissionManager.hasWritePermission(entry.getId(), super.getAccount())) {
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
     *            Entry to give permission to.
     * @param accounts
     *            Accounts to give write permission to.
     * @throws ManagerException
     * @throws PermissionException
     */
    public void setWriteUser(Entry entry, Set<Account> accounts) throws ManagerException,
            PermissionException {
        if (PermissionManager.hasWritePermission(entry.getId(), super.getAccount())) {
            PermissionManager.setWriteUser(entry, accounts);
        } else {
            throw new PermissionException("Write Permission not permitted");
        }
    }
}

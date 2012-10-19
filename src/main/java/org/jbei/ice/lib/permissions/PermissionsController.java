package org.jbei.ice.lib.permissions;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.lib.account.AccountController;
import org.jbei.ice.lib.account.model.Account;
import org.jbei.ice.lib.dao.DAOException;
import org.jbei.ice.lib.entry.model.Entry;
import org.jbei.ice.lib.group.Group;
import org.jbei.ice.lib.group.GroupController;
import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.shared.dto.permission.PermissionInfo;
import org.jbei.ice.shared.dto.permission.PermissionInfo.PermissionType;

public class PermissionsController {

    private final AccountController accountController;
    private final GroupController groupController;
    private final PermissionDAO dao;

    public PermissionsController() {
        accountController = new AccountController();
        groupController = new GroupController();
        dao = new PermissionDAO();
    }

    /**
     * Adds permission to a specific entry
     *
     * @param account user making request
     * @param type    type of permission being added
     * @param entry   entry permission is being added to
     * @param id      unique identifier for account or group that is to be granted permissions.
     * @throws ControllerException
     */
    public void addPermission(Account account, PermissionType type, Entry entry, long id) throws ControllerException {
        try {
            switch (type) {
                case READ_ACCOUNT:
                    Account readAccount = accountController.get(id);
                    if (readAccount != null && !readAccount.getEmail().equals(account.getEmail()))
                        addReadUser(account, entry, readAccount);
                    break;

                case READ_GROUP:
                    Group readGroup = groupController.getGroupById(id);
                    if (readGroup != null)
                        addReadGroup(account, entry, readGroup);
                    break;

                case WRITE_ACCOUNT:
                    Account writeAccount = accountController.get(id);
                    if (writeAccount != null && !writeAccount.getEmail().equals(account.getEmail())) {
                        addWriteUser(account, entry, writeAccount);
                    }
                    break;

                case WRITE_GROUP:
                    Group writeGroup = groupController.getGroupById(id);
                    if (writeGroup != null) {
                        addWriteGroup(account, entry, writeGroup);
                    }
                    break;
            }
        } catch (PermissionException pe) {
            Logger.error(pe);
            throw new ControllerException("User " + account.getEmail()
                                                  + " does not have permissions to modify entry permissions for "
                                                  + entry.getId());
        }
    }

    /**
     * Removes a type of permission from an entry
     *
     * @param type  type of permission to remove
     * @param entry entry
     * @param id    identifier for group or account being removed
     * @throws ControllerException
     */
    public void removePermission(Account account, PermissionType type, Entry entry, long id)
            throws ControllerException {

        try {

            switch (type) {
                case READ_ACCOUNT:
                    Account readAccount = accountController.get(id);
                    // cannot remove yourself
                    if (readAccount != null && !readAccount.getEmail().equals(account.getEmail()))
                        removeReadUser(account, entry, readAccount);
                    break;

                case READ_GROUP:
                    Group readGroup = groupController.getGroupById(id);
                    if (readGroup != null)
                        removeReadGroup(account, entry, readGroup);
                    break;

                case WRITE_ACCOUNT:
                    Account writeAccount = accountController.get(id);
                    // cannot remove yourself
                    if (writeAccount != null && !writeAccount.getEmail().equals(account.getEmail())) {
                        removeWriteUser(account, entry, writeAccount);
                    }
                    break;

                case WRITE_GROUP:
                    Group writeGroup = groupController.getGroupById(id);
                    if (writeGroup != null) {
                        removeWriteGroup(account, entry, writeGroup);
                    }
                    break;
            }
        } catch (PermissionException pe) {
            Logger.error(pe);
            throw new ControllerException("User " + account.getEmail()
                                                  + " does not have permissions to modify entry permissions for "
                                                  + entry.getId());
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
     * @throws ControllerException
     * @throws PermissionException
     */
    public void setReadGroup(Account account, Entry entry, Set<Group> groups) throws ControllerException,
            PermissionException {
        if (hasWritePermission(account, entry)) {
            try {
                dao.addGroupPermission(entry, groups, false, true);
            } catch (DAOException e) {
                throw new ControllerException(e);
            }
        } else {
            throw new PermissionException("Write Permission not permitted");
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
     * @throws ControllerException
     * @throws PermissionException
     */
    public void setWriteGroup(Account account, Entry entry, Set<Group> groups) throws ControllerException,
            PermissionException {
        if (hasWritePermission(account, entry)) {
            try {
                dao.addGroupPermission(entry, groups, true, false);
            } catch (DAOException e) {
                throw new ControllerException(e);
            }
        } else {
            throw new PermissionException("Write Permission not permitted");
        }
    }

    /**
     * Set read permissions for specified user {@link Account}s to the given {@link Entry}.
     * <p/>
     * This method creates new {@link org.jbei.ice.lib.permissions.model.ReadUser} objects using the given {@link
     * Account}s.
     *
     * @param entry    Entry to give read permission to.
     * @param accounts Accounts to give read permission to.
     * @throws ControllerException
     * @throws PermissionException
     */
    public void setReadUser(Account account, Entry entry, Set<Account> accounts) throws ControllerException,
            PermissionException {
        if (hasWritePermission(account, entry)) {
            try {
                dao.addAccountPermission(entry, accounts, false, true);
            } catch (DAOException e) {
                throw new ControllerException(e);
            }
        } else {
            throw new PermissionException("Write Permission not permitted");
        }
    }

    protected void addReadUser(Account requestingAccount, Entry entry, Account account) throws ControllerException,
            PermissionException {
        if (hasWritePermission(requestingAccount, entry)) {
            Set<Account> accounts = new HashSet<Account>();
            try {
                if (dao.hasAccountPermission(entry, account, false, true))
                    return;
            } catch (DAOException e) {
                throw new ControllerException(e);
            }

            accounts.add(account);
            setReadUser(requestingAccount, entry, accounts);
        } else {
            throw new PermissionException("User " + requestingAccount.getEmail()
                                                  + " does not have write permissions for entry " + entry.getId());
        }
    }

    protected void removeReadUser(Account requestingAccount, Entry entry, Account account) throws ControllerException,
            PermissionException {
        if (hasWritePermission(requestingAccount, entry)) {
            try {
                HashSet<Account> accounts = new HashSet<Account>();
                accounts.add(account);
                dao.removeAccountPermission(entry, accounts, false, true);
            } catch (DAOException e) {
                throw new ControllerException(e);
            }
        } else {
            throw new PermissionException("User " + requestingAccount.getEmail()
                                                  + " does not have write permissions for entry " + entry.getId());
        }
    }

    public void addReadGroup(Account account, Entry entry, Group readGroup) throws ControllerException,
            PermissionException {
        if (hasWritePermission(account, entry)) {
            Set<Group> groups = new HashSet<Group>();
            groups.add(readGroup);

            try {
                if (dao.hasGroupPermission(entry, groups, false, true))
                    return;
            } catch (DAOException e) {
                throw new ControllerException(e);
            }

            setReadGroup(account, entry, groups);
        } else {
            throw new PermissionException("Write Permission not permitted");
        }
    }

    protected void removeReadGroup(Account requestingAccount, Entry entry, Group readGroup) throws ControllerException,
            PermissionException {
        if (hasWritePermission(requestingAccount, entry)) {
            try {
                Set<Group> groups = new HashSet<Group>();
                groups.add(readGroup);
                dao.removeGroupPermission(entry, groups, false, true);
            } catch (DAOException e) {
                throw new ControllerException(e);
            }
        } else {
            throw new PermissionException("Write Permission not permitted");
        }
    }

    protected void addWriteUser(Account requestingAccount, Entry entry, Account account) throws ControllerException,
            PermissionException {
        if (hasWritePermission(requestingAccount, entry)) {
            try {
                Set<Account> accounts = new HashSet<Account>();
                accounts.add(account);
                dao.addAccountPermission(entry, accounts, true, false);
            } catch (DAOException e) {
                throw new ControllerException(e);
            }
        } else {
            throw new PermissionException("Write Permission not permitted");
        }
    }

    protected void removeWriteUser(Account requestingAccount, Entry entry, Account account) throws ControllerException,
            PermissionException {
        if (hasWritePermission(requestingAccount, entry)) {
            try {
                Set<Account> accounts = new HashSet<Account>();
                accounts.add(account);
                dao.removeAccountPermission(entry, accounts, true, false);
            } catch (DAOException e) {
                throw new ControllerException(e);
            }
        } else {
            throw new PermissionException("Write Permission not permitted");
        }
    }

    protected void removeWriteGroup(Account account, Entry entry, Group writeGroup) throws ControllerException,
            PermissionException {
        if (hasWritePermission(account, entry)) {
            try {
                Set<Group> groups = new HashSet<Group>();
                groups.add(writeGroup);
                dao.removeGroupPermission(entry, groups, true, false);
            } catch (DAOException e) {
                throw new ControllerException(e);
            }
        } else {
            throw new PermissionException("Write Permission not permitted");
        }
    }

    protected void addWriteGroup(Account account, Entry entry, Group writeGroup) throws ControllerException,
            PermissionException {
        if (hasWritePermission(account, entry)) {
            try {
                Set<Group> groups = new HashSet<Group>();
                groups.add(writeGroup);

                if (dao.hasGroupPermission(entry, groups, true, false))
                    return;

                dao.addGroupPermission(entry, groups, true, false);
            } catch (DAOException e) {
                throw new ControllerException(e);
            }
        } else {
            throw new PermissionException("Write Permission not permitted");
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
     * @throws ControllerException
     * @throws PermissionException
     */
    public void setWriteUser(Account account, Entry entry, Set<Account> accounts) throws ControllerException,
            PermissionException {
        if (hasWritePermission(account, entry)) {
            try {
                dao.addAccountPermission(entry, accounts, true, false);
            } catch (DAOException e) {
                throw new ControllerException(e);
            }
        } else {
            throw new PermissionException("Write Permission not permitted");
        }
    }

    protected boolean accountHasReadPermission(Account account, Entry entry) throws ControllerException {
        try {
            return dao.hasAccountPermission(entry, account, false, true);
        } catch (DAOException dao) {
            Logger.error(dao);
        }
        return false;
    }

    protected boolean accountHasWritePermission(Account account, Entry entry) throws ControllerException {
        try {
            return dao.hasAccountPermission(entry, account, true, false);
        } catch (DAOException dao) {
            Logger.error(dao);
        }
        return false;
    }

    // checks if at least one member of the group has read permission
    public boolean groupHasWritePermission(Set<Group> groups, Entry entry) throws ControllerException {
        try {
            if (groups.isEmpty())
                return false;

//            for (Group group : groups) {
            if (dao.hasGroupPermission(entry, groups, true, false))
                return true;
//            }
        } catch (DAOException dao) {
            Logger.error(dao);
        }
        return false;
    }

    public boolean groupHasReadPermission(Set<Group> groups, Entry entry) throws ControllerException {
        try {
            if (groups.isEmpty())
                return false;

//            for (Group group : groups) {
            if (dao.hasGroupPermission(entry, groups, false, true))
                return true;
//            }

            return groupHasWritePermission(groups, entry);
        } catch (DAOException dao) {
            Logger.error(dao);
            return false;
        }
    }

    /**
     * Checks if an account has read permissions for an entry
     *
     * @param account user account
     * @param entry   existing entry
     * @return true if account has read privileges either explicitly or implicitly through group membership
     *         false otherwise
     * @throws ControllerException
     */
    public boolean hasReadPermission(Account account, Entry entry) throws ControllerException {

        // check if account is a moderator account of is owner of the entry
        if (isOwnerOrAdministrator(account, entry))
            return true;

        // check explicit read permission
        if (accountHasReadPermission(account, entry))
            return true;

        // check explicit write permission (write permission confers read permission)
        if (accountHasWritePermission(account, entry))
            return true;

        // get groups for account
        Set<Group> accountGroups = groupController.getAllGroups(account);

        // check read permission through group membership
        // ie. belongs to group that has read privileges for entry (or a group whose parent group does)
        if (groupHasReadPermission(accountGroups, entry))
            return true;

        // check write permission through group membership
        // ie. belongs to group that has write privileges for entry
        return groupHasWritePermission(accountGroups, entry);
    }

    public boolean hasWritePermission(Account account, Entry entry) throws ControllerException {
        if (isOwnerOrAdministrator(account, entry))
            return true;

        // check write accounts for entry
        if (this.accountHasWritePermission(account, entry))
            return true;

        // get groups for account
        Set<Group> accountGroups = groupController.getAllGroups(account);

        // check group permissions
        return groupHasWritePermission(accountGroups, entry);
    }

    /**
     * checks if the account is the owner of the entry or if account is a
     * moderator account
     *
     * @param account user account
     * @param entry   entry
     * @return true if account is owner of entry or is a moderator, false otherwise
     * @throws ControllerException
     */
    protected boolean isOwnerOrAdministrator(Account account, Entry entry) throws ControllerException {
        // first check entry ownership
        if (account.getEmail().equals(entry.getOwnerEmail()))
            return true;

        // then check if moderator
        AccountController controller = new AccountController();
        if (controller.isAdministrator(account)) {
            return true;
        }

        // TODO : adding system account also but this needs to be handled in a better way
        Account systemAccount = controller.getSystemAccount();
        return (systemAccount != null && systemAccount.equals(account));
    }

    /**
     * retrieves permissions that have been explicitly set for entry
     *
     * @param account user making request
     * @param entry   entry whose permissions are being checked
     * @return list of set permissions
     */
    public ArrayList<PermissionInfo> retrieveSetEntryPermissions(Account account, Entry entry)
            throws ControllerException, PermissionException {

        if (!hasReadPermission(account, entry))
            throw new PermissionException(account.getEmail() + ": does not have read permissions for entry \""
                                                  + entry.getRecordId() + "\"");

        ArrayList<PermissionInfo> permissionInfos = new ArrayList<PermissionInfo>();

        try {
            // read accounts
            Set<Account> readAccounts = dao.retrieveAccountPermissions(entry, false, true);
            for (Account readAccount : readAccounts) {
                permissionInfos.add(new PermissionInfo(PermissionInfo.PermissionType.READ_ACCOUNT,
                                                       readAccount.getId(), readAccount.getFullName()));
            }

            // write accounts
            Set<Account> writeAccounts = dao.retrieveAccountPermissions(entry, true, false);
            for (Account writeAccount : writeAccounts) {
                permissionInfos.add(new PermissionInfo(PermissionInfo.PermissionType.WRITE_ACCOUNT,
                                                       writeAccount.getId(), writeAccount.getFullName()));
            }

            // read groups
            Set<Group> readGroups = dao.retrieveGroupPermissions(entry, false, true);
            for (Group group : readGroups) {
                permissionInfos.add(new PermissionInfo(PermissionInfo.PermissionType.READ_GROUP, group
                        .getId(), group.getLabel()));
            }

            // write groups
            Set<Group> writeGroups = dao.retrieveGroupPermissions(entry, true, false);
            for (Group group : writeGroups) {
                permissionInfos.add(new PermissionInfo(PermissionInfo.PermissionType.WRITE_GROUP, group
                        .getId(), group.getLabel()));
            }
        } catch (DAOException de) {
            throw new ControllerException(de);
        }

        return permissionInfos;
    }

    public void upgradePermissions() throws ControllerException {
        try {
            dao.upgradePermissions();
        } catch (DAOException e) {
            Logger.error(e);
        }
    }
}

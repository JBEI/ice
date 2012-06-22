package org.jbei.ice.lib.permissions;

import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.lib.account.AccountController;
import org.jbei.ice.lib.account.model.Account;
import org.jbei.ice.lib.dao.DAOException;
import org.jbei.ice.lib.entry.model.Entry;
import org.jbei.ice.lib.group.GroupController;
import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.lib.models.Group;
import org.jbei.ice.shared.dto.permission.PermissionInfo.PermissionType;

import java.util.List;
import java.util.Set;

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
     * @param type  type of permission being added
     * @param entry entry permission is being added to
     * @param id    unique identifier for account or group that is to be granted permissions.
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
                        addReadUser(account, entry, writeAccount);
                    }
                    break;

                case WRITE_GROUP:
                    Group writeGroup = groupController.getGroupById(id);
                    if (writeGroup != null) {
                        addWriteGroup(account, entry, writeGroup);
                        addReadGroup(account, entry, writeGroup);
                    }
                    break;
            }
        } catch (PermissionException pe) {
            Logger.error(pe);
            throw new ControllerException("User " + account.getEmail()
                                                  + " does not have permissions to modify entry permissions for " +
                                                  entry
                                                          .getId());
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
                    if (readAccount != null && !readAccount.getEmail().equals(
                            account.getEmail())) // cannot remove yourself
                        removeReadUser(account, entry, readAccount);
                    break;

                case READ_GROUP:
                    Group readGroup = groupController.getGroupById(id);
                    if (readGroup != null)
                        removeReadGroup(account, entry, readGroup);
                    break;

                case WRITE_ACCOUNT:
                    Account writeAccount = accountController.get(id);
                    if (writeAccount != null && !writeAccount.getEmail().equals(
                            account.getEmail())) { // cannot remove yourself
                        removeWriteUser(account, entry, writeAccount);
                        removeReadUser(account, entry, writeAccount);
                    }
                    break;

                case WRITE_GROUP:
                    Group writeGroup = groupController.getGroupById(id);
                    if (writeGroup != null) {
                        removeWriteGroup(account, entry, writeGroup);
                        removeReadGroup(account, entry, writeGroup);
                    }
                    break;
            }
        } catch (PermissionException pe) {
            Logger.error(pe);
            throw new ControllerException("User " + account.getEmail()
                                                  + " does not have permissions to modify entry permissions for " +
                                                  entry
                                                          .getId());
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
                dao.setReadGroup(entry, groups);
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
                dao.setWriteGroup(entry, groups);
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
                dao.setReadUser(entry, accounts);
            } catch (DAOException e) {
                throw new ControllerException(e);
            }
        } else {
            throw new PermissionException("Write Permission not permitted");
        }
    }

    public void addReadUser(Account requestingAccount, Entry entry, Account account) throws ControllerException,
            PermissionException {
        if (hasWritePermission(requestingAccount, entry)) {
            try {
                dao.addReadUser(entry, account);
            } catch (DAOException e) {
                throw new ControllerException(e);
            }
        } else {
            throw new PermissionException("User " + requestingAccount.getEmail()
                                                  + " does not have write permissions for entry " + entry.getId());
        }
    }

    public void removeReadUser(Account requestingAccount, Entry entry, Account account) throws ControllerException,
            PermissionException {
        if (hasWritePermission(requestingAccount, entry)) {
            try {
                dao.removeReadUser(entry, account);
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
            try {
                dao.addReadGroup(entry, readGroup);
            } catch (DAOException e) {
                throw new ControllerException(e);
            }
        } else {
            throw new PermissionException("Write Permission not permitted");
        }
    }

    public void removeReadGroup(Account requestingAccount, Entry entry, Group readGroup) throws ControllerException,
            PermissionException {
        if (hasWritePermission(requestingAccount, entry)) {
            try {
                dao.removeReadGroup(entry, readGroup);
            } catch (DAOException e) {
                throw new ControllerException(e);
            }
        } else {
            throw new PermissionException("Write Permission not permitted");
        }
    }

    public void addWriteUser(Account requestingAccount, Entry entry, Account account) throws ControllerException,
            PermissionException {
        if (hasWritePermission(requestingAccount, entry)) {
            try {
                dao.addWriteUser(entry, account);
            } catch (DAOException e) {
                throw new ControllerException(e);
            }
        } else {
            throw new PermissionException("Write Permission not permitted");
        }
    }

    public void removeWriteUser(Account requestingAccount, Entry entry, Account account) throws ControllerException,
            PermissionException {
        if (hasWritePermission(requestingAccount, entry)) {
            try {
                dao.removeWriteUser(entry, account);
            } catch (DAOException e) {
                throw new ControllerException(e);
            }
        } else {
            throw new PermissionException("Write Permission not permitted");
        }
    }

    public void removeWriteGroup(Account account, Entry entry, Group writeGroup) throws ControllerException,
            PermissionException {
        if (hasWritePermission(account, entry)) {
            try {
                dao.removeWriteGroup(entry, writeGroup);
            } catch (DAOException e) {
                throw new ControllerException(e);
            }
        } else {
            throw new PermissionException("Write Permission not permitted");
        }
    }

    public void addWriteGroup(Account account, Entry entry, Group writeGroup) throws ControllerException,
            PermissionException {
        if (hasWritePermission(account, entry)) {
            try {
                dao.addWriteGroup(entry, writeGroup);
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
                dao.setWriteUser(entry, accounts);
            } catch (DAOException e) {
                throw new ControllerException(e);
            }
        } else {
            throw new PermissionException("Write Permission not permitted");
        }
    }

    public boolean hasReadPermission(Account account, Entry entry) throws ControllerException {

        if (isOwnerOrModerator(account, entry))
            return true;

        try {
            // check read accounts for entry
            List<Long> accountIds = dao.getEntryReadAccounts(entry);
            if (accountIds.contains(account.getId()))
                return true;

            // check group accounts
            return dao.groupHasReadPermission(entry, account);
        } catch (DAOException e) {
            throw new ControllerException(e);
        }
    }

    public boolean hasWritePermission(Account account, Entry entry) throws ControllerException {
        if (isOwnerOrModerator(account, entry))
            return true;

        try {
            // check write accounts for entry
            List<Long> accountIds = dao.getEntryWriteAccounts(entry);
            if (accountIds.contains(account.getId()))
                return true;

            // check group accounts
            return dao.groupHasWritePermission(entry, account);
        } catch (DAOException e) {
            throw new ControllerException(e);
        }
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
    protected boolean isOwnerOrModerator(Account account, Entry entry) throws ControllerException {
        // first check entry ownership
        if (account.getEmail().equals(entry.getOwner()))
            return true;

        // then check if moderator
        AccountController controller = new AccountController();
        if (controller.isModerator(account)) {
            return true;
        }

        // TODO : adding system account also but this needs to be handled in a better way
        return (controller.getSystemAccount().equals(account));
    }

    /**
     * Check if the {@link Account} associated with the given sessionKey has read permission to the
     * given {@link Entry} by recordId.
     *
     * @param recordId   recordId of the Entry.
     * @param sessionKey session key.
     * @return True if the given session account has read permission to the specified entry.
     */
//    public boolean hasReadPermission(String recordId, String sessionKey) throws ControllerException {
//        boolean result = false;
//
//        Account account = null;
//        AccountController controller = new AccountController();
//
//        account = controller.getAccountByAuthToken(sessionKey);
//
//        if (account != null) {
//            result = hasReadPermission(recordId, account)
//                    || groupHasReadPermission(recordId, account);
//        }
//
//        return result;
//    }

    /**
     * Check if the {@link Account} has read permission to the specified {@link Entry} by entryId.
     *
     * @param recordId recordId of the Entry.
     * @param account  Account
     * @return True if the given Account has read permission to the specified Entry.
     */
//    public boolean userHasReadPermission(String recordId, Account account) throws ControllerException {
//        boolean result = false;
//        AccountController controller = new AccountController();
//
//        if (recordId != null && !recordId.isEmpty() && account != null) {
//            if (controller.isModerator(account)) {
//                result = true;
//            } else {
//                result = groupHasReadPermission(recordId, account)
//                        || userHasReadPermission(recordId, account);
//            }
//        }
//
//        return result;
//    }

    /**
     * Check if the {@link Account} has read permission to the specified {@link Entry}.
     *
     * @param entryId id of the specified Entry.
     * @param account Account
     * @return True if given Account has read permission to the specified Entry.
     */
//    public boolean hasReadPermission(long entryId, Account account) {
//        boolean result = false;
//        AccountController controller = new AccountController();
//
//        if (entryId > 0 && account != null) {
//            try {
//                if (controller.isModerator(account)) {
//                    result = true;
//                } else {
//                    result = groupHasReadPermission(entryId, account)
//                            || userHasReadPermission(entryId, account);
//                }
//            } catch (ControllerException e) {
//                e.printStackTrace();
//            }
//        }
//
//        return result;
//    }

    /**
     * Checks if the current {@link Account} logged in has read permission to the given
     * {@link Entry}.
     *
     * @param entry Entry to be queried.
     * @return True if the current user has read permission.
     */

//    public boolean hasReadPermission(Entry entry, Account account) throws ControllerException {
//        boolean result = false;
//        if (entry != null && account != null) {
//            AccountController controller = new AccountController();
//
//            if (controller.isModerator(account)) {
//                result = true;
//            } else {
//                result = userHasReadPermission(entry, account)
//                        || groupHasReadPermission(entry, account);
//            }
//        }
//        return result;
//    }
    public Set<Account> getReadUser(Account account, Entry entry) throws ControllerException, PermissionException {
        try {
            if (!hasReadPermission(account, entry))
                throw new PermissionException(account.getEmail() + " does not have read permission for entry " +
                                                      entry.getRecordId());
            return dao.getReadUser(entry);
        } catch (DAOException e) {
            throw new ControllerException(e);
        }
    }

    public Set<Account> getWriteUser(Account account, Entry entry) throws ControllerException, PermissionException {
        try {
            if (!hasReadPermission(account, entry))
                throw new PermissionException(account.getEmail() + " does not have read permission for entry " +
                                                      entry.getRecordId());
            return dao.getWriteUser(entry);
        } catch (DAOException e) {
            throw new ControllerException(e);
        }
    }

    public Set<Group> getReadGroup(Account account, Entry entry) throws ControllerException, PermissionException {
        try {
            if (!hasReadPermission(account, entry))
                throw new PermissionException(account.getEmail() + " does not have read permission for entry " +
                                                      entry.getRecordId());
            return dao.getReadGroup(entry);
        } catch (DAOException e) {
            throw new ControllerException(e);
        }
    }

    public Set<Group> getWriteGroup(Account account, Entry entry) throws ControllerException, PermissionException {
        try {
            if (!hasReadPermission(account, entry))
                throw new PermissionException(account.getEmail() + " does not have read permission for entry " +
                                                      entry.getRecordId());
            return dao.getWriteGroup(entry);
        } catch (DAOException e) {
            throw new ControllerException(e);
        }
    }
}

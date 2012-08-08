package org.jbei.ice.lib.permissions;

import java.util.HashSet;
import java.util.Set;

import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.lib.account.AccountController;
import org.jbei.ice.lib.account.model.Account;
import org.jbei.ice.lib.dao.DAOException;
import org.jbei.ice.lib.entry.model.Entry;
import org.jbei.ice.lib.group.GroupController;
import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.lib.models.Group;
import org.jbei.ice.shared.dto.permission.PermissionInfo.PermissionType;

public class PermissionsController {

    private final AccountController accountController;
    private final GroupController groupController;
    private final ReadGroupDAO readGroupDAO;
    private final WriteGroupDAO writeGroupDAO;
    private final ReadUserDAO readUserDAO;
    private final WriteUserDAO writeUserDAO;
    private final PermissionDAO dao;

    public PermissionsController() {
        accountController = new AccountController();
        groupController = new GroupController();
        readGroupDAO = new ReadGroupDAO();
        writeGroupDAO = new WriteGroupDAO();
        readUserDAO = new ReadUserDAO();
        writeUserDAO = new WriteUserDAO();
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
                readGroupDAO.setReadGroup(entry, groups);
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
                writeGroupDAO.setWriteGroup(entry, groups);
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
                readUserDAO.setReadUser(entry, accounts);
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
            try {
                Set<Account> accounts = readUserDAO.getReadUser(entry);
                boolean alreadyAdded = false;
                for (Account oldAccount : accounts) {
                    if (oldAccount.getId() == account.getId()) {
                        alreadyAdded = true;
                        break;
                    }
                }
                if (alreadyAdded == false) {
                    accounts.add(account);
                    readUserDAO.setReadUser(entry, accounts);
                }
            } catch (DAOException e) {
                throw new ControllerException(e);
            }
        } else {
            throw new PermissionException("User " + requestingAccount.getEmail()
                                                  + " does not have write permissions for entry " + entry.getId());
        }
    }

    protected void removeReadUser(Account requestingAccount, Entry entry, Account account) throws ControllerException,
            PermissionException {
        if (hasWritePermission(requestingAccount, entry)) {
            try {
                readUserDAO.removeReadUser(entry, account);
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
                Set<Group> groups = readGroupDAO.getReadGroup(entry);
                boolean alreadyAdded = false;
                for (Group existingGroup : groups) {
                    if (existingGroup.getId() == readGroup.getId()) {
                        alreadyAdded = true;
                        break;
                    }
                }
                if (alreadyAdded == false) {
                    groups.add(readGroup);
                    readGroupDAO.setReadGroup(entry, groups);
                }
            } catch (DAOException e) {
                throw new ControllerException(e);
            }
        } else {
            throw new PermissionException("Write Permission not permitted");
        }
    }

    protected void removeReadGroup(Account requestingAccount, Entry entry, Group readGroup) throws ControllerException,
            PermissionException {
        if (hasWritePermission(requestingAccount, entry)) {
            try {
                readGroupDAO.removeReadGroup(entry, readGroup);
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
                writeUserDAO.addWriteUser(entry, account);
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
                writeUserDAO.removeWriteUser(entry, account);
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
                writeGroupDAO.removeWriteGroup(entry, writeGroup);
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
                Set<Group> groups = writeGroupDAO.getWriteGroup(entry);
                boolean alreadyAdded = false;
                for (Group existingGroup : groups) {
                    if (existingGroup.getId() == writeGroup.getId()) {
                        alreadyAdded = true;
                        break;
                    }
                }
                if (alreadyAdded == false) {
                    groups.add(writeGroup);
                    writeGroupDAO.setWriteGroup(entry, groups);
                }
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
                writeUserDAO.setWriteUser(entry, accounts);
            } catch (DAOException e) {
                throw new ControllerException(e);
            }
        } else {
            throw new PermissionException("Write Permission not permitted");
        }
    }

    protected boolean accountHasReadPermission(Account account, Entry entry) throws ControllerException {
        try {
            return dao.isReadUserAccount(account, entry);
        } catch (DAOException dao) {
            Logger.error(dao);
        }
        return false;
    }

    protected boolean accountHasWritePermission(Account account, Entry entry) throws ControllerException {
        try {
            return dao.isWriteUserAccount(account, entry);
        } catch (DAOException dao) {
            Logger.error(dao);
        }
        return false;
    }

    protected boolean groupHasWritePermission(Set<Group> groups, Entry entry) throws ControllerException {
//        try {
////            return dao.isWriteUserAccount(account, entry);
//        } catch (DAOException dao) {
//            Logger.error(dao);
//        }
        return false;
    }

    protected boolean groupHasReadPermission(Set<Group> groups, Entry entry) throws ControllerException {
//        try {
////            return dao.isWriteUserAccount(account, entry);
//        } catch (DAOException dao) {
//            Logger.error(dao);
//        }
        return false;
    }

    /**
     * Retrieve all parent {@link Group}s of a given {@link Account}.
     *
     * @param account Account to query on.
     * @return Set of Group ids.
     */
    protected Set<Long> getAllAccountGroups(Account account) throws DAOException {
        HashSet<Long> accountGroups = new HashSet<Long>();

        for (Group group : account.getGroups()) {
            accountGroups = getParentGroups(group, accountGroups);
        }

        // Everyone belongs to the everyone group
        GroupController controller = new GroupController();
        try {
            Group everybodyGroup = controller.createOrRetrievePublicGroup();
            accountGroups.add(everybodyGroup.getId());
        } catch (ControllerException e) {
            Logger.warn("could not get everybody group: " + e.toString());
        }
        return accountGroups;
    }

    /**
     * Retrieve all parent {@link Group}s of a given group.
     *
     * @param group    Group to query on.
     * @param groupIds optional set of group ids. Can be empty.
     * @return Set of Parent group ids.
     */
    protected HashSet<Long> getParentGroups(Group group, HashSet<Long> groupIds) throws DAOException {
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
        if (isOwnerOrModerator(account, entry))
            return true;

        // check explicit read permission
        if (accountHasReadPermission(account, entry))
            return true;

        // check explicit write permission (write permission confers read permission)
        if (accountHasWritePermission(account, entry))
            return true;

        // get groups for account
        Set<Group> accountGroups = groupController.getAllGroups(account.getGroups());

        // check read permission through group membership
        // ie. belongs to group that has read privileges for entry (or a group whose parent group does)
        if (groupHasReadPermission(accountGroups, entry))
            return true;

        // check write permission through group membership
        // ie. belongs to group that has write privileges for entry
        return groupHasWritePermission(accountGroups, entry);
    }

    public boolean hasWritePermission(Account account, Entry entry) throws ControllerException {
        if (isOwnerOrModerator(account, entry))
            return true;

        // check write accounts for entry
        if (this.accountHasWritePermission(account, entry))
            return true;

        // check group accounts
//            return dao.groupHasWritePermission(entry, account);

        return false;
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

    public Set<Account> getReadUser(Account account, Entry entry) throws ControllerException, PermissionException {
        try {
            if (!hasReadPermission(account, entry))
                throw new PermissionException(account.getEmail() + " does not have read permission for entry " +
                                                      entry.getRecordId());
            return readUserDAO.getReadUser(entry);
        } catch (DAOException e) {
            throw new ControllerException(e);
        }
    }

    public Set<Account> getWriteUser(Account account, Entry entry) throws ControllerException, PermissionException {
        try {
            if (!hasReadPermission(account, entry))
                throw new PermissionException(account.getEmail() + " does not have read permission for entry " +
                                                      entry.getRecordId());
            return writeUserDAO.getWriteUser(entry);
        } catch (DAOException e) {
            throw new ControllerException(e);
        }
    }

    public Set<Group> getReadGroup(Account account, Entry entry) throws ControllerException, PermissionException {
        try {
            if (!hasReadPermission(account, entry))
                throw new PermissionException(account.getEmail() + " does not have read permission for entry " +
                                                      entry.getRecordId());
            return readGroupDAO.getReadGroup(entry);
        } catch (DAOException e) {
            throw new ControllerException(e);
        }
    }

    public Set<Group> getWriteGroup(Account account, Entry entry) throws ControllerException, PermissionException {
        try {
            if (!hasReadPermission(account, entry))
                throw new PermissionException(account.getEmail() + " does not have read permission for entry " +
                                                      entry.getRecordId());
            return writeGroupDAO.getWriteGroup(entry);
        } catch (DAOException e) {
            throw new ControllerException(e);
        }
    }
}

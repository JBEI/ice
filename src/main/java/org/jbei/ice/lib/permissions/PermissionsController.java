package org.jbei.ice.lib.permissions;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.jbei.ice.controllers.ControllerFactory;
import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.lib.account.AccountController;
import org.jbei.ice.lib.account.model.Account;
import org.jbei.ice.lib.dao.DAOException;
import org.jbei.ice.lib.entry.EntryController;
import org.jbei.ice.lib.entry.model.Entry;
import org.jbei.ice.lib.folder.Folder;
import org.jbei.ice.lib.folder.FolderController;
import org.jbei.ice.lib.group.Group;
import org.jbei.ice.lib.group.GroupController;
import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.lib.permissions.model.Permission;
import org.jbei.ice.lib.shared.dto.permission.AccessPermission;

/**
 * Controller for permissions
 *
 * @author Hector Plahar
 */
public class PermissionsController {

    private final AccountController accountController;
    private final GroupController groupController;
    private final FolderController folderController;
    private final PermissionDAO dao;

    public PermissionsController() {
        accountController = ControllerFactory.getAccountController();
        groupController = ControllerFactory.getGroupController();
        folderController = ControllerFactory.getFolderController();
        dao = new PermissionDAO();
    }

    /**
     * Creates a new permission object for groups from the fields in the parameter.
     * Used mainly by bulk upload since permissions are set at the group level
     *
     * @param access information about the access permission
     * @return saved permission
     * @throws ControllerException
     */
    public Permission recordGroupPermission(AccessPermission access) throws ControllerException {
        try {
            Group group = groupController.getGroupById(access.getArticleId());
            if (group == null)
                throw new ControllerException("Could retrieve group for permission add");

            Permission permission = new Permission();
            permission.setGroup(group);
            permission.setCanRead(access.isCanRead());
            permission.setCanWrite(access.isCanWrite());
            return dao.save(permission);
        } catch (DAOException e) {
            Logger.error(e);
            throw new ControllerException(e);
        }
    }

    public Permission addPermission(Account requestingAccount, AccessPermission access) throws ControllerException {
        EntryController entryController = ControllerFactory.getEntryController();

        if (access.isEntry()) {
            Entry entry = entryController.get(requestingAccount, access.getTypeId());
            if (entry == null)
                throw new ControllerException("Cannot find entry " + access.getTypeId());

            // can user modify permissions for entry
            if (!hasWritePermission(requestingAccount, entry))
                throw new ControllerException(requestingAccount.getEmail() + " cannot " + access.toString());
            return addPermission(access, entry, null);
        }

        if (access.isFolder()) {
            Folder folder = folderController.getFolderById(access.getTypeId());
            if (!hasWritePermission(requestingAccount, folder)) {
                throw new ControllerException(
                        requestingAccount.getEmail() + " not allowed to add " + access.toString());
            }

            // propagate permissions
            if (folder.isPropagatePermissions()) {
                for (Entry folderContent : folder.getContents()) {
                    addPermission(access, folderContent, null);
                }
            }
            return addPermission(access, null, folder);
        }

        return null;
    }

    protected Permission addPermission(AccessPermission access, Entry entry, Folder folder) throws ControllerException {
        // account or group
        Account account = null;
        Group group = null;
        switch (access.getArticle()) {
            case ACCOUNT:
            default:
                account = accountController.get(access.getArticleId());
                break;

            case GROUP:
                group = groupController.getGroupById(access.getArticleId());
                break;
        }

        // does the permissions already exists
        try {
            if (dao.hasPermission(entry, folder, account, group, access.isCanRead(), access.isCanWrite())) {
                return dao.retrievePermission(entry, folder, account, group, access.isCanRead(), access.isCanWrite());
            }

            // add the permission if not
            Permission permission = new Permission();
            permission.setEntry(entry);
            if (entry != null)
                entry.getPermissions().add(permission);
            permission.setGroup(group);
            permission.setFolder(folder);
            permission.setAccount(account);
            permission.setCanRead(access.isCanRead());
            permission.setCanWrite(access.isCanWrite());
            return dao.save(permission);
        } catch (DAOException e) {
            Logger.error(e);
            throw new ControllerException(e);
        }
    }

    /**
     * Removes permissions that are associated with folder. This is typically for
     * folders that are shared as public folders
     *
     * @param account  user account. typically an administrator
     * @param folderId unique identifier for folder whose permissions are to be removed
     * @throws ControllerException
     */
    public void removeAllFolderPermissions(Account account, long folderId) throws ControllerException {
        Folder folder = folderController.getFolderById(folderId);
        if (folder == null) {
            Logger.warn("Could not find folder with id " + folderId + " for permission removal");
            return;
        }

        if (!hasWritePermission(account, folder))
            throw new ControllerException(account.getEmail() + " not allowed to modify folder " + folderId);

        try {
            dao.clearPermissions(folder);
        } catch (DAOException e) {
            throw new ControllerException(e);
        }
    }

    public void removePermission(Account requestingAccount, AccessPermission access) throws ControllerException {
        EntryController entryController = ControllerFactory.getEntryController();

        if (access.isEntry()) {
            Entry entry = entryController.get(requestingAccount, access.getTypeId());
            if (entry == null)
                throw new ControllerException("Cannot find entry " + access.getTypeId());

            // can user modify permissions for entry
            if (!hasWritePermission(requestingAccount, entry))
                throw new ControllerException(requestingAccount.getEmail() + " not allowed to " + access.toString());

            // remove permission from entry
            removePermission(access, entry, null);

        } else if (access.isFolder()) {
            Folder folder = folderController.getFolderById(access.getTypeId());
            if (!hasWritePermission(requestingAccount, folder))
                throw new ControllerException(requestingAccount.getEmail() + " not allowed to " + access.toString());

            // if folder is to be propagated, add removing permission from contained entries
            if (folder.isPropagatePermissions()) {
                for (Entry folderContent : folder.getContents()) {
                    removePermission(access, folderContent, null);
                }
            }
            // remove permission from folder
            removePermission(access, null, folder);
        }
    }

    private void removePermission(AccessPermission access, Entry entry, Folder folder) throws ControllerException {
        // account or group
        Account account = null;
        Group group = null;
        switch (access.getArticle()) {
            case ACCOUNT:
            default:
                account = accountController.get(access.getArticleId());
                break;

            case GROUP:
                group = groupController.getGroupById(access.getArticleId());
                break;
        }

        try {
            dao.removePermission(entry, folder, account, group, access.isCanRead(), access.isCanWrite());
        } catch (DAOException e) {
            Logger.error(e);
            throw new ControllerException(e);
        }
    }

    /**
     * Clears all permission that have been set for an entry including
     * those for the owner. This is intended to be used in logical deletes of entries
     *
     * @param account account for user making request; should have write privileges
     * @param entry   part whose permissions are being removed
     * @return number of permission that were removed
     * @throws ControllerException on exception clearing permissions
     * @throws PermissionException if account in parameter those not have the appropriate permissions
     */
    public int clearEntryPermissions(Account account, Entry entry) throws ControllerException, PermissionException {
        if (!hasWritePermission(account, entry)) {
            throw new PermissionException(account.getEmail() + " doesn't have write permission for entry "
                                                  + entry.getId());
        }

        try {
            return dao.clearPermissions(entry);
        } catch (DAOException e) {
            throw new ControllerException(e);
        }
    }

    /**
     * Removes all permissions associated with the specified folder
     *
     * @param account account for user making request. Should have write permissions for folder
     * @param folder  folder whose permissions are to be removed
     * @return number of permissions that were removed for the folder
     * @throws ControllerException
     * @throws PermissionException
     */
    public int clearFolderPermissions(Account account, Folder folder) throws ControllerException, PermissionException {
        if (!hasWritePermission(account, folder)) {
            throw new PermissionException(account.getEmail() + " doesn't have write permissions for folder "
                                                  + folder.getId());
        }
        try {
            return dao.clearPermissions(folder);
        } catch (DAOException de) {
            throw new ControllerException(de);
        }
    }

    public int clearGroupPermissions(Account account, Group group) throws ControllerException, PermissionException {
        if (!group.getOwner().getEmail().equals(account.getEmail()) && !accountController.isAdministrator(account)) {
            throw new PermissionException(account.getEmail() + " does not have permission to delete group "
                                                  + group.getId());
        }

        try {
            return dao.clearPermissions(group);
        } catch (DAOException de) {
            throw new ControllerException(de);
        }
    }

    public boolean accountHasReadPermission(Account account, Entry entry) throws ControllerException {
        try {
            return dao.hasPermission(entry, null, account, null, true, false);
        } catch (DAOException dao) {
            Logger.error(dao);
        }
        return false;
    }

    public boolean accountHasReadPermission(Account account, Set<Folder> folders) throws ControllerException {
        try {
            return dao.hasPermissionMulti(null, folders, account, null, true, false);
        } catch (DAOException dao) {
            Logger.error(dao);
        }
        return false;
    }

    public boolean accountHasWritePermission(Account account, Entry entry) throws ControllerException {
        try {
            return dao.hasPermission(entry, null, account, null, false, true);
        } catch (DAOException dao) {
            Logger.error(dao);
        }
        return false;
    }

    public boolean accountHasWritePermission(Account account, Set<Folder> folders) throws ControllerException {
        try {
            return dao.hasPermissionMulti(null, folders, account, null, false, true);
        } catch (DAOException dao) {
            Logger.error(dao);
        }
        return false;
    }

    public boolean groupHasWritePermission(Set<Group> groups, Entry entry) throws ControllerException {
        try {
            if (groups.isEmpty())
                return false;

            return dao.hasPermissionMulti(entry, null, null, groups, false, true);
        } catch (DAOException dao) {
            Logger.error(dao);
        }
        return false;
    }

    // checks if there is a set permission with write user
    public boolean groupHasWritePermission(Set<Group> groups, Set<Folder> folders) throws ControllerException {
        try {
            if (groups.isEmpty())
                return false;

            return dao.hasPermissionMulti(null, folders, null, groups, false, true);
        } catch (DAOException dao) {
            Logger.error(dao);
        }
        return false;
    }

    public boolean groupHasReadPermission(Set<Group> groups, Entry entry) throws ControllerException {
        try {
            if (groups.isEmpty())
                return false;

            return dao.hasPermissionMulti(entry, null, null, groups, true, false);

        } catch (DAOException dao) {
            Logger.error(dao);
            return false;
        }
    }

    public boolean isPubliclyVisible(Entry entry) throws ControllerException {
        Group publicGroup = groupController.createOrRetrievePublicGroup();
        Set<Group> groups = new HashSet<>();
        groups.add(publicGroup);
        return groupHasReadPermission(groups, entry);
    }

    public boolean isPublicVisible(Folder folder) throws ControllerException {
        Group publicGroup = groupController.createOrRetrievePublicGroup();
        Set<Group> groups = new HashSet<>();
        groups.add(publicGroup);
        Set<Folder> folders = new HashSet<>();
        folders.add(folder);
        return groupHasReadPermission(groups, folders);
    }

    public boolean groupHasReadPermission(Set<Group> groups, Set<Folder> folders) throws ControllerException {
        if (groups.isEmpty() || folders.isEmpty())
            return false;

        try {
            return dao.hasPermissionMulti(null, folders, null, groups, true, false);
        } catch (DAOException e) {
            throw new ControllerException(e);
        }
    }

//    public boolean accountHasReadPermission()

    /**
     * Checks if an account has read permissions for an entry. If no account is specified,
     * the method checks for public access of entry
     *
     * @param account user account
     * @param entry   existing entry
     * @return true if account has read privileges either explicitly or implicitly through group membership
     *         false otherwise
     * @throws ControllerException
     */
    public boolean hasReadPermission(Account account, Entry entry) throws ControllerException {

        // get groups for account. if account is null, this will return everyone group
        Set<Group> accountGroups = groupController.getAllGroups(account);

        // check read permission through group membership
        // ie. belongs to group that has read privileges for entry (or a group whose parent group does)
        if (groupHasReadPermission(accountGroups, entry))
            return true;

        if (account == null)
            return false;

        // check if account is a moderator account of is owner of the entry
        if (isOwnerOrAdministrator(account, entry))
            return true;

        // check explicit read permission
        if (accountHasReadPermission(account, entry))
            return true;

        Set<Folder> entryFolders = entry.getFolders();

        // can any group that account belongs to read any folder that entry is contained in?
        if (groupHasReadPermission(accountGroups, entryFolders))
            return true;

        // can account read any folder that entry is contained in?
        if (accountHasReadPermission(account, entryFolders))
            return true;

        return hasWritePermission(account, entry);
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
        if (groupHasWritePermission(accountGroups, entry))
            return true;

        Set<Folder> entryFolders = entry.getFolders();

        // can any group that account belongs to read any folder that entry is contained in?
        if (groupHasWritePermission(accountGroups, entryFolders))
            return true;

        // can account read any folder that entry is contained in?
        return accountHasWritePermission(account, entryFolders);
    }

    public boolean hasWritePermission(Account account, Folder folder) throws ControllerException {
        if (accountController.isAdministrator(account) || folder.getOwnerEmail().equalsIgnoreCase(account.getEmail()))
            return true;

        try {
            return dao.hasSetWriteFolderPermission(folder, account);
        } catch (DAOException e) {
            throw new ControllerException(e);
        }
    }

    public boolean enablePublicReadAccess(Account account, long partId) throws ControllerException {
        AccessPermission permission = new AccessPermission();
        permission.setType(AccessPermission.Type.READ_ENTRY);
        permission.setTypeId(partId);
        permission.setArticle(AccessPermission.Article.GROUP);
        permission.setArticleId(groupController.createOrRetrievePublicGroup().getId());
        return addPermission(account, permission) != null;
    }

    public boolean disablePublicReadAccess(Account account, long partId) throws ControllerException {
        AccessPermission permission = new AccessPermission();
        permission.setType(AccessPermission.Type.READ_ENTRY);
        permission.setTypeId(partId);
        permission.setArticle(AccessPermission.Article.GROUP);
        permission.setArticleId(groupController.createOrRetrievePublicGroup().getId());
        removePermission(account, permission);
        return true;
    }

    public boolean enableOrDisableFolderPublicAccess(Account account, long folderId, boolean isEnable)
            throws ControllerException {
        Folder folder = folderController.getFolderById(folderId);
        if (folder == null)
            return false;

        if (!hasWritePermission(account, folder))
            throw new ControllerException(account.getEmail() + " cannot modify folder " + folder.getId());

        // propagate permissions
        if (folder.isPropagatePermissions()) {
            for (Entry folderContent : folder.getContents()) {
                if (isEnable)
                    enablePublicReadAccess(account, folderContent.getId());
                else
                    disablePublicReadAccess(account, folderContent.getId());
            }
        }

        AccessPermission access = new AccessPermission();
        access.setArticle(AccessPermission.Article.GROUP);
        access.setArticleId(folder.getId());
        access.setType(AccessPermission.Type.READ_FOLDER);
        access.setTypeId(folderId);
        return addPermission(access, null, folder) != null;
    }

    public Set<Folder> retrievePermissionFolders(Account account) throws ControllerException {
        Set<Group> groups = groupController.getAllGroups(account);
        try {
            return dao.retrieveFolderPermissions(account, groups);
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
    protected boolean isOwnerOrAdministrator(Account account, Entry entry) throws ControllerException {
        // first check entry ownership
        if (account.getEmail().equals(entry.getOwnerEmail()))
            return true;

        // then check if moderator
        AccountController controller = ControllerFactory.getAccountController();
        if (controller.isAdministrator(account)) {
            return true;
        }

        // TODO : adding system account also but this needs to be handled in a better way
        Account systemAccount = controller.getSystemAccount();
        return (systemAccount != null && systemAccount.equals(account));
    }

    /**
     * retrieves permissions that have been explicitly set for entry with the
     * exception of the public group read access. The check for that is a separate
     * method call
     *
     * @param account user making request
     * @param entry   entry whose permissions are being checked
     * @return list of set permissions
     */
    public ArrayList<AccessPermission> retrieveSetEntryPermissions(Account account, Entry entry)
            throws ControllerException, PermissionException {
        if (!hasWritePermission(account, entry))
            throw new PermissionException(account.getEmail() + ": does not have read permissions for entry \""
                                                  + entry.getRecordId() + "\"");

        ArrayList<AccessPermission> accessPermissions = new ArrayList<>();

        try {
            // read accounts
            Set<Account> readAccounts = dao.retrieveAccountPermissions(entry, false, true);
            for (Account readAccount : readAccounts) {
                accessPermissions.add(new AccessPermission(AccessPermission.Article.ACCOUNT, readAccount.getId(),
                                                           AccessPermission.Type.READ_ENTRY, entry.getId(),
                                                           readAccount.getFullName()));
            }

            // write accounts
            Set<Account> writeAccounts = dao.retrieveAccountPermissions(entry, true, false);
            for (Account writeAccount : writeAccounts) {
                accessPermissions.add(new AccessPermission(AccessPermission.Article.ACCOUNT, writeAccount.getId(),
                                                           AccessPermission.Type.WRITE_ENTRY, entry.getId(),
                                                           writeAccount.getFullName()));
            }

            // read groups
            Set<Group> readGroups = dao.retrieveGroupPermissions(entry, false, true);
            for (Group group : readGroups) {
                if (group.getUuid().equalsIgnoreCase(GroupController.PUBLIC_GROUP_UUID))
                    continue;
                accessPermissions.add(new AccessPermission(AccessPermission.Article.GROUP, group.getId(),
                                                           AccessPermission.Type.READ_ENTRY, entry.getId(),
                                                           group.getLabel()));
            }

            // write groups
            Set<Group> writeGroups = dao.retrieveGroupPermissions(entry, true, false);
            for (Group group : writeGroups) {
                accessPermissions.add(new AccessPermission(AccessPermission.Article.GROUP, group.getId(),
                                                           AccessPermission.Type.WRITE_ENTRY, entry.getId(),
                                                           group.getLabel()));
            }
        } catch (DAOException de) {
            throw new ControllerException(de);
        }

        return accessPermissions;
    }

    /**
     * Retrieves permissions that have been explicitly set for the folders with the exception
     * of the public read permission if specified in the parameter. The call for that is a separate method
     *
     * @param folder        folder whose permissions are being retrieved
     * @param includePublic whether to include public access if set
     * @return list of permissions that have been found for the specified folder
     */
    public ArrayList<AccessPermission> retrieveSetFolderPermission(Folder folder, boolean includePublic)
            throws ControllerException {
        ArrayList<AccessPermission> accessPermissions = new ArrayList<>();

        try {
            // read accounts
            Set<Account> readAccounts = dao.retrieveAccountPermissions(folder, false, true);
            for (Account readAccount : readAccounts) {
                accessPermissions.add(new AccessPermission(AccessPermission.Article.ACCOUNT, readAccount.getId(),
                                                           AccessPermission.Type.READ_FOLDER, folder.getId(),
                                                           readAccount.getFullName()));
            }

            // write accounts
            Set<Account> writeAccounts = dao.retrieveAccountPermissions(folder, true, false);
            for (Account writeAccount : writeAccounts) {
                accessPermissions.add(new AccessPermission(AccessPermission.Article.ACCOUNT, writeAccount.getId(),
                                                           AccessPermission.Type.WRITE_FOLDER, folder.getId(),
                                                           writeAccount.getFullName()));
            }

            // read groups
            Set<Group> readGroups = dao.retrieveGroupPermissions(folder, false, true);
            for (Group group : readGroups) {
                if (!includePublic && group.getUuid().equalsIgnoreCase(GroupController.PUBLIC_GROUP_UUID))
                    continue;
                accessPermissions.add(new AccessPermission(AccessPermission.Article.GROUP, group.getId(),
                                                           AccessPermission.Type.READ_FOLDER, folder.getId(),
                                                           group.getLabel()));
            }

            // write groups
            Set<Group> writeGroups = dao.retrieveGroupPermissions(folder, true, false);
            for (Group group : writeGroups) {
                accessPermissions.add(new AccessPermission(AccessPermission.Article.GROUP, group.getId(),
                                                           AccessPermission.Type.WRITE_FOLDER, folder.getId(),
                                                           group.getLabel()));
            }
        } catch (DAOException de) {
            throw new ControllerException(de);
        }

        return accessPermissions;
    }

    public ArrayList<AccessPermission> getDefaultPermissions(Account account) throws ControllerException {
        ArrayList<AccessPermission> accessPermissions = new ArrayList<>();
        for (Group group : ControllerFactory.getGroupController().getAllPublicGroupsForAccount(account)) {
            AccessPermission accessPermission = new AccessPermission();
            accessPermission.setType(AccessPermission.Type.READ_ENTRY);
            accessPermission.setArticle(AccessPermission.Article.GROUP);
            accessPermission.setArticleId(group.getId());
            accessPermission.setDisplay(group.getLabel());
            accessPermissions.add(accessPermission);
        }

        return accessPermissions;
    }

    public boolean propagateFolderPermissions(Account account, Folder folder, boolean prop) throws ControllerException {
        if (!accountController.isAdministrator(account) && !account.getEmail().equalsIgnoreCase(folder.getOwnerEmail()))
            return false;

        // retrieve folder permissions
        ArrayList<AccessPermission> permissions = retrieveSetFolderPermission(folder, true);
//        boolean isPublic = get

        // if propagate, add permissions to entries contained in here  //TODO : inefficient for large entries/perms
        if (prop) {
            for (Entry entry : folder.getContents()) {
                for (AccessPermission accessPermission : permissions) {
                    addPermission(accessPermission, entry, null);
                }
            }
        } else {
            // else remove permissions
            for (Entry entry : folder.getContents()) {
                for (AccessPermission accessPermission : permissions) {
                    removePermission(accessPermission, entry, null);
                }
            }
        }
        return true;
    }
}

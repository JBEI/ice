package org.jbei.ice.lib.access;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.jbei.ice.ControllerException;
import org.jbei.ice.lib.account.AccountController;
import org.jbei.ice.lib.account.model.Account;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.dao.DAOException;
import org.jbei.ice.lib.dao.DAOFactory;
import org.jbei.ice.lib.dao.hibernate.FolderDAO;
import org.jbei.ice.lib.dao.hibernate.PermissionDAO;
import org.jbei.ice.lib.dto.permission.AccessPermission;
import org.jbei.ice.lib.entry.EntryAuthorization;
import org.jbei.ice.lib.entry.model.Entry;
import org.jbei.ice.lib.folder.Folder;
import org.jbei.ice.lib.group.Group;
import org.jbei.ice.lib.group.GroupController;

/**
 * Controller for permissions
 *
 * @author Hector Plahar
 */
public class PermissionsController {

    private final AccountController accountController;
    private final GroupController groupController;
    private final FolderDAO folderDAO;
    private final PermissionDAO dao;

    public PermissionsController() {
        accountController = new AccountController();
        groupController = new GroupController();
        folderDAO = DAOFactory.getFolderDAO();
        dao = DAOFactory.getPermissionDAO();
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
            return dao.create(permission);
        } catch (DAOException e) {
            Logger.error(e);
            throw new ControllerException(e);
        }
    }

    public Permission addPermission(Account requestingAccount, AccessPermission access) throws ControllerException {
        if (access.isEntry()) {
            Entry entry = DAOFactory.getEntryDAO().get(access.getTypeId());
            if (entry == null)
                throw new ControllerException("Cannot find entry " + access.getTypeId());

            EntryAuthorization authorization = new EntryAuthorization();
            authorization.expectWrite(requestingAccount.getEmail(), entry);
            return addPermission(access, entry, null);
        }

        if (access.isFolder()) {
            Folder folder = folderDAO.get(access.getTypeId());
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
        return dao.create(permission);
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
        Folder folder = folderDAO.get(folderId);
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
        if (access.isEntry()) {
            Entry entry = DAOFactory.getEntryDAO().get(access.getTypeId());
            if (entry == null)
                throw new ControllerException("Cannot find entry " + access.getTypeId());

            EntryAuthorization authorization = new EntryAuthorization();
            authorization.expectWrite(requestingAccount.getEmail(), entry);

            // remove permission from entry
            removePermission(access, entry, null);

        } else if (access.isFolder()) {
            Folder folder = folderDAO.get(access.getTypeId());
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

    public boolean accountHasWritePermission(Account account, Set<Folder> folders) throws ControllerException {
        try {
            return dao.hasPermissionMulti(null, folders, account, null, false, true);
        } catch (DAOException dao) {
            Logger.error(dao);
        }
        return false;
    }

    // checks if there is a set permission with write user
    public boolean groupHasWritePermission(Set<Group> groups, Set<Folder> folders) throws ControllerException {
        if (groups.isEmpty())
            return false;

        return dao.hasPermissionMulti(null, folders, null, groups, false, true);
    }

    public boolean isPubliclyVisible(Entry entry) throws ControllerException {
        Group publicGroup = groupController.createOrRetrievePublicGroup();
        Set<Group> groups = new HashSet<>();
        groups.add(publicGroup);
        return dao.hasPermissionMulti(entry, null, null, groups, true, false);
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
        Folder folder = folderDAO.get(folderId);
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
        access.setArticleId(groupController.createOrRetrievePublicGroup().getId());
        access.setType(AccessPermission.Type.READ_FOLDER);
        access.setTypeId(folderId);
        if (isEnable)
            return addPermission(access, null, folder) != null;
        removePermission(access, null, folder);
        return true;
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
     * retrieves permissions that have been explicitly set for entry with the
     * exception of the public group read access. The check for that is a separate
     * method call
     *
     * @param entry entry whose permissions are being checked
     * @return list of set permissions
     */
    public ArrayList<AccessPermission> retrieveSetEntryPermissions(Entry entry) {
        ArrayList<AccessPermission> accessPermissions = new ArrayList<>();

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
        for (Group group : new GroupController().getAllPublicGroupsForAccount(account)) {
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
        if (permissions.isEmpty())
            return true;
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

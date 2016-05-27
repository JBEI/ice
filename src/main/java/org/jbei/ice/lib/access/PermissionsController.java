package org.jbei.ice.lib.access;

import org.jbei.ice.lib.account.AccountController;
import org.jbei.ice.lib.bulkupload.BulkUploadAuthorization;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.dto.access.AccessPermission;
import org.jbei.ice.lib.dto.folder.FolderAuthorization;
import org.jbei.ice.lib.dto.folder.FolderDetails;
import org.jbei.ice.lib.entry.EntryAuthorization;
import org.jbei.ice.lib.group.GroupController;
import org.jbei.ice.storage.DAOException;
import org.jbei.ice.storage.DAOFactory;
import org.jbei.ice.storage.hibernate.dao.*;
import org.jbei.ice.storage.model.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
    private final GroupDAO groupDAO;
    private final AccountDAO accountDAO;

    public PermissionsController() {
        accountController = new AccountController();
        groupController = new GroupController();
        folderDAO = DAOFactory.getFolderDAO();
        dao = DAOFactory.getPermissionDAO();
        groupDAO = DAOFactory.getGroupDAO();
        accountDAO = DAOFactory.getAccountDAO();
    }

    public Permission addPermission(String userId, AccessPermission access) {
        if (access.isEntry()) {
            Entry entry = DAOFactory.getEntryDAO().get(access.getTypeId());
            if (entry == null)
                throw new IllegalArgumentException("Cannot find entry " + access.getTypeId());

            EntryAuthorization authorization = new EntryAuthorization();
            authorization.expectWrite(userId, entry);
            return addPermission(access, entry, null, null);
        }

        if (access.isFolder()) {
            Folder folder = folderDAO.get(access.getTypeId());
            if (!hasWritePermission(userId, folder)) {
                Logger.error(userId + " not allowed to add " + access.toString());
                return null;
            }

            // propagate permissions
            if (folder.isPropagatePermissions()) {
                for (Entry folderContent : folder.getContents()) {
                    addPermission(access, folderContent, null, null);
                }
            }
            return addPermission(access, null, folder, null);
        }

        // if bulk upload
        if (access.isUpload()) {
            BulkUpload upload = DAOFactory.getBulkUploadDAO().get(access.getTypeId());
            if (upload == null)
                throw new IllegalArgumentException("Cannot find upload " + access.getId());
            BulkUploadAuthorization uploadAuthorization = new BulkUploadAuthorization();
            uploadAuthorization.expectWrite(userId, upload);
            return addPermission(access, null, null, upload);
        }

        return null;
    }

    protected Permission addPermission(AccessPermission access, Entry entry, Folder folder, BulkUpload upload) {
        // account or group
        Account account = null;
        Group group = null;
        switch (access.getArticle()) {
            case ACCOUNT:
            default:
                account = accountDAO.get(access.getArticleId());
                break;

            case GROUP:
                group = groupDAO.get(access.getArticleId());
                break;
        }

        // does the permissions already exists
        if (dao.hasPermission(entry, folder, upload, account, group, access.isCanRead(), access.isCanWrite())) {
            return dao.retrievePermission(entry, folder, upload, account, group, access.isCanRead(), access.isCanWrite());
        }

        // add the permission if not
        Permission permission = new Permission();
        permission.setEntry(entry);
        if (entry != null)
            entry.getPermissions().add(permission);
        permission.setGroup(group);
        permission.setFolder(folder);
        permission.setUpload(upload);
        permission.setAccount(account);
        permission.setCanRead(access.isCanRead());
        permission.setCanWrite(access.isCanWrite());
        return dao.create(permission);
    }

    public void removePermission(String userId, AccessPermission access) {
        if (access.isEntry()) {
            Entry entry = DAOFactory.getEntryDAO().get(access.getTypeId());
            if (entry == null)
                return;

            EntryAuthorization authorization = new EntryAuthorization();
            authorization.expectWrite(userId, entry);

            // remove permission from entry
            removePermission(access, entry, null, null);
            return;
        }

        if (access.isFolder()) {
            Folder folder = folderDAO.get(access.getTypeId());
            FolderAuthorization folderAuthorization = new FolderAuthorization();
            folderAuthorization.expectWrite(userId, folder);

            // if folder is to be propagated, add removing permission from contained entries
            if (folder.isPropagatePermissions()) {
                for (Entry folderContent : folder.getContents()) {
                    removePermission(access, folderContent, null, null);
                }
            }
            // remove permission from folder
            removePermission(access, null, folder, null);
            return;
        }

        if (access.isUpload()) {
            BulkUpload upload = DAOFactory.getBulkUploadDAO().get(access.getTypeId());
            if (upload == null)
                throw new IllegalArgumentException("Could not retrieve upload " + access.getTypeId());
            BulkUploadAuthorization uploadAuthorization = new BulkUploadAuthorization();
            uploadAuthorization.expectWrite(userId, upload);
            removePermission(access, null, null, upload);
        }
    }

    private void removePermission(AccessPermission access, Entry entry, Folder folder, BulkUpload upload) {
        // account or group
        Account account = null;
        Group group = null;
        switch (access.getArticle()) {
            case ACCOUNT:
            default:
                account = accountDAO.get(access.getArticleId());
                break;

            case GROUP:
                group = groupDAO.get(access.getArticleId());
                break;
        }

        dao.removePermission(entry, folder, upload, account, group, access.isCanRead(), access.isCanWrite());
    }

    public boolean accountHasReadPermission(Account account, Set<Folder> folders) {
        try {
            return dao.hasPermissionMulti(null, folders, account, null, true, false);
        } catch (DAOException dao) {
            Logger.error(dao);
        }
        return false;
    }

    public boolean accountHasWritePermission(Account account, Set<Folder> folders) {
        try {
            return dao.hasPermissionMulti(null, folders, account, null, false, true);
        } catch (DAOException dao) {
            Logger.error(dao);
        }
        return false;
    }

    // checks if there is a set permission with write user
    public boolean groupHasWritePermission(Set<Group> groups, Set<Folder> folders) {
        if (groups.isEmpty())
            return false;

        return dao.hasPermissionMulti(null, folders, null, groups, false, true);
    }

    public boolean isPubliclyVisible(Entry entry) {
        Group publicGroup = groupController.createOrRetrievePublicGroup();
        Set<Group> groups = new HashSet<>();
        groups.add(publicGroup);
        return dao.hasPermissionMulti(entry, null, null, groups, true, false);
    }

    public boolean isPublicVisible(Folder folder) {
        Group publicGroup = groupController.createOrRetrievePublicGroup();
        Set<Group> groups = new HashSet<>();
        groups.add(publicGroup);
        Set<Folder> folders = new HashSet<>();
        folders.add(folder);
        return groupHasReadPermission(groups, folders);
    }

    public boolean groupHasReadPermission(Set<Group> groups, Set<Folder> folders) {
        if (groups.isEmpty() || folders.isEmpty())
            return false;

        return dao.hasPermissionMulti(null, folders, null, groups, true, false);
    }

    public boolean hasWritePermission(String userId, Folder folder) {
        if (accountController.isAdministrator(userId) || folder.getOwnerEmail().equalsIgnoreCase(userId))
            return true;

        Account account = accountController.getByEmail(userId);
        return dao.hasSetWriteFolderPermission(folder, account);
    }

    /**
     * Retrieves permissions that have been explicitly set for the folders with the exception
     * of the public read permission if specified in the parameter. The call for that is a separate method
     *
     * @param folder        folder whose permissions are being retrieved
     * @param includePublic whether to include public access if set
     * @return list of permissions that have been found for the specified folder
     */
    public ArrayList<AccessPermission> retrieveSetFolderPermission(Folder folder, boolean includePublic) {
        ArrayList<AccessPermission> accessPermissions = new ArrayList<>();

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

        // remote accounts
        RemoteShareModelDAO remoteShareModelDAO = DAOFactory.getRemoteShareModelDAO();
        List<RemoteShareModel> remoteAccessModelList = remoteShareModelDAO.getByFolder(folder);
        for (RemoteShareModel remoteShareModel : remoteAccessModelList) {
            accessPermissions.add(remoteShareModel.toDataTransferObject());
        }

        return accessPermissions;
    }

    /**
     * Propagates the permissions for the folder to the contained entries
     *
     * @param userId unique identifier for account of user requesting action that led to this call
     * @param folder folder user permissions are being propagated
     * @param add    true if folder is to be added, false otherwise
     * @return true if action permission was propagated successfully
     */
    public boolean propagateFolderPermissions(String userId, Folder folder, boolean add) {
        if (!accountController.isAdministrator(userId) && !userId.equalsIgnoreCase(folder.getOwnerEmail()))
            return false;

        // retrieve folder permissions
        ArrayList<AccessPermission> permissions = retrieveSetFolderPermission(folder, true);
        if (permissions.isEmpty())
            return true;

        // if propagate, add permissions to entries contained in here  //TODO : inefficient for large entries/perms
        if (add) {
            for (Entry entry : folder.getContents()) {
                for (AccessPermission accessPermission : permissions) {
                    addPermission(accessPermission, entry, null, null);
                }
            }
        } else {
            // else remove permissions
            for (Entry entry : folder.getContents()) {
                for (AccessPermission accessPermission : permissions) {
                    removePermission(accessPermission, entry, null, null);
                }
            }
        }
        return true;
    }

    public FolderDetails setFolderPermissions(String userId, long folderId, ArrayList<AccessPermission> permissions) {
        Folder folder = folderDAO.get(folderId);
        FolderAuthorization folderAuthorization = new FolderAuthorization();
        folderAuthorization.expectWrite(userId, folder);

        dao.clearPermissions(folder);

        if (permissions == null)
            return null;

        Account account = accountController.getByEmail(userId);

        for (AccessPermission access : permissions) {
            Permission permission = new Permission();
            permission.setFolder(folder);
            permission.setAccount(account);
            permission.setCanRead(access.isCanRead());
            permission.setCanWrite(access.isCanWrite());
            dao.create(permission);
        }

        return folder.toDataTransferObject();
    }
}

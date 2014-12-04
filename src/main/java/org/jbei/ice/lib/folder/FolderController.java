package org.jbei.ice.lib.folder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jbei.ice.ControllerException;
import org.jbei.ice.lib.access.Permission;
import org.jbei.ice.lib.access.PermissionException;
import org.jbei.ice.lib.access.PermissionsController;
import org.jbei.ice.lib.account.AccountController;
import org.jbei.ice.lib.account.AccountType;
import org.jbei.ice.lib.account.model.Account;
import org.jbei.ice.lib.bulkupload.BulkUploadController;
import org.jbei.ice.lib.bulkupload.BulkUploadInfo;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.dao.DAOFactory;
import org.jbei.ice.lib.dao.hibernate.AccountDAO;
import org.jbei.ice.lib.dao.hibernate.EntryDAO;
import org.jbei.ice.lib.dao.hibernate.FolderDAO;
import org.jbei.ice.lib.dao.hibernate.PermissionDAO;
import org.jbei.ice.lib.dto.entry.PartData;
import org.jbei.ice.lib.dto.entry.Visibility;
import org.jbei.ice.lib.dto.folder.FolderAuthorization;
import org.jbei.ice.lib.dto.folder.FolderDetails;
import org.jbei.ice.lib.dto.folder.FolderType;
import org.jbei.ice.lib.dto.permission.AccessPermission;
import org.jbei.ice.lib.entry.EntryController;
import org.jbei.ice.lib.entry.model.Entry;
import org.jbei.ice.lib.group.Group;
import org.jbei.ice.lib.group.GroupController;
import org.jbei.ice.lib.shared.ColumnField;
import org.jbei.ice.servlet.ModelToInfoFactory;

import org.apache.commons.lang.StringUtils;

/**
 * @author Hector Plahar
 */
public class FolderController {

    private final FolderDAO dao;
    private final AccountController accountController;
    private final GroupController groupController;
    private final PermissionDAO permissionDAO;
    private final PermissionsController permissionsController;
    private final BulkUploadController bulkUploadController;
    private final AccountDAO accountDAO;
    private final FolderAuthorization authorization;

    public FolderController() {
        dao = DAOFactory.getFolderDAO();
        accountController = new AccountController();
        groupController = new GroupController();
        permissionDAO = DAOFactory.getPermissionDAO();
        permissionsController = new PermissionsController();
        bulkUploadController = new BulkUploadController();
        accountDAO = DAOFactory.getAccountDAO();
        authorization = new FolderAuthorization();
    }

    /**
     * Retrieves folders that have been "promoted" by an administrator to
     * imply that they are to be made available to everyone on the site
     *
     * @param userId unique user identifier
     * @return list of folders that are contained under the "Available" section
     */
    public ArrayList<FolderDetails> getAvailableFolders(String userId) {
        Set<Folder> folders = new HashSet<>();
        folders.addAll(dao.getFoldersByType(FolderType.PUBLIC));
        boolean isAdmin = accountController.isAdministrator(userId);

        ArrayList<FolderDetails> list = new ArrayList<>();
        for (Folder folder : folders) {
            FolderDetails details = folder.toDataTransferObject();
            long folderSize = dao.getFolderSize(folder.getId());
            details.setCount(folderSize);
            details.setType(FolderType.PUBLIC);
            details.setCanEdit(isAdmin);
            list.add(details);
        }
        Collections.sort(list);
        return list;
    }

    /**
     * Retrieves folders that are shared shared publicly. Note that this is different from
     * featured folders that have a type of PUBLIC
     *
     * @return list of public folders on this site
     */
    public ArrayList<FolderDetails> getPublicFolders() {
        Group publicGroup = groupController.createOrRetrievePublicGroup();
        Set<Folder> folders = permissionDAO.getFolders(publicGroup);
        ArrayList<FolderDetails> list = new ArrayList<>();
        for (Folder folder : folders) {
            FolderDetails details = folder.toDataTransferObject();
            long folderSize = dao.getFolderSize(folder.getId());
            details.setCount(folderSize);
            list.add(details);
        }

        Collections.sort(list);
        return list;
    }

    /**
     * Retrieves entries that are made available publicly
     *
     * @param sort   order of retrieval for the entries
     * @param offset start of retrieval
     * @param limit  maximum number of entries to retrieve
     * @param asc    whether to retrieve the entries in ascending order
     * @return wrapper around the retrieved entries
     */
    public FolderDetails getPublicEntries(ColumnField sort, int offset, int limit, boolean asc) {
        Group publicGroup = new GroupController().createOrRetrievePublicGroup();
        Set<Group> groups = new HashSet<>();
        groups.add(publicGroup);

        EntryDAO entryDAO = DAOFactory.getEntryDAO();
        Set<Entry> results = entryDAO.retrieveVisibleEntries(null, groups, sort, asc, offset, limit);
        long visibleCount = entryDAO.visibleEntryCount(null, groups);

        FolderDetails details = new FolderDetails();
        details.setCount(visibleCount);

        for (Entry entry : results) {
            try {
                PartData info = ModelToInfoFactory.createTableViewData(null, entry, false);
                info.setPublicRead(true);
                details.getEntries().add(info);
            } catch (Exception e) {
                Logger.error(e);
            }
        }
        return details;
    }

    public ArrayList<FolderDetails> getBulkUploadDrafts(String userId) {
        ArrayList<FolderDetails> folders = new ArrayList<>();
        ArrayList<BulkUploadInfo> list = bulkUploadController.retrieveByUser(userId, userId);
        for (BulkUploadInfo info : list) {
            FolderDetails details = new FolderDetails();
            details.setName(info.getName());
            details.setCount(info.getCount());
            details.setId(info.getId());
            details.setCanEdit(true);
            details.setType(FolderType.UPLOAD);
            folders.add(details);
        }
        return folders;
    }

    /**
     * Retrieves information about submitted bulk uploads that have status "PENDING". Administrator only function
     *
     * @param userId unique identifier for user performing action. Must have admin privileges
     * @return list of information about pending bulk uploads is user has administrative privileges, null otherwise
     */
    public ArrayList<FolderDetails> getPendingBulkUploads(String userId) {
        ArrayList<FolderDetails> folders = new ArrayList<>();
        ArrayList<BulkUploadInfo> list = bulkUploadController.getPendingUploads(userId);
        for (BulkUploadInfo info : list) {
            FolderDetails details = new FolderDetails();
            String name = info.getAccount() != null ? info.getAccount().getEmail() : info.getName();
            details.setName(name);
            details.setCount(info.getCount());
            details.setId(info.getId());
            details.setType(FolderType.UPLOAD);
            folders.add(details);
        }
        return folders;
    }

    public boolean removeFolderContents(String userId, long folderId, ArrayList<Long> entryIds) {
        boolean isAdministrator = accountController.isAdministrator(userId);
        Folder folder = dao.get(folderId);

        if (folder.getType() == FolderType.PUBLIC && !isAdministrator) {
            String errMsg = userId + ": cannot modify folder " + folder.getName();
            throw new PermissionException(errMsg);
        }

        return dao.removeFolderEntries(folder, entryIds) != null;
    }

    /**
     * Retrieves the folder specified in the parameter and contents
     *
     * @param userId   unique identifier for user making request
     * @param folderId unique identifier for folder to be retrieved
     * @param sort     sort order for folder content retrieval
     * @param asc      sort order for folder content retrieval; ascending if true
     * @param start    index of first item in retrieval
     * @param limit    upper limit count of items to be retrieval
     * @return wrapper around list of folder entries if folder is found, null otherwise
     * @throws PermissionException if user does not have read permissions on folder
     */
    public FolderDetails retrieveFolderContents(String userId, long folderId, ColumnField sort, boolean asc,
            int start, int limit) {
        Folder folder = dao.get(folderId);
        if (folder == null)
            return null;

        // should have permission to read folder (folder should be public, you should be an admin, or owner)
        authorization.expectRead(userId, folder);

        FolderDetails details = folder.toDataTransferObject();
        long folderSize = dao.getFolderSize(folderId);
        details.setCount(folderSize);
        ArrayList<AccessPermission> permissions = getAndFilterFolderPermissions(userId, folder);
        details.setAccessPermissions(permissions);

        details.setPublicReadAccess(permissionsController.isPublicVisible(folder));
        Account owner = accountController.getByEmail(folder.getOwnerEmail());
        if (owner != null)
            details.setOwner(owner.toDataTransferObject());

        // retrieve folder contents
        ArrayList<Entry> results = dao.retrieveFolderContents(folderId, sort, asc, start, limit);
        for (Entry entry : results) {
            PartData info = ModelToInfoFactory.createTableViewData(userId, entry, false);
            details.getEntries().add(info);
        }
        return details;
    }

    /**
     * The permission(s) enabling the share for user
     * is(are) included. If the user is an admin then all the permissions are included, otherwise only those pertaining
     * to the user are included.
     * <p>e.g. if a folder F is shared with groups A and B and the user is a non-admin belonging to group B, folder
     * F will be included in the list of folders returned but will only include permissions for group B
     *
     * @param userId identifier for user making request
     * @param folder Folder whose permissions are to be retrieved
     * @return list of filtered permissions
     */
    protected ArrayList<AccessPermission> getAndFilterFolderPermissions(String userId, Folder folder) {
        ArrayList<AccessPermission> permissions = permissionsController.retrieveSetFolderPermission(folder, false);
        if (accountController.isAdministrator(userId) || folder.getOwnerEmail().equalsIgnoreCase(userId)) {
            return permissions;
        }

        Account account = accountDAO.getByEmail(userId);

        // filter permissions
        ArrayList<AccessPermission> filteredPermissions = new ArrayList<>();
        for (AccessPermission accessPermission : permissions) {

            // account either has direct write permissions
            if (accessPermission.getArticle() == AccessPermission.Article.ACCOUNT
                    && accessPermission.getArticleId() == account.getId()) {
                filteredPermissions.add(accessPermission);
                continue;
            }

            if (account.getGroups() == null || account.getGroups().isEmpty())
                continue;

            // or belongs to a group that has the write permissions
            if (accessPermission.getArticle() == AccessPermission.Article.GROUP) {
                Group group = DAOFactory.getGroupDAO().get(accessPermission.getArticleId());
                if (group == null)
                    continue;

                if (account.getGroups().contains(group)) {
                    filteredPermissions.add(accessPermission);
                }
            }
        }

        return filteredPermissions;
    }

    public FolderDetails update(String userId, long folderId, FolderDetails details) {
        Folder folder = dao.get(folderId);
        if (folder == null)
            return null; // resource not found

        authorization.expectWrite(userId, folder);

        if (details.getType() == FolderType.PUBLIC && folder.getType() != FolderType.PUBLIC)
            return promoteFolder(userId, folder);

        if (details.getType() == FolderType.PRIVATE && folder.getType() != FolderType.PRIVATE)
            return demoteFolder(userId, folder);

        folder.setModificationTime(new Date());
        if (details.getName() != null && !folder.getName().equals(details.getName()))
            folder.setName(details.getName());

        if (details.isPropagatePermission() != folder.isPropagatePermissions()) {
            folder.setPropagatePermissions(details.isPropagatePermission());
        }

        return dao.update(folder).toDataTransferObject();
    }

    /**
     * Deletes either a user folder or bulk upload (which is represented as a folder to the user)
     *
     * @param userId   unique identifier for user requesting delete action
     * @param folderId unique identifier for folder to be deleted
     * @param type     type of folder to be deleted (either "UPLOAD" or "PRIVATE")
     * @return delete folder details
     */
    public FolderDetails delete(String userId, long folderId, FolderType type) {
        switch (type) {
            case UPLOAD:
                BulkUploadController controller = new BulkUploadController();
                BulkUploadInfo info = controller.deleteDraftById(userId, folderId);
                if (info == null) {
                    Logger.error("Could not locate bulk upload id " + folderId + " for deletion");
                    return null;
                }

                FolderDetails details = new FolderDetails();
                details.setId(info.getId());
                return details;

            case PRIVATE:
                Folder folder = dao.get(folderId);
                if (folder == null)
                    return null;

                if (!accountController.isAdministrator(userId) && !folder.getOwnerEmail().equalsIgnoreCase(userId)) {
                    String errorMsg = userId + ": insufficient permissions to delete folder " + folderId;
                    Logger.warn(errorMsg);
                    return null;
                }

                details = folder.toDataTransferObject();
                long folderSize = dao.getFolderSize(folderId);
                details.setCount(folderSize);

                dao.delete(folder);
                permissionDAO.clearPermissions(folder);
                return details;

            default:
                Logger.error("Cannot delete folder of type " + type);
                return null;
        }
    }

    public Folder addFolderContents(Account account, long id, ArrayList<Entry> entrys) throws ControllerException {
        Folder folder = dao.get(id);
        if (folder == null)
            throw new ControllerException("Could not retrieve folder with id " + id);
        folder = dao.addFolderContents(folder, entrys);
        if (folder.isPropagatePermissions()) {
            permissionsController.propagateFolderPermissions(account, folder, true);
        }
        return folder;
    }

    // expects the same entries to be added
    public ArrayList<FolderDetails> addEntriesToFolder(String userId, ArrayList<FolderDetails> folders) {
        EntryController entryController = new EntryController();
        Account account = accountDAO.getByEmail(userId);

        ArrayList<Long> entryIds = null;

        for (FolderDetails details : folders) {
            Folder folder = dao.get(details.getId());
            if (folder == null)
                return null;

            if (entryIds == null) {
                entryIds = new ArrayList<>();
                for (PartData datum : details.getEntries()) {
                    entryIds.add(datum.getId());
                }
            }

            ArrayList<Entry> entrys = entryController.getEntriesByIdSet(account, entryIds);
            // todo : check visibility; allow non 9 only if user owns it or is admin
            folder = dao.addFolderContents(folder, entrys);
            if (folder.isPropagatePermissions()) {
                permissionsController.propagateFolderPermissions(account, folder, true);
            }

            details.setCount(dao.getFolderSize(folder.getId()));
        }

        return folders;
    }

    public FolderDetails createPersonalFolder(String userId, FolderDetails folderDetails) {
        if (folderDetails.getName() == null)
            return null;
        Folder folder = new Folder(folderDetails.getName());
        folder.setOwnerEmail(userId);
        folder.setType(FolderType.PRIVATE);
        folder.setCreationTime(new Date());
        folder = dao.create(folder);
        FolderDetails details = folder.toDataTransferObject();
        details.setCanEdit(true);
        return details;
    }

    public Collection getFolderStats(String userId) {
        Account account = getAccount(userId);
        if (account == null)
            return null;

        EntryDAO entryDAO = DAOFactory.getEntryDAO();
        EntryController entryController = new EntryController();
        Collection collection = new Collection();
        collection.setAvailable(entryController.getNumberOfVisibleEntries(userId));
        collection.setDeleted(entryDAO.getDeletedCount(userId));
        collection.setPersonal(entryController.getNumberOfOwnerEntries(userId, userId));
        collection.setShared(entryController.getNumberOfEntriesSharedWithUser(userId));
        collection.setDrafts(entryDAO.getByVisibilityCount(userId, Visibility.DRAFT));
        if (account.getType() == AccountType.ADMIN)
            collection.setPending(entryDAO.getPendingCount());
        return collection;
    }

    public ArrayList<FolderDetails> getUserFolders(String userId) {
        Account account = getAccount(userId);
        List<Folder> folders = dao.getFoldersByOwner(account);
        ArrayList<FolderDetails> folderDetails = new ArrayList<>();
        for (Folder folder : folders) {
            if (!folder.getOwnerEmail().equalsIgnoreCase(userId))
                continue;

            FolderDetails details = new FolderDetails(folder.getId(), folder.getName());
            long folderSize = dao.getFolderSize(folder.getId());
            details.setCount(folderSize);
            details.setType(FolderType.PRIVATE);
            details.setCanEdit(true);
            folderDetails.add(details);
        }
        return folderDetails;
    }

    public ArrayList<AccessPermission> getPermissions(String userId, long folderId) {
        Folder folder = dao.get(folderId);
        if (folder == null)
            return null;

        authorization.expectWrite(userId, folder);

        ArrayList<AccessPermission> accessPermissions = new ArrayList<>();
        Set<Permission> permissions = permissionDAO.getFolderPermissions(folder);

        for (Permission permission : permissions) {
            if (permission.getGroup() != null && permission.getGroup().getUuid().equals(
                    GroupController.PUBLIC_GROUP_UUID))
                continue;

            accessPermissions.add(permission.toDataTransferObject());
        }

        return accessPermissions;
    }

    /**
     * Retrieves folders that have been shared with specified user as an individual or as part of a group.
     *
     * @param userId User whose shared folders are being retrieved
     * @return list of folders meeting the shared criteria
     */
    public ArrayList<FolderDetails> getSharedUserFolders(String userId) {
        Account account = getAccount(userId);
        ArrayList<FolderDetails> folderDetails = new ArrayList<>();

        Set<Group> groups = groupController.getAllGroups(account);
        Set<Folder> sharedFolders = DAOFactory.getPermissionDAO().retrieveFolderPermissions(account, groups);
        if (sharedFolders == null)
            return null;

        for (Folder folder : sharedFolders) {
            FolderDetails details = folder.toDataTransferObject();
            details.setType(FolderType.SHARED);
            long folderSize = dao.getFolderSize(folder.getId());
            details.setCount(folderSize);
            folderDetails.add(details);
        }

        return folderDetails;
    }

    public boolean enablePublicReadAccess(String userId, long folderId) {
        AccessPermission permission = new AccessPermission();
        permission.setType(AccessPermission.Type.READ_FOLDER);
        permission.setTypeId(folderId);
        permission.setArticle(AccessPermission.Article.GROUP);
        permission.setArticleId(groupController.createOrRetrievePublicGroup().getId());
        return createFolderPermission(userId, folderId, permission) != null;
    }

    public AccessPermission createFolderPermission(String userId, long folderId, AccessPermission accessPermission) {
        if (accessPermission == null)
            return null;

        Folder folder = dao.get(folderId);
        if (folder == null)
            return null;

        FolderAuthorization authorization = new FolderAuthorization();
        authorization.expectWrite(userId, folder);

        Permission permission = new Permission();
        permission.setFolder(folder);
        if (accessPermission.getArticle() == AccessPermission.Article.GROUP) {
            Group group = DAOFactory.getGroupDAO().get(accessPermission.getArticleId());
            if (group == null) {
                Logger.error("Could not assign group with id " + accessPermission.getArticleId() + " to folder");
                return null;
            }
            permission.setGroup(group);
        } else {
            Account account = DAOFactory.getAccountDAO().get(accessPermission.getArticleId());
            if (account == null) {
                Logger.error("Could not assign account with id " + accessPermission.getArticleId() + " to folder");
                return null;
            }
            permission.setAccount(account);
        }

        permission.setCanRead(accessPermission.isCanRead());
        permission.setCanWrite(accessPermission.isCanWrite());
        AccessPermission created = permissionDAO.create(permission).toDataTransferObject();
        if (folder.getType() == FolderType.PRIVATE) {
            folder.setType(FolderType.SHARED);
            folder.setModificationTime(new Date());
            dao.update(folder);
        }

        return created;
    }

    public boolean disablePublicReadAccess(String userId, long folderId) {
        Folder folder = dao.get(folderId);
        if (folder == null)
            return false;

        authorization.expectWrite(userId, folder);

        GroupController groupController = new GroupController();
        Group publicGroup = groupController.createOrRetrievePublicGroup();

        permissionDAO.removePermission(null, folder, null, publicGroup, true, false);
        return true;
    }

    protected Account getAccount(String userId) {
        Account account = accountDAO.getByEmail(userId);
        if (account == null)
            throw new IllegalArgumentException("No account with id " + userId);
        return account;
    }

    /**
     * "Promote"s a collection into a featured collection. This action is restricted to administrators.
     * The owner does not have to be an administrator and maintains ownership after being featured
     *
     * @param userId requesting account id
     * @param folder folder to be promoted
     * @return true if promotion is successful, false otherwise
     */
    protected FolderDetails promoteFolder(String userId, Folder folder) {
        if (folder.getType() == FolderType.PUBLIC)
            return folder.toDataTransferObject();

        authorization.expectAdmin(userId);

        folder.setType(FolderType.PUBLIC);
        folder.setModificationTime(new Date());
        return dao.update(folder).toDataTransferObject();
    }

    /**
     * Opposite of FolderController#demoteFolder(userId, long)
     * Removes the folder from being a featured collections
     *
     * @param userId requesting account
     * @param folder to be demoted
     * @return true on successful remote, false otherwise
     */
    protected FolderDetails demoteFolder(String userId, Folder folder) {
        if (folder.getType() != FolderType.PUBLIC)
            return folder.toDataTransferObject();

        authorization.expectAdmin(userId);

        folder.setType(FolderType.PRIVATE);
        folder.setModificationTime(new Date());
        if (StringUtils.isBlank(folder.getOwnerEmail()))
            folder.setOwnerEmail(userId);
        return dao.update(folder).toDataTransferObject();
    }
}

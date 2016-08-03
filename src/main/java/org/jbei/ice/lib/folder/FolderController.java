package org.jbei.ice.lib.folder;

import org.apache.commons.lang3.StringUtils;
import org.jbei.ice.lib.access.PermissionsController;
import org.jbei.ice.lib.account.AccountController;
import org.jbei.ice.lib.account.AccountTransfer;
import org.jbei.ice.lib.bulkupload.BulkUploadController;
import org.jbei.ice.lib.bulkupload.BulkUploadInfo;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.dto.entry.PartData;
import org.jbei.ice.lib.dto.entry.Visibility;
import org.jbei.ice.lib.dto.folder.FolderAuthorization;
import org.jbei.ice.lib.dto.folder.FolderDetails;
import org.jbei.ice.lib.dto.folder.FolderType;
import org.jbei.ice.lib.group.GroupController;
import org.jbei.ice.lib.shared.ColumnField;
import org.jbei.ice.storage.DAOFactory;
import org.jbei.ice.storage.ModelToInfoFactory;
import org.jbei.ice.storage.hibernate.dao.AccountDAO;
import org.jbei.ice.storage.hibernate.dao.EntryDAO;
import org.jbei.ice.storage.hibernate.dao.FolderDAO;
import org.jbei.ice.storage.hibernate.dao.PermissionDAO;
import org.jbei.ice.storage.model.Account;
import org.jbei.ice.storage.model.Entry;
import org.jbei.ice.storage.model.Folder;
import org.jbei.ice.storage.model.Group;

import java.util.*;

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
            long folderSize = dao.getFolderSize(folder.getId(), null, true);
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
            long folderSize = dao.getFolderSize(folder.getId(), null, true);
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
        Set<Entry> results = entryDAO.retrieveVisibleEntries(null, groups, sort, asc, offset, limit, null);
        long visibleCount = entryDAO.visibleEntryCount(null, groups, null);

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

    public FolderDetails update(String userId, long folderId, FolderDetails details) {
        Folder folder = dao.get(folderId);
        if (folder == null)
            return null; // resource not found

        authorization.expectWrite(userId, folder);

        // check if status is being updated first
        if (folder.getType() == FolderType.TRANSFERRED && details.getType() == FolderType.PRIVATE) {
            return acceptTransferredFolder(userId, folder);
        }

        if (details.getType() == FolderType.PUBLIC && folder.getType() != FolderType.PUBLIC)
            return promoteFolder(userId, folder);

        if (details.getType() == FolderType.PRIVATE && folder.getType() != FolderType.PRIVATE)
            return demoteFolder(userId, folder);

        folder.setModificationTime(new Date());
        if (details.getName() != null && !folder.getName().equals(details.getName()))
            folder.setName(details.getName());

        if (details.isPropagatePermission() != folder.isPropagatePermissions()) {
            permissionsController.propagateFolderPermissions(userId, folder, details.isPropagatePermission());
            folder.setPropagatePermissions(details.isPropagatePermission());
        }

        return dao.update(folder).toDataTransferObject();
    }

    private FolderDetails acceptTransferredFolder(String userId, Folder folder) {
        authorization.expectAdmin(userId);

        if (folder.getType() != FolderType.TRANSFERRED)
            throw new IllegalArgumentException("Folder " + folder.getId() + " is not a transferred folder");

        // change the status and those of the entries contained to "ok"
        if (dao.setFolderEntryVisibility(folder.getId(), Visibility.OK) == 0)
            return null;

        folder.setType(FolderType.PRIVATE);
        folder.setOwnerEmail(userId);  // todo : review. making the owner the current admin accepting
        folder = dao.update(folder);
        return folder.toDataTransferObject();
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
            case TRANSFERRED:
                Folder folder = dao.get(folderId);
                if (folder == null)
                    return null;

                if (!accountController.isAdministrator(userId) && !folder.getOwnerEmail().equalsIgnoreCase(userId)) {
                    String errorMsg = userId + ": insufficient permissions to delete folder " + folderId;
                    Logger.warn(errorMsg);
                    return null;
                }

                details = folder.toDataTransferObject();
                long folderSize = dao.getFolderSize(folderId, null, true);
                details.setCount(folderSize);

                dao.delete(folder);
                permissionDAO.clearPermissions(folder);
                return details;

            default:
                Logger.error("Cannot delete folder of type " + type);
                return null;
        }
    }

    public FolderDetails createPersonalFolder(String userId, FolderDetails folderDetails) {
        if (folderDetails.getName() == null)
            return null;
        Folder folder = new Folder(folderDetails.getName());
        AccountTransfer owner = folderDetails.getOwner();
        if (owner != null && !StringUtils.isEmpty(owner.getEmail()) && accountController.isAdministrator(userId))
            folder.setOwnerEmail(owner.getEmail());
        else
            folder.setOwnerEmail(userId);

        folder.setType(FolderType.PRIVATE);
        folder.setCreationTime(new Date());
        folder = dao.create(folder);
        FolderDetails details = folder.toDataTransferObject();
        details.setCanEdit(true);
        return details;
    }

    public FolderDetails createTransferredFolder(FolderDetails folderDetails) {
        Folder folder = new Folder(folderDetails.getName());
        AccountTransfer owner = folderDetails.getOwner();
        if (owner != null)
            folder.setOwnerEmail(owner.getEmail());
        else
            folder.setOwnerEmail("transferred");
        folder.setType(FolderType.TRANSFERRED);
        folder.setCreationTime(new Date(folderDetails.getCreationTime()));
        folder = dao.create(folder);
        return folder.toDataTransferObject();
    }

    public ArrayList<FolderDetails> getUserFolders(String userId) {
        Account account = getAccount(userId);
        List<Folder> folders = dao.getFoldersByOwner(account);
        ArrayList<FolderDetails> folderDetails = new ArrayList<>();
        for (Folder folder : folders) {
            if (!folder.getOwnerEmail().equalsIgnoreCase(userId))
                continue;

            FolderDetails details = new FolderDetails(folder.getId(), folder.getName());
            long folderSize = dao.getFolderSize(folder.getId(), null, true);
            details.setCount(folderSize);
            details.setType(folder.getType());
            if (folder.getCreationTime() != null)
                details.setCreationTime(folder.getCreationTime().getTime());
            details.setCanEdit(true);
            folderDetails.add(details);
        }
        return folderDetails;
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

        Set<Group> groups = account.getGroups();
        groups.remove(groupController.createOrRetrievePublicGroup());
        Set<Folder> sharedFolders = DAOFactory.getPermissionDAO().retrieveFolderPermissions(account, groups);
        if (sharedFolders == null)
            return null;

        for (Folder folder : sharedFolders) {
            FolderDetails details = folder.toDataTransferObject();
            details.setType(folder.getType());
            long folderSize = dao.getFolderSize(folder.getId(), null, true);
            details.setCount(folderSize);
            if (folder.getCreationTime() != null)
                details.setCreationTime(folder.getCreationTime().getTime());
            folderDetails.add(details);
        }

        return folderDetails;
    }

    public ArrayList<FolderDetails> getTransferredFolders(String userId) {
        if (!accountController.isAdministrator(userId))
            return new ArrayList<>();

        List<Folder> transferredFolders = dao.getFoldersByType(FolderType.TRANSFERRED);
        ArrayList<FolderDetails> folderDetails = new ArrayList<>();

        for (Folder folder : transferredFolders) {
            FolderDetails details = folder.toDataTransferObject();
            long folderSize = dao.getFolderSize(folder.getId(), null, false);
            details.setCount(folderSize);
            if (folder.getCreationTime() != null)
                details.setCreationTime(folder.getCreationTime().getTime());
            folderDetails.add(details);
        }

        return folderDetails;
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

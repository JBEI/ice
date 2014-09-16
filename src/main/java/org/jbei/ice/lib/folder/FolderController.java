package org.jbei.ice.lib.folder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jbei.ice.ControllerException;
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
import org.jbei.ice.lib.dto.folder.FolderWrapper;
import org.jbei.ice.lib.dto.permission.AccessPermission;
import org.jbei.ice.lib.entry.EntryController;
import org.jbei.ice.lib.entry.model.Entry;
import org.jbei.ice.lib.group.Group;
import org.jbei.ice.lib.group.GroupController;
import org.jbei.ice.lib.shared.ColumnField;
import org.jbei.ice.servlet.ModelToInfoFactory;

/**
 * @author Hector Plahar
 */
public class FolderController {

    private final FolderDAO dao;
    private final AccountController accountController;
    private final PermissionDAO permissionDAO;
    private final PermissionsController permissionsController;
    private final BulkUploadController bulkUploadController;
    private final AccountDAO accountDAO;
    private final FolderAuthorization authorization;

    public FolderController() {
        dao = DAOFactory.getFolderDAO();
        accountController = new AccountController();
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
     * Retrieves folders that are marked "public"
     *
     * @return list of public folders on this site
     */
    public FolderWrapper getPublicFolders() {
        List<Folder> folders = dao.getFoldersByType(FolderType.PUBLIC);
        FolderWrapper result = new FolderWrapper();
        for (Folder folder : folders) {
            FolderDetails details = folder.toDataTransferObject();
            long folderSize = dao.getFolderSize(folder.getId());
            details.setCount(folderSize);
            result.getFolders().add(details);
        }

        result.sort();
        return result;
    }

    public FolderDetails getPublicEntries(ColumnField sort, int limit, int offset, boolean asc) {
        Group publicGroup = new GroupController().createOrRetrievePublicGroup();
        Set<Group> groups = new HashSet<>();
        groups.add(publicGroup);

        EntryDAO entryDAO = DAOFactory.getEntryDAO();
        Set<Entry> results = entryDAO.retrieveVisibleEntries(null, groups, sort, asc, offset, limit);
        FolderDetails details = new FolderDetails();

        for (Entry entry : results) {
            try {
                PartData info = ModelToInfoFactory.createTableViewData(null, entry, false);
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
            details.setName(info.getName());
            details.setCount(info.getCount());
            details.setId(info.getId());
            details.setType(FolderType.UPLOAD);
            folders.add(details);
        }
        return folders;
    }

    public Folder removeFolderContents(Account account, long folderId, ArrayList<Long> entryIds)
            throws ControllerException {
        boolean isAdministrator = accountController.isAdministrator(account);

        Folder folder = dao.get(folderId);

        if (folder.getType() == FolderType.PUBLIC && !isAdministrator) {
            throw new ControllerException(account.getEmail() + ": cannot modify non user folder " + folder.getName());
        }

        dao.removeFolderEntries(folder, entryIds);
        return folder;
    }


    public FolderDetails retrieveFolderContents(String userId, long folderId, ColumnField sort, boolean asc,
            int start, int limit) {
        Folder folder = dao.get(folderId);
        if (folder == null)
            return null;

        // should have permission to read folder (folder should be public, you should be an admin, or owner)
        if (!authorization.canRead(userId, folder)) {
            Logger.warn(userId + ": does not have permissions to read folder " + folder.getId());
            return null;
        }

        PermissionsController controller = new PermissionsController();
        FolderDetails details = new FolderDetails(folder.getId(), folder.getName());
        details.setType(folder.getType());
        long folderSize = dao.getFolderSize(folderId);
        details.setCount(folderSize);
        details.setDescription(folder.getDescription());
        if (folder.getCreationTime() != null)
            details.setCreated(folder.getCreationTime().getTime());
        details.setAccessPermissions(controller.retrieveSetFolderPermission(folder, false));
        details.setPublicReadAccess(controller.isPublicVisible(folder));
        Account owner = accountController.getByEmail(folder.getOwnerEmail());
        if (owner != null)
            details.setOwner(owner.toDataTransferObject());

        ArrayList<Entry> results = dao.retrieveFolderContents(folderId, sort, asc, start, limit);
        for (Entry entry : results) {
            PartData info = ModelToInfoFactory.createTableViewData(userId, entry, false);
            details.getEntries().add(info);
        }
        return details;
    }

    public FolderDetails delete(String userId, long folderId, FolderType type) {
        Account account = DAOFactory.getAccountDAO().getByEmail(userId);

        if (type == FolderType.UPLOAD) {
            // delete bulk upload
            BulkUploadController controller = new BulkUploadController();
            try {
                BulkUploadInfo info = controller.deleteDraftById(account, folderId);
                FolderDetails details = new FolderDetails();
                details.setId(info.getId());
                return details;
            } catch (ControllerException | PermissionException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                return null;
            }
        }

        Folder folder = dao.get(folderId);
        if (folder == null)
            return null;

        if (account.getType() != AccountType.ADMIN && !folder.getOwnerEmail().equalsIgnoreCase(account.getEmail())) {
            String errorMsg = account.getEmail() + ": insufficient permissions to delete folder " + folderId;
            Logger.warn(errorMsg);
            return null;
        }

        FolderDetails details = new FolderDetails(folder.getId(), folder.getName());
        long folderSize = dao.getFolderSize(folderId);
        details.setCount(folderSize);
        details.setDescription(folder.getDescription());

        dao.delete(folder);
        permissionDAO.clearPermissions(folder);
        return details;
    }

    public Folder addFolderContents(Account account, long id, ArrayList<Entry> entrys) throws ControllerException {
        Folder folder = dao.get(id);
        if (folder == null)
            throw new ControllerException("Could not retrieve folder with id " + id);
        folder = dao.addFolderContents(folder, entrys);
        if (folder.isPropagatePermissions()) {
            new PermissionsController().propagateFolderPermissions(account, folder, true);
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
            folder = dao.addFolderContents(folder, entrys);
            if (folder.isPropagatePermissions()) {
                try {
                    permissionsController.propagateFolderPermissions(account, folder, true);
                } catch (ControllerException e) {
                    return null;
                }
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
        folder.setCreationTime(new Date(System.currentTimeMillis()));
        folder = dao.create(folder);
        return folder.toDataTransferObject();
    }

    public FolderDetails createNewFolder(Account account, String name, String description, ArrayList<Long> contents)
            throws ControllerException {
        Folder folder = new Folder(name);
        folder.setOwnerEmail(account.getEmail());
        folder.setDescription(description);
        folder.setType(FolderType.PRIVATE);
        folder.setCreationTime(new Date(System.currentTimeMillis()));
        folder = dao.create(folder);
        FolderDetails details = new FolderDetails(folder.getId(), folder.getName());
        if (contents != null && !contents.isEmpty()) {
            ArrayList<Entry> entrys = new ArrayList<>(new EntryController().getEntriesByIdSet(account, contents));
            dao.addFolderContents(folder, entrys);
            details.setCount(contents.size());
        } else {
            details.setCount(0l);
        }
        details.setType(folder.getType());
        details.setDescription(folder.getDescription());

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

    public ArrayList<FolderDetails> getSharedUserFolders(String userId) {
        Account account = getAccount(userId);
        ArrayList<FolderDetails> folderDetails = new ArrayList<>();
        // get folders shared with this user. permissions are included if the user has write permissions for folder
        Set<Folder> sharedFolders = permissionsController.retrievePermissionFolders(account);
        if (sharedFolders != null) {
            for (Folder folder : sharedFolders) {
                ArrayList<AccessPermission> permissions = new ArrayList<>();
                ArrayList<AccessPermission> folderPermissions = permissionsController.retrieveSetFolderPermission(
                        folder,
                        false);
                for (AccessPermission accessPermission : folderPermissions) {
                    if (!accessPermission.isCanWrite())
                        continue;

                    // account either has direct write permissions
                    if (accessPermission.getArticle() == AccessPermission.Article.ACCOUNT
                            && accessPermission.getArticleId() == account.getId()) {
                        permissions.add(accessPermission);
                        break;
                    }

                    if (account.getGroups() == null || account.getGroups().isEmpty())
                        continue;

                    // or belongs to a group that has the write permissions
                    if (accessPermission.getArticle() == AccessPermission.Article.GROUP) {
                        Group group = new GroupController().getGroupById(
                                accessPermission.getArticleId());
                        if (group == null)
                            continue;

                        if (account.getGroups().contains(group)) {
                            permissions.add(accessPermission);
                            break;
                        }
                    }
                }

                FolderDetails details = new FolderDetails(folder.getId(), folder.getName());
                if (!permissions.isEmpty())
                    details.setAccessPermissions(permissions);

                details.setType(FolderType.SHARED);
                long folderSize = dao.getFolderSize(folder.getId());
                details.setCount(folderSize);
                details.setDescription(folder.getDescription());
                Account owner = accountController.getByEmail(folder.getOwnerEmail());
                if (owner != null) {
                    details.setOwner(owner.toDataTransferObject());
                }
                details.setPropagatePermission(folder.isPropagatePermissions());
                folderDetails.add(details);
            }
        }

        return folderDetails;
    }

    protected Account getAccount(String userId) {
        Account account = accountDAO.getByEmail(userId);
        if (account == null)
            throw new IllegalArgumentException("No account with id " + userId);
        return account;
    }

    // available folders
//    public ArrayList<FolderDetails> retrieveFoldersForUser(String userId) throws ControllerException {
//        ArrayList<FolderDetails> results = new ArrayList<>();
//        Account account = getAccount(userId);
//        if (account == null)
//            throw new IllegalArgumentException("Invalid user id \"" + userId + "\"");
//
//        // publicly visible collections are owned by the system
//        List<Folder> folders = getPublicFolders();
//        for (Folder folder : folders) {
//            long id = folder.getId();
//            FolderDetails details = new FolderDetails(id, folder.getName());
//            long folderSize = dao.getFolderSize(id);
//            details.setCount(folderSize);
//            details.setDescription(folder.getDescription());
//            details.setType(FolderType.PUBLIC);
//            if (account.getType() == AccountType.ADMIN) {
//                ArrayList<AccessPermission> accesses = permissionsController.retrieveSetFolderPermission(folder,
// false);
//                details.setAccessPermissions(accesses);
//            }
//            details.setPropagatePermission(folder.isPropagatePermissions());
//            results.add(details);
//        }
//
//        // get user folders
//        List<Folder> userFolders = dao.getFoldersByOwner(account);
//        if (userFolders != null) {
//            for (Folder folder : userFolders) {
//                long id = folder.getId();
//                FolderDetails details = new FolderDetails(id, folder.getName());
//                long folderSize = dao.getFolderSize(id);
//                details.setCount(folderSize);
//                details.setType(FolderType.PRIVATE);
//                details.setDescription(folder.getDescription());
//                ArrayList<AccessPermission> accesses = permissionsController.retrieveSetFolderPermission(folder,
//                                                                                                         false);
//                details.setAccessPermissions(accesses);
//                details.setPropagatePermission(folder.isPropagatePermissions());
//                details.setPublicReadAccess(permissionsController.isPublicVisible(folder));
//                results.add(details);
//            }
//        }
//
//
//        return results;
//    }

    /**
     * "Promote"s a collection into a system collection. This allows it to be categorised under "Collections"
     * This action is restricted to administrators
     *
     * @param account requesting account
     * @param id      collection id
     * @return true if promotion is successful false otherwise
     * @throws ControllerException
     */
    public boolean promoteFolder(Account account, long id) throws ControllerException {
        if (account.getType() != AccountType.ADMIN)
            throw new ControllerException(account.getEmail() + " does not have sufficient access privs for action");

        Folder folder = dao.get(id);
        if (folder.getType() == FolderType.PUBLIC)
            return true;

        folder.setType(FolderType.PUBLIC);
        folder.setOwnerEmail("");
        folder.setModificationTime(new Date(System.currentTimeMillis()));
        dao.update(folder);

        // remove all permissions for folder
        permissionsController.removeAllFolderPermissions(account.getEmail(), id);
        return true;
    }

    /**
     * Opposite of FolderController#demoteFolder(org.jbei.ice.lib.account.model.Account, long)
     * Removes the folder from the system collections menu
     *
     * @param account requesting account. should have administrator privileges
     * @param id      collection identifier
     * @return true on successful remote, false otherwise
     * @throws ControllerException
     */
    public boolean demoteFolder(Account account, long id) throws ControllerException {
        if (account.getType() != AccountType.ADMIN)
            throw new ControllerException(account.getEmail() + " does not have sufficient access privs for action");

        Folder folder = dao.get(id);
        if (folder.getType() != FolderType.PUBLIC)
            return true;

        folder.setType(FolderType.PRIVATE);
        folder.setModificationTime(new Date(System.currentTimeMillis()));
        folder.setOwnerEmail(account.getEmail());
        dao.update(folder);
        return true;
    }

    public boolean setPropagatePermissionForFolder(Account account, long folderId, boolean propagate)
            throws ControllerException {
        Folder folder = dao.get(folderId);
        if (folder == null)
            return false;

        if (!accountController.isAdministrator(account) &&
                !folder.getOwnerEmail().equalsIgnoreCase(account.getEmail()))
            return false;

        folder.setPropagatePermissions(propagate);
        folder.setModificationTime(new Date(System.currentTimeMillis()));
        dao.update(folder);
        return permissionsController.propagateFolderPermissions(account, folder, propagate);
    }

    public void test(List<FolderDetails> folders) {
        Logger.info(folders.get(0).getName());
    }
}

package org.jbei.ice.lib.folder;

import org.jbei.ice.controllers.ControllerFactory;
import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.lib.account.AccountController;
import org.jbei.ice.lib.account.model.Account;
import org.jbei.ice.lib.dao.DAOException;
import org.jbei.ice.lib.entry.model.Entry;
import org.jbei.ice.lib.group.Group;
import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.lib.permissions.PermissionException;
import org.jbei.ice.server.ModelToInfoFactory;
import org.jbei.ice.shared.ColumnField;
import org.jbei.ice.shared.dto.AccountType;
import org.jbei.ice.shared.dto.entry.EntryInfo;
import org.jbei.ice.shared.dto.folder.FolderDetails;
import org.jbei.ice.shared.dto.folder.FolderType;
import org.jbei.ice.shared.dto.permission.PermissionInfo;

import java.util.*;

/**
 * @author Hector Plahar
 */
public class FolderController {

    private final FolderDAO dao;
    private final AccountController accountController;

    public FolderController() {
        dao = new FolderDAO();
        accountController = ControllerFactory.getAccountController();
    }

    public Folder removeFolderContents(Account account, long folderId, ArrayList<Long> entryIds)
            throws ControllerException {
        boolean isAdministrator = accountController.isAdministrator(account);

        Folder folder;
        try {
            folder = dao.get(folderId);
        } catch (DAOException e) {
            throw new ControllerException(e);
        }

        if (folder.getType() == FolderType.PUBLIC && !isAdministrator) {
            throw new ControllerException(account.getEmail() + ": cannot modify non user folder " + folder.getName());
        }

        try {
            dao.removeFolderEntries(folder, entryIds);
        } catch (DAOException e) {
            throw new ControllerException(e);
        }
        return folder;
    }

    /**
     * @return folders that are shared with everyone on the site. These are listed under "Collections".
     * @throws ControllerException
     */
    protected List<Folder> getPublicFolders() throws ControllerException {
        Set<Folder> folders = new HashSet<>();
        try {
            folders.addAll(dao.getFoldersByType(FolderType.PUBLIC));
            return new ArrayList<>(folders);
        } catch (DAOException de) {
            throw new ControllerException(de);
        }
    }

    public Long getFolderSize(long id) throws ControllerException {
        try {
            return dao.getFolderSize(id);
        } catch (DAOException e) {
            throw new ControllerException(e);
        }
    }

    public Folder getFolderById(long folderId) throws ControllerException {
        try {
            return dao.get(folderId);
        } catch (DAOException e) {
            throw new ControllerException(e);
        }
    }

    protected boolean canReadFolderContents(Account account, Folder folder) {
        if (folder.getType() == FolderType.PUBLIC)
            return true;

        if (account.getType() == AccountType.ADMIN)
            return true;

        if (account.getEmail().equals(folder.getOwnerEmail()))
            return true;

        // now check actual permissions
        Set<Group> groups = account.getGroups();

        try {
            ArrayList<PermissionInfo> permissionInfos =
                    ControllerFactory.getPermissionController().retrieveSetFolderPermission(folder);
            for (PermissionInfo info : permissionInfos) {
                switch (info.getArticle()) {
                    case ACCOUNT:
                        if (account.getId() == info.getArticleId())
                            return true;
                        break;

                    case GROUP:
                        for (Group group : groups) {
                            if (group.getId() == info.getArticleId())
                                return true;
                        }
                        break;

                    default:
                        continue;
                }
            }
        } catch (ControllerException e) {
            Logger.error(e);
            return false;
        }

        return false;
    }

    public FolderDetails retrieveFolderContents(Account account, long folderId, ColumnField sort, boolean asc,
                                                int start, int limit) throws ControllerException {
        try {
            Folder folder = getFolderById(folderId);
            if (folder == null)
                return null;

            // should have permission to read folder (folder should be public, you should be an admin, or owner)
            if (!canReadFolderContents(account, folder)) {
                Logger.warn(account.getEmail() + ": does not have permissions to read folder " + folder.getId());
                return null;
            }

            FolderDetails details = new FolderDetails(folder.getId(), folder.getName());
            details.setType(folder.getType());
            long folderSize = getFolderSize(folderId);
            details.setCount(folderSize);
            details.setDescription(folder.getDescription());
            details.setPermissions(ControllerFactory.getPermissionController().retrieveSetFolderPermission(folder));
            Account owner = accountController.getByEmail(folder.getOwnerEmail());
            details.setOwner(Account.toDTO(owner));

            ArrayList<Entry> results = dao.retrieveFolderContents(folderId, sort, asc, start, limit);
            for (Entry entry : results) {
                EntryInfo info = ModelToInfoFactory.createTableViewData(entry, false);
                details.getEntries().add(info);
            }
            return details;
        } catch (DAOException de) {
            throw new ControllerException(de);
        }
    }

    public FolderDetails delete(Account account, long folderId) throws ControllerException {
        Folder folder;
        try {
            folder = dao.get(folderId);
        } catch (DAOException e) {
            throw new ControllerException(e);
        }

        if (folder == null)
            return null;

        if (account.getType() != AccountType.ADMIN && !folder.getOwnerEmail().equalsIgnoreCase(account.getEmail())) {
            String errorMsg = account.getEmail() + ": does not have sufficient permissions to delete folder";
            Logger.warn(errorMsg);
        }

        FolderDetails details = new FolderDetails(folder.getId(), folder.getName());
        long folderSize = getFolderSize(folderId);
        details.setCount(folderSize);
        details.setDescription(folder.getDescription());

        try {
            dao.delete(folder);
            ControllerFactory.getPermissionController().clearFolderPermissions(account, folder);
            return details;
        } catch (DAOException | PermissionException e) {
            throw new ControllerException(e);
        }
    }

    public Folder addFolderContents(long id, ArrayList<Entry> entrys) throws ControllerException {
        try {
            Folder folder = dao.get(id);
            if (folder == null)
                throw new ControllerException("Could not retrieve folder with id " + id);
            dao.addFolderContents(folder, entrys);
            return folder;
        } catch (DAOException e) {
            throw new ControllerException(e);
        }
    }

    public Folder createNewFolder(String owner, String name, String description) throws ControllerException {
        Folder folder = new Folder(name);
        folder.setOwnerEmail(owner);
        folder.setDescription(description);
        folder.setCreationTime(new Date(System.currentTimeMillis()));
        try {
            return dao.save(folder);
        } catch (DAOException e) {
            throw new ControllerException(e);
        }
    }

    public Folder updateFolder(Folder folder) throws ControllerException {
        try {
            return dao.save(folder);
        } catch (DAOException e) {
            throw new ControllerException();
        }
    }

    public List<Folder> getFoldersByEntry(Entry entry) throws ControllerException {
        try {
            return dao.getFoldersByEntry(entry);
        } catch (DAOException e) {
            throw new ControllerException(e);
        }
    }

    public ArrayList<FolderDetails> retrieveFoldersForUser(Account account) throws ControllerException {
        ArrayList<FolderDetails> results = new ArrayList<>();

        try {
            // publicly visible collections are owned by the system
            List<Folder> folders = getPublicFolders();
            for (Folder folder : folders) {
                long id = folder.getId();
                FolderDetails details = new FolderDetails(id, folder.getName());
                long folderSize = getFolderSize(id);
                details.setCount(folderSize);
                details.setDescription(folder.getDescription());
                details.setType(FolderType.PUBLIC);
                if (account.getType() == AccountType.ADMIN) {
                    ArrayList<PermissionInfo> infos = ControllerFactory.getPermissionController().
                            retrieveSetFolderPermission(folder);
                    details.setPermissions(infos);
                }
                results.add(details);
            }

            // get user folders
            List<Folder> userFolders = dao.getFoldersByOwner(account);
            if (userFolders != null) {
                for (Folder folder : userFolders) {
                    long id = folder.getId();
                    FolderDetails details = new FolderDetails(id, folder.getName());
                    long folderSize = getFolderSize(id);
                    details.setCount(folderSize);
                    details.setType(FolderType.PRIVATE);
                    details.setDescription(folder.getDescription());
                    ArrayList<PermissionInfo> infos = ControllerFactory.getPermissionController().
                            retrieveSetFolderPermission(folder);
                    details.setPermissions(infos);
                    results.add(details);
                }
            }

            // get folders shared with this user
            Set<Folder> sharedFolders = ControllerFactory.getPermissionController().retrievePermissionFolders(account);
            if (sharedFolders != null) {
                for (Folder folder : sharedFolders) {
                    if (userFolders != null && userFolders.contains(folder))
                        continue;

                    FolderDetails details = new FolderDetails(folder.getId(), folder.getName());
                    details.setType(FolderType.SHARED);
                    long folderSize = getFolderSize(folder.getId());
                    details.setCount(folderSize);
                    details.setDescription(folder.getDescription());
                    Account owner = accountController.getByEmail(folder.getOwnerEmail());
                    if (owner != null) {
                        details.setOwner(Account.toDTO(owner));
                    }
                    results.add(details);
                }
            }
        } catch (DAOException de) {
            throw new ControllerException(de);
        }

        return results;
    }

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

        try {
            Folder folder = dao.get(id);
            if (folder.getType() == FolderType.PUBLIC)
                return true;

            folder.setType(FolderType.PUBLIC);
            folder.setModificationTime(new Date(System.currentTimeMillis()));
            dao.update(folder);

            // remove account permissions for this administrator
            PermissionInfo info = new PermissionInfo();
            info.setType(PermissionInfo.Type.READ_FOLDER);
            info.setArticle(PermissionInfo.Article.ACCOUNT);
            info.setArticleId(account.getId());
            info.setTypeId(id);
            ControllerFactory.getPermissionController().removePermission(account, info);
            return true;
        } catch (DAOException e) {
            throw new ControllerException(e);
        }
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

        try {
            Folder folder = dao.get(id);
            if (folder.getType() != FolderType.PUBLIC)
                return true;

            folder.setType(FolderType.PRIVATE);
            folder.setModificationTime(new Date(System.currentTimeMillis()));
            folder.setOwnerEmail(account.getEmail());
            dao.update(folder);
            return true;
        } catch (DAOException e) {
            throw new ControllerException(e);
        }
    }

    public void upgradeFolders() throws ControllerException {
        Logger.info("Upgrading collections...please wait");
        try {
            for (long id : dao.getAllFolderIds()) {
                Folder folder = dao.get(id);
                String owner = folder.getOwnerEmail();
                if ("system".equalsIgnoreCase(owner)) {
                    folder.setOwnerEmail("");
                    folder.setType(FolderType.PUBLIC);
                    dao.update(folder);
                } else {
                    Account account = accountController.getByEmail(owner);
                    if (account != null) {
                        ArrayList<PermissionInfo> infos = ControllerFactory.getPermissionController().
                                retrieveSetFolderPermission(folder);
                        if (infos != null) {
                            for (PermissionInfo info : infos) {
                                if (info.isCanRead() || info.isCanWrite()) {
                                    // skip setting update to shared if the permission is associated with the owner
                                    if (info.getArticle() == PermissionInfo.Article.ACCOUNT
                                            && info.getArticleId() == account.getId()) {
                                        folder.setType(FolderType.PRIVATE);
                                        continue;
                                    }

                                    folder.setType(FolderType.SHARED);
                                    break;
                                }
                            }
                        } else
                            folder.setType(FolderType.PRIVATE);
                        dao.update(folder);
                    }
                }
            }
            Logger.info("Collections upgrade completed");
        } catch (DAOException de) {
            throw new ControllerException(de);
        }
    }
}

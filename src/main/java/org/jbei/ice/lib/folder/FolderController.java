package org.jbei.ice.lib.folder;

import org.jbei.ice.controllers.ControllerFactory;
import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.lib.account.AccountController;
import org.jbei.ice.lib.account.model.Account;
import org.jbei.ice.lib.dao.DAOException;
import org.jbei.ice.lib.entry.model.Entry;
import org.jbei.ice.lib.permissions.PermissionException;
import org.jbei.ice.lib.permissions.PermissionsController;
import org.jbei.ice.server.ModelToInfoFactory;
import org.jbei.ice.shared.ColumnField;
import org.jbei.ice.shared.dto.AccountType;
import org.jbei.ice.shared.dto.entry.EntryInfo;
import org.jbei.ice.shared.dto.folder.FolderDetails;
import org.jbei.ice.shared.dto.folder.FolderShareType;
import org.jbei.ice.shared.dto.folder.FolderStatus;
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
        Account systemAccount = accountController.getSystemAccount();
        boolean isAdministrator = accountController.isAdministrator(account);

        Folder folder;
        try {
            folder = dao.get(folderId);
        } catch (DAOException e) {
            throw new ControllerException(e);
        }

        boolean isSystemFolder = (folder.getOwnerEmail().equals(systemAccount.getEmail())
                || folder.getStatus() == FolderStatus.PINNED);

        if (isSystemFolder && !isAdministrator) {
            throw new ControllerException("Cannot modify non user folder " + folder.getName());
        }

        try {
            dao.removeFolderEntries(folder, entryIds);
        } catch (DAOException e) {
            throw new ControllerException(e);
        }
        return folder;
    }

    public List<Folder> getFoldersByOwner(Account userAccount) throws ControllerException {
        try {
            return dao.getFoldersByOwner(userAccount);
        } catch (DAOException e) {
            throw new ControllerException(e);
        }
    }

    protected List<Folder> getSystemFolders() throws ControllerException {
        Set<Folder> folders = new HashSet<>();
        try {
            folders.addAll(dao.getFoldersByStatus(FolderStatus.PINNED));
            Account system = ControllerFactory.getAccountController().getSystemAccount();
            folders.addAll(dao.getFoldersByOwner(system));
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

    public FolderDetails retrieveFolderContents(long folderId, ColumnField sort, boolean asc,
                                                int start, int limit) throws ControllerException {
        try {
            Folder folder = getFolderById(folderId);
            if (folder == null)
                return null;

            Account system = accountController.getSystemAccount();
            boolean isSystem = system.getEmail().equals(folder.getOwnerEmail());
            FolderDetails details = new FolderDetails(folder.getId(), folder.getName(), isSystem);
            long folderSize = getFolderSize(folderId);
            details.setCount(folderSize);
            details.setDescription(folder.getDescription());

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

    public void delete(Account account, Folder folder) throws ControllerException, PermissionException {
        PermissionsController controller = ControllerFactory.getPermissionController();
        if (!controller.hasWritePermission(account, folder))
            throw new PermissionException("No write permission for folder");

        try {
            dao.delete(folder);
            controller.clearFolderPermissions(account, folder);
        } catch (DAOException e) {
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

        // publicly visible collections are owned by the system
        List<Folder> folders = getSystemFolders();
        for (Folder folder : folders) {
            long id = folder.getId();
            FolderDetails details = new FolderDetails(id, folder.getName(), true);
            long folderSize = getFolderSize(id);
            details.setCount(folderSize);
            details.setDescription(folder.getDescription());
            details.setShareType(FolderShareType.PUBLIC);
            if (account.getType() == AccountType.ADMIN) {
                ArrayList<PermissionInfo> infos = ControllerFactory.getPermissionController().
                        retrieveSetFolderPermission(account, folder);
                details.setPermissions(infos);
            }
            results.add(details);
        }

        // get user folder
        List<Folder> userFolders = getFoldersByOwner(account);
        if (userFolders != null) {
            for (Folder folder : userFolders) {
                long id = folder.getId();
                FolderDetails details = new FolderDetails(id, folder.getName(), false);
                long folderSize = getFolderSize(id);
                details.setCount(folderSize);
                details.setShareType(FolderShareType.PRIVATE);
                details.setDescription(folder.getDescription());
                ArrayList<PermissionInfo> infos = ControllerFactory.getPermissionController().
                        retrieveSetFolderPermission(account, folder);
                details.setPermissions(infos);
                results.add(details);
            }
        }

        // get folders shared with this user
        Set<Folder> sharedFolders = ControllerFactory.getPermissionController().retrievePermissionFolders(account);
        if (sharedFolders != null) {
            for (Folder folder : sharedFolders) {
                if (userFolders.contains(folder))
                    continue;

                long id = folder.getId();
                boolean isSystemFolder = (account.getType() != AccountType.ADMIN);
                FolderDetails details = new FolderDetails(id, folder.getName(), isSystemFolder);
                details.setShareType(FolderShareType.SHARED);
                long folderSize = getFolderSize(id);
                details.setCount(folderSize);
                details.setDescription(folder.getDescription());
                Account owner = accountController.getByEmail(folder.getOwnerEmail());
                if (owner != null) {
                    details.setOwner(Account.toDTO(owner));
                }
                results.add(details);
            }
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
            if (folder.getOwnerEmail().equalsIgnoreCase(AccountController.SYSTEM_ACCOUNT_EMAIL)
                    || folder.getStatus() == FolderStatus.PINNED)
                return true;

            folder.setStatus(FolderStatus.PINNED);
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
            if (!folder.getOwnerEmail().equalsIgnoreCase(AccountController.SYSTEM_ACCOUNT_EMAIL)
                    && folder.getStatus() != FolderStatus.PINNED)
                return true;

            folder.setStatus(FolderStatus.UNPINNED);
            folder.setModificationTime(new Date(System.currentTimeMillis()));
            dao.update(folder);
            return true;
        } catch (DAOException e) {
            throw new ControllerException(e);
        }
    }
}

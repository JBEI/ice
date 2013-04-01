package org.jbei.ice.lib.folder;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

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
import org.jbei.ice.shared.dto.entry.EntryInfo;
import org.jbei.ice.shared.dto.folder.FolderDetails;
import org.jbei.ice.shared.dto.folder.FolderShareType;

/**
 * @author Hector Plahar
 */
public class FolderController {

    private final FolderDAO dao;
    private final AccountController accountController;
//    private final PermissionsController permissionsController;

    public FolderController() {
        dao = new FolderDAO();
        accountController = ControllerFactory.getAccountController();
//        permissionsController = ControllerFactory.getPermissionController();
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

        boolean isSystemFolder = folder.getOwnerEmail().equals(systemAccount.getEmail());

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
        AccountController controller = ControllerFactory.getAccountController();
        ArrayList<FolderDetails> results = new ArrayList<>();

        // publicly visible collections are owned by the system
        Account system = controller.getSystemAccount();
        List<Folder> folders = getFoldersByOwner(system);
        for (Folder folder : folders) {
            long id = folder.getId();
            FolderDetails details = new FolderDetails(id, folder.getName(), true);
            long folderSize = getFolderSize(id);
            details.setCount(folderSize);
            details.setDescription(folder.getDescription());
            details.setShareType(FolderShareType.PUBLIC);
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
                results.add(details);
            }
        }

        // get folders shared with this user
        Set<Folder> sharedFolders = ControllerFactory.getPermissionController().retrievePermissionFolders(account);
        if (sharedFolders != null) {
            for (Folder folder : sharedFolders) {
                long id = folder.getId();
                FolderDetails details = new FolderDetails(id, folder.getName(), true);
                details.setShareType(FolderShareType.SHARED);
                long folderSize = getFolderSize(id);
                details.setCount(folderSize);
                details.setDescription(folder.getDescription());
                results.add(details);
            }
        }

        return results;
    }
}

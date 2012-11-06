package org.jbei.ice.lib.folder;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.lib.account.AccountController;
import org.jbei.ice.lib.account.model.Account;
import org.jbei.ice.lib.dao.DAOException;
import org.jbei.ice.lib.entry.model.Entry;

/**
 * @author Hector Plahar
 */
public class FolderController {

    private final FolderDAO dao;

    public FolderController() {
        dao = new FolderDAO();
    }

    public Folder removeFolderContents(Account account, long folderId,
            ArrayList<Long> entryIds) throws ControllerException {

        AccountController controller = new AccountController();
        Account systemAccount = controller.getSystemAccount();
        boolean isModerator = controller.isAdministrator(account);

        Folder folder;
        try {
            folder = dao.get(folderId);
        } catch (DAOException e) {
            throw new ControllerException(e);
        }

        boolean isSystemFolder = folder.getOwnerEmail().equals(
                systemAccount.getEmail());

        if (isSystemFolder && !isModerator) {
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

    public ArrayList<Long> getFolderContents(long folderId) throws ControllerException {
        try {
            return dao.getFolderContents(folderId);
        } catch (DAOException e) {
            throw new ControllerException(e);
        }
    }

    public void delete(Folder folder) throws ControllerException {
        try {
            dao.delete(folder);
        } catch (DAOException e) {
            throw new ControllerException(e);
        }
    }

    public Folder addFolderContents(long id, ArrayList<Entry> entrys) throws ControllerException {
        try {
            Folder folder = dao.get(id);
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
}

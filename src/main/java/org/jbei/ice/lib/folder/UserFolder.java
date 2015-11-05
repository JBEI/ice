package org.jbei.ice.lib.folder;

import org.jbei.ice.lib.dto.folder.FolderAuthorization;
import org.jbei.ice.lib.dto.folder.FolderDetails;
import org.jbei.ice.storage.DAOFactory;
import org.jbei.ice.storage.model.Account;
import org.jbei.ice.storage.model.Folder;

/**
 * @author Hector Plahar
 */
public class UserFolder {

    private final String userId;

    public UserFolder(String userId) {
        this.userId = userId;
    }

    public FolderDetails getFolder(long folderId) {
        Folder folder = DAOFactory.getFolderDAO().get(folderId);
        if (folder == null)
            throw new IllegalArgumentException("Folder with id " + folderId + " does not exist");

        FolderAuthorization folderAuthorization = new FolderAuthorization();
        folderAuthorization.expectRead(this.userId, folder);
        FolderDetails folderDetails = folder.toDataTransferObject();

        Account owner = DAOFactory.getAccountDAO().getByEmail(folder.getOwnerEmail());
        if (owner != null) {
            folderDetails.setOwner(owner.toDataTransferObject());
        }
        return folderDetails;
    }
}

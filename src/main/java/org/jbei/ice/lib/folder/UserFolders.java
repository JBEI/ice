package org.jbei.ice.lib.folder;

import org.jbei.ice.lib.dto.folder.FolderAuthorization;
import org.jbei.ice.lib.dto.folder.FolderDetails;
import org.jbei.ice.lib.dto.folder.FolderType;
import org.jbei.ice.lib.dto.sample.SampleRequest;
import org.jbei.ice.storage.DAOFactory;
import org.jbei.ice.storage.hibernate.dao.AccountDAO;
import org.jbei.ice.storage.hibernate.dao.FolderDAO;
import org.jbei.ice.storage.model.Account;
import org.jbei.ice.storage.model.Folder;
import org.jbei.ice.storage.model.SampleCreateModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Represents a folder created or owned by a user
 *
 * @author Hector Plahar
 */
public class UserFolders {

    private final String userId;
    private final FolderDAO dao;
    private final AccountDAO accountDAO;
    private final FolderAuthorization authorization;

    /**
     * @param userId identifier for owner or creator of the folder
     */
    public UserFolders(String userId) {
        this.userId = userId;
        this.dao = DAOFactory.getFolderDAO();
        this.accountDAO = DAOFactory.getAccountDAO();
        this.authorization = new FolderAuthorization();
    }

    public FolderDetails get(long folderId) {
        Folder folder = this.dao.get(folderId);
        if (folder == null)
            throw new IllegalArgumentException("Folder with id " + folderId + " does not exist");

        authorization.expectRead(this.userId, folder);
        FolderDetails folderDetails = folder.toDataTransferObject();

        Account owner = accountDAO.getByEmail(folder.getOwnerEmail());
        if (owner != null) {
            folderDetails.setOwner(owner.toDataTransferObject());
        }

        // get sample details
        if (folder.getType() == FolderType.SAMPLE) {
            Optional<SampleCreateModel> optional = DAOFactory.getSampleCreateModelDAO().getByFolder(folder);
            if (optional.isPresent()) {
                SampleCreateModel model = optional.get();
                SampleRequest request = new SampleRequest();
                request.setId(model.getId());
                request.setStatus(model.getStatus());
                folderDetails.setSampleRequest(request);
            }
        }

        return folderDetails;
    }

    public List<FolderDetails> getList(String requestor) {
        Account account = accountDAO.getByEmail(this.userId);
        List<Folder> folders = this.dao.getFoldersByOwner(account);
        List<FolderDetails> results = new ArrayList<>();

        // is requesting user same as folder owner?
        boolean check = !requestor.equalsIgnoreCase(userId);
        for (Folder folder : folders) {
            if (check && !authorization.canRead(requestor, folder))
                continue;

            results.add(folder.toDataTransferObject());
        }

        return results;
    }
}


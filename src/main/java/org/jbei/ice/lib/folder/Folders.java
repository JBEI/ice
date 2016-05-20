package org.jbei.ice.lib.folder;

import org.jbei.ice.lib.account.AccountTransfer;
import org.jbei.ice.lib.dto.folder.FolderDetails;
import org.jbei.ice.lib.dto.folder.FolderType;
import org.jbei.ice.lib.group.GroupController;
import org.jbei.ice.storage.DAOFactory;
import org.jbei.ice.storage.hibernate.dao.AccountDAO;
import org.jbei.ice.storage.hibernate.dao.FolderDAO;
import org.jbei.ice.storage.hibernate.dao.RemoteAccessModelDAO;
import org.jbei.ice.storage.model.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * ICE Folders
 *
 * @author Hector Plahar
 */
public class Folders {

    private final FolderDAO dao;
    private final String userId;
    private final RemoteAccessModelDAO remoteAccessModelDAO;
    private final AccountDAO accountDAO;

    public Folders(String userId) {
        this.dao = DAOFactory.getFolderDAO();
        this.accountDAO = DAOFactory.getAccountDAO();
        this.userId = userId;
        this.remoteAccessModelDAO = DAOFactory.getRemoteAccessModelDAO();
    }

    /**
     * Retrieves list of folders that specified user has write privileges on
     *
     * @return list of folders
     */
    public ArrayList<FolderDetails> getCanEditFolders() {
        Account account = this.accountDAO.getByEmail(userId);
        Set<Group> accountGroups = new HashSet<>(account.getGroups());
        GroupController controller = new GroupController();
        Group everybodyGroup = controller.createOrRetrievePublicGroup();
        accountGroups.add(everybodyGroup);

        List<Folder> folders = dao.getCanEditFolders(account, accountGroups);
        ArrayList<FolderDetails> result = new ArrayList<>();

        for (Folder folder : folders) {
            FolderDetails details = folder.toDataTransferObject();
            if (folder.getType() == FolderType.REMOTE) {
                RemoteAccessModel model = remoteAccessModelDAO.getByFolder(account, folder);
                if (model == null) {
                    result.add(details);
                    continue;
                }

                AccountTransfer owner = new AccountTransfer();
                owner.setEmail(model.getRemoteClientModel().getEmail());
                details.setOwner(owner);
                RemotePartner remotePartner = model.getRemoteClientModel().getRemotePartner();
                details.setRemotePartner(remotePartner.toDataTransferObject());
            }

            result.add(details);
        }

        return result;
    }

    public List<FolderDetails> filter(String token, int limit) {
        List<Folder> list = dao.filterByName(token, limit);
        List<FolderDetails> results = new ArrayList<>();
        for (Folder folder : list) {
            results.add(folder.toDataTransferObject());
        }
        return results;
    }
}

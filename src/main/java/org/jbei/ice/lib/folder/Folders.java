package org.jbei.ice.lib.folder;

import org.jbei.ice.lib.dto.folder.FolderAuthorization;
import org.jbei.ice.lib.dto.folder.FolderDetails;
import org.jbei.ice.lib.group.GroupController;
import org.jbei.ice.storage.DAOFactory;
import org.jbei.ice.storage.hibernate.dao.FolderDAO;
import org.jbei.ice.storage.model.Account;
import org.jbei.ice.storage.model.Folder;
import org.jbei.ice.storage.model.Group;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Hector Plahar
 */
public class Folders {

    private final FolderDAO dao;
    private final FolderAuthorization authorization;
    private final String userId;

    public Folders(String userId) {
        this.dao = DAOFactory.getFolderDAO();
        this.authorization = new FolderAuthorization();
        this.userId = userId;
    }

    /**
     * Retrieves list of folders that specified user has write privileges on
     *
     * @return list of folders
     */
    public ArrayList<FolderDetails> getCanEditFolders(String userId) {
        Account account = DAOFactory.getAccountDAO().getByEmail(userId);
        Set<Group> accountGroups = new HashSet<>(account.getGroups());
        GroupController controller = new GroupController();
        Group everybodyGroup = controller.createOrRetrievePublicGroup();
        accountGroups.add(everybodyGroup);

        List<Folder> folders = dao.getCanEditFolders(account, accountGroups);
        ArrayList<FolderDetails> result = new ArrayList<>();

        for (Folder folder : folders) {
            result.add(folder.toDataTransferObject());
        }

        return result;
    }


}

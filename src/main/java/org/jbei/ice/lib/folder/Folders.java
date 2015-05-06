package org.jbei.ice.lib.folder;

import org.jbei.ice.lib.account.model.Account;
import org.jbei.ice.lib.dao.DAOFactory;
import org.jbei.ice.lib.dao.hibernate.FolderDAO;
import org.jbei.ice.lib.dto.folder.FolderDetails;
import org.jbei.ice.lib.group.Group;
import org.jbei.ice.lib.group.GroupController;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Hector Plahar
 */
public class Folders {

    private final FolderDAO dao;

    public Folders() {
        this.dao = DAOFactory.getFolderDAO();
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

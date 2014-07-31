package org.jbei.ice.lib.dto.folder;

import java.util.HashSet;
import java.util.Set;

import org.jbei.ice.lib.access.Authorization;
import org.jbei.ice.lib.access.PermissionsController;
import org.jbei.ice.lib.account.AccountType;
import org.jbei.ice.lib.account.model.Account;
import org.jbei.ice.lib.dao.DAOFactory;
import org.jbei.ice.lib.folder.Folder;

/**
 * @author Hector Plahar
 */
public class FolderAuthorization extends Authorization<Folder> {

    public FolderAuthorization() {
        super(DAOFactory.getFolderDAO());
    }

    @Override
    public String getOwner(Folder folder) {
        return folder.getOwnerEmail();
    }

    public boolean canRead(String userId, Folder folder) {
        Account account = getAccount(userId);
        if (account == null)
            return false;

        if (folder.getType() == FolderType.PUBLIC)
            return true;

        if (account.getType() == AccountType.ADMIN)
            return true;

        if (userId.equals(folder.getOwnerEmail()))
            return true;

        // now check actual permissions
        Set<Folder> folders = new HashSet<>();
        folders.add(folder);
        PermissionsController controller = new PermissionsController();
        if (controller.groupHasReadPermission(account.getGroups(), folders)
                || controller.groupHasWritePermission(account.getGroups(), folders))
            return true;

        return controller.accountHasReadPermission(account, folders)
                || controller.accountHasWritePermission(account, folders);
    }
}

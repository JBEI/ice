package org.jbei.ice.dto.folder;

import org.jbei.ice.access.Authorization;
import org.jbei.ice.access.PermissionsController;
import org.jbei.ice.storage.DAOFactory;
import org.jbei.ice.storage.model.AccountModel;
import org.jbei.ice.storage.model.Folder;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * Authorization specific to folder objects
 *
 * @author Hector Plahar
 */
public class FolderAuthorization extends Authorization<Folder> {

    private final PermissionsController controller = new PermissionsController();

    public FolderAuthorization() {
        super(DAOFactory.getFolderDAO());
    }

    @Override
    public String getOwner(Folder folder) {
        return folder.getOwnerEmail();
    }

    public boolean canRead(String userId, Folder folder) {
        if (controller.isPublicVisible(folder))
            return true;

        AccountModel account = getAccount(userId);
        if (account == null)
            return false;

        if (folder.getType() == FolderType.PUBLIC)
            return true;

        if (super.canRead(userId, folder))
            return true;

        // now check actual permissions
        Set<Folder> folders = new HashSet<>();
        folders.add(folder);
        if (controller.groupHasReadPermission(new ArrayList<>(account.getGroups()), folders)
                || controller.groupHasWritePermission(new ArrayList<>(account.getGroups()), folders))
            return true;

        return controller.accountHasReadPermission(account, folders)
                || controller.accountHasWritePermission(account, folders);
    }

    public boolean canWrite(String userId, Folder folder) {
        AccountModel account = getAccount(userId);
        if (account == null)
            return false;

        if (folder.getType() == FolderType.SAMPLE && !isAdmin(userId))
            return false;

        if (super.canWrite(userId, folder))
            return true;

        // now check actual permissions
        Set<Folder> folders = new HashSet<>();
        folders.add(folder);
        return controller.groupHasWritePermission(new ArrayList<>(account.getGroups()), folders)
                || controller.accountHasWritePermission(account, folders);
    }
}

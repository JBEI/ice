package org.jbei.ice.lib.entry;

import org.jbei.ice.lib.access.Authorization;
import org.jbei.ice.lib.access.PermissionsController;
import org.jbei.ice.lib.dto.folder.FolderType;
import org.jbei.ice.lib.group.GroupController;
import org.jbei.ice.storage.DAOFactory;
import org.jbei.ice.storage.hibernate.dao.PermissionDAO;
import org.jbei.ice.storage.model.Account;
import org.jbei.ice.storage.model.Entry;
import org.jbei.ice.storage.model.Folder;
import org.jbei.ice.storage.model.Group;

import java.util.Set;

/**
 * @author Hector Plahar
 */
public class EntryAuthorization extends Authorization<Entry> {

    private final PermissionDAO permissionDAO;
    private final GroupController groupController;

    public EntryAuthorization() {
        super(DAOFactory.getEntryDAO());
        permissionDAO = DAOFactory.getPermissionDAO();
        groupController = new GroupController();
    }

    public boolean canRead(String userId, Entry entry) {
        // super checks for owner or admin
        if (userId == null) {
            return new PermissionsController().isPubliclyVisible(entry);
        }

        if (super.canRead(userId, entry) || super.canWrite(userId, entry))
            return true;

        Account account = getAccount(userId);

        // get groups for account. if account is null, this will return everyone group
        Set<Group> accountGroups = groupController.getAllGroups(account);

        // check read permission through group membership
        // ie. belongs to group that has read privileges for entry (or a group whose parent group does)
        if (permissionDAO.hasPermissionMulti(entry, null, null, accountGroups, true, false))
            return true;

        if (permissionDAO.hasPermissionMulti(entry, null, null, accountGroups, false, true))
            return true;

        // check explicit read permission
        if (permissionDAO.hasPermissionMulti(entry, null, account, null, true, false))
            return true;

        Set<Folder> entryFolders = entry.getFolders();

        // is in a public folder
        for (Folder folder : entryFolders) {
            if (folder.getType() == FolderType.PUBLIC)
                return true;
        }

        // can any group that account belongs to read any folder that entry is contained in?
        if (permissionDAO.hasPermissionMulti(null, entryFolders, null, accountGroups, true, false))
            return true;

        // can account read any folder that entry is contained in?
        if (permissionDAO.hasPermissionMulti(null, entryFolders, account, null, true, false))
            return true;

        return canWrite(userId, entry);
    }

    @Override
    public boolean canWrite(String userId, Entry entry) {
        if (super.canWrite(userId, entry))
            return true;

        Account account = getAccount(userId);

        // check explicit write permission
        if (permissionDAO.hasPermissionMulti(entry, null, account, null, false, true))
            return true;

        return false;
    }

    public boolean canWriteThoroughCheck(String userId, Entry entry) {
        if (userId == null)
            return false;

        // super checks for admin or owner
        if (super.canWrite(userId, entry))
            return true;

        Account account = getAccount(userId);

        // check write accounts for entry
        if (permissionDAO.hasPermission(entry, null, null, account, null, false, true))
            return true;

        // get groups for account
        Set<Group> accountGroups = groupController.getAllGroups(account);

        // check group permissions
        if (permissionDAO.hasPermissionMulti(entry, null, null, accountGroups, false, true))
            return true;

        Set<Folder> entryFolders = entry.getFolders();
        if (entryFolders == null || entryFolders.isEmpty())
            return false;

        // can any group that account belongs to read any folder that entry is contained in?
        if (permissionDAO.hasPermissionMulti(null, entryFolders, null, accountGroups, false, true))
            return true;

        // can account read any folder that entry is contained in?
        return permissionDAO.hasPermissionMulti(null, entryFolders, account, null, false, true);
    }

    @Override
    public String getOwner(Entry entry) {
        return entry.getOwnerEmail();
    }
}

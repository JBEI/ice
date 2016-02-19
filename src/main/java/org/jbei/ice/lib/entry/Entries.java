package org.jbei.ice.lib.entry;

import org.jbei.ice.lib.account.AccountController;
import org.jbei.ice.lib.account.AccountType;
import org.jbei.ice.lib.dto.access.AccessPermission;
import org.jbei.ice.lib.dto.entry.EntryType;
import org.jbei.ice.lib.dto.entry.Visibility;
import org.jbei.ice.lib.dto.folder.FolderAuthorization;
import org.jbei.ice.lib.dto.folder.FolderType;
import org.jbei.ice.lib.group.GroupController;
import org.jbei.ice.storage.DAOFactory;
import org.jbei.ice.storage.hibernate.dao.EntryDAO;
import org.jbei.ice.storage.hibernate.dao.PermissionDAO;
import org.jbei.ice.storage.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author Hector Plahar
 */
public class Entries extends HasEntry {

    private final EntryDAO dao;
    private final EntryAuthorization authorization;

    public Entries() {
        this.dao = DAOFactory.getEntryDAO();
        this.authorization = new EntryAuthorization();
    }

    public boolean updateVisibility(String userId, List<Long> entryIds, Visibility visibility) {
        Account account = DAOFactory.getAccountDAO().getByEmail(userId);
        Set<Group> accountGroups = new GroupController().getAllGroups(account);
        PermissionDAO permissionDAO = DAOFactory.getPermissionDAO();
        if (!new AccountController().isAdministrator(userId) && !permissionDAO.canWrite(account, accountGroups, entryIds))
            return false;

        for (long entryId : entryIds) {
            Entry entry = dao.get(entryId);
            if (entry.getVisibility() == visibility.getValue())
                continue;

            entry.setVisibility(visibility.getValue());
            dao.update(entry);
        }

        return true;
    }

    public ArrayList<AccessPermission> getEntryPermissions(String userId, String id) {
        Entry entry = getEntry(id);
        if (entry == null)
            return null;

        // viewing permissions requires write permissions
        authorization.expectWrite(userId, entry);

        ArrayList<AccessPermission> accessPermissions = new ArrayList<>();
        Set<Permission> permissions = DAOFactory.getPermissionDAO().getEntryPermissions(entry);

        GroupController groupController = new GroupController();
        Group publicGroup = groupController.createOrRetrievePublicGroup();
        for (Permission permission : permissions) {
            if (permission.getAccount() == null && permission.getGroup() == null)
                continue;
            if (permission.getGroup() != null && permission.getGroup() == publicGroup)
                continue;
            accessPermissions.add(permission.toDataTransferObject());
        }

        return accessPermissions;
    }

    /**
     * Retrieve {@link Entry} from the database by id.
     *
     * @param userId account identifier of user performing action
     * @param id     unique local identifier for entry
     * @return entry retrieved from the database.
     */
    public Entry get(String userId, long id) {
        Entry entry = dao.get(id);
        if (entry == null)
            return null;

        authorization.expectRead(userId, entry);
        return entry;
    }

    public List<Long> getEntriesFromSelectionContext(String userId, EntrySelection context) {
        boolean all = context.isAll();
        EntryType entryType = context.getEntryType();

        if (context.getSelectionType() == null)
            return context.getEntries();

        switch (context.getSelectionType()) {
            default:
            case FOLDER:
                if (!context.getEntries().isEmpty()) {
                    return context.getEntries();
                } else {
                    long folderId = Long.decode(context.getFolderId());
                    return getFolderEntries(userId, folderId, all, entryType);
                }

            case SEARCH:
                // todo
                break;

            case COLLECTION:
                if (!context.getEntries().isEmpty()) {
                    return context.getEntries();
                } else {
                    return getCollectionEntries(userId, context.getFolderId(), all, entryType);
                }
        }

        return null;
    }

    protected List<Long> getCollectionEntries(String userId, String collection, boolean all, EntryType type) {
        List<Long> entries = null;
        Account account = DAOFactory.getAccountDAO().getByEmail(userId);

        switch (collection.toLowerCase()) {
            case "personal":
                if (all)
                    type = null;
                entries = dao.getOwnerEntryIds(userId, type);
                break;
            case "shared":
                entries = dao.sharedWithUserEntryIds(account, account.getGroups());
                break;
            case "available":
                entries = dao.getVisibleEntryIds(account.getType() == AccountType.ADMIN);
                break;
        }

        return entries;
    }

    // todo : folder controller
    protected List<Long> getFolderEntries(String userId, long folderId, boolean all, EntryType type) {
        Folder folder = DAOFactory.getFolderDAO().get(folderId);
        FolderAuthorization folderAuthorization = new FolderAuthorization();
        folderAuthorization.expectRead(userId, folder);

        if (all)
            type = null;

        boolean visibleOnly = folder.getType() != FolderType.TRANSFERRED;
        return DAOFactory.getFolderDAO().getFolderContentIds(folderId, type, visibleOnly);
    }
}

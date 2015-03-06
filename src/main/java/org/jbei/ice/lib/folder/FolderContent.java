package org.jbei.ice.lib.folder;

import org.jbei.ice.lib.access.Permission;
import org.jbei.ice.lib.account.model.Account;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.dao.DAOFactory;
import org.jbei.ice.lib.dao.hibernate.FolderDAO;
import org.jbei.ice.lib.dao.hibernate.PermissionDAO;
import org.jbei.ice.lib.dto.folder.FolderAuthorization;
import org.jbei.ice.lib.dto.folder.FolderDetails;
import org.jbei.ice.lib.entry.EntryAuthorization;
import org.jbei.ice.lib.entry.EntryRetriever;
import org.jbei.ice.lib.entry.EntrySelection;
import org.jbei.ice.lib.entry.model.Entry;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Manages folder content (entries) add/remove
 *
 * @author Hector Plahar
 */
public class FolderContent {

    private FolderDAO folderDAO = DAOFactory.getFolderDAO();
    private FolderAuthorization folderAuthorization = new FolderAuthorization();

    public List<FolderDetails> addEntrySelection(String userId, EntrySelection entryLocation) {
        EntryRetriever retriever = new EntryRetriever();
        List<Long> entries = retriever.getEntriesFromSelectionContext(userId, entryLocation);
        return addEntriesToFolders(userId, entries, entryLocation.getDestination());
    }

    /**
     * Attempts to add the specified list of entries to the specified folder destinations.
     * The user making the request must have read privileges on the entries and write privileges on the destination
     * folders.
     * Any entries that the user is not permitted to read will not be be added and any destination folders that the user
     * does not have write privileges for will not have entries added to it
     *
     * @param userId  unique identifier for user making request
     * @param entries list of entry identifiers to be added. Specified user must have read privileges on any
     *                that are to be added
     * @param folders list of folders that that entries are to be added to
     * @return list of destination folders that were updated successfully
     */
    protected List<FolderDetails> addEntriesToFolders(String userId, List<Long> entries, List<FolderDetails> folders) {
        Account account = DAOFactory.getAccountDAO().getByEmail(userId);
        PermissionDAO permissionDAO = DAOFactory.getPermissionDAO();
        entries = DAOFactory.getPermissionDAO().getCanReadEntries(account, entries);

        if (entries.isEmpty())
            return new ArrayList<>();

        for (FolderDetails details : folders) {
            Folder folder = folderDAO.get(details.getId());
            if (folder == null) {
                Logger.warn("Could not add entries to folder " + details.getId() + " which doesn't exist");
                continue;
            }

            if (!folderAuthorization.canWrite(userId, folder)) {
                Logger.warn(userId + " lacks write privs on folder " + folder.getId());
                continue;
            }

            List<Entry> entryModelList = DAOFactory.getEntryDAO().getEntriesByIdSet(entries);
            folderDAO.addFolderContents(folder, entryModelList);
            if (folder.isPropagatePermissions()) {
                Set<Permission> folderPermissions = permissionDAO.getFolderPermissions(folder);
                addEntryPermission(userId, folderPermissions, entryModelList);
            }

            details.setCount(folderDAO.getFolderSize(folder.getId()));
        }
        return folders;
    }

    private void addEntryPermission(String userId, Set<Permission> permissions, List<Entry> entries) {
        PermissionDAO permissionDAO = DAOFactory.getPermissionDAO();
        EntryAuthorization entryAuthorization = new EntryAuthorization();

        for (Permission folderPermission : permissions) {
            for (Entry entry : entries) {
                if (!entryAuthorization.canWrite(userId, entry))
                    continue;

                // does the permissions already exists
                if (permissionDAO.hasPermission(entry, null, null, folderPermission.getAccount(), folderPermission.getGroup(), folderPermission.isCanRead(), folderPermission.isCanWrite())) {
                    continue;
                }

                Permission permission = new Permission();
                permission.setEntry(entry);
                if (entry != null)
                    entry.getPermissions().add(permission);
                permission.setGroup(folderPermission.getGroup());
                permission.setAccount(folderPermission.getAccount());
                permission.setCanRead(folderPermission.isCanRead());
                permission.setCanWrite(folderPermission.isCanWrite());
                permissionDAO.create(permission);
            }
        }
    }
}

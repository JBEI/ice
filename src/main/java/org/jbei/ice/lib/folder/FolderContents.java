package org.jbei.ice.lib.folder;

import org.apache.commons.lang3.StringUtils;
import org.jbei.ice.lib.access.PermissionException;
import org.jbei.ice.lib.access.PermissionsController;
import org.jbei.ice.lib.account.AccountController;
import org.jbei.ice.lib.account.AccountTransfer;
import org.jbei.ice.lib.account.TokenHash;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.dto.access.AccessPermission;
import org.jbei.ice.lib.dto.entry.PartData;
import org.jbei.ice.lib.dto.entry.Visibility;
import org.jbei.ice.lib.dto.folder.FolderAuthorization;
import org.jbei.ice.lib.dto.folder.FolderDetails;
import org.jbei.ice.lib.dto.folder.FolderType;
import org.jbei.ice.lib.dto.web.RegistryPartner;
import org.jbei.ice.lib.entry.Entries;
import org.jbei.ice.lib.entry.EntryAuthorization;
import org.jbei.ice.lib.entry.EntrySelection;
import org.jbei.ice.lib.entry.EntrySelectionType;
import org.jbei.ice.lib.group.GroupController;
import org.jbei.ice.lib.net.RemoteContact;
import org.jbei.ice.lib.net.RemoteTransfer;
import org.jbei.ice.lib.shared.ColumnField;
import org.jbei.ice.storage.DAOFactory;
import org.jbei.ice.storage.ModelToInfoFactory;
import org.jbei.ice.storage.hibernate.dao.FolderDAO;
import org.jbei.ice.storage.hibernate.dao.PermissionDAO;
import org.jbei.ice.storage.hibernate.dao.RemoteAccessModelDAO;
import org.jbei.ice.storage.hibernate.dao.RemoteShareModelDAO;
import org.jbei.ice.storage.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Contents of a folder which could be other folders or biological parts (entries)
 *
 * @author Hector Plahar
 */
public class FolderContents {

    private FolderDAO folderDAO = DAOFactory.getFolderDAO();
    private RemoteAccessModelDAO remoteAccessModelDAO = DAOFactory.getRemoteAccessModelDAO();
    private FolderAuthorization folderAuthorization = new FolderAuthorization();
    private PermissionsController permissionsController = new PermissionsController();
    private AccountController accountController = new AccountController();

    // adds a specified entry to a folder. The entry was transferred earlier so already exists
    public boolean remotelyAddEntrySelection(String remoteUserId, long folderId, String remoteUserToken,
                                             EntrySelection selection, RegistryPartner requestingPartner) {
        Folder folder = DAOFactory.getFolderDAO().get(folderId);      // folder that the entry is contained in
        if (folder == null)
            return false;

        RemotePartner remotePartner = DAOFactory.getRemotePartnerDAO().getByUrl(requestingPartner.getUrl());

        // check that the remote user has the right token
        RemoteShareModel shareModel = DAOFactory.getRemoteShareModelDAO().get(remoteUserId, remotePartner, folder);
        if (shareModel == null) {
            Logger.error("Could not retrieve share model");
            return false;
        }

        Permission permission = shareModel.getPermission(); // folder must match
        if (permission.getFolder().getId() != folderId || !permission.isCanWrite()) {
            throw new PermissionException("permission could not be verified");
        }

        // validate access token
        TokenHash tokenHash = new TokenHash();
        String secret = tokenHash.encrypt(remotePartner.getUrl() + remoteUserId, remoteUserToken);
        if (!secret.equals(shareModel.getSecret())) {
            throw new PermissionException("Secret does not match");
        }

        List<Long> entries = selection.getEntries();
        DAOFactory.getEntryDAO().setEntryVisibility(entries, Visibility.OK);

        // good to go?
        FolderDetails details = addEntriesToTransferredFolder(entries, folder);
        return details != null;
    }

    /**
     * Adds entries in the selection context, to specified folders
     *
     * @param userId        unique identifier for user making request
     * @param entryLocation entry selection context which also contains the folders to
     *                      add the entries obtained from the context to
     * @return list of folders that the entries where added to. They should correspond to the specified
     * folders in the selection context
     */
    public List<FolderDetails> addEntrySelection(String userId, EntrySelection entryLocation) {
        Entries retriever = new Entries();
        List<Long> entries = retriever.getEntriesFromSelectionContext(userId, entryLocation);
        if (StringUtils.isEmpty(userId)) {
            ArrayList<FolderDetails> destination = entryLocation.getDestination();

            // check that folder is transferred before rejecting
            if (destination == null || destination.isEmpty())
                throw new IllegalArgumentException("Cannot add without valid user id or destination");

            Folder folder = folderDAO.get(destination.get(0).getId());
            if (folder == null)
                throw new IllegalArgumentException("Cannot find folder");

            if (folder.getType() != FolderType.TRANSFERRED)
                throw new PermissionException("Can only add to transferred folder without id");

            FolderDetails details = addEntriesToTransferredFolder(entries, folder);
            List<FolderDetails> result = new ArrayList<>();
            result.add(details);
            return result;
        }
        return addEntriesToFolders(userId, entries, entryLocation.getDestination());
    }

    protected FolderDetails addEntriesToTransferredFolder(List<Long> entries, Folder folder) {
        List<Entry> entryModelList = DAOFactory.getEntryDAO().getEntriesByIdSet(entries);  // todo : performance
        Logger.info("Adding " + entryModelList.size() + " transferred entries to folder " + folder.getId());
        folderDAO.addFolderContents(folder, entryModelList);
        return folder.toDataTransferObject();
    }

    /**
     * Removes the specified contents of a folder, optionally adding them to another folder
     *
     * @param userId    unique identifier for user making request
     * @param folderId  unique identifier for folder whose (specified) entries are being removed
     * @param selection wrapper around the selection context for the contents
     * @param move      true, if the contents are to be added to another (set of) folder(s) (which should be specified in
     *                  <code>selection</code> parameter)
     * @return true, if action completed successfully; false otherwise
     */
    public boolean removeFolderContents(String userId, long folderId, EntrySelection selection, boolean move) {
        // remove entries from specified folder
        boolean isAdministrator = accountController.isAdministrator(userId);
        Folder folder = folderDAO.get(folderId);

        if (folder.getType() == FolderType.PUBLIC && !isAdministrator) {
            String errMsg = userId + ": cannot modify folder " + folder.getName();
            throw new PermissionException(errMsg);
        }

        Entries entries = new Entries();
        List<Long> entryIds = entries.getEntriesFromSelectionContext(userId, selection);
        boolean successRemove = folderDAO.removeFolderEntries(folder, entryIds) != null;
        if (!move)
            return successRemove;

        // add to specified folder
        selection.setFolderId(Long.toString(folderId));
        List<FolderDetails> details = addEntrySelection(userId, selection);
        return !details.isEmpty();
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
        Set<Group> accountGroups = new GroupController().getAllGroups(account);
        if (!folderAuthorization.isAdmin(userId))
            entries = DAOFactory.getPermissionDAO().getCanReadEntries(account, accountGroups, entries);

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

            // check if the folder is remote or local
            if (folder.getType() == FolderType.REMOTE) {
                Logger.info("Adding entries to remote folder " + folder.getId());
                addToRemoteFolder(account, folder, entries);
            } else {
                List<Entry> entryModelList = DAOFactory.getEntryDAO().getEntriesByIdSet(entries);
                folderDAO.addFolderContents(folder, entryModelList);
                if (folder.isPropagatePermissions()) {
                    Set<Permission> folderPermissions = permissionDAO.getFolderPermissions(folder);
                    addEntryPermission(userId, folderPermissions, entryModelList);
                }

                details.setCount(folderDAO.getFolderSize(folder.getId(), null, true));
            }
        }
        return folders;
    }

    // transfer entries to remote partner and add to folder
    protected void addToRemoteFolder(Account account, Folder folder, List<Long> entries) {
        RemoteAccessModel remoteAccessModel = remoteAccessModelDAO.getByFolder(account, folder);
        if (remoteAccessModel == null) {
            Logger.error("Could not retrieve remote access for folder " + folder.getId());
            return;
        }

        RemotePartner remotePartner = remoteAccessModel.getClientModel().getRemotePartner();
        RemoteTransfer remoteTransfer = new RemoteTransfer();
        List<PartData> results = remoteTransfer.getPartsForTransfer(entries);
        List<Long> remoteIds = remoteTransfer.transferEntries(remotePartner.getId(), results);

        FolderDetails remoteFolder = new FolderDetails();
        remoteFolder.setId(Long.decode(remoteAccessModel.getIdentifier()));

        // add transferred entries to the folder
        EntrySelection selection = new EntrySelection();
        selection.getEntries().addAll(remoteIds);

        selection.getDestination().add(remoteFolder);
        selection.setSelectionType(EntrySelectionType.FOLDER);

        RemoteContact remoteContact = new RemoteContact();
        String token = remoteAccessModel.getToken();
        remoteContact.addTransferredEntriesToFolder(remotePartner.getUrl(), account.getEmail(), selection,
                remoteFolder.getId(), token, remotePartner.getApiKey());
    }

    /**
     * Retrieves the folder specified in the parameter and contents
     *
     * @param userId   unique identifier for user making request. If null, folder must have public read privs
     * @param folderId unique identifier for folder to be retrieved
     * @param sort     sort order for folder content retrieval
     * @param asc      sort order for folder content retrieval; ascending if true
     * @param start    index of first item in retrieval
     * @param limit    upper limit count of items to be retrieval
     * @return wrapper around list of folder entries if folder is found, null otherwise
     * @throws PermissionException if user does not have read permissions on folder
     */
    public FolderDetails getContents(String userId, long folderId, ColumnField sort, boolean asc,
                                     int start, int limit, String filter) {
        Folder folder = folderDAO.get(folderId);
        if (folder == null)
            return null;

        // should have permission to read folder (folder should be public, you should be an admin, or owner)
        folderAuthorization.expectRead(userId, folder);

        FolderDetails details = folder.toDataTransferObject();
        boolean visibleOnly = folder.getType() != FolderType.TRANSFERRED;
        if (folder.getType() == FolderType.REMOTE)
            return getRemoteContents(userId, folder);

        // all local entries at this point
        long folderSize = folderDAO.getFolderSize(folderId, filter, visibleOnly);
        details.setCount(folderSize);

        if (userId != null) {
            ArrayList<AccessPermission> permissions = getAndFilterFolderPermissions(userId, folder);
            details.setAccessPermissions(permissions);
            boolean canEdit = permissionsController.hasWritePermission(userId, folder);
            details.setCanEdit(canEdit);
        }

        details.setPublicReadAccess(permissionsController.isPublicVisible(folder));
        Account owner = DAOFactory.getAccountDAO().getByEmail(folder.getOwnerEmail());
        if (owner != null)
            details.setOwner(owner.toDataTransferObject());

        // retrieve folder contents
        List<Entry> results = folderDAO.retrieveFolderContents(folderId, sort, asc, start, limit, filter, visibleOnly);
        for (Entry entry : results) {
            PartData info = ModelToInfoFactory.createTableViewData(userId, entry, false);
            details.getEntries().add(info);
        }
        return details;
    }

    /**
     * @param userId user folder is shared with
     * @param folder remote shared folder
     * @return
     */
    protected FolderDetails getRemoteContents(String userId, Folder folder) {
        // get remote access
        Account account = DAOFactory.getAccountDAO().getByEmail(userId);
        RemoteAccessModel model = remoteAccessModelDAO.getByFolder(account, folder);

        FolderDetails details = folder.toDataTransferObject();
        AccountTransfer owner = new AccountTransfer();
        owner.setEmail(model.getClientModel().getEmail());
        details.setOwner(owner);
        RemotePartner remotePartner = model.getClientModel().getRemotePartner();
        details.setRemotePartner(remotePartner.toDataTransferObject());

        String token = model.getToken();
        long remoteFolderId = Long.decode(model.getIdentifier());

        // retrieve entries from remote partner (ends up in the call below)
        RemoteContact remoteContact = new RemoteContact();
        FolderDetails remoteDetails = remoteContact.getRemoteContents(remotePartner.getUrl(), userId, remoteFolderId, token, remotePartner.getApiKey());
        details.setCount(remoteDetails.getCount());
        details.setEntries(remoteDetails.getEntries());
        return details;
    }

    // remote request for shared contents
    public FolderDetails getRemotelySharedContents(String remoteUserId, String token, RegistryPartner partner,
                                                   long folderId, ColumnField sort, boolean asc,
                                                   int start, int limit, String filter) {
        RemotePartner remotePartner = DAOFactory.getRemotePartnerDAO().getByUrl(partner.getUrl());
        if (remotePartner == null) {
            Logger.error("Cannot retrieve remote partner " + partner.getUrl());
            return null;
        }

        Folder folder = folderDAO.get(folderId);
        if (folder == null) {
            Logger.error("Cannot retrieve folder with id " + folderId);
            return null;
        }

        RemoteShareModelDAO shareModelDAO = DAOFactory.getRemoteShareModelDAO();
        RemoteShareModel shareModel = shareModelDAO.get(remoteUserId, remotePartner, folder);
        if (shareModel == null) {
            Logger.error("Could not retrieve share model");
            return null;
        }

        //verify access
        TokenHash tokenHash = new TokenHash();
        String secret = tokenHash.encrypt(remotePartner.getUrl() + remoteUserId, token);
        if (!secret.equalsIgnoreCase(shareModel.getSecret())) {
            Logger.error("Authorization failed for remote folder retrieve");
            return null;
        }
        // todo : move everything above to folder permissions
        FolderDetails details = folder.toDataTransferObject();

        long folderSize = folderDAO.getFolderSize(folderId, filter, true);
        details.setCount(folderSize);

        // retrieve folder contents
        List<Entry> results = folderDAO.retrieveFolderContents(folderId, sort, asc, start, limit, filter, true);
        for (Entry entry : results) {
            PartData info = ModelToInfoFactory.createTableViewData(null, entry, false);
            details.getEntries().add(info);
        }
        return details;
    }

    /**
     * The permission(s) enabling the share for user
     * is(are) included. If the user is an admin then all the permissions are included, otherwise only those pertaining
     * to the user are included.
     * <p>e.g. if a folder F is shared with groups A and B and the user is a non-admin belonging to group B, folder
     * F will be included in the list of folders returned but will only include permissions for group B
     *
     * @param userId identifier for user making request
     * @param folder Folder whose permissions are to be retrieved
     * @return list of filtered permissions
     */
    protected ArrayList<AccessPermission> getAndFilterFolderPermissions(String userId, Folder folder) {
        ArrayList<AccessPermission> permissions = permissionsController.retrieveSetFolderPermission(folder, false);
        if (accountController.isAdministrator(userId) || folder.getOwnerEmail().equalsIgnoreCase(userId)) {
            return permissions;
        }

        Account account = DAOFactory.getAccountDAO().getByEmail(userId);

        // filter permissions
        ArrayList<AccessPermission> filteredPermissions = new ArrayList<>();
        for (AccessPermission accessPermission : permissions) {

            // account either has direct write permissions
            if (accessPermission.getArticle() == AccessPermission.Article.ACCOUNT
                    && accessPermission.getArticleId() == account.getId()) {
                filteredPermissions.add(accessPermission);
                continue;
            }

            if (account.getGroups() == null || account.getGroups().isEmpty())
                continue;

            // or belongs to a group that has the write permissions
            if (accessPermission.getArticle() == AccessPermission.Article.GROUP) {
                Group group = DAOFactory.getGroupDAO().get(accessPermission.getArticleId());
                if (group == null)
                    continue;

                if (account.getGroups().contains(group)) {
                    filteredPermissions.add(accessPermission);
                }
            }
        }

        return filteredPermissions;
    }

    private void addEntryPermission(String userId, Set<Permission> permissions, List<Entry> entries) {
        PermissionDAO permissionDAO = DAOFactory.getPermissionDAO();
        EntryAuthorization entryAuthorization = new EntryAuthorization();

        for (Permission folderPermission : permissions) {
            for (Entry entry : entries) {
                if (!entryAuthorization.canWriteThoroughCheck(userId, entry))
                    continue;

                // does the permissions already exists
                if (permissionDAO.hasPermission(entry, null, null, folderPermission.getAccount(),
                        folderPermission.getGroup(), folderPermission.isCanRead(), folderPermission.isCanWrite())) {
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

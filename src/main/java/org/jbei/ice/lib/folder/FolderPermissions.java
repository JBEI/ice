package org.jbei.ice.lib.folder;

import org.jbei.ice.lib.access.PermissionException;
import org.jbei.ice.lib.access.PermissionsController;
import org.jbei.ice.lib.account.AccountTransfer;
import org.jbei.ice.lib.account.TokenHash;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.dto.access.AccessPermission;
import org.jbei.ice.lib.dto.folder.FolderAuthorization;
import org.jbei.ice.lib.dto.folder.FolderType;
import org.jbei.ice.lib.dto.web.RegistryPartner;
import org.jbei.ice.lib.entry.EntryPermissions;
import org.jbei.ice.lib.group.GroupController;
import org.jbei.ice.lib.net.RemoteContact;
import org.jbei.ice.storage.DAOFactory;
import org.jbei.ice.storage.hibernate.dao.*;
import org.jbei.ice.storage.model.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.Set;

/**
 * Represents permissions for a specified folder with methods to manipulate them
 *
 * @author Hector Plahar
 */
public class FolderPermissions {

    private final Folder folder;
    private final FolderAuthorization authorization;
    private final FolderDAO dao;
    private final TokenHash tokenHash;
    private final String userId;
    private final PermissionDAO permissionDAO;
    private final AccountDAO accountDAO;
    private final RemoteShareModelDAO remoteShareModelDAO;
    private final RemoteClientModelDAO remoteClientModelDAO;
    private final RemotePartnerDAO remotePartnerDAO;

    public FolderPermissions(String userId, long folderId) {
        this.dao = DAOFactory.getFolderDAO();
        this.permissionDAO = DAOFactory.getPermissionDAO();
        this.accountDAO = DAOFactory.getAccountDAO();
        this.remoteShareModelDAO = DAOFactory.getRemoteShareModelDAO();
        this.remoteClientModelDAO = DAOFactory.getRemoteClientModelDAO();
        this.remotePartnerDAO = DAOFactory.getRemotePartnerDAO();
        this.folder = this.dao.get(folderId);
        if (folder == null)
            throw new IllegalArgumentException("Cannot retrieve folder with id " + folderId);
        this.authorization = new FolderAuthorization();
        this.tokenHash = new TokenHash();
        this.userId = userId;
    }

    /**
     * Retrieves list of available folder permissions (both local and remote)
     *
     * @return list available folders
     */
    public ArrayList<AccessPermission> get() {
        authorization.expectWrite(userId, folder);
        ArrayList<AccessPermission> accessPermissions = new ArrayList<>();

        // get local permissions
        Set<Permission> permissions = this.permissionDAO.getFolderPermissions(folder);
        for (Permission permission : permissions) {
            if (permission.getGroup() != null && permission.getGroup().getUuid()
                    .equals(GroupController.PUBLIC_GROUP_UUID))
                continue;

            AccessPermission accessPermission = permission.toDataTransferObject();

            if (permission.getRemoteShare() != null) {
                RemoteShareModel remoteShareModel = permission.getRemoteShare();
                accessPermission.setArticle(AccessPermission.Article.REMOTE);
                accessPermission.setArticleId(permission.getId()); // for remote access permissions, the article id is the actual permission
                accessPermission.setId(remoteShareModel.getId());
//                accessPermission.setType(this.permission.isCanWrite() ? AccessPermission.Type.WRITE_FOLDER : AccessPermission.Type.READ_FOLDER);
                AccountTransfer accountTransfer = new AccountTransfer();
                accountTransfer.setEmail(remoteShareModel.getClient().getEmail());
                accessPermission.setPartner(remoteShareModel.getClient().getRemotePartner().toDataTransferObject());
                accessPermission.setDisplay(accountTransfer.getEmail());
            }

            accessPermissions.add(accessPermission);
        }

        return accessPermissions;
    }

    /**
     * Creates a new access permission record to enable read or write privileges for a folder.
     * User initiating request must have write privileges for the folder
     *
     * @param accessPermission details about access permissions to create
     * @return access permission data transfer object with unique record identifier
     * @throws IllegalArgumentException if the <code>accessPermission</code> object is null
     * @throws PermissionException      if specified user does not have write privileges
     *                                  on specified folder.
     */
    public AccessPermission createPermission(AccessPermission accessPermission) {
        if (accessPermission == null)
            throw new IllegalArgumentException("Cannot add null permission");

        // check if permission for remote folder is being created
        if (accessPermission.getArticle() == AccessPermission.Article.REMOTE) {
            return createRemotePermission(accessPermission);
        }

        // verify write authorization
        authorization.expectWrite(userId, folder);

        // permission object
        Permission permission = new Permission();
        permission.setFolder(folder);
        if (accessPermission.getArticle() == AccessPermission.Article.GROUP) {
            Group group = DAOFactory.getGroupDAO().get(accessPermission.getArticleId());
            if (group == null) {
                String errorMessage = "Could not assign group with id " + accessPermission.getArticleId() + " to folder";
                Logger.error(errorMessage);
                throw new IllegalArgumentException(errorMessage);
            }
            permission.setGroup(group);
        } else {
            Account account = accountDAO.get(accessPermission.getArticleId());
            if (account == null) {
                String errorMessage = "Could not assign account with id " + accessPermission.getArticleId() + " to folder";
                Logger.error(errorMessage);
                throw new IllegalArgumentException(errorMessage);
            }

            permission.setAccount(account);
        }

        permission.setCanRead(accessPermission.isCanRead());
        permission.setCanWrite(accessPermission.isCanWrite());
        AccessPermission created = permissionDAO.create(permission).toDataTransferObject();

        // todo : on remote folder as well
        if (folder.getType() == FolderType.PRIVATE) {
            folder.setType(FolderType.SHARED);
            folder.setModificationTime(new Date());
            dao.update(folder);
        }

        PermissionsController permissionsController = new PermissionsController();

        // propagate permission
        if (folder.isPropagatePermissions()) {
            permissionsController.propagateFolderPermissions(userId, folder, true);
        }
        return created;
    }

    public boolean enablePublicReadAccess() {
        AccessPermission permission = new AccessPermission();
        permission.setType(AccessPermission.Type.READ_FOLDER);
        permission.setTypeId(folder.getId());
        permission.setArticle(AccessPermission.Article.GROUP);
        GroupController groupController = new GroupController();
        permission.setArticleId(groupController.createOrRetrievePublicGroup().getId());
        return createPermission(permission) != null;
    }

    public boolean disablePublicReadAccess() {
        authorization.expectWrite(userId, folder);

        GroupController groupController = new GroupController();
        Group publicGroup = groupController.createOrRetrievePublicGroup();

        permissionDAO.removePermission(null, folder, null, null, publicGroup, true, false);
        if (folder.isPropagatePermissions()) {
            for (Entry folderContent : folder.getContents()) {
                EntryPermissions entryPermissions = new EntryPermissions(Long.toString(folderContent.getId()), userId);
                entryPermissions.disablePublicReadAccess();
            }
        }
        return true;
    }

    /**
     * Creates an access folder permission for a remote user
     *
     * @param accessPermission access details
     * @return wrapper around the unique identifier for the remote permission created
     * @throws IllegalArgumentException if the partner record cannot be retrieved
     */
    public AccessPermission createRemotePermission(AccessPermission accessPermission) {
        RegistryPartner partner = accessPermission.getPartner();
        RemotePartner remotePartner = remotePartnerDAO.get(partner.getId());
        if (remotePartner == null) {
            String errorMessage = "Could not find remote partner for remote permission";
            Logger.error(errorMessage);
            throw new IllegalArgumentException(errorMessage);
        }

        // todo : must be owner?
        authorization.expectWrite(userId, folder);

        String remoteUserId = accessPermission.getUserId();
        String token = tokenHash.generateSalt();

        // send token and also verify user Id
        accessPermission.setSecret(token);
        AccountTransfer accountTransfer = new AccountTransfer();
        accountTransfer.setEmail(userId);
        accessPermission.setAccount(accountTransfer);
        accessPermission.setDisplay(folder.getName());
        accessPermission.setTypeId(folder.getId());
        if (!sendToken(accessPermission, remotePartner))
            return null; // something happened with the send; likely user id is invalid

        // create local client record mapping to remote
        RemoteClientModel remoteClientModel = getOrCreateRemoteClient(remoteUserId, remotePartner);

        // create remote share record storing the secret
        // todo : use folder uuid instead of folder id ?
        String secret = tokenHash.encrypt(folder.getId() + remotePartner.getUrl() + remoteUserId, token);
        RemoteShareModel remoteShare = new RemoteShareModel();
        remoteShare.setClient(remoteClientModel);
        remoteShare.setSecret(secret);
        Account account = accountDAO.getByEmail(userId);
        remoteShare.setSharer(account);
        remoteShare = remoteShareModelDAO.create(remoteShare);

        // create permission object
        Permission permission = createPermissionModel(accessPermission, remoteShare);

        accessPermission.setId(remoteShare.getId());
        accessPermission.setArticleId(permission.getId());
        accessPermission.setArticle(AccessPermission.Article.REMOTE);

        RemoteShareModel remoteShareModel = permission.getRemoteShare();
        accessPermission.setPartner(remoteShareModel.getClient().getRemotePartner().toDataTransferObject());
        accessPermission.setDisplay(remoteShareModel.getClient().getEmail());

        return accessPermission;
    }

    /**
     * Checks if there is an existing client for the specified userId and remote partner.
     * Retrieves and returns if so, or creates a new record if not
     *
     * @param remoteUserId  email address of remote user
     * @param remotePartner remote partner
     * @return client model stored in the database with specified user id and partner
     */
    protected RemoteClientModel getOrCreateRemoteClient(String remoteUserId, RemotePartner remotePartner) {
        RemoteClientModel remoteClientModel = remoteClientModelDAO.getModel(remoteUserId, remotePartner);
        if (remoteClientModel == null) {
            remoteClientModel = new RemoteClientModel();
            remoteClientModel.setRemotePartner(remotePartner);
            remoteClientModel.setEmail(remoteUserId);
            remoteClientModel = remoteClientModelDAO.create(remoteClientModel);
        }
        return remoteClientModel;
    }

    public boolean remove(long permissionId) {
        authorization.expectWrite(userId, this.folder);

        // get the permission
        Permission permission = permissionDAO.get(permissionId);
        if (permission == null)
            return false;

        permissionDAO.delete(permission);
        return true;
    }

    /**
     * Send access permission to a remote partner with a "secret"
     *
     * @param accessPermission permission details including secret token
     * @param partner          remote ICE partner to send information to
     * @return true if permission is successfully sent with a reply received, false otherwise
     */
    protected boolean sendToken(AccessPermission accessPermission, RemotePartner partner) {
        RemoteContact remoteContact = new RemoteContact();
        // send to remote partner at POST /rest/permissions/remote
        return remoteContact.shareFolder(partner.getUrl(), accessPermission, partner.getApiKey()) != null;
    }

    protected Permission createPermissionModel(AccessPermission accessPermission, RemoteShareModel remoteShare) {
        Permission permission = new Permission();
        permission.setFolder(folder);
        permission.setCanWrite(accessPermission.isCanWrite());
        permission.setCanRead(accessPermission.isCanRead());
        permission.setRemoteShare(remoteShare);
        remoteShare.setPermission(permission);
        return this.permissionDAO.create(permission);
    }
}

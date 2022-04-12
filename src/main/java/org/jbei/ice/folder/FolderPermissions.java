package org.jbei.ice.folder;

import org.jbei.ice.access.PermissionException;
import org.jbei.ice.access.Permissions;
import org.jbei.ice.access.PermissionsController;
import org.jbei.ice.account.Account;
import org.jbei.ice.account.TokenHash;
import org.jbei.ice.dto.access.AccessPermission;
import org.jbei.ice.dto.folder.FolderAuthorization;
import org.jbei.ice.dto.folder.FolderType;
import org.jbei.ice.dto.web.RegistryPartner;
import org.jbei.ice.entry.EntryPermissions;
import org.jbei.ice.group.GroupController;
import org.jbei.ice.logging.Logger;
import org.jbei.ice.net.RemoteContact;
import org.jbei.ice.storage.DAOFactory;
import org.jbei.ice.storage.hibernate.dao.*;
import org.jbei.ice.storage.model.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Represents permissions for a specified folder with methods to manipulate them
 *
 * @author Hector Plahar
 */
public class FolderPermissions extends Permissions {

    private final Folder folder;
    private final FolderAuthorization authorization;
    private final FolderDAO dao;
    private final TokenHash tokenHash;
    private final String userId;
    private final PermissionDAO permissionDAO;
    private final AccountDAO accountDAO;
    private final RemoteClientModelDAO remoteClientModelDAO;
    private final RemotePartnerDAO remotePartnerDAO;

    /**
     * @param userId   unique identifier of user
     * @param folderId unique local folder identifier
     * @throws IllegalArgumentException if the referenced folder identifier doesn't resolve to an existing folder
     */
    public FolderPermissions(String userId, long folderId) {
        this.dao = DAOFactory.getFolderDAO();
        this.permissionDAO = DAOFactory.getPermissionDAO();
        this.accountDAO = DAOFactory.getAccountDAO();
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
     * Retrieves list of available folder permissions (both local and remote).
     * User must have write privileges on folder to perform this action
     *
     * @return list available folders
     * @throws PermissionException is user doesn't have write permissions on folder
     */
    public List<AccessPermission> get() {
        authorization.expectWrite(userId, folder);
        ArrayList<AccessPermission> accessPermissions = new ArrayList<>();

        // get local permissions
        List<Permission> permissions = this.permissionDAO.getFolderPermissions(folder);
        for (Permission permission : permissions) {
            if (permission.getGroup() != null && permission.getGroup().getUuid()
                    .equals(GroupController.PUBLIC_GROUP_UUID))
                continue;

            accessPermissions.add(permission.toDataTransferObject());
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

        Permission permission = addPermission(accessPermission, null, folder, null);
        if (permission == null)
            return null;

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
        return permission.toDataTransferObject();
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
     * @throws PermissionException      if user doesn't have write privileges on folder
     */
    public AccessPermission createRemotePermission(AccessPermission accessPermission) {

        authorization.expectWrite(userId, folder);

        RegistryPartner partner = accessPermission.getPartner();
        RemotePartner remotePartner = remotePartnerDAO.get(partner.getId());
        if (remotePartner == null) {
            String errorMessage = "Could not find remote partner for remote permission";
            Logger.error(errorMessage);
            throw new IllegalArgumentException(errorMessage);
        }

        String remoteUserId = accessPermission.getUserId();
        String randomToken = tokenHash.generateRandomToken();

        // generate random token and also verify user Id
        accessPermission.setSecret(randomToken);
        Account accountTransfer = new Account();
        accountTransfer.setEmail(userId);

        accessPermission.setAccount(accountTransfer);
        accessPermission.setDisplay(folder.getName());
        accessPermission.setTypeId(folder.getId());

        // send to remote partner (throws illegal argument exception if user id doesn't exist on other side)
        if (!sendToken(accessPermission, remotePartner)) {
            Logger.error("Could not share folder remotely");
            return null; // something happened with the send; likely user id is invalid
        }

        // create local client record mapping to remote partner + user id
        // this narrows down the remote client allowed to access the article (folder)
        RemoteClientModel remoteClientModel = getOrCreateRemoteClient(remoteUserId, remotePartner);

        // store the access credentials locally by hashing the random token that was sent
        // when requesting the folder, the remote user will send the random token
        String secret = tokenHash.encrypt(folder.getId() + remotePartner.getUrl() + remoteUserId, randomToken);
        Permission remoteShare = new Permission();
        remoteShare.setClient(remoteClientModel);
        remoteShare.setSecret(secret);
        AccountModel account = accountDAO.getByEmail(userId);
        remoteShare.setSharer(account);

        // add permissions
        remoteShare.setFolder(folder);
        remoteShare.setCanWrite(accessPermission.isCanWrite());
        remoteShare.setCanRead(accessPermission.isCanRead());
        remoteShare = this.permissionDAO.create(remoteShare);

        accessPermission.setId(remoteShare.getId());
        accessPermission.setArticleId(remoteShare.getId());
        accessPermission.setArticle(AccessPermission.Article.REMOTE);
        accessPermission.setPartner(remoteShare.getClient().getRemotePartner().toDataTransferObject());
        accessPermission.setDisplay(remoteShare.getClient().getEmail());

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
}

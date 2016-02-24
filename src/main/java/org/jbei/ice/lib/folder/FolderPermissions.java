package org.jbei.ice.lib.folder;

import org.jbei.ice.lib.access.PermissionsController;
import org.jbei.ice.lib.account.AccountTransfer;
import org.jbei.ice.lib.account.TokenHash;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.dto.access.AccessPermission;
import org.jbei.ice.lib.dto.folder.FolderAuthorization;
import org.jbei.ice.lib.dto.folder.FolderType;
import org.jbei.ice.lib.dto.web.RegistryPartner;
import org.jbei.ice.lib.group.GroupController;
import org.jbei.ice.lib.net.RemoteContact;
import org.jbei.ice.storage.DAOFactory;
import org.jbei.ice.storage.hibernate.dao.*;
import org.jbei.ice.storage.model.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.Set;

/**
 * @author Hector Plahar
 */
public class FolderPermissions {

    private final Folder folder;
    private final FolderAuthorization authorization;
    private final FolderDAO dao;
    private final TokenHash tokenHash;
    private final PermissionDAO permissionDAO;
    private final AccountDAO accountDAO;
    private final RemoteShareModelDAO remoteShareModelDAO;
    private final ClientModelDAO clientModelDAO;

    public FolderPermissions(long folderId) {
        this.dao = DAOFactory.getFolderDAO();
        this.permissionDAO = DAOFactory.getPermissionDAO();
        this.accountDAO = DAOFactory.getAccountDAO();
        this.remoteShareModelDAO = DAOFactory.getRemoteShareModelDAO();
        this.clientModelDAO = DAOFactory.getClientModelDAO();
        this.folder = this.dao.get(folderId);
        if (folder == null)
            throw new IllegalArgumentException("Cannot retrieve folder with id " + folderId);
        this.authorization = new FolderAuthorization();
        this.tokenHash = new TokenHash();
    }

    /**
     * Retrieves list of available folder permissions (both local and remote)
     *
     * @param userId unique identifier for user requesting the permission. Must have write privileges on the folder
     * @return list available folders
     */
    public ArrayList<AccessPermission> get(String userId) {
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

    // folder for local
    public AccessPermission createPermission(String userId, AccessPermission accessPermission) {
        if (accessPermission == null)
            throw new IllegalArgumentException("Cannot add null permission");

        if (accessPermission.getArticle() == AccessPermission.Article.REMOTE) {
            return createRemotePermission(userId, accessPermission);
        }

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
            Account account = DAOFactory.getAccountDAO().get(accessPermission.getArticleId());
            if (account == null) {
                String errorMessage = "Could not assign account with id " + accessPermission.getArticleId() + " to folder";
                Logger.error(errorMessage);
                throw new IllegalArgumentException(errorMessage);
            }

            permission.setAccount(account);
        }

        PermissionDAO permissionDAO = DAOFactory.getPermissionDAO();
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

    /**
     * Creates folder permission for a remote user
     *
     * @param userId           unique identifier of user sharing the folder
     * @param accessPermission access details
     * @return wrapper around the unique identifier for the remote permission created
     */
    public AccessPermission createRemotePermission(String userId, AccessPermission accessPermission) {
        RegistryPartner partner = accessPermission.getPartner();
        RemotePartner remotePartner = DAOFactory.getRemotePartnerDAO().get(partner.getId());
        if (remotePartner == null) {
            Logger.error("Could not find remote partner for remote permission");
            return null;
        }

        // todo : must be owner?
        authorization.expectWrite(userId, folder);

        String remoteUserId = accessPermission.getUserId();
        String token = tokenHash.generateSalt();
        String secret = tokenHash.encryptPassword(remotePartner.getUrl() + remoteUserId, token);

        // send token and also verify user Id
        accessPermission.setSecret(token);
        AccountTransfer accountTransfer = new AccountTransfer();
        accountTransfer.setEmail(userId);
        accessPermission.setAccount(accountTransfer);
        accessPermission.setDisplay(folder.getName());
        accessPermission.setTypeId(folder.getId());
        if (!sendToken(accessPermission, remotePartner))
            return null; // something happened with the send; likely user id is invalid

        // get create local client record mapping to remote
        ClientModel clientModel = clientModelDAO.getModel(remoteUserId, remotePartner);
        if (clientModel == null) {
            clientModel = new ClientModel();
            clientModel.setRemotePartner(remotePartner);
            clientModel.setEmail(remoteUserId);
            clientModel = clientModelDAO.create(clientModel);
        }

        // create remote share record storing the secret
        RemoteShareModel remoteShare = new RemoteShareModel();
        remoteShare.setClient(clientModel);
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

    public boolean remove(String userId, long permissionId) {
        authorization.expectWrite(userId, this.folder);

        // get the permission
        Permission permission = permissionDAO.get(permissionId);
        if (permission == null)
            return false;

        permissionDAO.delete(permission);
        return true;
    }

    protected boolean sendToken(AccessPermission accessPermission, RemotePartner partner) {
        RemoteContact remoteContact = new RemoteContact();
        // send to remote partner at POST /rest/permissions/remote
        remoteContact.shareFolder(partner.getUrl(), accessPermission, partner.getApiKey());
        return true;
    }

    // todo : remove shared token from user
    protected void removeToken() {
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

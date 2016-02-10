package org.jbei.ice.lib.folder;

import org.jbei.ice.lib.access.PermissionsController;
import org.jbei.ice.lib.account.TokenHash;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.dto.access.AccessPermission;
import org.jbei.ice.lib.dto.folder.FolderAuthorization;
import org.jbei.ice.lib.dto.folder.FolderType;
import org.jbei.ice.storage.DAOFactory;
import org.jbei.ice.storage.hibernate.dao.FolderDAO;
import org.jbei.ice.storage.hibernate.dao.PermissionDAO;
import org.jbei.ice.storage.model.*;

import java.util.Date;
import java.util.List;

/**
 * @author Hector Plahar
 */
public class FolderPermissions {

    private final Folder folder;
    private final FolderAuthorization authorization;
    private final FolderDAO dao;
    private final TokenHash tokenHash;

    public FolderPermissions(long folderId) {
        this.dao = DAOFactory.getFolderDAO();
        this.folder = this.dao.get(folderId);
        if (folder == null)
            throw new IllegalArgumentException("Cannot retrieve folder with id " + folderId);
        this.authorization = new FolderAuthorization();
        this.tokenHash = new TokenHash();
    }

    // folder for local
    public AccessPermission createFolderPermission(String userId, AccessPermission accessPermission) {
        if (accessPermission == null)
            throw new IllegalArgumentException("Cannot add null permission");

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

    protected void generateSecretForRemote(Group group, AccessPermission accessPermission) {
        List<ClientModel> clients = DAOFactory.getClientModelDAO().getForGroup(group);
        if (clients == null || clients.isEmpty())
            return;

        // secret
        for (ClientModel clientModel : clients) {
            RemotePartner remotePartner = clientModel.getRemotePartner();
            if (remotePartner == null) {
                Logger.error("No remote partner found associated with clientModel " + clientModel.getId());
                continue;
            }

//            String secret =
            // secret = hash(url, email, folder id)
            String token = tokenHash.generateSalt();
            String secret = tokenHash.encryptPassword(remotePartner.getUrl() + clientModel.getEmail(), token);

            RemotePermission remotePermission = new RemotePermission();
            remotePermission.setCanRead(accessPermission.isCanRead());
            remotePermission.setCanWrite(accessPermission.isCanWrite());
            remotePermission.setClient(clientModel);
            remotePermission.setSecret(secret);

            // send token (not kept here)

            DAOFactory.getRemotePermissionDAO().create(remotePermission);
        }
    }
}

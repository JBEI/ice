package org.jbei.ice.lib.access;

import org.apache.commons.lang3.StringUtils;
import org.jbei.ice.lib.account.AccountTransfer;
import org.jbei.ice.lib.dto.ConfigurationKey;
import org.jbei.ice.lib.dto.access.AccessPermission;
import org.jbei.ice.lib.dto.folder.FolderType;
import org.jbei.ice.lib.dto.web.RegistryPartner;
import org.jbei.ice.lib.utils.Utils;
import org.jbei.ice.services.rest.IceRestClient;
import org.jbei.ice.storage.DAOFactory;
import org.jbei.ice.storage.hibernate.dao.*;
import org.jbei.ice.storage.model.*;

/**
 * Access privileges for remote resources
 *
 * @author Hector Plahar
 */
public class RemoteAccess {

    private final FolderDAO folderDAO;
    private final RemoteClientModelDAO remoteClientModelDAO;
    private final RemotePartnerDAO remotePartnerDAO;
    private final AccountDAO accountDAO;
    private final RemoteAccessModelDAO remoteAccessModelDAO;
    private final PermissionDAO permissionDAO;

    public RemoteAccess() {
        this.folderDAO = DAOFactory.getFolderDAO();
        this.remoteClientModelDAO = DAOFactory.getRemoteClientModelDAO();
        this.remotePartnerDAO = DAOFactory.getRemotePartnerDAO();
        this.accountDAO = DAOFactory.getAccountDAO();
        this.remoteAccessModelDAO = DAOFactory.getRemoteAccessModelDAO();
        this.permissionDAO = DAOFactory.getPermissionDAO();
    }

    /**
     * Checks if the web of registries admin config value has been set to enable this ICE instance
     * to join the web of registries configuration
     *
     * @return true if value has been set to the affirmative, false otherwise
     */
    private boolean isInWebOfRegistries() {
        String value = Utils.getConfigValue(ConfigurationKey.JOIN_WEB_OF_REGISTRIES);
        return ("yes".equalsIgnoreCase(value) || "true".equalsIgnoreCase(value));
    }

    /**
     * // todo : check if already shared
     * Add access privileges for a user on this instance to enable access
     * to a (currently folder only) resource on a remote ICE instance
     *
     * @param partner          remote partner requesting add
     * @param accessPermission details of access privilege, including access token and user on this instance
     *                         that the permission is for
     * @throws IllegalArgumentException if the permission details is missing some required information or has invalid
     *                                  information. e.g. the specified user does not exist on this ICE instance
     * @throws PermissionException      if this instance is not in a web of registries configuration or it is but
     *                                  not with the specified partner
     */
    public AccessPermission add(RegistryPartner partner, AccessPermission accessPermission) {
        if (!isInWebOfRegistries())
            throw new PermissionException("This ICE instance doesn't have WoR enabled");

        // person on this site that the permission is for
        String userId = accessPermission.getUserId();

        // verify that it is valid
        Account account = accountDAO.getByEmail(userId);
        if (account == null)
            throw new IllegalArgumentException("Email address " + userId + " not on this registry instance");

        // remote person doing the sharing
        AccountTransfer accountTransfer = accessPermission.getAccount();
        if (accountTransfer == null || StringUtils.isEmpty(accountTransfer.getEmail()))
            throw new IllegalArgumentException("Remote sharer information not available");

        // read or write permission
        if (!accessPermission.isCanRead() && !accessPermission.isCanWrite())
            throw new IllegalArgumentException("Invalid read/write values for permission. Both are false");

        // verify secret token
        if (StringUtils.isEmpty(accessPermission.getSecret()))
            throw new IllegalArgumentException("No access token sent with permission");

        // get the remote partner object
        RemotePartner remotePartner = remotePartnerDAO.getByUrl(partner.getUrl());
        if (remotePartner == null)
            throw new IllegalArgumentException("Cannot retrieve remote partner with url " + partner.getUrl());

        // email of remote user making share request
        String remoteEmail = accountTransfer.getEmail();

        // todo : can also be single entry sharing
        // create a local folder instance that references a remote folder (can also acts like a cache)
        // if remote folder already exists then retrieve it and share that
        Folder folder = getOrCreateRemoteFolder(accessPermission.getDisplay(),
                Long.toString(accessPermission.getTypeId()), remoteEmail);

        // get or create the client for the remote user who is sharing the folder
        RemoteClientModel remoteClientModel = getOrCreateRemoteClient(remoteEmail, remotePartner);

        // store access permission to remote folder for referenced account
        Permission permission = createPermissionModel(accessPermission, folder, account);

        // remote access model associated with permission and remote client for storing secret
        RemoteAccessModel remoteAccessModel = createRemoteAccessModel(accessPermission, remoteClientModel, permission);
        return remoteAccessModel.toDataTransferObject();
    }

    private Folder getOrCreateRemoteFolder(String display, String remoteFolderId, String remoteEmail) {
        Folder folder = this.folderDAO.getRemote(remoteFolderId, remoteEmail);
        if (folder == null) {
            folder = new Folder();
            folder.setType(FolderType.REMOTE);
            folder.setName(display);
            folder.setDescription(remoteFolderId);
            folder.setOwnerEmail(remoteEmail);
            folder = this.folderDAO.create(folder);
        }
        return folder;
    }

    public AccountTransfer getRemoteUser(long remoteId, String email) {
        RemotePartner partner = this.remotePartnerDAO.get(remoteId);
        if (partner == null)
            return null;

        IceRestClient client = new IceRestClient(partner.getUrl(), partner.getApiKey(), "rest/users/" + email);
        return client.get(AccountTransfer.class);
    }

    /**
     * Checks if there is an existing client for the specified userId and remote partner.
     * Retrieves and returns if so, or creates a new record if not
     *
     * @param remoteUserId  email address of remote user
     * @param remotePartner remote partner
     * @return client model stored in the database with specified user id and partner
     */
    private RemoteClientModel getOrCreateRemoteClient(String remoteUserId, RemotePartner remotePartner) {
        RemoteClientModel remoteClientModel = remoteClientModelDAO.getModel(remoteUserId, remotePartner);
        if (remoteClientModel == null) {
            remoteClientModel = new RemoteClientModel();
            remoteClientModel.setRemotePartner(remotePartner);
            remoteClientModel.setEmail(remoteUserId);
            remoteClientModel = remoteClientModelDAO.create(remoteClientModel);
        }
        return remoteClientModel;
    }

    private Permission createPermissionModel(AccessPermission accessPermission, Folder folder, Account account) {
        Permission permission = new Permission();
        permission.setFolder(folder);
        permission.setCanWrite(accessPermission.isCanWrite());
        permission.setCanRead(accessPermission.isCanRead());
        permission.setAccount(account);
        return this.permissionDAO.create(permission);
    }

    private RemoteAccessModel createRemoteAccessModel(AccessPermission accessPermission,
                                                      RemoteClientModel remoteClientModel, Permission permission) {
        RemoteAccessModel remoteAccessModel = new RemoteAccessModel();
        remoteAccessModel.setToken(accessPermission.getSecret());
        remoteAccessModel.setRemoteClientModel(remoteClientModel);
        remoteAccessModel.setIdentifier(accessPermission.getTypeId() + "");
        remoteAccessModel.setPermission(permission);
        return remoteAccessModelDAO.create(remoteAccessModel);
    }
}

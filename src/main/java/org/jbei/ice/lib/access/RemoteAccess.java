package org.jbei.ice.lib.access;

import org.apache.commons.lang3.StringUtils;
import org.jbei.ice.lib.account.AccountTransfer;
import org.jbei.ice.lib.dto.access.AccessPermission;
import org.jbei.ice.lib.dto.folder.FolderType;
import org.jbei.ice.lib.dto.web.RegistryPartner;
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
     * Add access privileges for a user on this instance to enable access
     * to a (currently folder only) resource on a remote ICE instance
     *
     * @param partner          remote partner requesting add
     * @param accessPermission details of access privilege, including access token and user on this instance
     *                         that the permission is for
     * @throws IllegalArgumentException if the permission details is missing some required information or has invalid
     *                                  information. e.g. the specified user does not exist on this ICE instance
     */
    public AccessPermission add(RegistryPartner partner, AccessPermission accessPermission) {
        // todo : must be in web of registries to accept add remote permission

        // person on this site that the permission is for
        String userId = accessPermission.getUserId();

        // verify that it is valid
        Account account = accountDAO.getByEmail(userId);   // todo : if null
        if (account == null)
            throw new IllegalArgumentException("Email address " + userId + " not on this registry instance");

        // remote person doing the sharing
        AccountTransfer accountTransfer = accessPermission.getAccount();
        if (accountTransfer == null)
            throw new IllegalArgumentException("No account for remote permission add");

        // read of write permission
        if (!accessPermission.isCanRead() && !accessPermission.isCanWrite())
            throw new IllegalArgumentException("Invalid read/write values for permission");

        // verify secret token
        if (StringUtils.isEmpty(accessPermission.getSecret()))
            throw new IllegalArgumentException("No access token sent with permission");

        String remoteEmail = accountTransfer.getEmail();

        // create a local folder instance that references a remote folder (also acts like a cache)
        Folder folder = new Folder();
        folder.setType(FolderType.REMOTE);
        folder.setName(accessPermission.getDisplay());
        folder.setOwnerEmail(remoteEmail);
        folder = this.folderDAO.create(folder);

        // get the remote partner object
        RemotePartner remotePartner = remotePartnerDAO.getByUrl(partner.getUrl());

        // get or create the client for the remote user who is sharing the folder
        RemoteClientModel remoteClientModel = getOrCreateRemoteClient(remoteEmail, remotePartner);

        // store access
        Permission permission = createPermissionModel(accessPermission, folder, account);

        RemoteAccessModel remoteAccessModel = createRemoteAccessModel(accessPermission, remoteClientModel, permission);
        return remoteAccessModel.toDataTransferObject();
    }

    public AccountTransfer getRemoteUser(long remoteId, String email) {
        RemotePartner partner = this.remotePartnerDAO.get(remoteId);
        if (partner == null)
            return null;

        AccountTransfer result = IceRestClient.getInstance().getWor(partner.getUrl(), "rest/users/" + email,
                AccountTransfer.class, null, partner.getApiKey());
        if (result == null)
            return null;

        return result;
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

    protected Permission createPermissionModel(AccessPermission accessPermission, Folder folder, Account account) {
        Permission permission = new Permission();
        permission.setFolder(folder);
        permission.setCanWrite(accessPermission.isCanWrite());
        permission.setCanRead(accessPermission.isCanRead());
        permission.setAccount(account);
        return this.permissionDAO.create(permission);
    }

    protected RemoteAccessModel createRemoteAccessModel(AccessPermission accessPermission, RemoteClientModel remoteClientModel,
                                                        Permission permission) {
        RemoteAccessModel remoteAccessModel = new RemoteAccessModel();
        remoteAccessModel.setToken(accessPermission.getSecret());
        remoteAccessModel.setRemoteClientModel(remoteClientModel);
        remoteAccessModel.setIdentifier(accessPermission.getTypeId() + "");
        remoteAccessModel.setPermission(permission);
        return remoteAccessModelDAO.create(remoteAccessModel);
    }
}

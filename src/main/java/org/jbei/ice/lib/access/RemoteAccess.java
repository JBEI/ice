package org.jbei.ice.lib.access;

import org.jbei.ice.lib.account.AccountTransfer;
import org.jbei.ice.lib.dto.access.AccessPermission;
import org.jbei.ice.lib.dto.folder.FolderType;
import org.jbei.ice.lib.dto.web.RegistryPartner;
import org.jbei.ice.storage.DAOFactory;
import org.jbei.ice.storage.hibernate.HibernateUtil;
import org.jbei.ice.storage.hibernate.dao.*;
import org.jbei.ice.storage.model.*;

/**
 * @author Hector Plahar
 */
public class RemoteAccess {

    private final FolderDAO folderDAO;
    private final ClientModelDAO clientModelDAO;
    private final RemotePartnerDAO remotePartnerDAO;
    private final AccountDAO accountDAO;
    private final RemoteAccessModelDAO remoteAccessModelDAO;
    private final PermissionDAO permissionDAO;

    public RemoteAccess() {
        this.folderDAO = DAOFactory.getFolderDAO();
        this.clientModelDAO = DAOFactory.getClientModelDAO();
        this.remotePartnerDAO = DAOFactory.getRemotePartnerDAO();
        this.accountDAO = DAOFactory.getAccountDAO();
        this.remoteAccessModelDAO = DAOFactory.getRemoteAccessModelDAO();
        this.permissionDAO = DAOFactory.getPermissionDAO();
    }

    // only supports folders for now
    public void add(RegistryPartner partner, AccessPermission accessPermission) {

        // todo : must be in web of registries to add remote permission

        // person on this site that the permission is for
        String userId = accessPermission.getUserId();

        // remote person doing the sharing
        AccountTransfer accountTransfer = accessPermission.getAccount();
        if (accountTransfer == null)
            throw new IllegalArgumentException("No account for remote permission add");

        if (!accessPermission.isCanRead() && !accessPermission.isCanWrite())
            throw new IllegalArgumentException("Invalid read/write values for permission");

        String remoteEmail = accountTransfer.getEmail();

        Folder folder = new Folder();
        folder.setType(FolderType.REMOTE);
        folder.setName(accessPermission.getDisplay());
        folder.setOwnerEmail(remoteEmail);
        folder = this.folderDAO.create(folder);

        // folder
        RemotePartner remotePartner = remotePartnerDAO.getByUrl(partner.getUrl());

        ClientModel clientModel = clientModelDAO.getModel(remoteEmail, remotePartner);
        if (clientModel == null) {
            clientModel = new ClientModel();
            clientModel.setRemotePartner(remotePartner);
            clientModel.setEmail(remoteEmail);
            clientModel = clientModelDAO.create(clientModel);
        }

        // if null, then the email address entered by the user on the remote partner is not available here
        Account account = accountDAO.getByEmail(userId);   // todo : if null
        if (account == null)
            throw new IllegalArgumentException("Email address " + userId + " not on this registry instance");

        Permission permission = createPermissionModel(accessPermission, folder, account);
        RemoteAccessModel remoteAccessModel = new RemoteAccessModel();
        remoteAccessModel.setToken(accessPermission.getSecret());
        remoteAccessModel.setClientModel(clientModel);
        remoteAccessModel.setIdentifier(accessPermission.getTypeId() + "");
        remoteAccessModel.setPermission(permission);
        remoteAccessModelDAO.create(remoteAccessModel);
    }

    protected Permission createPermissionModel(AccessPermission accessPermission, Folder folder, Account account) {
        Permission permission = new Permission();
        permission.setFolder(folder);
        permission.setCanWrite(accessPermission.isCanWrite());
        permission.setCanRead(accessPermission.isCanRead());
        permission.setAccount(account);
        return this.permissionDAO.create(permission);
    }

    public static void main(String[] args) {
        HibernateUtil.beginTransaction();
        RemoteAccess remoteAccess = new RemoteAccess();
        RegistryPartner partner = new RegistryPartner();
        partner.setUrl("registry-test2.jbei.org");

        // create permission to share with this user
        AccessPermission permission = new AccessPermission();
        AccountTransfer accountTransfer = new AccountTransfer();
        accountTransfer.setEmail("haplahar@lbl.gov");
        permission.setAccount(accountTransfer);
        permission.setDisplay("Remote from RegTest");
        permission.setUserId("haplahar@lbl.gov");
        permission.setType(AccessPermission.Type.WRITE_FOLDER);

        remoteAccess.add(partner, permission);
        HibernateUtil.commitTransaction();
    }
}

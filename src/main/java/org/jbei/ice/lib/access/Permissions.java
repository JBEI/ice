package org.jbei.ice.lib.access;

import org.jbei.ice.lib.dto.access.AccessPermission;
import org.jbei.ice.lib.entry.HasEntry;
import org.jbei.ice.storage.DAOFactory;
import org.jbei.ice.storage.hibernate.dao.GroupDAO;
import org.jbei.ice.storage.hibernate.dao.PermissionDAO;
import org.jbei.ice.storage.model.*;

/**
 * @author Hector Plahar
 */
public abstract class Permissions extends HasEntry {

    protected GroupDAO groupDAO;
    protected PermissionDAO permissionDAO;

    public Permissions() {
        groupDAO = DAOFactory.getGroupDAO();
        permissionDAO = DAOFactory.getPermissionDAO();
    }

    protected Permission addPermission(AccessPermission access, Entry entry, Folder folder, BulkUpload upload) {
        // account or group
        Account account = null;
        Group group = null;
        switch (access.getArticle()) {
            case ACCOUNT:
            default:
                account = accountDAO.get(access.getArticleId());
                break;

            case GROUP:
                group = groupDAO.get(access.getArticleId());
                break;
        }

        // does the permissions already exists
        if (permissionDAO.hasPermission(entry, folder, upload, account, group, access.isCanRead(), access.isCanWrite())) {
            return permissionDAO.retrievePermission(entry, folder, upload, account, group, access.isCanRead(), access.isCanWrite());
        }

        // add the permission if not
        Permission permission = new Permission();
        permission.setEntry(entry);
        if (entry != null)
            entry.getPermissions().add(permission);
        permission.setGroup(group);
        permission.setFolder(folder);
        permission.setUpload(upload);
        permission.setAccount(account);
        permission.setCanRead(access.isCanRead());
        permission.setCanWrite(access.isCanWrite());
        return permissionDAO.create(permission);
    }
}

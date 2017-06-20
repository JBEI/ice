package org.jbei.ice.lib.entry;

import org.jbei.ice.lib.dto.access.AccessPermission;
import org.jbei.ice.lib.executor.Task;
import org.jbei.ice.storage.DAOFactory;
import org.jbei.ice.storage.hibernate.dao.AccountDAO;
import org.jbei.ice.storage.hibernate.dao.EntryDAO;
import org.jbei.ice.storage.hibernate.dao.GroupDAO;
import org.jbei.ice.storage.hibernate.dao.PermissionDAO;
import org.jbei.ice.storage.model.Account;
import org.jbei.ice.storage.model.Entry;
import org.jbei.ice.storage.model.Group;
import org.jbei.ice.storage.model.Permission;

import java.util.ArrayList;
import java.util.List;

/**
 * Task to update the permissions for a provided list of entries
 *
 * @author Hector Plahar
 */
public class EntryPermissionTask extends Task {

    private final String userId;
    private final List<Long> entries;
    private final List<AccessPermission> permissions;
    private final boolean isAdd;
    private final PermissionDAO permissionDAO;
    private final GroupDAO groupDAO;
    private final AccountDAO accountDAO;

    public EntryPermissionTask(String userId, List<Long> entriesId, List<AccessPermission> permissions, boolean isAdd) {
        this.userId = userId;
        this.entries = new ArrayList<>(entriesId);
        this.permissions = new ArrayList<>(permissions);
        this.isAdd = isAdd;
        this.permissionDAO = DAOFactory.getPermissionDAO();
        this.groupDAO = DAOFactory.getGroupDAO();
        this.accountDAO = DAOFactory.getAccountDAO();
    }

    @Override
    public void execute() {

        EntryDAO entryDAO = DAOFactory.getEntryDAO();
        EntryAuthorization entryAuthorization = new EntryAuthorization();

        for (long entryId : entries) {
            Entry entry = entryDAO.get(entryId);
            if (entry == null)
                continue;

            // check permission on individual entries
            if (!entryAuthorization.canWrite(userId, entry)) {
                continue;
            }

            // add or remove permissions
            if (this.isAdd) {
                addPermissions(entry);
            } else {
                removePermissions(entry);
            }
        }
    }

    protected void addPermissions(Entry entry) {
        for (AccessPermission access : permissions) {
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
            if (permissionDAO.hasPermission(entry, null, null, account, group, access.isCanRead(), access.isCanWrite()))
                return;

            // add the permission if not
            Permission permission = new Permission();
            permission.setEntry(entry);
            entry.getPermissions().add(permission);

            permission.setGroup(group);
            permission.setFolder(null);
            permission.setUpload(null);
            permission.setAccount(account);
            permission.setCanRead(access.isCanRead());
            permission.setCanWrite(access.isCanWrite());
            permissionDAO.create(permission);
        }
    }

    protected void removePermissions(Entry entry) {
        for (AccessPermission access : permissions) {
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

            permissionDAO.removePermission(entry, null, null, account, group, access.isCanRead(), access.isCanWrite());
        }
    }
}

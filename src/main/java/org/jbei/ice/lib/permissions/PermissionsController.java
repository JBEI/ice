package org.jbei.ice.lib.permissions;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Set;

import org.jbei.ice.controllers.ControllerFactory;
import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.lib.account.AccountController;
import org.jbei.ice.lib.account.model.Account;
import org.jbei.ice.lib.dao.DAOException;
import org.jbei.ice.lib.dao.hibernate.HibernateHelper;
import org.jbei.ice.lib.entry.EntryController;
import org.jbei.ice.lib.entry.EntryDAO;
import org.jbei.ice.lib.entry.model.Entry;
import org.jbei.ice.lib.folder.Folder;
import org.jbei.ice.lib.folder.FolderController;
import org.jbei.ice.lib.group.Group;
import org.jbei.ice.lib.group.GroupController;
import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.lib.permissions.model.Permission;
import org.jbei.ice.shared.dto.permission.PermissionInfo;

public class PermissionsController {

    private final AccountController accountController;
    private final GroupController groupController;
    private final FolderController folderController;
    private final PermissionDAO dao;

    public PermissionsController() {
        accountController = ControllerFactory.getAccountController();
        groupController = ControllerFactory.getGroupController();
        folderController = ControllerFactory.getFolderController();
        dao = new PermissionDAO();
    }

    public Permission recordPermission(PermissionInfo info) throws ControllerException {
        try {
            Group group = groupController.getGroupById(info.getArticleId());
            // add the permission if not
            Permission permission = new Permission();
            permission.setGroup(group);
            permission.setCanRead(info.isCanRead());
            permission.setCanWrite(info.isCanWrite());
            return dao.save(permission);
        } catch (DAOException e) {
            Logger.error(e);
            throw new ControllerException(e);
        }
    }

    public Permission addPermission(Account requestingAccount, PermissionInfo info) throws ControllerException {
        Entry entry = null;
        Folder folder = null;

        EntryController entryController = ControllerFactory.getEntryController();

        if (info.isEntry()) {
            entry = entryController.get(requestingAccount, info.getTypeId());
            if (entry == null)
                throw new ControllerException("Cannot find entry " + info.getTypeId());

            // can user modify permissions for entry
            if (!hasWritePermission(requestingAccount, entry))
                throw new ControllerException(requestingAccount.getEmail() + " not allowed to " + info.toString());
        } else if (info.isFolder()) {
            folder = folderController.getFolderById(info.getTypeId());
            if (!hasWritePermission(requestingAccount, folder))
                throw new ControllerException(requestingAccount.getEmail() + " not allowed to " + info.toString());
        }

        // account or group
        Account account = null;
        Group group = null;
        switch (info.getArticle()) {
            case ACCOUNT:
            default:
                account = accountController.get(info.getArticleId());
                break;

            case GROUP:
                group = groupController.getGroupById(info.getArticleId());
                break;
        }

        // does the permissions already exists
        try {
            if (dao.hasPermission(entry, folder, account, group, info.isCanRead(), info.isCanWrite())) {
                return dao.retrievePermission(entry, folder, account, group, info.isCanRead(), info.isCanWrite());
            }

            // add the permission if not
            Permission permission = new Permission();
            permission.setEntry(entry);
            if (entry != null)
                entry.getPermissions().add(permission);
            permission.setGroup(group);
            permission.setFolder(folder);
            permission.setAccount(account);
            permission.setCanRead(info.isCanRead());
            permission.setCanWrite(info.isCanWrite());
            return dao.save(permission);
        } catch (DAOException e) {
            Logger.error(e);
            throw new ControllerException(e);
        }
    }

    public void removePermission(Account requestingAccount, PermissionInfo info) throws ControllerException {
        Entry entry = null;
        Folder folder = null;

        EntryController entryController = ControllerFactory.getEntryController();

        if (info.isEntry()) {
            entry = entryController.get(requestingAccount, info.getTypeId());
            if (entry == null)
                throw new ControllerException("Cannot find entry " + info.getTypeId());

            // can user modify permissions for entry
            if (!hasWritePermission(requestingAccount, entry))
                throw new ControllerException(requestingAccount.getEmail() + " not allowed to " + info.toString());
        } else if (info.isFolder()) {
            folder = folderController.getFolderById(info.getTypeId());
            if (!hasWritePermission(requestingAccount, folder))
                throw new ControllerException(requestingAccount.getEmail() + " not allowed to " + info.toString());
        }

        // account or group
        Account account = null;
        Group group = null;
        switch (info.getArticle()) {
            case ACCOUNT:
            default:
                account = accountController.get(info.getArticleId());
                break;

            case GROUP:
                group = groupController.getGroupById(info.getArticleId());
                break;
        }

        try {
            dao.removePermission(entry, folder, account, group, info.isCanRead(), info.isCanWrite());
        } catch (DAOException e) {
            Logger.error(e);
            throw new ControllerException(e);
        }
    }

    public int clearPermissions(Account account, Entry entry) throws ControllerException, PermissionException {
        if (!hasWritePermission(account, entry)) {
            throw new PermissionException(account.getEmail() + " doesn't have write permission for entry "
                                                  + entry.getId());
        }

        try {
            return dao.clearPermissions(entry);
        } catch (DAOException e) {
            throw new ControllerException(e);
        }
    }

    public int clearFolderPermissions(Account account, Folder folder) throws ControllerException, PermissionException {
        if (!hasWritePermission(account, folder)) {
            throw new PermissionException(account.getEmail() + " doesn't have write permissions for folder "
                                                  + folder.getId());
        }
        try {
            return dao.clearPermissions(folder);
        } catch (DAOException de) {
            throw new ControllerException(de);
        }
    }

    public int clearGroupPermissions(Account account, Group group) throws ControllerException, PermissionException {
        if (!group.getOwner().getEmail().equals(account.getEmail()) && !accountController.isAdministrator(account)) {
            throw new PermissionException(account.getEmail() + " does not have permission to delete group "
                                                  + group.getId());
        }

        try {
            return dao.clearPermissions(group);
        } catch (DAOException de) {
            throw new ControllerException(de);
        }
    }

    protected boolean accountHasReadPermission(Account account, Entry entry) throws ControllerException {
        try {
            return dao.hasPermission(entry, null, account, null, true, false);
        } catch (DAOException dao) {
            Logger.error(dao);
        }
        return false;
    }

    protected boolean accountHasReadPermission(Account account, Set<Folder> folders) throws ControllerException {
        try {
            return dao.hasPermissionMulti(null, folders, account, null, true, false);
        } catch (DAOException dao) {
            Logger.error(dao);
        }
        return false;
    }

    protected boolean accountHasWritePermission(Account account, Entry entry) throws ControllerException {
        try {
            return dao.hasPermission(entry, null, account, null, false, true);
        } catch (DAOException dao) {
            Logger.error(dao);
        }
        return false;
    }

    protected boolean accountHasWritePermission(Account account, Set<Folder> folders) throws ControllerException {
        try {
            return dao.hasPermissionMulti(null, folders, account, null, false, true);
        } catch (DAOException dao) {
            Logger.error(dao);
        }
        return false;
    }

    public boolean groupHasWritePermission(Set<Group> groups, Entry entry) throws ControllerException {
        try {
            if (groups.isEmpty())
                return false;

            return dao.hasPermissionMulti(entry, null, null, groups, false, true);
        } catch (DAOException dao) {
            Logger.error(dao);
        }
        return false;
    }

    // checks if there is a set permission with write user
    public boolean groupHasWritePermission(Set<Group> groups, Set<Folder> folders) throws ControllerException {
        try {
            if (groups.isEmpty())
                return false;

            return dao.hasPermissionMulti(null, folders, null, groups, false, true);
        } catch (DAOException dao) {
            Logger.error(dao);
        }
        return false;
    }

    public boolean groupHasReadPermission(Set<Group> groups, Entry entry) throws ControllerException {
        try {
            if (groups.isEmpty())
                return false;

            return dao.hasPermissionMulti(entry, null, null, groups, true, false);

        } catch (DAOException dao) {
            Logger.error(dao);
            return false;
        }
    }

    public boolean groupHasReadPermission(Set<Group> groups, Set<Folder> folders) throws ControllerException {
        if (groups.isEmpty() || folders.isEmpty())
            return false;

        try {
            return dao.hasPermissionMulti(null, folders, null, groups, true, false);
        } catch (DAOException e) {
            throw new ControllerException(e);
        }
    }

    /**
     * Checks if an account has read permissions for an entry. If no account is specified,
     * the method checks for public access of entry
     *
     * @param account user account
     * @param entry   existing entry
     * @return true if account has read privileges either explicitly or implicitly through group membership
     *         false otherwise
     * @throws ControllerException
     */
    public boolean hasReadPermission(Account account, Entry entry) throws ControllerException {

        // get groups for account. if account is null, this will return everyone group
        Set<Group> accountGroups = groupController.getAllGroups(account);

        // check read permission through group membership
        // ie. belongs to group that has read privileges for entry (or a group whose parent group does)
        if (groupHasReadPermission(accountGroups, entry))
            return true;

        if (account == null)
            return false;

        // check if account is a moderator account of is owner of the entry
        if (isOwnerOrAdministrator(account, entry))
            return true;

        // check explicit read permission
        if (accountHasReadPermission(account, entry))
            return true;

        Set<Folder> entryFolders = entry.getFolders();

        // can any group that account belongs to read any folder that entry is contained in?
        if (groupHasReadPermission(accountGroups, entryFolders))
            return true;

        // can account read any folder that entry is contained in?
        if (accountHasReadPermission(account, entryFolders))
            return true;

        return hasWritePermission(account, entry);
    }

    public boolean hasWritePermission(Account account, Entry entry) throws ControllerException {
        if (isOwnerOrAdministrator(account, entry))
            return true;

        // check write accounts for entry
        if (this.accountHasWritePermission(account, entry))
            return true;

        // get groups for account
        Set<Group> accountGroups = groupController.getAllGroups(account);

        // check group permissions
        if (groupHasWritePermission(accountGroups, entry))
            return true;

        Set<Folder> entryFolders = entry.getFolders();

        // can any group that account belongs to read any folder that entry is contained in?
        if (groupHasWritePermission(accountGroups, entryFolders))
            return true;

        // can account read any folder that entry is contained in?
        return accountHasWritePermission(account, entryFolders);
    }

    public boolean hasWritePermission(Account account, Folder folder) throws ControllerException {
        if (accountController.isAdministrator(account))
            return true;

        return folder.getOwnerEmail().equalsIgnoreCase(account.getEmail());
    }

    public Set<Folder> retrievePermissionFolders(Account account) throws ControllerException {
        Set<Group> groups = groupController.getAllGroups(account);
        try {
            return dao.retrieveFolderPermissions(account, groups);
        } catch (DAOException e) {
            throw new ControllerException(e);
        }
    }

    /**
     * checks if the account is the owner of the entry or if account is a
     * moderator account
     *
     * @param account user account
     * @param entry   entry
     * @return true if account is owner of entry or is a moderator, false otherwise
     * @throws ControllerException
     */
    protected boolean isOwnerOrAdministrator(Account account, Entry entry) throws ControllerException {
        // first check entry ownership
        if (account.getEmail().equals(entry.getOwnerEmail()))
            return true;

        // then check if moderator
        AccountController controller = ControllerFactory.getAccountController();
        if (controller.isAdministrator(account)) {
            return true;
        }

        // TODO : adding system account also but this needs to be handled in a better way
        Account systemAccount = controller.getSystemAccount();
        return (systemAccount != null && systemAccount.equals(account));
    }

    /**
     * retrieves permissions that have been explicitly set for entry
     *
     * @param account user making request
     * @param entry   entry whose permissions are being checked
     * @return list of set permissions
     */
    public ArrayList<PermissionInfo> retrieveSetEntryPermissions(Account account, Entry entry)
            throws ControllerException, PermissionException {
        if (!hasWritePermission(account, entry))
            throw new PermissionException(account.getEmail() + ": does not have read permissions for entry \""
                                                  + entry.getRecordId() + "\"");

        ArrayList<PermissionInfo> permissionInfos = new ArrayList<>();

        try {
            // read accounts
            Set<Account> readAccounts = dao.retrieveAccountPermissions(entry, false, true);
            for (Account readAccount : readAccounts) {
                permissionInfos.add(new PermissionInfo(PermissionInfo.Article.ACCOUNT, readAccount.getId(),
                                                       PermissionInfo.Type.READ_ENTRY, entry.getId(),
                                                       readAccount.getFullName()));
            }

            // write accounts
            Set<Account> writeAccounts = dao.retrieveAccountPermissions(entry, true, false);
            for (Account writeAccount : writeAccounts) {
                permissionInfos.add(new PermissionInfo(PermissionInfo.Article.ACCOUNT, writeAccount.getId(),
                                                       PermissionInfo.Type.WRITE_ENTRY, entry.getId(),
                                                       writeAccount.getFullName()));
            }

            // read groups
            Set<Group> readGroups = dao.retrieveGroupPermissions(entry, false, true);
            for (Group group : readGroups) {
                permissionInfos.add(new PermissionInfo(PermissionInfo.Article.GROUP, group.getId(),
                                                       PermissionInfo.Type.READ_ENTRY, entry.getId(),
                                                       group.getLabel()));
            }

            // write groups
            Set<Group> writeGroups = dao.retrieveGroupPermissions(entry, true, false);
            for (Group group : writeGroups) {
                permissionInfos.add(new PermissionInfo(PermissionInfo.Article.GROUP, group.getId(),
                                                       PermissionInfo.Type.WRITE_ENTRY, entry.getId(),
                                                       group.getLabel()));
            }
        } catch (DAOException de) {
            throw new ControllerException(de);
        }

        return permissionInfos;
    }

    /**
     * Retrieves permissions that have been explicitly set for the folders
     *
     * @param folder folder whose permissions are being retrieved
     * @return list of permissions that have been found for the specified folder
     */
    public ArrayList<PermissionInfo> retrieveSetFolderPermission(Folder folder)
            throws ControllerException {
        ArrayList<PermissionInfo> permissionInfos = new ArrayList<>();

        try {
            // read accounts
            Set<Account> readAccounts = dao.retrieveAccountPermissions(folder, false, true);
            for (Account readAccount : readAccounts) {
                permissionInfos.add(new PermissionInfo(PermissionInfo.Article.ACCOUNT, readAccount.getId(),
                                                       PermissionInfo.Type.READ_FOLDER, folder.getId(),
                                                       readAccount.getFullName()));
            }

            // write accounts
            Set<Account> writeAccounts = dao.retrieveAccountPermissions(folder, true, false);
            for (Account writeAccount : writeAccounts) {
                permissionInfos.add(new PermissionInfo(PermissionInfo.Article.ACCOUNT, writeAccount.getId(),
                                                       PermissionInfo.Type.WRITE_FOLDER, folder.getId(),
                                                       writeAccount.getFullName()));
            }

            // read groups
            Set<Group> readGroups = dao.retrieveGroupPermissions(folder, false, true);
            for (Group group : readGroups) {
                permissionInfos.add(new PermissionInfo(PermissionInfo.Article.GROUP, group.getId(),
                                                       PermissionInfo.Type.READ_FOLDER, folder.getId(),
                                                       group.getLabel()));
            }

            // write groups
            Set<Group> writeGroups = dao.retrieveGroupPermissions(folder, true, false);
            for (Group group : writeGroups) {
                permissionInfos.add(new PermissionInfo(PermissionInfo.Article.GROUP, group.getId(),
                                                       PermissionInfo.Type.WRITE_FOLDER, folder.getId(),
                                                       group.getLabel()));
            }
        } catch (DAOException de) {
            throw new ControllerException(de);
        }

        return permissionInfos;
    }

    public void upgradePermissions() throws ControllerException {
        try {
            Logger.info("Upgrading permissions....please wait");
            dao.upgradePermissions();

            LinkedList<Long> entries = ControllerFactory.getEntryController().getAllEntryIds();
            int count = entries.size();
            int i = -1;

            EntryDAO dao = new EntryDAO();

            for (long id : entries) {
                i += 1;
                if (i % 20 == 0) {
                    HibernateHelper.getSessionFactory().getCurrentSession().flush();
                    HibernateHelper.getSessionFactory().getCurrentSession().clear();
                    if (i % 1000 == 0)
                        Logger.info("Processed " + i + " entries (" + ((float) i / (float) count * 100) + "%)");
                }

                Entry entry;
                try {
                    entry = dao.get(id);
                } catch (DAOException e) {
                    Logger.warn(e.getMessage());
                    continue;
                }
                Account account = accountController.getByEmail(entry.getOwnerEmail());
                if (account == null)
                    continue;

                // add write permissions for owner
                PermissionInfo info = new PermissionInfo(PermissionInfo.Article.ACCOUNT, account.getId(),
                                                         PermissionInfo.Type.WRITE_ENTRY,
                                                         entry.getId(), account.getFullName());
                addPermission(account, info);
            }
            Logger.info("Permissions upgrade complete");
        } catch (DAOException e) {
            Logger.error(e);
        }
    }
}

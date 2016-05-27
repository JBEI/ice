package org.jbei.ice.lib.entry;

import org.jbei.ice.lib.access.PermissionException;
import org.jbei.ice.lib.access.Permissions;
import org.jbei.ice.lib.dto.access.AccessPermission;
import org.jbei.ice.lib.group.GroupController;
import org.jbei.ice.storage.DAOFactory;
import org.jbei.ice.storage.hibernate.dao.PermissionDAO;
import org.jbei.ice.storage.model.Entry;
import org.jbei.ice.storage.model.Group;
import org.jbei.ice.storage.model.Permission;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Permissions associated with a specified entry
 *
 * @author Hector Plahar
 */
public class EntryPermissions extends Permissions {

    private final PermissionDAO permissionDAO;
    private final GroupController groupController;
    private final Entry entry;
    private final String userId;
    private final EntryAuthorization authorization;

    public EntryPermissions(String entryId, String userId) {
        this.permissionDAO = DAOFactory.getPermissionDAO();
        this.groupController = new GroupController();
        this.entry = getEntry(entryId);
        this.userId = userId;
        this.authorization = new EntryAuthorization();
    }

    public void removePermission(long permissionId) {
        Permission permission = permissionDAO.get(permissionId);
        if (permission == null)
            return;

        // expect user to be able to modify entry
        authorization.expectWrite(userId, entry);

        // permission must be for entry and specified entry
        if (permission.getEntry() == null || permission.getEntry().getId() != entry.getId())
            return;

        permissionDAO.delete(permission);
    }

    /**
     * Retrieves permissions associated with a part. Requires that the requesting user has write permissions
     * on the specified part
     *
     * @return list of available permissions for the specified part
     * @throws PermissionException if the requesting user does not have write permissions for the part
     */
    public List<AccessPermission> getEntryPermissions() {
        // viewing permissions requires write permissions
        authorization.expectWrite(userId, entry);

        ArrayList<AccessPermission> accessPermissions = new ArrayList<>();
        Set<Permission> permissions = permissionDAO.getEntryPermissions(entry);

        GroupController groupController = new GroupController();
        Group publicGroup = groupController.createOrRetrievePublicGroup();
        for (Permission permission : permissions) {
            if (permission.getAccount() == null && permission.getGroup() == null)
                continue;
            if (permission.getGroup() != null && permission.getGroup() == publicGroup)
                continue;
            accessPermissions.add(permission.toDataTransferObject());
        }

        return accessPermissions;
    }

    /**
     * Adds a new permission to the specified entry. If the entry does not exist, a new one is created
     *
     * @param access permissions to be added to the entry
     * @return created permission if successful, null otherwise
     * @throws PermissionException if the requesting user does not have write permissions on the entry
     */
    public AccessPermission add(AccessPermission access) {
        if (access == null)
            return null;

        authorization.expectWrite(userId, entry);

        Permission permission = addPermission(access, entry, null, null);
        if (permission == null)
            return null;

        return permission.toDataTransferObject();
    }

    public boolean enablePublicReadAccess() {
        AccessPermission permission = new AccessPermission();
        permission.setType(AccessPermission.Type.READ_ENTRY);
        permission.setTypeId(this.entry.getId());
        permission.setArticle(AccessPermission.Article.GROUP);
        permission.setArticleId(groupController.createOrRetrievePublicGroup().getId());

        authorization.expectWrite(userId, entry);
        return addPermission(permission, entry, null, null) != null;
    }

    public boolean disablePublicReadAccess() {
        AccessPermission permission = new AccessPermission();
        permission.setType(AccessPermission.Type.READ_ENTRY);
        permission.setTypeId(this.entry.getId());
        permission.setArticle(AccessPermission.Article.GROUP);
        Group publicGroup = groupController.createOrRetrievePublicGroup();
        permission.setArticleId(publicGroup.getId());

        authorization.expectWrite(userId, entry);

        permissionDAO.removePermission(entry, null, null, null, publicGroup, true, false);
        return true;
    }
}

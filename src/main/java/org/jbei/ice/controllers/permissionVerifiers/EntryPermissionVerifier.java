package org.jbei.ice.controllers.permissionVerifiers;

import org.jbei.ice.lib.dao.IModel;
import org.jbei.ice.lib.models.Account;
import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.lib.permissions.PermissionManager;

/**
 * General Permission Verifier for Entry.
 * 
 * @author Zinovii Dmytriv
 * 
 */
public class EntryPermissionVerifier implements IPermissionVerifier {
    @Override
    public boolean hasReadPermissions(IModel model, Account account) {
        return PermissionManager.hasReadPermission((Entry) model, account);
    }

    @Override
    public boolean hasWritePermissions(IModel model, Account account) {
        return PermissionManager.hasWritePermission((Entry) model, account);
    }

    public boolean hasReadPermissionsById(long id, Account account) {
        return PermissionManager.hasReadPermission(id, account);
    }

    public boolean hasWritePermissionsById(long id, Account account) {
        return PermissionManager.hasWritePermission(id, account);
    }

    public boolean hasReadPermissionsByRecordId(String entryId, Account account) {
        return PermissionManager.hasReadPermission(entryId, account);
    }

    public boolean hasWritePermissionsByRecordId(String entryId, Account account) {
        return PermissionManager.hasWritePermission(entryId, account);
    }
}

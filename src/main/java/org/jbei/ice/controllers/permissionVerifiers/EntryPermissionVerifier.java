package org.jbei.ice.controllers.permissionVerifiers;

import org.jbei.ice.lib.dao.IModel;
import org.jbei.ice.lib.models.Account;
import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.lib.permissions.PermissionDAO;

/**
 * General Permission Verifier for Entry.
 * 
 * @author Hector Plahar, Zinovii Dmytriv
 * 
 */
public class EntryPermissionVerifier implements IPermissionVerifier {
    @Override
    public boolean hasReadPermissions(IModel model, Account account) {
        return PermissionDAO.hasReadPermission((Entry) model, account);
    }

    @Override
    public boolean hasWritePermissions(IModel model, Account account) {
        return PermissionDAO.hasWritePermission((Entry) model, account);
    }

    public boolean hasReadPermissionsById(long id, Account account) {
        return PermissionDAO.hasReadPermission(id, account);
    }

    public boolean hasReadPermissionsByRecordId(String entryId, Account account) {
        return PermissionDAO.hasReadPermission(entryId, account);
    }
}

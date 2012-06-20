package org.jbei.ice.controllers.permissionVerifiers;

import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.lib.account.model.Account;
import org.jbei.ice.lib.dao.IModel;
import org.jbei.ice.lib.entry.model.Entry;
import org.jbei.ice.lib.permissions.PermissionsController;

/**
 * General Permission Verifier for Entry.
 *
 * @author Hector Plahar, Zinovii Dmytriv
 */
public class EntryPermissionVerifier implements IPermissionVerifier {

    private final PermissionsController controller;

    public EntryPermissionVerifier() {
        controller = new PermissionsController();

    }

    @Override
    public boolean hasReadPermissions(IModel model, Account account) {
        try {
            return controller.hasReadPermission(account, (Entry) model);
        } catch (ControllerException e) {
            return false;
        }
    }

    @Override
    public boolean hasWritePermissions(IModel model, Account account) {
        try {
            return controller.hasWritePermission(account, (Entry) model);
        } catch (ControllerException e) {
            return false;
        }
    }
}

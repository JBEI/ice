package org.jbei.ice.controllers.permissionVerifiers;

import org.jbei.ice.lib.dao.IModel;
import org.jbei.ice.lib.models.Account;
import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.lib.models.Sample;
import org.jbei.ice.lib.permissions.PermissionManager;

/* Permission verifier for Sample and Location models 
 * 
 * If user is sample depositor then user has write permissions;
 * If user has write permissions for entry then user has write permissions;
 * Otherwise user has only read permissions
 * */

public class SamplePermissionVerifier implements IPermissionVerifier {
    @Override
    public boolean hasReadPermissions(IModel model, Account account) {
        return true;
    }

    @Override
    public boolean hasWritePermissions(IModel model, Account account) {
        if (model == null || account == null) {
            return false;
        }

        if (model instanceof Sample) {
            return hasWritePermissionSample((Sample) model, account);
        } else {
            return false;
        }
    }

    private boolean hasWritePermissionSample(Sample sample, Account account) {
        Entry entry = sample.getEntry();

        if (entry != null && PermissionManager.hasWritePermission(entry, account)) {
            return true;
        }

        if (sample.getDepositor() == null) {
            return false;
        } else if (sample.getDepositor().equals(account.getEmail())) {
            return true;
        } else {
            return false;
        }
    }

}

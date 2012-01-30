package org.jbei.ice.controllers.permissionVerifiers;

import org.jbei.ice.lib.dao.IModel;
import org.jbei.ice.lib.models.Account;
import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.lib.models.Sample;
import org.jbei.ice.lib.models.Storage;
import org.jbei.ice.lib.permissions.PermissionManager;

/**
 * Permission Verifier for {@link Sample}s and {@link Storage}s.
 * <p>
 * If the user is the sample depositor, or if the user has write permission to the {@link Entry}
 * associated with the sample, then user has write permissions. Otherwise the user has only read
 * permissions.
 * 
 * @author Zinovii Dmytriv, Timothy Ham
 * 
 */
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

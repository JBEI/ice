package org.jbei.ice.controllers.permissionVerifiers;

import org.jbei.ice.lib.account.model.Account;
import org.jbei.ice.lib.dao.IModel;
import org.jbei.ice.lib.models.Project;

/**
 * Permission Verifier for {@link Project}s.
 *
 * @author Zinovii Dmytriv
 */
public class ProjectPermissionVerifier implements IPermissionVerifier {
    @Override
    public boolean hasReadPermissions(IModel model, Account account) {
        return true;
    }

    @Override
    public boolean hasWritePermissions(IModel model, Account account) {
        if (model == null || account == null) {
            return false;
        }

        return account.getEmail().equals(((Project) model).getAccount().getEmail());
    }
}

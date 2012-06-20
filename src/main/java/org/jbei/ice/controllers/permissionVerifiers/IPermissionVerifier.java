package org.jbei.ice.controllers.permissionVerifiers;

import org.jbei.ice.lib.account.model.Account;
import org.jbei.ice.lib.dao.IModel;

/**
 * Interface for Permission Verifiers
 *
 * @author Zinovii Dmytriv
 */
public interface IPermissionVerifier {
    boolean hasReadPermissions(IModel model, Account account);

    boolean hasWritePermissions(IModel model, Account account);
}

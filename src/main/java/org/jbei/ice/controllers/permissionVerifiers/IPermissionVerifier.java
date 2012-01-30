package org.jbei.ice.controllers.permissionVerifiers;

import org.jbei.ice.lib.dao.IModel;
import org.jbei.ice.lib.models.Account;

/**
 * Interface for Permission Verifiers
 * 
 * @author Zinovii Dmytriv
 * 
 */
public interface IPermissionVerifier {
    boolean hasReadPermissions(IModel model, Account account);

    boolean hasWritePermissions(IModel model, Account account);
}

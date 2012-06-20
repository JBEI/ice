package org.jbei.ice.controllers.common;

import org.jbei.ice.controllers.permissionVerifiers.IPermissionVerifier;
import org.jbei.ice.lib.account.model.Account;

/**
 * Base class for all Controllers.
 *
 * @author Zinovii Dmytriv
 */
public class Controller {
    private final Account account;
    private final IPermissionVerifier permissionVerifier;

    /**
     * Constructor.
     *
     * @param account
     * @param permissionsVerifier
     */
    public Controller(Account account, IPermissionVerifier permissionsVerifier) {
        this.account = account;
        permissionVerifier = permissionsVerifier;
    }

    /**
     * Retrieve {@link Account} associated with this controller object.
     *
     * @return Account
     */
    public Account getAccount() {
        return account;
    }

    /**
     * Retrieve the {@link IPermissionVerifier} object associated with this controller.
     *
     * @return IPermissionVerifier.
     */
    public IPermissionVerifier getPermissionVerifier() {
        return permissionVerifier;
    }
}

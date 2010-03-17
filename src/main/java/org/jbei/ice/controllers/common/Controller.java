package org.jbei.ice.controllers.common;

import org.jbei.ice.controllers.permissionVerifiers.IPermissionVerifier;
import org.jbei.ice.lib.models.Account;

public class Controller {
    private Account account;
    private IPermissionVerifier permissionVerifier;

    public Controller(Account account, IPermissionVerifier permissionsVerifier) {
        this.account = account;
        this.permissionVerifier = permissionsVerifier;
    }

    public Account getAccount() {
        return account;
    }

    public IPermissionVerifier getPermissionVerifier() {
        return permissionVerifier;
    }
}

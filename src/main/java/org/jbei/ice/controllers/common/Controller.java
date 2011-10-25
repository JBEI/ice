package org.jbei.ice.controllers.common;

import org.jbei.ice.controllers.permissionVerifiers.IPermissionVerifier;
import org.jbei.ice.lib.models.Account;

/**
 * Base class for all Controllers.
 * <p>
 * Controllers represent the ABI for the gd-ice software. They try to hide the underlying databases,
 * file systems, and third party libraries to present a uniform interface to all of the
 * functionality within gd-ice.
 * <p>
 * Controllers must also be initiated with a user {@link Account} object, which then provide
 * permission checking for that user. Therefore, any operation that require permission checking,
 * that is any operation that are <em>not</em> run as System, should go through Controllers. This
 * means all user facing web pages also should go through controllers.
 * <p>
 * External API's such as BlazeDS or SOAP should wrap controllers and provide session persistence.
 * <p>
 * Controllers also wrap underlying exceptions and throw their own ControllerExceptions.
 * 
 * @author Zinovii Dmytriv
 * 
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

/**
 * The Application Binary Interface for gd-ice.<p>
 * Controllers try to hide the underlying databases,
 * file systems, and third party libraries to present a uniform interface to all of the
 * functionality within gd-ice.
 * <p>
 * For database interactions, controllers wrap their respective {@link org.jbei.ice.lib.managers managers},
 * and add permissions.
 * <p>
 * Controllers must also be initiated with a user {@link org.jbei.ice.lib.account.model.Account Account} object,
 * which then provide
 * permission checking for that user. Therefore, any operation that require permission checking,
 * that is any operation that are <em>not</em> run as System, should go through Controllers. This
 * means all user facing web pages also should go through controllers.
 * <p>
 * External API's such as BlazeDS or SOAP should wrap controllers and provide session persistence.
 * <p>
 * Controllers also wrap underlying exceptions and throw their own ControllerExceptions.
 */
package org.jbei.ice.controllers;


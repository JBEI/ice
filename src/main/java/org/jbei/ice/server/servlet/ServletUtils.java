package org.jbei.ice.server.servlet;

import javax.servlet.http.Cookie;

import org.jbei.ice.controllers.ControllerFactory;
import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.lib.account.AccountController;
import org.jbei.ice.lib.account.model.Account;
import org.jbei.ice.lib.logging.Logger;

/**
 * @author Hector Plahar
 */
public class ServletUtils {

    public final static String COOKIE_NAME = "gd-ice";

    /**
     * Goes through the stored cookies to determine if user has a valid session id
     *
     * @param cookies cookies
     * @return Account for the stored session id, if available and still valid; null otherwise
     */
    public static Account isLoggedIn(Cookie[] cookies) {
        try {
            for (Cookie cookie : cookies) {
                if (COOKIE_NAME.equals(cookie.getName())) {
                    String sid = cookie.getValue();
                    if (sid == null || sid.isEmpty())
                        return null;

                    if (!AccountController.isAuthenticated(sid))
                        return null;

                    AccountController controller = ControllerFactory.getAccountController();
                    return controller.getAccountBySessionKey(sid);
                }
            }
        } catch (ControllerException ce) {
            Logger.error(ce);
        }
        return null;
    }
}

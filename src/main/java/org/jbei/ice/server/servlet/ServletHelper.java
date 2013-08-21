package org.jbei.ice.server.servlet;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.servlet.http.Cookie;

import org.jbei.ice.controllers.ControllerFactory;
import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.lib.account.AccountController;
import org.jbei.ice.lib.account.model.Account;
import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.lib.utils.Utils;

/**
 * Helper class for the servlets
 *
 * @author Hector Plahar
 */
public class ServletHelper {

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

    public static String uploadSequence(File sequenceFile) {
        Path existingPath = Paths.get(sequenceFile.getAbsolutePath());
        String uuid = Utils.generateUUID();
        try {
            Files.move(existingPath, existingPath.resolveSibling(uuid));
            return uuid;
        } catch (IOException e) {
            return "Error: Could not upload file";
        }
    }
}

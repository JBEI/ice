package org.jbei.ice.server.servlet;

import java.io.File;
import java.io.IOException;
import javax.servlet.http.Cookie;

import org.jbei.ice.controllers.ControllerFactory;
import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.lib.account.AccountController;
import org.jbei.ice.lib.account.model.Account;
import org.jbei.ice.lib.entry.EntryController;
import org.jbei.ice.lib.entry.model.Entry;
import org.jbei.ice.lib.entry.sequence.SequenceController;
import org.jbei.ice.lib.logging.Logger;

import org.apache.commons.io.FileUtils;

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

    public static String uploadSequence(Account account, long entryId, File sequenceFile) {
        EntryController entryController = ControllerFactory.getEntryController();
        Entry entry;
        try {
            entry = entryController.get(account, entryId);
            String sequenceUser = FileUtils.readFileToString(sequenceFile);
            SequenceController sequenceController = ControllerFactory.getSequenceController();
            try {
                sequenceController.parseAndSaveSequence(account, entry, sequenceUser);
                return "";
            } catch (ControllerException e) {
                Logger.error(e);
                return e.getMessage();
            }
        } catch (ControllerException | IOException e) {
            Logger.error(e);
            return "Could not retrieve entry with id " + entryId;
        }
    }
}

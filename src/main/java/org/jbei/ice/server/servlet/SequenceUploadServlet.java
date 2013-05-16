package org.jbei.ice.server.servlet;

import java.util.List;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.jbei.ice.controllers.ControllerFactory;
import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.lib.account.AccountController;
import org.jbei.ice.lib.account.model.Account;
import org.jbei.ice.lib.entry.EntryController;
import org.jbei.ice.lib.entry.model.Entry;
import org.jbei.ice.lib.entry.sequence.SequenceController;
import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.lib.utils.Utils;
import org.jbei.ice.shared.dto.ConfigurationKey;

import gwtupload.server.UploadAction;
import gwtupload.server.exceptions.UploadActionException;
import org.apache.commons.fileupload.FileItem;

public class SequenceUploadServlet extends UploadAction {

    private static final long serialVersionUID = 1L;

    private Account isLoggedIn(AccountController controller, Cookie[] cookies)
            throws ControllerException {

        final String COOKIE_NAME = Utils.getConfigValue(ConfigurationKey.COOKIE_NAME);

        for (Cookie cookie : cookies) {
            if (COOKIE_NAME.equals(cookie.getName())) {
                String sid = cookie.getValue();
                if (sid == null || sid.isEmpty())
                    return null;

                if (!AccountController.isAuthenticated(sid))
                    return null;
                return controller.getAccountBySessionKey(sid);
            }
        }
        return null;
    }

    @Override
    public String executeAction(HttpServletRequest request, List<FileItem> sessionFiles) throws UploadActionException {
        Account account;
        AccountController controller = ControllerFactory.getAccountController();

        try {
            account = isLoggedIn(controller, request.getCookies());
            if (account == null) {
                String sessionId = request.getParameter("sid");
                if (sessionId == null)
                    return "Could not validate account";
                account = controller.getAccountBySessionKey(sessionId);
                if (account == null)
                    return "Could not validate account";
            }
        } catch (ControllerException ce) {
            Logger.error(ce);
            String url = request.getRequestURL().toString();
            String path = request.getServletPath();
            url = url.substring(0, url.indexOf(path));
            Logger.info(SequenceUploadServlet.class.getSimpleName()
                                + ": authentication failed. Redirecting user to " + url);
            return "";
        }

        String sequenceUser = request.getParameter("seq");
        String entryId = request.getParameter("eid");
        String type = request.getParameter("type");

        // check entry
        EntryController entryController = ControllerFactory.getEntryController();
        Entry entry;
        try {
            entry = entryController.get(account, Long.decode(entryId));
        } catch (NumberFormatException e1) {
            Logger.error(e1);
            return "Invalid entry Id received: " + entryId;
        } catch (ControllerException e1) {
            Logger.error(e1);
            return "Could not retrieve entry with id " + entryId;
        }

        if ("file".equalsIgnoreCase(type)) {

            for (FileItem item : sessionFiles) {
                if (item.isFormField())
                    continue;

                // get contents of file
                sequenceUser = item.getString();
                if (sequenceUser != null && !sequenceUser.isEmpty()) {
                    removeSessionFileItems(request);
                    return saveSequence(entry, account, sequenceUser);
                }
            }
        }

        return saveSequence(entry, account, sequenceUser);
    }

    private String saveSequence(Entry entry, Account account, String sequenceUser) {
        SequenceController sequenceController = ControllerFactory.getSequenceController();
        try {
            sequenceController.parseAndSaveSequence(account, entry, sequenceUser);
            return "";
        } catch (ControllerException e) {
            Logger.error(e);
            return e.getMessage();
        }
    }
}

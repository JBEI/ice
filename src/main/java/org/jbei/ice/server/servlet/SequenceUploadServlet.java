package org.jbei.ice.server.servlet;

import gwtupload.server.UploadAction;
import gwtupload.server.exceptions.UploadActionException;

import java.util.List;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItem;
import org.jbei.ice.controllers.EntryController;
import org.jbei.ice.controllers.SequenceController;
import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.lib.account.AccountController;
import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.lib.models.Account;
import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.lib.models.Sequence;
import org.jbei.ice.lib.parsers.GeneralParser;
import org.jbei.ice.lib.permissions.PermissionException;
import org.jbei.ice.lib.vo.IDNASequence;

public class SequenceUploadServlet extends UploadAction {

    private static final long serialVersionUID = 1L;

    private Account isLoggedIn(Cookie[] cookies) throws ControllerException {
        for (Cookie cookie : cookies) {
            if ("gd-ice".equals(cookie.getName())) {
                String sid = cookie.getValue();
                if (sid == null || sid.isEmpty())
                    return null;

                if (!AccountController.isAuthenticated(sid))
                    return null;
                return AccountController.getAccountBySessionKey(sid);
            }
        }
        return null;
    }

    @Override
    public String executeAction(HttpServletRequest request, List<FileItem> sessionFiles)
            throws UploadActionException {

        Account account;

        try {
            account = isLoggedIn(request.getCookies());
            if (account == null) {
                String sessionId = request.getParameter("sid");
                if (sessionId == null)
                    return "Could not validate account";
                account = AccountController.getAccountBySessionKey(sessionId);
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
        EntryController entryController = new EntryController(account);
        Entry entry;
        try {
            entry = entryController.get(Long.decode(entryId));
        } catch (NumberFormatException e1) {
            Logger.error(e1);
            return "Invalid entry Id received: " + entryId;
        } catch (ControllerException e1) {
            Logger.error(e1);
            return "Could not retrieve entry with id " + entryId;
        } catch (PermissionException e1) {
            Logger.error(e1);
            return "User does not have permissions to modify entry with id " + entryId;
        }

        if ("file".equalsIgnoreCase(type)) {

            for (FileItem item : sessionFiles) {
                if (item.isFormField())
                    continue;

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

        SequenceController sequenceController = new SequenceController(account);
        IDNASequence dnaSequence = SequenceController.parse(sequenceUser);

        if (dnaSequence == null || dnaSequence.getSequence().equals("")) {
            String errorMsg = "Couldn't parse sequence file! Supported formats: "
                    + GeneralParser.getInstance().availableParsersToString()
                    + ". "
                    + "If you believe this is an error, please contact the administrator with your file";

            return errorMsg;
        }

        Sequence sequence = null;

        try {
            sequence = SequenceController.dnaSequenceToSequence(dnaSequence);
            sequence.setSequenceUser(sequenceUser);
            sequence.setEntry(entry);
            sequenceController.save(sequence);
        } catch (ControllerException e) {
            Logger.error(e);
            return "Error saving sequence";
        } catch (PermissionException e) {
            Logger.error(e);
            return "User does not have permissions to save sequence";
        }

        return "";
    }
}

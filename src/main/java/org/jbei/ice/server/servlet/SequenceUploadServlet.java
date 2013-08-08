package org.jbei.ice.server.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jbei.ice.controllers.ControllerFactory;
import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.lib.account.AccountController;
import org.jbei.ice.lib.account.model.Account;
import org.jbei.ice.lib.entry.EntryController;
import org.jbei.ice.lib.entry.model.Entry;
import org.jbei.ice.lib.entry.sequence.SequenceController;
import org.jbei.ice.lib.logging.Logger;

import org.apache.commons.fileupload.FileItem;

public class SequenceUploadServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final String COOKIE_NAME = "jbei_ice_cookie";

    private Account isLoggedIn(AccountController controller, Cookie[] cookies) throws ControllerException {
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
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        List<FileItem> sessionFiles = (List<FileItem>) req.getSession().getAttribute("FILES");
        String msg = executeAction(req, sessionFiles);
        if (msg != null && !msg.trim().isEmpty()) {
            sendServerResponse(resp, msg);
        }
    }

    /**
     * Writes a response to the client.
     */
    protected void sendServerResponse(HttpServletResponse response, String message) throws IOException {
        response.setContentType("text/html; charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        out.print(message);
        out.flush();
        out.close();
    }

    public String executeAction(HttpServletRequest request, List<FileItem> sessionFiles) {
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

        String entryId = request.getParameter("eid");

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

        for (FileItem item : sessionFiles) {
            if (item.isFormField())
                continue;

            // get contents of file
            String sequenceUser = item.getString();
            if (sequenceUser != null && !sequenceUser.isEmpty()) {
                removeSessionFileItems(request);
                return saveSequence(entry, account, sequenceUser);
            }
            return saveSequence(entry, account, sequenceUser);
        }

        return "Error: No file uploaded";
    }

    protected void removeSessionFileItems(HttpServletRequest request) {
        request.getSession().removeAttribute("FILES");
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

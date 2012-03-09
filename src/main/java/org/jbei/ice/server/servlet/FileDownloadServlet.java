package org.jbei.ice.server.servlet;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jbei.ice.controllers.AccountController;
import org.jbei.ice.controllers.AttachmentController;
import org.jbei.ice.controllers.SequenceAnalysisController;
import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.lib.models.Account;
import org.jbei.ice.lib.models.Attachment;
import org.jbei.ice.lib.models.TraceSequence;
import org.jbei.ice.lib.permissions.PermissionException;

/**
 * Servlet for serving the different kinds of files
 * available on gd-ice. Requires a valid session id as
 * a parameter
 * 
 * @author Hector Plahar
 */
public class FileDownloadServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final int BYTES_DOWNLOAD = 1024;
    private static final String SEQUENCE_TYPE = "sequence";
    private static final String ATTACHMENT_TYPE = "attachment";

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        Logger.info(FileDownloadServlet.class.getSimpleName() + ": attempt to download file");

        Account account;

        try {
            account = isLoggedIn(request.getCookies());
        } catch (ControllerException ce) {
            Logger.error(ce);
            String url = request.getRequestURL().toString();
            String path = request.getServletPath();
            url = url.substring(0, url.indexOf(path));
            response.sendRedirect(url);
            Logger.info(FileDownloadServlet.class.getSimpleName()
                    + ": authenication failed. Redirecting user to " + url);
            return;
        }

        String fileId = request.getParameter("id");
        String type = request.getParameter("type");
        Logger.info(FileDownloadServlet.class.getSimpleName() + ": user = " + account.getEmail()
                + ", file type = " + type + ", file id = " + fileId);

        File file = null;

        if (SEQUENCE_TYPE.equalsIgnoreCase(type))
            file = getTraceSequenceFile(account, fileId);
        else if (ATTACHMENT_TYPE.equalsIgnoreCase(type))
            file = getAttachmentFile(account, fileId);

        // check for null file
        if (file == null) {
            Logger.info(FileDownloadServlet.class.getSimpleName() + ": attempt failed");
            return;
        }

        response.setContentType("application/octet-stream");
        response.setContentLength((int) file.length());
        response.setHeader("Content-Disposition", "attachment;filename=" + file.getName());

        OutputStream os = response.getOutputStream();
        DataInputStream is = new DataInputStream(new FileInputStream(file));

        int read = 0;
        byte[] bytes = new byte[BYTES_DOWNLOAD];

        while ((read = is.read(bytes)) != -1) {
            os.write(bytes, 0, read);
        }
        os.flush();
        os.close();
    }

    private File getTraceSequenceFile(Account account, String fileId) {
        SequenceAnalysisController controller = new SequenceAnalysisController(account);

        try {
            TraceSequence sequence = controller.getTraceSequenceByFileId(fileId);
            if (sequence == null)
                return null;

            return controller.getFile(sequence);
        } catch (ControllerException ce) {
            Logger.error("Error retrieving sequence trace file with id " + fileId + ". Details...");
            Logger.error(ce);
            return null;
        }
    }

    private File getAttachmentFile(Account account, String fileId) {
        AttachmentController controller = new AttachmentController(account);
        try {
            Attachment attachment = controller.getAttachmentByFileId(fileId);
            if (attachment == null)
                return null;

            return controller.getFile(attachment);
        } catch (ControllerException ce) {
            Logger.error("Error retrieving attachment file with id " + fileId + ". Details...");
            Logger.error(ce);
            return null;
        } catch (PermissionException e) {
            Logger.error("User " + account.getEmail()
                    + " does not have appropriate permissions to view file");
            Logger.error(e);
            return null;
        }
    }

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
}

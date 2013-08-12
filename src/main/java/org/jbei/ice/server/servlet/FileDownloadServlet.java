package org.jbei.ice.server.servlet;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Paths;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jbei.ice.controllers.ControllerFactory;
import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.lib.account.AccountController;
import org.jbei.ice.lib.account.model.Account;
import org.jbei.ice.lib.entry.attachment.Attachment;
import org.jbei.ice.lib.entry.attachment.AttachmentController;
import org.jbei.ice.lib.entry.sequence.SequenceAnalysisController;
import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.lib.models.TraceSequence;
import org.jbei.ice.lib.permissions.PermissionException;
import org.jbei.ice.lib.shared.dto.ConfigurationKey;

import org.apache.commons.io.IOUtils;

/**
 * Servlet for serving the different kinds of files
 * available on gd-ice. Requires a valid session id as
 * a parameter
 *
 * @author Hector Plahar
 */
public class FileDownloadServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final String SEQUENCE_TYPE = "sequence";
    private static final String ATTACHMENT_TYPE = "attachment";
    private static final String SBOL_VISUAL_TYPE = "sbol_visual";

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Logger.info(FileDownloadServlet.class.getSimpleName() + ": attempt to download file");
        String fileId = request.getParameter("id");
        String type = request.getParameter("type");
        String sid = request.getParameter("sid");

        Account account;

        try {
            account = ServletUtils.isLoggedIn(request.getCookies());
            if (account == null) {
                if (!AccountController.isAuthenticated(sid))
                    return;

                AccountController controller = ControllerFactory.getAccountController();
                account = controller.getAccountBySessionKey(sid);
                if (account == null)
                    return;
            }

        } catch (ControllerException ce) {
            Logger.error(ce);
            String url = request.getRequestURL().toString();
            String path = request.getServletPath();
            url = url.substring(0, url.indexOf(path));
            response.sendRedirect(url);
            Logger.info(FileDownloadServlet.class.getSimpleName()
                                + ": authentication failed. Redirecting user to " + url);
            return;
        }

        Logger.info(FileDownloadServlet.class.getSimpleName() + ": user = " + account.getEmail()
                            + ", file type = " + type + ", file id = " + fileId);

        File file = null;

        if (SEQUENCE_TYPE.equalsIgnoreCase(type))
            file = getTraceSequenceFile(fileId, response);
        else if (ATTACHMENT_TYPE.equalsIgnoreCase(type))
            file = getAttachmentFile(account, fileId, response);
        else if (SBOL_VISUAL_TYPE.equalsIgnoreCase(type)) {
            getSBOLVisualType(fileId, response);
            return;
        }

        // check for null file
        if (file == null) {
            Logger.info(FileDownloadServlet.class.getSimpleName() + ": attempt failed");
            return;
        }

        response.setContentType("application/octet-stream");
        response.setContentLength((int) file.length());
        IOUtils.copy(new FileInputStream(file), response.getOutputStream());
    }

    private File getTraceSequenceFile(String fileId, HttpServletResponse response) {
        SequenceAnalysisController controller = ControllerFactory.getSequenceAnalysisController();

        try {
            TraceSequence sequence = controller.getTraceSequenceByFileId(fileId);
            if (sequence == null)
                return null;

            response.setHeader("Content-Disposition", "attachment;filename=" + sequence.getFilename());
            return controller.getFile(sequence);
        } catch (ControllerException ce) {
            Logger.error("Error retrieving sequence trace file with id " + fileId + ". Details...");
            Logger.error(ce);
            return null;
        }
    }

    private void getSBOLVisualType(String fileId, HttpServletResponse response) {
        response.setContentType("image/png");
        String tmpDir;
        try {
            tmpDir = ControllerFactory.getConfigurationController().getPropertyValue(
                    ConfigurationKey.TEMPORARY_DIRECTORY);
        } catch (ControllerException e) {
            Logger.error(e);
            return;
        }

        File file = Paths.get(tmpDir, fileId).toFile();
        response.setContentLength((int) file.length());
        try {
            IOUtils.copy(new FileInputStream(file), response.getOutputStream());
        } catch (IOException ioe) {
            Logger.error(ioe);
        }
    }

    private File getAttachmentFile(Account account, String fileId, HttpServletResponse response) {
        AttachmentController controller = ControllerFactory.getAttachmentController();
        try {
            Attachment attachment = controller.getAttachmentByFileId(fileId);
            if (attachment == null)
                return null;

            response.setHeader("Content-Disposition", "attachment;filename=" + attachment.getFileName());
            return controller.getFile(account, attachment);
        } catch (ControllerException ce) {
            Logger.error("Error retrieving attachment file with id " + fileId + ". Details...");
            Logger.error(ce);
            return null;
        } catch (PermissionException e) {
            Logger.error("User " + account.getEmail() + " does not have appropriate permissions to view file");
            Logger.error(e);
            return null;
        }
    }
}

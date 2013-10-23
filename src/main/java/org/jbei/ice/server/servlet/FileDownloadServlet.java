package org.jbei.ice.server.servlet;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
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
import org.jbei.ice.lib.shared.EntryAddType;
import org.jbei.ice.lib.shared.dto.ConfigurationKey;
import org.jbei.ice.lib.shared.dto.bulkupload.EntryField;
import org.jbei.ice.lib.utils.Utils;
import org.jbei.ice.server.servlet.helper.BulkCSVUploadHeaders;

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
    private static final String TMP_FILE_TYPE = "tmp";
    private static final String TEMPLATE_TYPE = "template";

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Logger.info(FileDownloadServlet.class.getSimpleName() + ": attempt to download file");
        String fileId = request.getParameter("id");
        String type = request.getParameter("type");
        String sid = request.getParameter("sid");

        Account account;

        try {
            account = ServletHelper.isLoggedIn(request.getCookies());
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
        } else if (TMP_FILE_TYPE.equalsIgnoreCase(type)) {
            file = getTempFile(fileId, response);
        } else if (TEMPLATE_TYPE.equalsIgnoreCase(type)) {
            String addType = request.getParameter("add_type");
            getCSVTemplate(addType, response);
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
        String tmpDir = Utils.getConfigValue(ConfigurationKey.TEMPORARY_DIRECTORY);
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

    private File getTempFile(String fileId, HttpServletResponse response) {
        String tempDir = Utils.getConfigValue(ConfigurationKey.TEMPORARY_DIRECTORY);
        File file = Paths.get(tempDir, fileId).toFile();
        response.setHeader("Content-Disposition", "attachment;filename=" + fileId);
        return file;
    }

    private void getCSVTemplate(String addType, HttpServletResponse response) {
        EntryAddType entryAddType = EntryAddType.valueOf(addType);
        if (entryAddType == null)
            return;

        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment;filename=" + addType.toLowerCase() + "_csv_upload.csv");
        ArrayList<EntryField> headers = BulkCSVUploadHeaders.getHeadersForType(entryAddType);
        if (headers == null)
            return;

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < headers.size(); i++) {
            if (i != 0) {
                sb.append(",");
            }

            sb.append('"');
            sb.append(headers.get(i).toString());
            if (BulkCSVUploadHeaders.isRequired(headers.get(i), entryAddType))
                sb.append("*");
            sb.append('"');
        }

        sb.append("\n");
        try {
            IOUtils.copy(new ByteArrayInputStream(sb.toString().getBytes()), response.getOutputStream());
        } catch (IOException e) {
            Logger.error(e);
        }
    }
}

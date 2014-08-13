package org.jbei.ice.servlet;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Paths;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jbei.ice.ControllerException;
import org.jbei.ice.lib.access.PermissionException;
import org.jbei.ice.lib.account.model.Account;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.dto.ConfigurationKey;
import org.jbei.ice.lib.entry.attachment.Attachment;
import org.jbei.ice.lib.entry.attachment.AttachmentController;
import org.jbei.ice.lib.entry.sequence.SequenceAnalysisController;
import org.jbei.ice.lib.models.TraceSequence;
import org.jbei.ice.lib.utils.Utils;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

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

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String fileId = request.getParameter("id");
        String type = request.getParameter("type");
        String sid = request.getParameter("sid");


//        Logger.info(FileDownloadServlet.class.getSimpleName() + ": user = " + account.getEmail()
//                            + ", file type = " + type + ", file id = " + fileId);

        File file = null;

        if (SEQUENCE_TYPE.equalsIgnoreCase(type))
            file = getTraceSequenceFile(fileId, response);
        else if (ATTACHMENT_TYPE.equalsIgnoreCase(type)) {
        }
//            file = getAttachmentFile(account, fileId, response);
        else if (SBOL_VISUAL_TYPE.equalsIgnoreCase(type)) {
            getSBOLVisualType(fileId, response);
            return;
        } else if (TMP_FILE_TYPE.equalsIgnoreCase(type)) {
            file = getTempFile(fileId, response);
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
        SequenceAnalysisController controller = new SequenceAnalysisController();

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
        if (StringUtils.isEmpty(fileId))
            return;

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
        AttachmentController controller = new AttachmentController();
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
}

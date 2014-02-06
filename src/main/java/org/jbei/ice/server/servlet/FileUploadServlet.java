package org.jbei.ice.server.servlet;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Paths;
import java.util.ArrayList;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jbei.ice.controllers.ControllerFactory;
import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.lib.account.AccountController;
import org.jbei.ice.lib.account.model.Account;
import org.jbei.ice.lib.bulkupload.FileBulkUpload;
import org.jbei.ice.lib.entry.EntryController;
import org.jbei.ice.lib.entry.attachment.Attachment;
import org.jbei.ice.lib.entry.attachment.AttachmentController;
import org.jbei.ice.lib.entry.model.Entry;
import org.jbei.ice.lib.entry.sequence.SequenceAnalysisController;
import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.lib.permissions.PermissionException;
import org.jbei.ice.lib.shared.EntryAddType;
import org.jbei.ice.lib.shared.dto.ConfigurationKey;
import org.jbei.ice.lib.shared.dto.bulkupload.BulkUploadAutoUpdate;
import org.jbei.ice.lib.shared.dto.entry.EntryType;
import org.jbei.ice.lib.utils.Utils;

import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.util.Streams;
import org.apache.commons.io.FileUtils;

/**
 * Servlet that handles file uploads. If an upload type (e.g. sequence or attachment)
 * is associated with an entry if the entry id (EID) passed is valid. If no valid EID is provided
 * then the file is simply uploaded and the file id (essentially a unique identifier based on type)
 * is returned
 *
 * @author Hector Plahar
 */
public class FileUploadServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private static final String TRACE_SEQUENCE = "trace_sequence";
    private static final String ATTACHMENT_TYPE = "attachment";
    private static final String BULK_UPLOAD_FILE_TYPE = "bulk_file_upload";
    private static final String BULK_CSV_UPLOAD = "bulk_csv";
    private static final String SEQUENCE = "sequence";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doPost(req, resp);
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String desc = request.getParameter("desc");
        String type = request.getParameter("type");
        String entryId = request.getParameter("eid");
        String bulkUploadId = request.getParameter("bid");
        String sid = request.getParameter("sid");
        String isSequenceStr = request.getParameter("is_sequence");
        String isTraceStr = request.getParameter("is_trace");
        String entryType = request.getParameter("entry_type");
        String entryAddType = request.getParameter("entry_add_type");

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
            Logger.info(FileUploadServlet.class.getSimpleName() + ": Redirecting user to login at \"" + url + "\"");
            return;
        }

        String result = "";
        String tmpDir = Utils.getConfigValue(ConfigurationKey.TEMPORARY_DIRECTORY);

        // upload
        try {
            FileItemFactory fileItemFactory = new DiskFileItemFactory();
            ServletFileUpload servletFileUpload = new ServletFileUpload(fileItemFactory);
            FileItemIterator fileItemIterator = servletFileUpload.getItemIterator(request);

            while (fileItemIterator.hasNext()) {
                FileItemStream fileItemStream = fileItemIterator.next();

                String filePath = fileItemStream.getName();
                if (filePath.isEmpty()) {
                    sendServerResponse(response, "Error: Could not upload the file");
                    return;
                }

                String fileName = filePath.substring(filePath.lastIndexOf(File.pathSeparatorChar) + 1);
                File file = new File(tmpDir, fileName);
                if (file.length() > 3000000l) {
                    sendServerResponse(response, "Error: File size is too large");
                    return;
                }

                Streams.copy(fileItemStream.openStream(), new FileOutputStream(file), true);
                switch (type) {
                    case ATTACHMENT_TYPE:
                        result = uploadAttachment(account, file, entryId, desc, fileName);
                        break;

                    case TRACE_SEQUENCE:
                        try {
                            result = uploadSequenceTraceFile(file, entryId, account, fileName, false);
                        } catch (IOException e) {
                            Logger.error(e);
                        }
                        break;

                    case BULK_UPLOAD_FILE_TYPE:
                        long entryIdl = Long.decode(entryId);
                        Boolean isSequence = Boolean.parseBoolean(isSequenceStr);
                        Boolean isTrace = Boolean.parseBoolean(isTraceStr);
                        result = uploadBulkUploadFile(account, file, bulkUploadId, entryIdl, fileName, isSequence,
                                                      isTrace, entryType, entryAddType);
                        break;

                    case BULK_CSV_UPLOAD:
                        result = uploadCSV(account, request, file);
                        break;

                    case SEQUENCE:
                        result = ServletHelper.uploadSequence(file);
                        break;

                    default:
                        return;
                }
            }
        } catch (FileUploadException fue) {
            String errMsg = "Error: " + fue.getMessage();
            sendServerResponse(response, errMsg);
            return;
        }

        sendServerResponse(response, result);
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

    public String uploadBulkUploadFile(Account account, File file, String bulkUploadIdStr, long entryId,
            String saveName, boolean isSequence, boolean isTrace, String entryType, String entryAddType) {
        long bulkUploadId;
        try {
            bulkUploadId = Long.decode(bulkUploadIdStr);
        } catch (NumberFormatException e) {
            Logger.error(e);
            bulkUploadId = 0;
        }

        EntryType type = EntryType.valueOf(entryType);
        EntryAddType addType = EntryAddType.valueOf(entryAddType);
        EntryController entryController = ControllerFactory.getEntryController();

        Entry entry;
        try {
            entry = entryController.get(account, entryId);
        } catch (ControllerException ce) {
            return "Error";
        }

        try {
            if (entry == null) {
                return uploadToNewEntry(account, file, saveName, isSequence, isTrace, type, addType, bulkUploadId,
                                        true);
            }

            // associate with entry
            AttachmentController attachmentController = ControllerFactory.getAttachmentController();

            boolean isStrainWithPlasmidPlasmid = (addType == EntryAddType.STRAIN_WITH_PLASMID
                    && type == EntryType.PLASMID);
            if (isStrainWithPlasmidPlasmid && !entry.getLinkedEntries().isEmpty()) {
                entry = (Entry) entry.getLinkedEntries().toArray()[0];
            }

            if (isTrace) {
                return uploadSequenceTraceFile(file, entry.getId() + "", account, saveName, true);
            }

            if (isSequence) {
                String sequenceString = FileUtils.readFileToString(file);
                ControllerFactory.getSequenceController().parseAndSaveSequence(account, entry, sequenceString);
                return saveName;
            } else {
                try (FileInputStream inputStream = new FileInputStream(file)) {
                    ArrayList<Attachment> attachments = attachmentController.getByEntry(account, entry);
                    if (attachments != null && !attachments.isEmpty()) {
                        for (Attachment attachment : attachments) {
                            try {
                                attachmentController.delete(account, attachment);
                            } catch (PermissionException e) {
                                Logger.warn(e.getMessage());
                            }
                        }
                    }

                    Attachment attachment = new Attachment();
                    attachment.setEntry(entry);
                    attachment.setDescription("");
                    attachment.setFileName(saveName);
                    attachment = attachmentController.save(account, attachment, inputStream);
                    if (attachment != null)
                        return attachment.getFileId() + ", " + saveName;
                    return "";
                } catch (ControllerException e) {
                    Logger.error(e);
                }
                return "";
            }
        } catch (IOException | ControllerException e) {
            Logger.error(e);
            return "";
        }
    }

    public String uploadToNewEntry(Account account, File file, String saveName, boolean isSequence, boolean isTrace,
            EntryType type, EntryAddType addType, long bid, boolean deleteExisting) {
        BulkUploadAutoUpdate update = new BulkUploadAutoUpdate(type);
        update.setBulkUploadId(bid);
        try {
            update = ControllerFactory.getBulkUploadController().autoUpdateBulkUpload(account.getEmail(), update,
                                                                                      addType);
            boolean isStrainWithPlasmidPlasmid = (addType == EntryAddType.STRAIN_WITH_PLASMID
                    && type == EntryType.PLASMID);
            Entry entry = ControllerFactory.getEntryController().get(account, update.getEntryId());

            if (isStrainWithPlasmidPlasmid && !entry.getLinkedEntries().isEmpty()) {
                entry = (Entry) entry.getLinkedEntries().toArray()[0];
            }

            // check if trace sequence
            if (isTrace) {
                return uploadSequenceTraceFile(file, entry.getId() + "", account, saveName, deleteExisting);
            }

            if (isSequence) {
                String sequenceString = FileUtils.readFileToString(file);
                ControllerFactory.getSequenceController().parseAndSaveSequence(account, entry, sequenceString);
                return Long.toString(update.getBulkUploadId()) + ","
                        + Long.toString(update.getEntryId()) + "," + saveName;
            }

            // if attachment
            AttachmentController attachmentController = ControllerFactory.getAttachmentController();
            FileInputStream inputStream = new FileInputStream(file);
            ArrayList<Attachment> attachments = attachmentController.getByEntry(account, entry);
            if (attachments != null && !attachments.isEmpty()) {
                for (Attachment attachment : attachments) {
                    try {
                        attachmentController.delete(account, attachment);
                    } catch (PermissionException e) {
                        Logger.warn(e.getMessage());
                    }
                }
            }

            Attachment attachment = new Attachment();
            attachment.setEntry(entry);
            attachment.setDescription("");
            attachment.setFileName(saveName);
            Attachment saved = attachmentController.save(account, attachment, inputStream);
            if (saved != null) {
                return Long.toString(update.getBulkUploadId()) + "," + Long.toString(update.getEntryId())
                        + ", " + saved.getFileId();
            }
            return "Error: Could not upload attachment";
        } catch (ControllerException | IOException e) {
            Logger.error(e);
            return "Error " + e.getMessage();
        }
    }

    // TODO : this needs to go to manager/controller
    private String uploadSequenceTraceFile(File file, String entryId, Account account, String uploadFileName,
            boolean deleteExisting) throws IOException {
        EntryController controller = ControllerFactory.getEntryController();
        Entry entry = null;
        try {
            entry = controller.get(account, Long.decode(entryId));
        } catch (NumberFormatException | ControllerException e) {
            Logger.error(e);
        }

        if (entry == null)
            return "Error: Unknown entry (" + entryId + "). Upload aborted";

        SequenceAnalysisController sequenceAnalysisController = ControllerFactory.getSequenceAnalysisController();

        try (FileInputStream inputStream = new FileInputStream(file)) {
            sequenceAnalysisController.uploadTraceSequenceFile(account, entry, uploadFileName, inputStream,
                                                               deleteExisting);
            return uploadFileName;
        } catch (ControllerException e) {
            Logger.error(e.getMessage());
            return "Error: " + e.getMessage();
        }
    }

    // TODO : check for path information in filename. safari includes it
    private String uploadAttachment(Account account, File file, String entryId, String desc, String filename) {
        EntryController controller = ControllerFactory.getEntryController();
        Entry entry;
        try {
            entry = controller.get(account, Long.decode(entryId));

            if (entry != null) {
                Attachment attachment = new Attachment();
                attachment.setEntry(entry);
                attachment.setDescription(desc);
                attachment.setFileName(filename);
                AttachmentController attachmentController = ControllerFactory.getAttachmentController();
                Attachment saved = attachmentController.save(account, attachment, new FileInputStream(file));
                if (saved != null)
                    return saved.getFileId() + "," + filename;
            } else {
                // upload file. return file id
                String fileId = Utils.generateUUID();
                File attachmentFile = Paths.get(Utils.getConfigValue(ConfigurationKey.DATA_DIRECTORY),
                                                AttachmentController.attachmentDirName, fileId).toFile();
                FileUtils.copyFile(file, attachmentFile);
                return fileId + "," + filename;
            }
        } catch (ControllerException | IOException | NumberFormatException e) {
            Logger.error(e);
        }

        return null;
    }

    protected String uploadCSV(Account account, HttpServletRequest request, File file) {
        String entryAddTypeString = request.getParameter("upload");
        EntryAddType addType;
        try {
            addType = EntryAddType.valueOf(entryAddTypeString);
        } catch (IllegalArgumentException ie) {
            return "Error: File upload has unknown type [\"" + entryAddTypeString + "\"]";
        }
        if (addType == null)
            return "Error: File upload has unknown type";

        try {
            FileBulkUpload bulkUpload = new FileBulkUpload(account.getEmail(), file.toPath(), addType);
            return bulkUpload.process();
        } catch (IOException ie) {
            return "Error: " + ie.getMessage();
        }
    }
}

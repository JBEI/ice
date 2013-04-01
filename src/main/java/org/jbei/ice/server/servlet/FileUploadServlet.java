package org.jbei.ice.server.servlet;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jbei.ice.controllers.ControllerFactory;
import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.lib.account.AccountController;
import org.jbei.ice.lib.account.model.Account;
import org.jbei.ice.lib.entry.EntryController;
import org.jbei.ice.lib.entry.attachment.Attachment;
import org.jbei.ice.lib.entry.attachment.AttachmentController;
import org.jbei.ice.lib.entry.model.Entry;
import org.jbei.ice.lib.entry.sequence.SequenceAnalysisController;
import org.jbei.ice.lib.entry.sequence.SequenceController;
import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.lib.permissions.PermissionException;
import org.jbei.ice.lib.utils.FileUtils;
import org.jbei.ice.lib.utils.Utils;
import org.jbei.ice.lib.vo.IDNASequence;
import org.jbei.ice.shared.dto.ConfigurationKey;

import gwtupload.server.UploadAction;
import gwtupload.server.exceptions.UploadActionException;
import gwtupload.shared.UConsts;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.io.IOUtils;

/**
 * GWTUpload servlet that handles file uploads. If an upload type (e.g. sequence or attachment)
 * is associated with an entry if the entry id (EID) passed is valid. If no valid EID is provided
 * then the file is simply uploaded and the file id (essentially a unique identifier based on type)
 * is returned
 *
 * @author Hector Plahar
 */
public class FileUploadServlet extends UploadAction {

    private static final long serialVersionUID = 1L;

    private static final String SEQUENCE_TYPE = "sequence";
    private static final String ATTACHMENT_TYPE = "attachment";
    private static final String BULK_UPLOAD_FILE_TYPE = "bulk_file_upload";
    private static final String BULK_CSV_UPLOAD = "bulk_csv";

    Hashtable<String, String> receivedContentTypes = new Hashtable<>();
    Hashtable<String, File> receivedFiles = new Hashtable<>(); // received files list and content types

    private Account isLoggedIn(Cookie[] cookies) throws ControllerException {
        for (Cookie cookie : cookies) {
            if ("gd-ice".equals(cookie.getName())) {
                String sid = cookie.getValue();
                if (sid == null || sid.isEmpty())
                    return null;

                if (!AccountController.isAuthenticated(sid))
                    return null;
                AccountController controller = ControllerFactory.getAccountController();
                return controller.getAccountBySessionKey(sid);
            }
        }
        return null;
    }

    /**
     * Override executeAction to save the received files in a custom place and delete this items from session.
     */
    @Override
    public String executeAction(HttpServletRequest request, List<FileItem> sessionFiles) throws UploadActionException {
        String desc = request.getParameter("desc");
        String type = request.getParameter("type");
        String entryId = request.getParameter("eid");
        String sid = request.getParameter("sid");
        String isSequenceStr = request.getParameter("is_sequence");

        Account account;

        try {
            account = isLoggedIn(request.getCookies());
            if (account == null) {
                if (!AccountController.isAuthenticated(sid))
                    return "";
                AccountController controller = ControllerFactory.getAccountController();
                account = controller.getAccountBySessionKey(sid);
                if (account == null)
                    return "";
            }
        } catch (ControllerException ce) {
            Logger.error(ce);
            String url = request.getRequestURL().toString();
            String path = request.getServletPath();
            url = url.substring(0, url.indexOf(path));
            Logger.info(FileUploadServlet.class.getSimpleName() + ": Redirecting user to login at \"" + url + "\"");
            return "";
        }

        String result = "";
        String tmpDir = Utils.getConfigValue(ConfigurationKey.TEMPORARY_DIRECTORY);

        // retrieve file
        for (FileItem item : sessionFiles) {
            if (item.isFormField())
                continue;

            String saveName = item.getName().replaceAll("[\\\\/><\\|\\s\"'{}()\\[\\]]+", "_");
            if (saveName.isEmpty())
                continue;

            File file = new File(tmpDir + File.separator + saveName);

            try {
                item.write(file);
            } catch (Exception e) {
                Logger.error(e);
                continue;
            }

            switch (type) {
                case ATTACHMENT_TYPE:
                    result = uploadAttachment(account, file, entryId, desc, saveName);
                    break;

                case SEQUENCE_TYPE:
                    try {
                        result = uploadSequenceTraceFile(file, entryId, account, saveName);
                    } catch (IOException e) {
                        Logger.error(e);
                    }
                    break;

                case BULK_UPLOAD_FILE_TYPE:
                case BULK_CSV_UPLOAD:
                    Boolean isSequence = Boolean.parseBoolean(isSequenceStr);
                    result = uploadBulkCSVFile(account, file, entryId, saveName, isSequence);
                    break;
            }
            break;
        }

        removeSessionFileItems(request);
        return result;
    }

    public String uploadBulkCSVFile(Account account, File file, String entryId, String saveName, boolean isSequence) {
        try {
            if (entryId == null || "0".equals(entryId.trim())) // TODO : create new file Id and return it
                return uploadFileToTemp(file, saveName, isSequence);

            // associate with entry
            EntryController entryController = ControllerFactory.getEntryController();
            AttachmentController attachmentController = ControllerFactory.getAttachmentController();

            Entry entry = null;
            try {
                entry = entryController.get(account, Long.decode(entryId));
            } catch (NumberFormatException | ControllerException | PermissionException e) {
                Logger.error(e);
            }
            if (entry == null)
                return uploadFileToTemp(file, saveName, isSequence);

            if (isSequence) {
                String sequenceString = FileUtils.readFileToString(file);
                ControllerFactory.getSequenceController().parseAndSaveSequence(account, entry, sequenceString);
                return saveName;
            } else {
                FileInputStream inputStream = new FileInputStream(file);
                if (entry != null) {
                    try {
                        ArrayList<Attachment> attachments = attachmentController.getByEntry(account, entry);
                        if (attachments != null && !attachments.isEmpty()) {
                            for (Attachment attachment : attachments) {
                                try {
                                    attachmentController.delete(account, attachment);
                                } catch (PermissionException e) {
                                    continue;
                                }
                            }
                        }

                        Attachment attachment = new Attachment();
                        attachment.setEntry(entry);
                        attachment.setDescription("");
                        attachment.setFileName(saveName);
                        Attachment saved = attachmentController.save(account, attachment, inputStream);
                        if (saved != null)
                            return saved.getFileId();
                        return "";
                    } catch (ControllerException e) {
                        Logger.error(e);
                    }
                }
                return "";
            }
        } catch (IOException | ControllerException e) {
            Logger.error(e);
            return "";
        }
    }

    public String uploadFileToTemp(File file, String saveName, boolean isSequence) {
        if (isSequence) {
            try {
                String sequenceString = FileUtils.readFileToString(file);
                if (SequenceController.parse(sequenceString) == null)
                    return "F\tCould not parse the sequence file";
            } catch (IOException e) {
                Logger.error(e);
                return "F\tError uploading file.";
            }
        }

        String fileId = Utils.generateUUID();
        if (file.renameTo(new File(file.getParentFile() + File.separator + fileId)))
            return fileId;
        return saveName;
    }

    // TODO : this needs to go to manager/controller
    private String uploadSequenceTraceFile(File file, String entryId, Account account, String uploadFileName)
            throws IOException {
        EntryController controller = ControllerFactory.getEntryController();
        Entry entry = null;
        try {
            entry = controller.get(account, Long.decode(entryId));
        } catch (NumberFormatException | ControllerException | PermissionException e) {
            Logger.error(e);
        }

        if (entry == null)
            return "Unknown entry (" + entryId + "). Upload aborted";

        SequenceAnalysisController sequenceAnalysisController = ControllerFactory.getSequenceAnalysisController();
        IDNASequence dnaSequence;

        ArrayList<ByteHolder> byteHolders = new ArrayList<>();
        FileInputStream inputStream = new FileInputStream(file);

        if (uploadFileName.toLowerCase().endsWith(".zip")) {
            try {
                ZipInputStream zis = new ZipInputStream(inputStream);
                ZipEntry zipEntry;
                while (true) {
                    zipEntry = zis.getNextEntry();

                    if (zipEntry != null) {
                        if (!zipEntry.isDirectory() && !zipEntry.getName().startsWith("__MACOSX")) {
                            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                            int c;
                            while ((c = zis.read()) != -1) {
                                byteArrayOutputStream.write(c);
                            }
                            ByteHolder byteHolder = new ByteHolder();
                            byteHolder.setBytes(byteArrayOutputStream.toByteArray());
                            byteHolder.setName(zipEntry.getName());
                            byteHolders.add(byteHolder);
                        }
                    } else {
                        break;
                    }
                }
            } catch (IOException e) {
                String errMsg = ("Could not parse zip file.");
                Logger.error(errMsg);
                return errMsg;
            }
        } else {
            ByteHolder byteHolder = new ByteHolder();
            byteHolder.setBytes(IOUtils.toByteArray(inputStream));
            byteHolder.setName(uploadFileName);
            byteHolders.add(byteHolder);
        }

        String currentFileName = "";
        try {
            for (ByteHolder byteHolder : byteHolders) {
                currentFileName = byteHolder.getName();
                dnaSequence = sequenceAnalysisController.parse(byteHolder.getBytes());
                if (dnaSequence == null || dnaSequence.getSequence() == null) {
                    String errMsg = ("Could not parse \"" + currentFileName
                            + "\". Only Fasta, GenBank & ABI files are supported.");
                    Logger.error(errMsg);
                    return errMsg;
                }

                sequenceAnalysisController.uploadTraceSequence(entry, byteHolder.getName(),
                                                               account.getEmail(),
                                                               dnaSequence.getSequence().toLowerCase(),
                                                               new ByteArrayInputStream(byteHolder.getBytes()));
            }
            sequenceAnalysisController.rebuildAllAlignments(entry);
            return "";
        } catch (ControllerException e) {
            String errMsg = ("Could not parse \"" + currentFileName
                    + "\". Only Fasta, GenBank & ABI files are supported.");
            Logger.error(errMsg);
            return errMsg;
        }
    }

    // TODO : check for path information in filename. safari includes it
    private String uploadAttachment(Account account, File file, String entryId, String desc, String filename) {
        EntryController controller = ControllerFactory.getEntryController();
        Entry entry = null;
        try {
            entry = controller.get(account, Long.decode(entryId));
        } catch (NumberFormatException | ControllerException | PermissionException e) {
            Logger.error(e);
        }

        try {
            FileInputStream inputStream = new FileInputStream(file);
            if (entry != null) {
                try {
                    Attachment attachment = new Attachment();
                    attachment.setEntry(entry);
                    attachment.setDescription(desc);
                    attachment.setFileName(filename);
                    AttachmentController attachmentController = ControllerFactory.getAttachmentController();
                    Attachment saved = attachmentController.save(account, attachment, inputStream);
                    if (saved != null)
                        return saved.getFileId();
                } catch (ControllerException e) {
                    Logger.error(e);
                }
            } else {
                // upload file. return file id
                String fileId = Utils.generateUUID();
                File attDir = new File(Utils.getConfigValue(ConfigurationKey.ATTACHMENTS_DIRECTORY));
                FileUtils.writeFile(attDir, fileId, inputStream);
                return fileId;
            }
        } catch (IOException e) {
            Logger.error(e);
            return null;
        }

        return null;
    }

    /**
     * Get the content of an uploaded file.
     */
    @Override
    public void getUploadedFile(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String fieldName = request.getParameter(UConsts.PARAM_SHOW);
        File f = receivedFiles.get(fieldName);
        if (f != null) {
            response.setContentType(receivedContentTypes.get(fieldName));
            FileInputStream is = new FileInputStream(f);
            copyFromInputStreamToOutputStream(is, response.getOutputStream());
        } else {
            renderXmlResponse(request, response, XML_ERROR_ITEM_NOT_FOUND);
        }
    }

    /**
     * Remove a file when the user sends a delete request.
     */
    @Override
    public void removeItem(HttpServletRequest request, String fieldName) throws UploadActionException {
        File file = receivedFiles.get(fieldName);
        receivedFiles.remove(fieldName);
        receivedContentTypes.remove(fieldName);
        if (file != null) {
            file.delete();
        }
    }
}

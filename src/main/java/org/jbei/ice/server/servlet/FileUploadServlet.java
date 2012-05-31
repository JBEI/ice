package org.jbei.ice.server.servlet;

import gwtupload.server.UploadAction;
import gwtupload.server.exceptions.UploadActionException;
import gwtupload.shared.UConsts;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.io.IOUtils;
import org.jbei.ice.controllers.AccountController;
import org.jbei.ice.controllers.SequenceAnalysisController;
import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.lib.managers.AttachmentManager;
import org.jbei.ice.lib.managers.EntryManager;
import org.jbei.ice.lib.managers.ManagerException;
import org.jbei.ice.lib.models.Account;
import org.jbei.ice.lib.models.Attachment;
import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.lib.utils.JbeirSettings;
import org.jbei.ice.lib.utils.Utils;
import org.jbei.ice.lib.vo.IDNASequence;

// TODO : Robust exception handling
public class FileUploadServlet extends UploadAction {

    private static final long serialVersionUID = 1L;
    private static final String SEQUENCE_TYPE = "sequence";
    private static final String ATTACHMENT_TYPE = "attachment";
    private static final String BULK_ATTACHMENT_TYPE = "bulk_attachment";
    private static final String BULK_SEQUENCE_TYPE = "bulk_sequence";

    Hashtable<String, String> receivedContentTypes = new Hashtable<String, String>();
    Hashtable<String, File> receivedFiles = new Hashtable<String, File>(); // received files list and content types

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

    /**
     * Override executeAction to save the received files in a custom place
     * and delete this items from session.
     */
    @Override
    public String executeAction(HttpServletRequest request, List<FileItem> sessionFiles)
            throws UploadActionException {

        String desc = request.getParameter("desc");
        String type = request.getParameter("type");
        String entryId = request.getParameter("eid");
        String sid = request.getParameter("sid");
        Account account;

        try {
            account = isLoggedIn(request.getCookies());
            if (account == null) {
                if (!AccountController.isAuthenticated(sid))
                    return "";
                account = AccountController.getAccountBySessionKey(sid);
                if (account == null)
                    return "";
            }

        } catch (ControllerException ce) {
            Logger.error(ce);
            String url = request.getRequestURL().toString();
            String path = request.getServletPath();
            url = url.substring(0, url.indexOf(path));
            Logger.info(FileUploadServlet.class.getSimpleName()
                    + ": authenication failed. Redirecting user to " + url);
            return "";
        }

        for (FileItem item : sessionFiles) {
            if (item.isFormField())
                continue;

            String saveName = item.getName().replaceAll("[\\\\/><\\|\\s\"'{}()\\[\\]]+", "_");
            String tmpDir = JbeirSettings.getSetting("TEMPORARY_DIRECTORY");
            File file = new File(tmpDir + File.separator + saveName);

            try {
                item.write(file);
            } catch (Exception e) {
                Logger.error(e);
                continue;
            }

            if (ATTACHMENT_TYPE.equalsIgnoreCase(type)) {
                if (entryId == null || entryId.isEmpty())
                    return "No entry id specified for file upload";
                return uploadAttachment(file, entryId, desc, saveName);
            } else if (SEQUENCE_TYPE.equalsIgnoreCase(type)) {
                if (entryId == null || entryId.isEmpty())
                    return "No entry id specified for file upload";
                try {
                    return uploadSequenceTraceFile(file, entryId, account, saveName);
                } catch (IOException e) {
                    Logger.error(e);
                }
            } else if (BULK_ATTACHMENT_TYPE.equalsIgnoreCase(type)) {

                return uploadBulkAttachment(file, saveName);
            } else if (BULK_SEQUENCE_TYPE.equalsIgnoreCase(type)) {
                // TODO : ??
            }
        }

        removeSessionFileItems(request);
        return "";

    }

    public String uploadBulkAttachment(File file, String saveName) {
        String fileId = Utils.generateUUID();
        if (file.renameTo(new File(file.getParentFile() + File.separator + fileId)))
            return fileId;
        return saveName;
    }

    // TODO : this needs to go to manager/controller
    private String uploadSequenceTraceFile(File file, String entryId, Account account,
            String uploadFileName) throws IOException {

        Entry entry = null;
        try {
            entry = EntryManager.get(Long.decode(entryId));
        } catch (NumberFormatException e1) {
            Logger.error("Exception decoding entry id ", e1);
        } catch (ManagerException e1) {
            Logger.error("Exception retrieving entry with decoded entryId " + entryId, e1);
        }
        if (entry == null)
            return "Could not retrieve entry with id : " + entryId;

        SequenceAnalysisController sequenceAnalysisController = new SequenceAnalysisController(
                account);

        IDNASequence dnaSequence = null;

        ArrayList<ByteHolder> byteHolders = new ArrayList<ByteHolder>();
        FileInputStream inputStream = new FileInputStream(file);

        if ((uploadFileName.endsWith(".zip")) || uploadFileName.endsWith(".ZIP")) {
            try {

                ZipInputStream zis = new ZipInputStream(inputStream);
                ZipEntry zipEntry = null;

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
                    String errMsg = ("Could not parse file: " + currentFileName + ". Only Fasta, GenBank, or ABI files are supported.");
                    Logger.error(errMsg);
                    return errMsg;
                }

                sequenceAnalysisController.uploadTraceSequence(entry, byteHolder.getName(),
                    account.getEmail(), dnaSequence.getSequence().toLowerCase(),
                    new ByteArrayInputStream(byteHolder.getBytes()));
            }
            sequenceAnalysisController.rebuildAllAlignments(entry);
            return "ok";

        } catch (ControllerException e) { // 
            String errMsg = ("Could not parse file: " + currentFileName + ". Only Fasta, GenBank, or ABI files are supported.");
            Logger.error(errMsg);
            return errMsg;
        }
    }

    // TODO : check for path information in filename. safari includes it
    private String uploadAttachment(File file, String entryId, String desc, String filename) {

        try {
            Entry entry = EntryManager.get(Long.decode(entryId));
            if (entry == null)
                return "Could not retrieve entry with id : " + entryId;
            Attachment attachment = new Attachment();
            attachment.setEntry(entry);
            attachment.setDescription(desc);
            attachment.setFileName(filename);

            FileInputStream inputStream = new FileInputStream(file);
            // TODO : this save method also writes the attachment to file
            Attachment saved = AttachmentManager.save(attachment, inputStream);
            if (saved != null)
                return saved.getFileId();
        } catch (ManagerException e) {
            Logger.error(e);
        } catch (FileNotFoundException e) {
            Logger.error(e);
        }

        return null;
    }

    /**
     * Get the content of an uploaded file.
     */
    @Override
    public void getUploadedFile(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
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
    public void removeItem(HttpServletRequest request, String fieldName)
            throws UploadActionException {
        File file = receivedFiles.get(fieldName);
        receivedFiles.remove(fieldName);
        receivedContentTypes.remove(fieldName);
        if (file != null) {
            file.delete();
        }
    }
}

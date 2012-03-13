package org.jbei.ice.server.servlet;

import gwtupload.server.UploadAction;
import gwtupload.server.exceptions.UploadActionException;
import gwtupload.shared.UConsts;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Hashtable;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.jbei.ice.lib.managers.AttachmentManager;
import org.jbei.ice.lib.managers.EntryManager;
import org.jbei.ice.lib.managers.ManagerException;
import org.jbei.ice.lib.models.Attachment;
import org.jbei.ice.lib.models.Entry;

public class FileUploadServlet extends UploadAction {

    private static final long serialVersionUID = 1L;
    private static final String SEQUENCE_TYPE = "sequence";
    private static final String ATTACHMENT_TYPE = "attachment";

    Hashtable<String, String> receivedContentTypes = new Hashtable<String, String>();
    Hashtable<String, File> receivedFiles = new Hashtable<String, File>(); // received files list and content types

    /**
     * Override executeAction to save the received files in a custom place
     * and delete this items from session.
     */
    @Override
    public String executeAction(HttpServletRequest request, List<FileItem> sessionFiles)
            throws UploadActionException {

        // TODO : check cookie
        // TODO : if non available, check session id

        String desc = request.getParameter("desc");
        String type = request.getParameter("type");
        String entryId = request.getParameter("eid");
        if (entryId == null || entryId.isEmpty())
            return "No entry id specified for file upload";

        for (FileItem item : sessionFiles) {
            if (item.isFormField())
                continue;

            String saveName = item.getName().replaceAll("[\\\\/><\\|\\s\"'{}()\\[\\]]+", "_");
            File file = new File("/tmp/" + saveName);
            try {
                item.write(file);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                continue;
            }

            if (ATTACHMENT_TYPE.equalsIgnoreCase(type)) {
                return uploadAttachment(file, entryId, desc, saveName);
            } else {
                //                throw new ServletException("Do not know what to do with type " + type);
            }
        }

        removeSessionFileItems(request);
        return "";

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
            Attachment saved = AttachmentManager.save(attachment, inputStream);
            if (saved != null)
                return saved.getFileId();
        } catch (ManagerException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
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

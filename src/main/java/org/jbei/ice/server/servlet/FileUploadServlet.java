package org.jbei.ice.server.servlet;

import gwtupload.server.UploadAction;
import gwtupload.server.exceptions.UploadActionException;
import gwtupload.shared.UConsts;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Hashtable;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;

public class FileUploadServlet extends UploadAction {

    private static final long serialVersionUID = 1L;

    Hashtable<String, String> receivedContentTypes = new Hashtable<String, String>();
    Hashtable<String, File> receivedFiles = new Hashtable<String, File>(); // received files list and content types

    /**
     * Override executeAction to save the received files in a custom place
     * and delete this items from session.
     */
    @Override
    public String executeAction(HttpServletRequest request, List<FileItem> sessionFiles)
            throws UploadActionException {
        String response = "";
        int cont = 0;
        for (FileItem item : sessionFiles) {
            if (false == item.isFormField()) {
                cont++;
                try {
                    /// Create a new file based on the remote file name in the client
                    // String saveName = item.getName().replaceAll("[\\\\/><\\|\\s\"'{}()\\[\\]]+", "_");
                    // File file =new File("/tmp/" + saveName);

                    /// Create a temporary file placed in /tmp (only works in unix)
                    // File file = File.createTempFile("upload-", ".bin", new File("/tmp"));

                    /// Create a temporary file placed in the default system temp folder
                    File file = File.createTempFile("upload-", ".bin");
                    item.write(file);

                    /// Save a list with the received files
                    receivedFiles.put(item.getFieldName(), file);
                    receivedContentTypes.put(item.getFieldName(), item.getContentType());

                    /// Send a customized message to the client.
                    response += "File saved as " + file.getAbsolutePath();

                } catch (Exception e) {
                    throw new UploadActionException(e);
                }
            }
        }

        /// Remove files from session because we have a copy of them
        removeSessionFileItems(request);

        /// Send your customized message to the client.
        return response;
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

// another way to do it
//    private static final long serialVersionUID = 1L;
//
//    @Override
//    public void doPost(HttpServletRequest request, HttpServletResponse response)
//            throws ServletException, IOException {
//        ServletFileUpload upload = new ServletFileUpload();
//
//        try{
//            FileItemIterator iter = upload.getItemIterator(request);
//
//            while (iter.hasNext()) {
//                FileItemStream item = iter.next();
//
//                String name = item.getFieldName();
//                InputStream stream = item.openStream();
//
//
//                // Process the input stream
//                ByteArrayOutputStream out = new ByteArrayOutputStream();
//                int len;
//                byte[] buffer = new byte[8192];
//                while ((len = stream.read(buffer, 0, buffer.length)) != -1) {
//                    out.write(buffer, 0, len);
//                }
//
//                int maxFileSize = 10*(1024*2); //10 megs max 
//                if (out.size() > maxFileSize) { 
//                    throw new RuntimeException("File is > than " + maxFileSize);
//                }
//            }
//        }
//        catch(Exception e){
//            throw new RuntimeException(e);
//        }

//    }

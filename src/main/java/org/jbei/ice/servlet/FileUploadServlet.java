package org.jbei.ice.servlet;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jbei.ice.ControllerException;
import org.jbei.ice.lib.account.model.Account;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.dao.DAOFactory;
import org.jbei.ice.lib.dto.ConfigurationKey;
import org.jbei.ice.lib.entry.EntryController;
import org.jbei.ice.lib.entry.model.Entry;
import org.jbei.ice.lib.entry.sequence.SequenceAnalysisController;
import org.jbei.ice.lib.utils.Utils;
import org.jbei.ice.lib.vo.DNASequence;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.util.Streams;
import org.apache.commons.io.IOUtils;

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
        String entryType = request.getParameter("entry_type");
        String entryAddType = request.getParameter("entry_add_type");

        String result = "";
        String tmpDir = Utils.getConfigValue(ConfigurationKey.TEMPORARY_DIRECTORY);

        // upload
        try {
//            FileItemFactory fileItemFactory = new DiskFileItemFactory();
            ServletFileUpload servletFileUpload = new ServletFileUpload();
            FileItemIterator fileItemIterator = servletFileUpload.getItemIterator(request);

            while (fileItemIterator.hasNext()) {
                FileItemStream fileItemStream = fileItemIterator.next();
                if (!fileItemStream.getFieldName().equals("file"))
                    continue;

                String filePath = fileItemStream.getName();
                if (filePath == null || filePath.isEmpty()) {
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
                if (type == null) {
                    sendServerResponse(response, "Upload type not known");
                    continue;
                }

                switch (type) {
                    case TRACE_SEQUENCE:
                        try {
                            result = uploadSequenceTraceFile(file, entryId, null, fileName);
                        } catch (IOException e) {
                            Logger.error(e);
                        }
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
        response.setContentType("text/html; charset=UTF-8");   // application/json
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        out.print(message);
        out.flush();
        out.close();
    }




    // TODO : this needs to go to manager/controller
    private String uploadSequenceTraceFile(File file, String entryId, Account account, String uploadFileName)
            throws IOException {
        EntryController controller = new EntryController();
        Entry entry = null;
        try {
            entry = DAOFactory.getEntryDAO().get(Long.decode(entryId));
        } catch (NumberFormatException e) {
            Logger.error(e);
        }

        if (entry == null)
            return "Unknown entry (" + entryId + "). Upload aborted";

        SequenceAnalysisController sequenceAnalysisController = new SequenceAnalysisController();
        DNASequence dnaSequence;

        ArrayList<ByteHolder> byteHolders = new ArrayList<>();
        FileInputStream inputStream = new FileInputStream(file);

        if (uploadFileName.toLowerCase().endsWith(".zip")) {
            try (ZipInputStream zis = new ZipInputStream(inputStream)) {
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



}

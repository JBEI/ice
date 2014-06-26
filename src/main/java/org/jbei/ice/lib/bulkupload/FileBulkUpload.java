package org.jbei.ice.lib.bulkupload;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;

import org.jbei.ice.lib.dto.bulkupload.EntryField;
import org.jbei.ice.lib.dto.entry.EntryType;

/**
 * Bulk upload via files
 *
 * @author Hector Plahar
 */
public class FileBulkUpload {

    private final Path filePath;
    private final String account;
    private final EntryType addType;

    public FileBulkUpload(String account, Path path, EntryType addType) {
        this.account = account;
        this.filePath = path;
        this.addType = addType;
    }

    public String process() throws IOException {
        String fileName = filePath.toFile().getName();

        // process csv
        if (fileName.endsWith(".csv")) {
            BulkCSVUpload upload = HelperFactory.createCSVUpload(account, addType, filePath);
            return upload.processUpload();
        }

        // process zip
        if (fileName.endsWith(".zip")) {
            BulkZipUpload upload = new BulkZipUpload(account, filePath, addType);
            return Long.toString(upload.processUpload());
        }

        // process sbol
        if (fileName.endsWith(".xml")) {
            BulkFileSBOLUpload upload = new BulkFileSBOLUpload(account, filePath, addType);
            return Long.toString(upload.processUpload());
        }

        throw new IOException("Unknown file type " + fileName);
    }

    /**
     * Creates a CSV template for download based on the the type of entries
     *
     * @param addType entry type that is to be uploaded
     * @return byte array of the template or null if the headers for the type cannot be retrieved/is unsupported
     */
    public static byte[] getCSVTemplateBytes(EntryType addType) {
        ArrayList<EntryField> headers = BulkCSVUploadHeaders.getHeadersForType(addType);
        if (headers == null)
            return null;

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < headers.size(); i++) {
            if (i != 0) {
                sb.append(",");
            }

            sb.append('"');
            sb.append(headers.get(i).getLabel());
            if (BulkCSVUploadHeaders.isRequired(headers.get(i), addType))
                sb.append("*");
            sb.append('"');
        }

        sb.append("\n");
        return sb.toString().getBytes();
    }
}

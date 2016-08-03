package org.jbei.ice.lib.bulkupload;

import org.jbei.ice.lib.dto.entry.EntryField;
import org.jbei.ice.lib.dto.entry.EntryType;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * Processes bulk uploads. Supported file formats are "csv", "zip" and "xml", with the latter being for SBOL
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

    /**
     * Process bulk file upload. Uses the file extension to determine the type of file
     * being uploaded.
     * <ul>
     * <li>Files with a <code>.csv</code> extension are processed as comma separated value files</li>
     * <li>Files with a <code>.zip</code> extension are processed as zip files. They are expected
     * to contain exactly 1 csv file and optional attachment/sequence files whose names are referenced
     * in the csv file</li>
     * <li>Files with a <code>.xml</code> extension are processed as SBOL files</li>
     * </ul>
     *
     * @return
     * @throws IOException
     */
    public ProcessedBulkUpload process() throws IOException {
        String fileName = filePath.toFile().getName();

        // process csv
        if (fileName.endsWith(".csv")) {
            BulkCSVUpload upload = new BulkCSVUpload(account, filePath, addType);
            return upload.processUpload();
        }

        // process zip
        if (fileName.endsWith(".zip")) {
            BulkZipUpload upload = new BulkZipUpload(account, filePath, addType);
            return upload.processUpload();
        }

        // process sbol
        if (fileName.endsWith(".xml")) {
            BulkFileSBOLUpload upload = new BulkFileSBOLUpload(account, filePath, addType);
            // todo
            ProcessedBulkUpload processedBulkUpload = new ProcessedBulkUpload();
            processedBulkUpload.setUploadId(upload.processUpload());
            return processedBulkUpload;
        }

        throw new IOException("Unsupported file type " + fileName);
    }

    /**
     * Creates a CSV template for download based on the the type of entries
     *
     * @param addType        entry type that is to be uploaded
     * @param linked         optional type that is linked to this entry. Should be one of {@link EntryType} or null
     * @param linkToExisting true, if <code>addType</code> is to be linked to an existing entry
     * @return byte array of the template or null if the headers for the type cannot be retrieved/is unsupported
     * @throws IllegalArgumentException if the addType is invalid
     */
    public static byte[] getCSVTemplateBytes(EntryType addType, EntryType linked, boolean linkToExisting) {
        List<EntryField> headers = BulkCSVUploadHeaders.getHeadersForType(addType);
        if (headers == null)
            throw new IllegalArgumentException("Could not retrieve headers for type " + addType);

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < headers.size(); i++) {
            if (i != 0) {
                sb.append(",");
            }

            sb.append('"');
            EntryField header = headers.get(i);
            sb.append(header.getLabel());
            if (header.isRequired())
                sb.append("*");
            sb.append('"');
        }

        // check linked
        if (linkToExisting) {
            sb.append(",");
            sb.append('"');
            sb.append("Existing Part Number");
            sb.append('"');
        } else {
            if (linked != null) {
                headers = BulkCSVUploadHeaders.getHeadersForType(linked);
                if (headers != null) {
                    for (EntryField header : headers) {
                        sb.append(",");
                        sb.append('"');
                        sb.append(linked.getDisplay()).append(" ").append(header.getLabel());
                        if (header.isRequired())
                            sb.append("*");
                        sb.append('"');
                    }
                }
            }
        }

        sb.append("\n");
        return sb.toString().getBytes();
    }
}

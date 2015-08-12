package org.jbei.ice.lib.bulkupload;

import org.jbei.ice.lib.dto.bulkupload.EntryField;
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

    public long process() throws IOException {
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
            return upload.processUpload();
        }

        throw new IOException("Unsupported file type " + fileName);
    }

//    protected final BulkCSVUpload getUploadType(String userId, Path csvFilePath, EntryType addType) {
//        switch (addType) {
//            default:
//                return new BulkCSVUpload(userId, csvFilePath, addType);
//
//            case ARABIDOPSIS:
//                return new ArabidopsisSeedUpload(userId, csvFilePath);
//        }
//    }

    /**
     * Creates a CSV template for download based on the the type of entries
     *
     * @param addType entry type that is to be uploaded
     * @param linked  type that is linked to this entry
     * @return byte array of the template or null if the headers for the type cannot be retrieved/is unsupported
     */
    public static byte[] getCSVTemplateBytes(EntryType addType, EntryType linked) {
        List<EntryField> headers = BulkCSVUploadHeaders.getHeadersForType(addType);
        if (headers == null)
            return null;

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
        if (linked != null) {
            headers = BulkCSVUploadHeaders.getHeadersForType(linked);
            if (headers != null) {
                for (int i = 0; i < headers.size(); i++) {
                    sb.append(",");
                    sb.append('"');
                    EntryField header = headers.get(i);
                    sb.append(linked.getDisplay()).append(" ").append(header.getLabel());
                    if (header.isRequired())
                        sb.append("*");
                    sb.append('"');
                }
            }
        }

        sb.append("\n");
        return sb.toString().getBytes();
    }
}

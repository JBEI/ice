package org.jbei.ice.lib.bulkupload;

import org.apache.commons.lang3.StringUtils;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.dto.entry.*;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Processes bulk uploads.
 * Supported file formats are <code>csv</code>, <code>zip</code> and <code>xml</code>, with the latter being for SBOL
 *
 * @author Hector Plahar
 */
public class FileBulkUpload implements Closeable {

    static final String ASTERISK_SYMBOL = "\u002A";

    private final String userId;
    private final EntryType addType;
    private final InputStream inputStream;
    private final FileUploadFormat format;
    private long bulkUploadId;

    public FileBulkUpload(String userId, InputStream inputStream, long bulkUploadId, EntryType entryType, FileUploadFormat format) {
        this.userId = userId;
        this.addType = entryType;
        this.inputStream = inputStream;
        this.format = format;
        this.bulkUploadId = bulkUploadId;
    }

    private static void appendHeader(StringBuilder sb, boolean isRequired, String label, String link) {
        sb.append('"');
        if (!StringUtils.isEmpty(link))
            sb.append(link).append(" ");

        sb.append(label);

        if (isRequired)
            sb.append(ASTERISK_SYMBOL);
        sb.append('"');
    }

    private static String buildHeaders(EntryType entryType, boolean isLink) {
        StringBuilder sb = new StringBuilder();
        String link = isLink ? entryType.getDisplay() : null;
        for (CustomEntryField field : getHeaders(entryType)) {
            sb.append(",");
            appendHeader(sb, field.isRequired(), field.getLabel(), link);
        }

        return sb.toString();
    }

    public static List<CustomEntryField> getHeaders(EntryType entryType) {
        List<EntryField> defaultHeaders = BulkCSVUploadHeaders.getHeadersForType(entryType);
        CustomFields customFields = new CustomFields();
        List<CustomEntryField> fields = customFields.get(entryType).getData();
        for (CustomEntryField customField : fields) {
            if (customField.getFieldType() == FieldType.EXISTING && customField.getExistingField() != null) {
                if (!defaultHeaders.remove(customField.getExistingField())) {
                    Logger.error("Existing custom field \"" + customField.getExistingField() + "\" not removed from header fields");
                }
            }
        }

        List<CustomEntryField> headers = new ArrayList<>();
        for (EntryField defaultHeader : defaultHeaders) {
            CustomEntryField entryField = new CustomEntryField();
            entryField.setRequired(defaultHeader.isRequired());
            entryField.setLabel(defaultHeader.getLabel());
            headers.add(entryField);
        }

        // add custom headers
        headers.addAll(fields);

        // file headers
        for (EntryField fileHeader : BulkCSVUploadHeaders.getFileHeaders()) {
            CustomEntryField entryField = new CustomEntryField();
            entryField.setRequired(fileHeader.isRequired());
            entryField.setLabel(fileHeader.getLabel());
            headers.add(entryField);
        }

        return headers;
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
        if (addType == null)
            throw new IllegalArgumentException("Could not retrieve headers for null entry type ");

        // main
        StringBuilder sb = new StringBuilder();
        sb.append(buildHeaders(addType, false).substring(1)); // remove initial comma

        // check linked
        if (linkToExisting) {
            sb.append(",");
            appendHeader(sb, false, "Existing Part Number", null);
        } else if (linked != null) {
            sb.append(buildHeaders(linked, true));
        }

        sb.append("\n");
        return sb.toString().getBytes(StandardCharsets.UTF_8);
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
     * @return wrapper around processed upload
     * @throws IOException on exception processing file
     */
    public ProcessedBulkUpload process() throws IOException {
        switch (format) {
            default:
            case CSV: {
                BulkCSVUpload upload = new BulkCSVUpload(userId, inputStream, bulkUploadId);
                return upload.processUpload();
            }

            case ZIP: {
                BulkZipUpload upload = new BulkZipUpload(userId, inputStream, bulkUploadId);
                return upload.processUpload();
            }

            case SBOL: {
                BulkFileSBOLUpload upload = new BulkFileSBOLUpload(userId, inputStream, addType);
                // todo
                ProcessedBulkUpload processedBulkUpload = new ProcessedBulkUpload();
                processedBulkUpload.setUploadId(upload.processUpload());
                return processedBulkUpload;
            }
        }
    }

    @Override
    public void close() throws IOException {
        this.inputStream.close();
    }
}

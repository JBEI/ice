package org.jbei.ice.bulkupload;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.jbei.ice.dto.ConfigurationKey;
import org.jbei.ice.dto.entry.*;
import org.jbei.ice.logging.Logger;
import org.jbei.ice.storage.DAOFactory;
import org.jbei.ice.storage.model.AccountModel;
import org.jbei.ice.storage.model.BulkUploadModel;
import org.jbei.ice.utils.Utils;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
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
    private final long bulkUploadId;
    private final String filename;

    public FileBulkUpload(String userId, InputStream inputStream, long bulkUploadId, EntryType entryType, FileUploadFormat format) {
        this.userId = userId;
        this.addType = entryType;
        this.inputStream = inputStream;
        this.format = format;
        this.bulkUploadId = bulkUploadId;
        this.filename = null;
    }

    public FileBulkUpload(String userId, InputStream stream, String filename, EntryType type) {
        this.userId = userId;
        this.addType = type;
        this.inputStream = stream;
        this.format = FileUploadFormat.CSV;
        this.bulkUploadId = 0;
        this.filename = filename;
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
        List<EntryFieldLabel> defaultHeaders = BulkCSVUploadHeaders.getHeadersForType(entryType);
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
        for (EntryFieldLabel defaultHeader : defaultHeaders) {
            CustomEntryField entryField = new CustomEntryField();
            entryField.setRequired(defaultHeader.isRequired());
            entryField.setLabel(defaultHeader.getDisplay());
            headers.add(entryField);
        }

        // add custom headers
        headers.addAll(fields);

        // file headers
        for (EntryFieldLabel fileHeader : BulkCSVUploadHeaders.getFileHeaders()) {
            CustomEntryField entryField = new CustomEntryField();
            entryField.setRequired(fileHeader.isRequired());
            entryField.setLabel(fileHeader.getDisplay());
            headers.add(entryField);
        }

        return headers;
    }

    /**
     * Creates a CSV template for download based on the type of entries
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
        }
    }

    public BulkUpload uploadFile() throws IOException {
        // file
        BulkUploadModel model = new BulkUploadModel();
        AccountModel account = DAOFactory.getAccountDAO().getByEmail(this.userId);
        if (account == null)
            throw new IllegalArgumentException("Invalid user id: " + this.userId);

        model.setAccount(account);
        model.setCreationTime(new Date());
        model.setLastUpdateTime(new Date());
        model.setImportType(this.addType.getName());
        model.setFileIdentifier(Utils.generateUUID());

        String dataDir = Utils.getConfigValue(ConfigurationKey.DATA_DIRECTORY);

        // todo : use a reference to file storage (include filename here)
        try {
            if (inputStream != null) {
                Path path = Paths.get(dataDir, "bulk-uploads", model.getFileIdentifier());
                Files.write(path, IOUtils.toByteArray(inputStream));
            }
        } catch (IOException e) {
            Logger.error(e);
            return null;
        }

        model = DAOFactory.getBulkUploadDAO().create(model);
        return model.toDataTransferObject();
    }

    @Override
    public void close() throws IOException {
        this.inputStream.close();
    }
}

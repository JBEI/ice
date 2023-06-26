package org.jbei.ice.bulkupload;

import com.opencsv.CSVReader;
import org.apache.commons.lang3.StringUtils;
import org.jbei.ice.dto.ConfigurationKey;
import org.jbei.ice.dto.entry.CustomEntryField;
import org.jbei.ice.dto.entry.EntryFieldLabel;
import org.jbei.ice.dto.entry.EntryType;
import org.jbei.ice.logging.Logger;
import org.jbei.ice.storage.DAOFactory;
import org.jbei.ice.storage.hibernate.dao.BulkUploadDAO;
import org.jbei.ice.storage.model.BulkUploadModel;
import org.jbei.ice.utils.Utils;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Validation class for Bulk Upload Entries
 *
 * @author Hector Plahar
 */
public class BulkUploadValidation {

    private final Set<EntryFieldLabel> failedFields;
    private final BulkUploadModel model;
    private final BulkUploadDAO dao;
    private final String userId;

    private final Map<Integer, CustomEntryField> fieldColCache;    // mapping of col to specific field

    private final List<BitSet> errorSet = new ArrayList<>();

    public BulkUploadValidation(String userId, long uploadId) {
        this.dao = DAOFactory.getBulkUploadDAO();
        this.model = this.dao.get(uploadId);

        if (this.model == null)
            throw new IllegalArgumentException("Invalid bulk upload id: " + uploadId);

        this.failedFields = new HashSet<>();
        this.userId = userId;
        this.fieldColCache = new HashMap<>();
    }

    public boolean perform() {
        EntryType type = EntryType.nameToType(model.getImportType());

        // get list of fields for type
//        EntryFields entryFields = new EntryFields(this.userId);
//        List<EntryField> fields = entryFields.get(type);

        // retrieve headers (including file headers)
        List<CustomEntryField> fields = FileBulkUpload.getHeaders(type);
        // parts csv
        String dataDir = Utils.getConfigValue(ConfigurationKey.DATA_DIRECTORY);
        Path path = Paths.get(dataDir, "bulk-uploads", model.getFileIdentifier());

        // todo : use a reference to file storage (include filename here) WAT!!??
        try (CSVReader reader = new CSVReader(new FileReader(path.toFile()))) {

            // read header
            Iterator<String[]> iterator = reader.iterator();
            String[] headers = null;
            if (iterator.hasNext()) {
                headers = iterator.next();
                if (!validateHeaders(fields, iterator.next())) // todo track failure of header validation
                    return false;
            }

            if (!iterator.hasNext())     // empty csv list. no entries to create
                return false;

            // read rest of document
            while (iterator.hasNext()) {

                // process current row
                String[] currentRow = iterator.next();
                for (int i = 0; i < currentRow.length; i += 1) {
                    CustomEntryField field = getFieldForColumn(i, fields, headers[i]);
                    if (field != null) {
                        // validate field
                        String value = currentRow[i];
                        if (StringUtils.isEmpty(value) && field.isRequired()) {
                            // todo: col i doesnt have a value and is required
                            return false;
                        }

                    } else {
                        // todo : error: do something
                    }
                }
            }

        } catch (IOException e) {
            Logger.error(e);
        }

        return true;
    }

    private CustomEntryField getFieldForColumn(int i, List<CustomEntryField> fields, String header) {
        if (fieldColCache.get(i) != null)
            return fieldColCache.get(i);

        for (CustomEntryField field : fields) {
            if (field.getLabel().toLowerCase().equals(header)) {
                fieldColCache.put(i, field);
                return field;
            }
        }

        return null;    // todo : validation issue here
    }

    /**
     * Validates the headers in the csv. Checks and records missing headers and headers in csv that are not part of
     * entry type
     *
     * @return false if validation fails for any reason
     */
    private boolean validateHeaders(List<CustomEntryField> fields, String[] headers) {

        HashSet<String> headerSet = new HashSet<>();
        // check that all headers are in the fields
        for (int i = 0; i < headers.length; i += 1) {
            String header = headers[i];

            if (header.endsWith("*"))
                headerSet.add(header.substring(0, header.length() - 1).toLowerCase());
            else
                headerSet.add(header.toLowerCase());
        }

        for (CustomEntryField field : fields) {
            headerSet.remove(field.getLabel().toLowerCase());
        }

        return headerSet.isEmpty();
    }

    /**
     * @return the set of fields that have failed validation if called after isValid()
     * otherwise returns an empty list
     */
    public Set<EntryFieldLabel> getFailedFields() {
        return new HashSet<>(this.failedFields);
    }

}

package org.jbei.ice.lib.bulkupload;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.jbei.ice.controllers.ControllerFactory;
import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.lib.shared.EntryAddType;
import org.jbei.ice.lib.shared.dto.bulkupload.BulkUploadAutoUpdate;
import org.jbei.ice.lib.shared.dto.bulkupload.EntryField;

import au.com.bytecode.opencsv.CSVParser;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

/**
 * CSV uploader for parts
 *
 * @author Hector Plahar
 */
public class PartBulkCSVUpload extends BulkCSVUpload {

    public PartBulkCSVUpload(EntryAddType addType, String account, Path csvFilePath) {
        super(addType, account, csvFilePath);
    }

    protected boolean isValidHeader(EntryField field) {
        return (field != null && headerFields.contains(field));
    }

    @Override
    protected void populateHeaderFields() {
        headerFields.clear();
        headerFields.add(EntryField.PI);
        headerFields.add(EntryField.FUNDING_SOURCE);
        headerFields.add(EntryField.IP);
        headerFields.add(EntryField.BIOSAFETY_LEVEL);
        headerFields.add(EntryField.NAME);
        headerFields.add(EntryField.ALIAS);
        headerFields.add(EntryField.KEYWORDS);
        headerFields.add(EntryField.SUMMARY);
        headerFields.add(EntryField.NOTES);
        headerFields.add(EntryField.REFERENCES);
        headerFields.add(EntryField.LINKS);
        headerFields.add(EntryField.STATUS);
        headerFields.add(EntryField.ATT_FILENAME);
        headerFields.add(EntryField.SEQ_FILENAME);
        headerFields.add(EntryField.SEQ_TRACE_FILES);
    }

    @Override
    protected void populateRequiredFields() {
        requiredFields.clear();
        requiredFields.add(EntryField.PI);
        requiredFields.add(EntryField.NAME);
        requiredFields.add(EntryField.SUMMARY);
    }

    @Override
    public String processUpload() {
        // maintains list of fields in the order they are contained in the file
        List<BulkUploadAutoUpdate> updates;
        try {
            updates = getBulkUploadUpdates(new FileInputStream(csvFilePath.toFile()));
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }

        // validate to ensure all required fields are present
        String errorString = validate(updates);
        if (!StringUtils.isBlank(errorString))
            return errorString;

        // create actual parts in the registry
        try {
            return Long.toString(createRegistryParts(updates));
        } catch (ControllerException ce) {
            Logger.error(ce);
            return "Error: " + ce.getMessage();
        }
    }

    @Override
    List<BulkUploadAutoUpdate> getBulkUploadUpdates(InputStream inputStream) throws Exception {
        CSVParser parser = null;
        List<BulkUploadAutoUpdate> updates = new LinkedList<>();
        List<EntryField> fields = new LinkedList<>();

        // read file
        try {

            int row = 0;
            List<String> lines = IOUtils.readLines(inputStream);
            for (String line : lines) {
                line = line.trim();
                if (line.isEmpty() || line.replaceAll(",", "").trim().isEmpty())
                    continue;

                if (parser == null) {
                    // header is a good indicator of what the separator char is (tab vs comma)
                    if (line.contains("\t") && !line.contains(","))
                        parser = new CSVParser('\t');
                    else
                        parser = new CSVParser();

                    String[] fieldStrArray = parser.parseLine(line);
                    for (int i = 0; i < fieldStrArray.length; i += 1) {
                        String fieldStr = fieldStrArray[i];

                        // account for "*" that indicates a header is required
                        if (fieldStr.lastIndexOf("*") != -1)
                            fieldStr = fieldStr.substring(0, fieldStr.length() - 1);

                        EntryField field = EntryField.fromString(fieldStr);
                        if (!isValidHeader(field)) {
                            throw new Exception("The selected upload type doesn't support the following field ["
                                                        + fieldStr + "]");
                        }

                        fields.add(i, field);
                    }
                } else {
                    // process values
                    BulkUploadAutoUpdate autoUpdate = new BulkUploadAutoUpdate(EntryAddType.addTypeToType(addType));
                    autoUpdate.setRow(row);
                    row += 1;
                    String[] valuesArray = parser.parseLine(line);
                    for (int i = 0; i < valuesArray.length; i += 1) {
                        EntryField field = fields.get(i);
                        autoUpdate.getKeyValue().put(field, valuesArray[i]);
                    }
                    updates.add(autoUpdate);
                }
            }
        } catch (IOException io) {
            Logger.error(io);
        }

        if (updates.isEmpty())
            throw new Exception("CSV file could not be read");

        return updates;
    }

    protected long createRegistryParts(List<BulkUploadAutoUpdate> updates) throws ControllerException {
        BulkUploadController controller = ControllerFactory.getBulkUploadController();
        long bulkUploadId = 0;

        for (BulkUploadAutoUpdate update : updates) {
            if (update.getBulkUploadId() <= 0)
                update.setBulkUploadId(bulkUploadId);

            Logger.info(account + ": " + update.toString());
            update = controller.autoUpdateBulkUpload(account, update, addType);
            if (bulkUploadId == 0)
                bulkUploadId = update.getBulkUploadId();
        }
        return bulkUploadId;
    }

    protected String validate(List<BulkUploadAutoUpdate> updates) {
        for (BulkUploadAutoUpdate update : updates) {
            ArrayList<EntryField> toValidate = new ArrayList<EntryField>(requiredFields);

            for (Map.Entry<EntryField, String> entry : update.getKeyValue().entrySet()) {
                EntryField entryField = entry.getKey();
                String value = entry.getValue();

                if (!requiredFields.contains(entryField))
                    continue;

                toValidate.remove(entryField);

                if (StringUtils.isBlank(value)) {
                    return "Error: \"" + entryField.toString() + "\" is a required field.";
                }
            }

            if (!toValidate.isEmpty()) {
                StringBuilder builder = new StringBuilder();
                builder.append("Error: File is missing the following required fields [");
                int i = 0;
                for (EntryField field : toValidate) {
                    if (i > 0)
                        builder.append(",");
                    builder.append(field.toString());
                    i += 1;
                }
                builder.append("]");
                return builder.toString();
            }
        }
        return null;
    }
}

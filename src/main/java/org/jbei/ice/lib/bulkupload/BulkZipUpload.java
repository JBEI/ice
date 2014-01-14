package org.jbei.ice.lib.bulkupload;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.jbei.ice.ControllerException;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.dto.bulkupload.EntryField;
import org.jbei.ice.lib.shared.EntryAddType;

import org.apache.commons.lang.StringUtils;
import org.apache.cxf.helpers.IOUtils;

/**
 * Bulk Upload with zip files. It is expected that the zip contains a csv
 * of the upload with the attachment and sequences files containing
 * the names of the files (which are to be enclosed in the zip)
 *
 * @author Hector Plahar
 */
public class BulkZipUpload {

    private BulkCSVUpload csvUpload;
    private final Path zipFilePath;
    private final EntryAddType addType;
    private final String userId;

    public BulkZipUpload(String userId, Path path, EntryAddType addType) {
        this.userId = userId;
        this.zipFilePath = path;
        this.addType = addType;
        csvUpload = HelperFactory.createCSVUpload(userId, addType, path);
    }

    /**
     * Process the zip file. It expects that there is exactly one file with the .csv extension.
     * This means that a .csv cannot be used as an attachment
     *
     * @throws IOException on error processing the file
     */
    public long processUpload() throws IOException {
        ZipFile zipFile = new ZipFile(zipFilePath.toFile());
        Enumeration<? extends ZipEntry> enumeration = zipFile.entries();

        String csvFile = null;
        HashMap<String, InputStream> files = new HashMap<>();

        // go through zip elements
        while (enumeration.hasMoreElements()) {
            ZipEntry zipEntry = enumeration.nextElement();
            // does not go into directories for now
            if (zipEntry.isDirectory())
                continue;

            String name = zipEntry.getName();
            if (name.contains("/"))
                name = name.substring(name.lastIndexOf("/") + 1);

            // get main csv
            if (name.endsWith(".csv")) {
                if (csvFile != null)
                    throw new IOException("Duplicate csv file in zip archive");

                csvFile = IOUtils.toString(zipFile.getInputStream(zipEntry));
            } else {
                InputStream inputStream = zipFile.getInputStream(zipEntry);
                files.put(name, inputStream);
            }
        }

        if (csvFile == null)
            throw new IOException("Could not find a csv file in the zip archive");

        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(csvFile.getBytes())) {
            List<BulkUploadAutoUpdate> updates = csvUpload.getBulkUploadUpdates(inputStream);
            verify(updates, files.keySet());
            // create actual registry parts
            return createRegistryParts(updates, files);
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    protected long createRegistryParts(List<BulkUploadAutoUpdate> updates, HashMap<String, InputStream> files)
            throws ControllerException {
        BulkUploadController controller = new BulkUploadController();
        long bulkUploadId = 0;

        for (BulkUploadAutoUpdate update : updates) {
            // set to correct id after first iteration
            if (update.getBulkUploadId() <= 0)
                update.setBulkUploadId(bulkUploadId);

            Logger.info(userId + ": " + update.toString());
            update = controller.autoUpdateBulkUpload(userId, update, addType);
            if (bulkUploadId == 0)
                bulkUploadId = update.getBulkUploadId();

            long entryId = update.getEntryId();

            // create sequence or attachment if any
            for (Map.Entry<EntryField, String> entrySet : update.getKeyValue().entrySet()) {
                EntryField field = entrySet.getKey();

                // create attachment based on name
                if (field == EntryField.ATT_FILENAME || field == EntryField.STRAIN_ATT_FILENAME
                        || field == EntryField.PLASMID_ATT_FILENAME) {
                    String value = entrySet.getValue().trim();
                    if (value != null && !value.isEmpty()) {
                        // create attachment
                        try (InputStream stream = files.get(value)) {
                            boolean isStrainWithPlasmidPlasmid = (addType == EntryAddType.STRAIN_WITH_PLASMID
                                    && field == EntryField.PLASMID_SEQ_FILENAME);
                            PartFileAdd.uploadAttachmentToEntry(entryId, userId, stream, value,
                                                                isStrainWithPlasmidPlasmid);
                        } catch (Exception e) {
                            Logger.error(e);
                        }
                    }
                } else {
                    // create sequence based on name
                    if (field == EntryField.SEQ_FILENAME || field == EntryField.STRAIN_SEQ_FILENAME
                            || field == EntryField.PLASMID_SEQ_FILENAME) {
                        String value = entrySet.getValue().trim();
                        if (value != null && !value.isEmpty()) {
                            // create sequence
                            try (InputStream stream = files.get(value)) {
                                boolean isStrainWithPlasmidPlasmid = (addType == EntryAddType.STRAIN_WITH_PLASMID
                                        && field == EntryField.PLASMID_SEQ_FILENAME);
                                PartFileAdd.uploadSequenceToEntry(entryId, userId, stream, isStrainWithPlasmidPlasmid);
                            } catch (Exception e) {
                                Logger.error(e);
                            }
                        }
                    }
                }
            }
        }
        return bulkUploadId;
    }

    protected void verify(List<BulkUploadAutoUpdate> updates, Set<String> fileNames) throws Exception {
        List<EntryField> required = csvUpload.getRequiredFields();

        // for each auto update
        for (BulkUploadAutoUpdate update : updates) {
            ArrayList<EntryField> toValidate = new ArrayList<EntryField>(csvUpload.getRequiredFields());

            // for each field in the update
            for (Map.Entry<EntryField, String> entry : update.getKeyValue().entrySet()) {
                EntryField entryField = entry.getKey();
                String value = entry.getValue().trim();

                // check attachment and sequence files
                if (entryField == EntryField.ATT_FILENAME || entryField == EntryField.SEQ_FILENAME) {
                    if (!value.isEmpty() && !fileNames.contains(value))
                        throw new Exception("File with name \"" + value + "\" not found in the zip archive");
                }

                // skip non-required
                if (!required.contains(entryField))
                    continue;

                // remove encountered required fields in temp list of all required fields
                toValidate.remove(entryField);

                if (StringUtils.isBlank(value)) {
                    throw new Exception("Required field " + entryField.toString() + "\" is missing");
                }
            }

            // check if all required fields are encountered
            if (!toValidate.isEmpty()) {
                StringBuilder builder = new StringBuilder();
                builder.append("The following required fields are missing [");
                int i = 0;
                for (EntryField field : toValidate) {
                    if (i > 0)
                        builder.append(",");
                    builder.append(field.toString());
                    i += 1;
                }
                builder.append("]");
                throw new Exception(builder.toString());
            }
        }
    }
}

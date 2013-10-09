package org.jbei.ice.server.servlet.helper;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;

import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.lib.shared.EntryAddType;
import org.jbei.ice.lib.shared.dto.bulkupload.BulkUploadAutoUpdate;
import org.jbei.ice.lib.shared.dto.bulkupload.EntryField;
import org.jbei.ice.lib.shared.dto.entry.EntryType;

import au.com.bytecode.opencsv.CSVReader;

/**
 * Helper class for dealing with bulk CSV uploads
 *
 * @author Hector Plahar
 */
public class BulkCSVUpload {

    private final EntryAddType uploadType;
    private final Path csvFilePath;

    public BulkCSVUpload(EntryAddType addType, Path csvFilePath) {
        this.uploadType = addType;
        this.csvFilePath = csvFilePath;
    }

    public String processUpload() {
        List<EntryField> fields = new LinkedList<>();
        List<BulkUploadAutoUpdate> updates = new LinkedList<>();
        try (CSVReader csvReader = new CSVReader(new FileReader(csvFilePath.toString()))) {
            String[] lines;
            while ((lines = csvReader.readNext()) != null) {
                if (fields.isEmpty()) {
                    for (int i = 0; i < lines.length; i += 1) {
                        String line = lines[i];
                        EntryField field = EntryField.fromString(line);
                        if (field == null)
                            return "Error: Unrecognized field " + line;

                        fields.add(i, field);
                    }
                } else {
                    // process values
                    for (int i = 0; i < lines.length; i += 1) {
                        EntryField field = fields.get(i);
                        EntryType type = toEntryType(uploadType, field);
                        BulkUploadAutoUpdate autoUpdate = new BulkUploadAutoUpdate(type);
                        autoUpdate.getKeyValue().put(field, lines[i]);
                        updates.add(autoUpdate);
                    }
                }
            }
        } catch (IOException io) {
            Logger.error(io);
        }
        return "";
    }

    // TODO : combine with StrainWithPlasmidHeaders::isPlasmidHeader()
    private EntryType toEntryType(EntryAddType type, EntryField field) {
        EntryType entryType;
        if (type == EntryAddType.STRAIN_WITH_PLASMID) {
            boolean isPlasmid = isPlasmidHeader(field);

            if (isPlasmid) {
                // if updating plasmid portion of strain with one plasmid
                entryType = EntryType.PLASMID;
            } else {
                entryType = EntryType.STRAIN;
            }
        } else {
            entryType = EntryAddType.addTypeToType(type);
        }
        return entryType;
    }

    protected boolean isPlasmidHeader(EntryField entryField) {
        return (entryField == EntryField.PLASMID_NAME
                || entryField == EntryField.PLASMID_ALIAS
                || entryField == EntryField.PLASMID_LINKS
                || entryField == EntryField.PLASMID_SELECTION_MARKERS
                || entryField == EntryField.CIRCULAR
                || entryField == EntryField.PLASMID_BACKBONE
                || entryField == EntryField.PLASMID_PROMOTERS
                || entryField == EntryField.PLASMID_REPLICATES_IN
                || entryField == EntryField.PLASMID_ORIGIN_OF_REPLICATION
                || entryField == EntryField.PLASMID_KEYWORDS
                || entryField == EntryField.PLASMID_SUMMARY
                || entryField == EntryField.PLASMID_NOTES
                || entryField == EntryField.PLASMID_REFERENCES
                || entryField == EntryField.PLASMID_SEQ_FILENAME
                || entryField == EntryField.PLASMID_ATT_FILENAME);
    }
}

package org.jbei.ice.lib.bulkupload;

import java.nio.file.Path;

import org.jbei.ice.lib.dto.bulkupload.EntryField;
import org.jbei.ice.lib.dto.entry.EntryType;

/**
 * Bulk CSV Uploader for plasmids
 *
 * @author Hector Plahar
 */
public class PlasmidBulkCSVUpload extends PartBulkCSVUpload {

    public PlasmidBulkCSVUpload(EntryType addType, String account, Path csvFilePath) {
        super(addType, account, csvFilePath);
    }

    protected void populateHeaderFields() {
        super.populateHeaderFields();
        headerFields.add(EntryField.CIRCULAR);
        headerFields.add(EntryField.BACKBONE);
        headerFields.add(EntryField.PROMOTERS);
        headerFields.add(EntryField.REPLICATES_IN);
        headerFields.add(EntryField.ORIGIN_OF_REPLICATION);
        headerFields.add(EntryField.SELECTION_MARKERS);
    }

    protected void populateRequiredFields() {
        super.populateRequiredFields();
        requiredFields.add(EntryField.SELECTION_MARKERS);
    }
}

package org.jbei.ice.lib.bulkupload;

import java.nio.file.Path;

import org.jbei.ice.lib.shared.EntryAddType;
import org.jbei.ice.lib.shared.dto.bulkupload.EntryField;

/**
 * @author Hector Plahar
 */
public class SeedBulkCSVUpload extends PartBulkCSVUpload {

    public SeedBulkCSVUpload(EntryAddType addType, String account, Path csvFilePath) {
        super(addType, account, csvFilePath);
    }

    @Override
    protected void populateHeaderFields() {
        super.populateHeaderFields();
        headerFields.add(EntryField.HOMOZYGOSITY);
        headerFields.add(EntryField.HARVEST_DATE);
        headerFields.add(EntryField.ECOTYPE);
        headerFields.add(EntryField.PARENTS);
        headerFields.add(EntryField.GENERATION);
        headerFields.add(EntryField.PLANT_TYPE);
        headerFields.add(EntryField.GENERATION);
        headerFields.add(EntryField.SENT_TO_ABRC);
    }

    @Override
    protected void populateRequiredFields() {
        super.populateRequiredFields();
        requiredFields.add(EntryField.SELECTION_MARKERS);
        requiredFields.add(EntryField.PLANT_TYPE);
        requiredFields.add(EntryField.GENERATION);
    }
}

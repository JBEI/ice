package org.jbei.ice.lib.bulkupload;

import java.nio.file.Path;

import org.jbei.ice.lib.dto.bulkupload.EntryField;
import org.jbei.ice.lib.shared.EntryAddType;

/**
 * Supports Arabidopsis seed bulk upload including samples for the
 * default Arabidopsis storage scheme [Shelf, Box, Tube, Barcode]
 *
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
        headerFields.add(EntryField.SELECTION_MARKERS);
        headerFields.add(EntryField.SENT_TO_ABRC);

        // default sample
        headerFields.add(EntryField.SAMPLE_NAME);
        headerFields.add(EntryField.SAMPLE_NOTES);
        headerFields.add(EntryField.SAMPLE_SHELF);
        headerFields.add(EntryField.SAMPLE_BOX);
        headerFields.add(EntryField.SAMPLE_TUBE_NUMBER);
        headerFields.add(EntryField.SAMPLE_TUBE_BARCODE);
    }

    @Override
    protected void populateRequiredFields() {
        super.populateRequiredFields();
        requiredFields.add(EntryField.PLANT_TYPE);
        requiredFields.add(EntryField.GENERATION);
    }
}

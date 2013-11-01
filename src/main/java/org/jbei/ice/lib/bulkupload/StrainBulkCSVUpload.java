package org.jbei.ice.lib.bulkupload;

import java.nio.file.Path;

import org.jbei.ice.lib.shared.EntryAddType;
import org.jbei.ice.lib.shared.dto.bulkupload.EntryField;

/**
 * Bulk CSV uploader for strains
 *
 * @author Hector Plahar
 */
public class StrainBulkCSVUpload extends PartBulkCSVUpload {

    public StrainBulkCSVUpload(EntryAddType addType, String account, Path csvFilePath) {
        super(addType, account, csvFilePath);
    }

    @Override
    protected void populateHeaderFields() {
        super.populateHeaderFields();
        headerFields.add(EntryField.PARENTAL_STRAIN);
        headerFields.add(EntryField.GENOTYPE_OR_PHENOTYPE);
        headerFields.add(EntryField.PLASMIDS);
        headerFields.add(EntryField.SELECTION_MARKERS);
    }

    @Override
    protected void populateRequiredFields() {
        super.populateRequiredFields();
        requiredFields.add(EntryField.SELECTION_MARKERS);
    }
}

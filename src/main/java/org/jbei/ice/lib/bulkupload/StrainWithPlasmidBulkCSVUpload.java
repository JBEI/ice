package org.jbei.ice.lib.bulkupload;

import java.nio.file.Path;

import org.jbei.ice.lib.dto.bulkupload.EntryField;
import org.jbei.ice.lib.shared.EntryAddType;

/**
 * Bulk CSV Upload helper class which supports uploading strain with plasmids
 *
 * @author Hector Plahar
 */
public class StrainWithPlasmidBulkCSVUpload extends PartBulkCSVUpload {

    public StrainWithPlasmidBulkCSVUpload(EntryAddType addType, String account, Path csvFilePath) {
        super(addType, account, csvFilePath);
    }

    @Override
    protected void populateHeaderFields() {
        headerFields.clear();

        headerFields.add(EntryField.PI);
        headerFields.add(EntryField.FUNDING_SOURCE);
        headerFields.add(EntryField.IP);
        headerFields.add(EntryField.BIOSAFETY_LEVEL);
        headerFields.add(EntryField.STATUS);

        //strain information
        headerFields.add(EntryField.STRAIN_NAME);
        headerFields.add(EntryField.STRAIN_ALIAS);
        headerFields.add(EntryField.STRAIN_LINKS);
        headerFields.add(EntryField.STRAIN_SELECTION_MARKERS);
        headerFields.add(EntryField.STRAIN_PARENTAL_STRAIN);
        headerFields.add(EntryField.STRAIN_GEN_PHEN);
        headerFields.add(EntryField.STRAIN_KEYWORDS);
        headerFields.add(EntryField.STRAIN_SUMMARY);
        headerFields.add(EntryField.STRAIN_NOTES);
        headerFields.add(EntryField.STRAIN_REFERENCES);
        headerFields.add(EntryField.STRAIN_ATT_FILENAME);
        headerFields.add(EntryField.STRAIN_SEQ_FILENAME);

        // plasmid information
        headerFields.add(EntryField.PLASMID_NAME);
        headerFields.add(EntryField.PLASMID_ALIAS);
        headerFields.add(EntryField.PLASMID_LINKS);
        headerFields.add(EntryField.PLASMID_SELECTION_MARKERS);
        headerFields.add(EntryField.CIRCULAR);
        headerFields.add(EntryField.PLASMID_BACKBONE);
        headerFields.add(EntryField.PLASMID_PROMOTERS);
        headerFields.add(EntryField.REPLICATES_IN);
        headerFields.add(EntryField.PLASMID_ORIGIN_OF_REPLICATION);
        headerFields.add(EntryField.PLASMID_KEYWORDS);
        headerFields.add(EntryField.PLASMID_SUMMARY);
        headerFields.add(EntryField.PLASMID_NOTES);
        headerFields.add(EntryField.PLASMID_REFERENCES);
        headerFields.add(EntryField.PLASMID_ATT_FILENAME);
        headerFields.add(EntryField.PLASMID_SEQ_FILENAME);

        headerFields.add(EntryField.PARENTAL_STRAIN);
        headerFields.add(EntryField.GENOTYPE_OR_PHENOTYPE);
        headerFields.add(EntryField.PLASMIDS);
        headerFields.add(EntryField.SELECTION_MARKERS);
    }

    @Override
    protected void populateRequiredFields() {
        requiredFields.clear();
        requiredFields.add(EntryField.PI);
        requiredFields.add(EntryField.STRAIN_NAME);
        requiredFields.add(EntryField.STRAIN_SELECTION_MARKERS);
        requiredFields.add(EntryField.STRAIN_SUMMARY);
        requiredFields.add(EntryField.PLASMID_NAME);
        requiredFields.add(EntryField.PLASMID_SELECTION_MARKERS);
        requiredFields.add(EntryField.PLASMID_SUMMARY);
    }
}

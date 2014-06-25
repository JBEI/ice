package org.jbei.ice.lib.dto.bulkupload;

import org.jbei.ice.lib.dao.IDataTransferModel;

/**
 * Represents the complete list of fields for parts.
 * These are mostly used in bulk uploads as headers
 *
 * @author Hector Plahar
 */
public enum EntryField implements IDataTransferModel {

    PI("Principal Investigator"),
    PI_EMAIL("Principal Investigator Email"),
    FUNDING_SOURCE("Funding Source"),
    IP("Intellectual Property"),
    BIOSAFETY_LEVEL("BioSafety Level"),
    NAME("Name"),
    ALIAS("Alias"),
    KEYWORDS("Keywords"),
    SUMMARY("Summary"),
    NOTES("Notes"),
    REFERENCES("References"),
    LINKS("Links"),
    STATUS("Status"),
    CREATOR("Creator"),
    CREATOR_EMAIL("Creator Email"),
    SEQ_FILENAME("Sequence File"),
    ATT_FILENAME("Attachment File"),
    SEQ_TRACE_FILES("Sequence Trace File(s)"),
    SELECTION_MARKERS("Selection Markers"),
    PARENTAL_STRAIN("Parent Strain"),
    GENOTYPE_OR_PHENOTYPE("Genotype or Phenotype"),
    PLASMIDS("Plasmids"),
    CIRCULAR("Circular"),
    BACKBONE("Backbone"),
    PROMOTERS("Promoters"),
    REPLICATES_IN("Replicates In"),
    ORIGIN_OF_REPLICATION("Origin of Replication"),
    HOMOZYGOSITY("Homozygosity"),
    ECOTYPE("Ecotype"),
    HARVEST_DATE("Harvest Date"),
    GENERATION("Generation"),
    SENT_TO_ABRC("Sent to ABRC?"),
    PLANT_TYPE("Plant Type"),
    PARENTS("Parents");

    private String label;

    EntryField(String label) {
        this.label = label;
    }

    public static EntryField fromString(String label) {
        for (EntryField field : EntryField.values()) {
            if (field.label.equalsIgnoreCase(label))
                return field;
        }
        return null;
    }

    @Override
    public String toString() {
        return this.name();
    }

    public String getLabel() {
        return this.label;
    }
}
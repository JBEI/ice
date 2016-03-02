package org.jbei.ice.lib.dto.entry;

import org.jbei.ice.storage.IDataTransferModel;

/**
 * Represents the complete list of fields for parts.
 * These are mostly used in bulk uploads as headers
 *
 * @author Hector Plahar
 */
public enum EntryField implements IDataTransferModel {

    PI("Principal Investigator", true),
    PI_EMAIL("Principal Investigator Email", false),
    PART_NUMBER("Part Number", false),
    FUNDING_SOURCE("Funding Source", false),
    IP("Intellectual Property", false),
    BIO_SAFETY_LEVEL("BioSafety Level", true),
    NAME("Name", true),
    ALIAS("Alias", false),
    KEYWORDS("Keywords", false),
    SUMMARY("Summary", true),
    NOTES("Notes", false),
    REFERENCES("References", false),
    LINKS("Links", false),
    STATUS("Status", true),
    CREATOR("Creator", true),
    CREATOR_EMAIL("Creator Email", true),
    SEQ_FILENAME("Sequence File", false),
    ATT_FILENAME("Attachment File", false),
    SEQ_TRACE_FILES("Sequence Trace File(s)", false),
    SELECTION_MARKERS("Selection Markers", true),
    PARENTAL_STRAIN("Parent Strain", false),
    GENOTYPE_OR_PHENOTYPE("Genotype or Phenotype", false),
    CIRCULAR("Circular", false),
    BACKBONE("Backbone", false),
    PROMOTERS("Promoters", false),
    REPLICATES_IN("Replicates In", false),
    ORIGIN_OF_REPLICATION("Origin of Replication", false),
    HOMOZYGOSITY("Homozygosity", false),
    ECOTYPE("Ecotype", false),
    HARVEST_DATE("Harvest Date", false),
    GENERATION("Generation", false),
    SENT_TO_ABRC("Sent to ABRC?", false),
    PLANT_TYPE("Plant Type", false),
    PARENTS("Parents", false),
    EXISTING_PART_NUMBER("Existing Part Number", false);

    private String label;
    private boolean required;

    EntryField(String label, boolean required) {
        this.label = label;
        this.required = required;
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

    public boolean isRequired() {
        return this.required;
    }
}
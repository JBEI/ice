package org.jbei.ice.lib.dto.bulkupload;

import org.jbei.ice.lib.dao.IDataTransferModel;

/**
 * Represents the complete list of fields for parts.
 * These are mostly used in bulk uploads as headers
 *
 * @author Hector Plahar
 */
public enum EntryField implements IDataTransferModel {

    PI("Principal Investigator", true),
    FUNDING_SOURCE("Funding Source", true),
    IP("Intellectual Property", false),
    BIOSAFETY_LEVEL("BioSafety Level", true),
    NAME("Name", false),
    ALIAS("Alias", false),
    KEYWORDS("Keywords", true),
    SUMMARY("Summary", true),
    NOTES("Notes", true),
    REFERENCES("References", true),
    LINKS("Links", false),
    STATUS("Status", true),
    SEQ_FILENAME("Sequence File", false),
    ATT_FILENAME("Attachment File", false),
    SELECTION_MARKERS("Selection Markers", true),
    PARENTAL_STRAIN("Parent Strain", false),
    GENOTYPE_OR_PHENOTYPE("Genotype or Phenotype", false),
    PLASMIDS("Plasmids", false),
    CIRCULAR("Circular", true),
    BACKBONE("Backbone", false),
    PROMOTERS("Promoters", false),
    REPLICATES_IN("Replicates In", false),
    ORIGIN_OF_REPLICATION("Origin of Replication", true),
    HOMOZYGOSITY("Homozygosity", false),
    ECOTYPE("Ecotype", false),
    HARVEST_DATE("Harvest Date", false),
    GENERATION("Generation", false),
    SENT_TO_ABRC("Sent to ABRC?", false),
    PLANT_TYPE("Plant Type", false),
    PARENTS("Parents", false),

    // sample headers
    SAMPLE_NAME("Sample Name", false),
    SAMPLE_NOTES("Sample Notes", false),
    SAMPLE_SHELF("Sample Shelf", false),
    SAMPLE_BOX("Sample Box", false),
    SAMPLE_TUBE("Sample Tube", false),
    SAMPLE_PLATE("Sample Plate", false),
    SAMPLE_WELL("Sample Well", false),
    SAMPLE_TYPE("Sample Type", false),
    SAMPLE_DRAWER("Sample Drawer", false),
    SAMPLE_STOCK("Sample Stock", false),
    SAMPLE_JBEI_STRAIN("Sample JBEI Strain", false),
    SAMPLE_TUBE_NUMBER("Sample Tube Number", false),
    SAMPLE_TUBE_BARCODE("Sample Tube Barcode", false);

    private String label;
    private boolean canLock;

    EntryField(String label, boolean canLock) {
        this.label = label;
        this.canLock = canLock;
    }

    private EntryField() {
    }

    public boolean isCanLock() {
        return canLock;
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
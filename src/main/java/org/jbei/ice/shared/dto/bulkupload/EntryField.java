package org.jbei.ice.shared.dto.bulkupload;

import org.jbei.ice.shared.dto.IDTOModel;

public enum EntryField implements IDTOModel {

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
    ORIGIN_OF_REPLICATION("Origin of Replication", true),
    HOMOZYGOSITY("Homozygosity", false),
    ECOTYPE("Ecotype", false),
    HARVEST_DATE("Harvest Date", false),
    GENERATION("Generation", false),
    SENT_TO_ABRC("Sent to ABRC?", false),
    PLANT_TYPE("Plant Type", false),
    PARENTS("Parents", false),
    PLASMID_NAME("Plasmid Name", false),
    PLASMID_ALIAS("Plasmid Alias", false),
    PLASMID_KEYWORDS("Plasmid Keywords", true),
    PLASMID_SUMMARY("Plasmid Summary", true),
    PLASMID_NOTES("Plasmid Notes", true),
    PLASMID_REFERENCES("Plasmid References", true),
    PLASMID_LINKS("Plasmid Links", false),
    PLASMID_STATUS("Plasmid Status", true),
    PLASMID_BACKBONE("Plasmid Backbone", false),
    PLASMID_PROMOTERS("Plasmid Promoters", false),
    PLASMID_ORIGIN_OF_REPLICATION("Plasmid Origin of Replication", true),
    PLASMID_SEQ_FILENAME("Plasmid Sequence File", false),
    PLASMID_ATT_FILENAME("Plasmid Attachment File", false),
    PLASMID_SELECTION_MARKERS("Plasmid Selection Markers", true),
    STRAIN_NAME("Strain Number", false),
    STRAIN_ALIAS("Strain Alias", false),
    STRAIN_KEYWORDS("Strain Keywords", true),
    STRAIN_SUMMARY("Strain Summary", true),
    STRAIN_NOTES("Strain Notes", true),
    STRAIN_REFERENCES("Strain References", true),
    STRAIN_LINKS("Strain Links", false),
    STRAIN_STATUS("Status", true),
    STRAIN_SELECTION_MARKERS("Strain Selection Markers", true),
    STRAIN_PARENTAL_STRAIN("Parent Strain", false),
    STRAIN_GEN_PHEN("Genotype or Phenotype", false),
    STRAIN_SEQ_FILENAME("Strain Sequence File", false),
    STRAIN_ATT_FILENAME("Strain Attachment File", false),

    // sample headers
    SAMPLE_NAME("Sample Name", false),
    SAMPLE_SHELF("Sample Shelf", false),
    SAMPLE_BOX("Sample Box", false),
    SAMPLE_TUBE("Sample Tube", false),
    SAMPLE_PLATE("Sample Plate", false),
    SAMPLE_WELL("Sample Well", false),
    SAMPLE_TYPE("Sample Type", false),
    SAMPLE_DRAWER("Sample Drawer", false),
    SAMPLE_STOCK("Sample Stock", false),
    SAMPLE_JBEI_STRAIN("Sample JBEI Strain", false),
    SAMPLE_TUBE_NUMBER("Tube Number", false),
    SAMPLE_TUBE_BARCODE("Tube Barcode", false);

    private String label;
    private boolean canLock;

    EntryField(String label, boolean canLock) {
        this.label = label;
        this.canLock = canLock;
    }

    private EntryField() {}

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
        return this.label;
    }
}
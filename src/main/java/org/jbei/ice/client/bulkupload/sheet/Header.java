package org.jbei.ice.client.bulkupload.sheet;

public enum Header {
    PI("Principal Investigator"),
    FUNDING_SOURCE("Funding Source"),
    IP("Intellectual Property"),
    BIOSAFETY("BioSafety Level"),
    NAME("Name"),
    ALIAS("Alias"),
    KEYWORDS("Keywords"),
    SUMMARY("Summary"),
    NOTES("Notes"),
    REFERENCES("References"),
    LINKS("Links"),
    STATUS("Status"),
    SEQ_FILENAME("Sequence File"),
    ATT_FILENAME("Attachment File"),
    SELECTION_MARKERS("Selection Markers"),
    PARENTAL_STRAIN("Parental Strain"),
    GEN_PHEN("Genotype or Phenotype"),
    PLASMIDS("Plasmids"),
    CIRCULAR("Circular"),
    BACKBONE("Backbone"),
    PROMOTERS("Promoters"),
    ORIGIN_OF_REPLICATION("Origin of Replication"),
    HOMOZYGOSITY("Homozygosity"),
    ECOTYPE("Ecotype"),
    HARVEST_DATE("Harvest Date"),
    GENERATION("Generation"),
    PLANT_TYPE("Plant Type"),
    PARENTS("Parents"),
    PLASMID_NAME("Plasmid Name"),
    PLASMID_ALIAS("Plasmid Alias"),
    PLASMID_KEYWORDS("Plasmid Keywords"),
    PLASMID_SUMMARY("Plasmid Summary"),
    PLASMID_NOTES("Plasmid Notes"),
    PLASMID_REFERENCES("Plasmid References"),
    PLASMID_LINKS("Plasmid Links"),
    PLASMID_STATUS("Plasmid Status"),
    PLASMID_BACKBONE("Plasmid Backbone"),
    PLASMID_PROMOTERS("Plasmid Promoters"),
    PLASMID_ORIGIN_OF_REPLICATION("Plasmid Origin of Replication"),
    PLASMID_SEQ_FILENAME("Plasmid Sequence File"),
    PLASMID_ATT_FILENAME("Plasmid Attachment File"),
    PLASMID_SELECTION_MARKERS("Plasmid Selection Markers"),
    STRAIN_NAME("Strain Number"),
    STRAIN_ALIAS("Strain Alias"),
    STRAIN_KEYWORDS("Strain Keywords"),
    STRAIN_SUMMARY("Strain Summary"),
    STRAIN_NOTES("Strain Notes"),
    STRAIN_REFERENCES("Strain References"),
    STRAIN_LINKS("Strain Links"),
    STRAIN_STATUS("Status"),
    STRAIN_SELECTION_MARKERS("Strain Selection Markers"),
    STRAIN_PARENTAL_STRAIN("Parental Strain"),
    STRAIN_GEN_PHEN("Genotype or Phenotype"),
    STRAIN_PLASMIDS("Strain Plasmids"),
    STRAIN_SEQ_FILENAME("Strain Sequence File"),
    STRAIN_ATT_FILENAME("Strain Attachment File");

    private String label;

    Header(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return this.label;
    }
}
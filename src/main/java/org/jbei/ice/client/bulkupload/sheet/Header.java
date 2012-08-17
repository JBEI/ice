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
    PLASMID_KEYWORDS("Keywords"),
    PLASMID_SUMMARY("Summary"),
    PLASMID_NOTES("Notes"),
    PLASMID_REFERENCES("References"),
    PLASMID_LINKS("Links"),
    PLASMID_STATUS("Status"),
    PLASMID_BACKBONE("Backbone"),
    PLASMID_PROMOTERS("Promoters"),
    PLASMID_ORIGIN_OF_REPLICATION("Origin of Replication"),
    PLASMID_SEQ_FILENAME("Sequence File"),
    PLASMID_ATT_FILENAME("Attachment File"),
    PLASMID_SELECTION_MARKERS("Selection Markers"),
    STRAIN_NAME("Name"),
    STRAIN_ALIAS("Alias"),
    STRAIN_KEYWORDS("Keywords"),
    STRAIN_SUMMARY("Summary"),
    STRAIN_NOTES("Notes"),
    STRAIN_REFERENCES("References"),
    STRAIN_LINKS("Links"),
    STRAIN_STATUS("Status"),
    STRAIN_SELECTION_MARKERS("Selection Markers"),
    STRAIN_PARENTAL_STRAIN("Parental Strain"),
    STRAIN_GEN_PHEN("Genotype or Phenotype"),
    STRAIN_PLASMIDS("Plasmids"),
    STRAIN_SEQ_FILENAME("Sequence File"),
    STRAIN_ATT_FILENAME("Attachment File");

    private String label;

    Header(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return this.label;
    }
}
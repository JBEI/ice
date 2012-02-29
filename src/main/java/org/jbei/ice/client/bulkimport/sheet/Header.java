package org.jbei.ice.client.bulkimport.sheet;

public enum Header {
    PI("Principal Investigator", true, null), FUNDING_SOURCE("Funding Source", false, null), IP(
            "Intellectual Property", false, null), BIOSAFETY("BioSafety Level", true, null), NAME(
            "Name", true, null), ALIAS("Alias", false, null), KEYWORDS("Keywords", false, null), SUMMARY(
            "Summary", true, null), NOTES("Notes", false, null), REFERENCES("References", false,
            null), LINKS("Links", false, null), STATUS("Status", true, null), SEQ_FILENAME(
            "Sequence Filename", false, null), ATT_FILENAME("Attachments Filename", false, null), SELECTION_MARKERS(
            "Selection Markers", false, FieldType.AUTO_COMPLETE), PARENTAL_STRAIN(
            "Parental Strain", false, null), GEN_PHEN("Genotype or Phenotype", false, null), PLASMIDS(
            "Plasmids", false, null), CIRCULAR("Circular", false, null), BACKBONE("Backbone",
            false, null), PROMOTERS("Promoters", false, null), ORIGIN_OF_REPLICATION(
            "Origin of Replication", false, null), HOMOZYGOSITY("Homozygosity", false, null), ECOTYPE(
            "Ecotype", false, null), HARVEST_DATE("Harvest Data", false, null), GENERATION(
            "Generation", true, null), PLANT_TYPE("Plant Type", true, null), PARENTS("Parents",
            false, null);

    private String label;
    private boolean required;
    private FieldType type;

    Header(String label, boolean required, FieldType type) {
        this.label = label;
        this.required = required;
        this.type = type;
    }

    public boolean isRequired() {
        return this.required;
    }

    public FieldType geFieldType() {
        return this.type;
    }

    @Override
    public String toString() {
        return this.label;
    }
}
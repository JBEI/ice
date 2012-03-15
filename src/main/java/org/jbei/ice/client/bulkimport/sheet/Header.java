package org.jbei.ice.client.bulkimport.sheet;

public enum Header {
    PI("Principal Investigator", true, null), FUNDING_SOURCE("Funding Source", false, null), IP(
            "Intellectual Property", false, null), BIOSAFETY("BioSafety Level", true, null), NAME(
            "Name", true, null), ALIAS("Alias", false, null), KEYWORDS("Keywords", false, null), SUMMARY(
            "Summary", true, null), NOTES("Notes", false, null), REFERENCES("References", false,
            null), LINKS("Links", false, null), STATUS("Status", true, null), SEQ_FILENAME(
            "Sequence Filename", false, FieldType.FILE_INPUT), ATT_FILENAME("Attachments Filename",
            false, FieldType.FILE_INPUT), SELECTION_MARKERS("Selection Markers", false,
            FieldType.AUTO_COMPLETE), PARENTAL_STRAIN("Parental Strain", false, null), GEN_PHEN(
            "Genotype or Phenotype", false, null), PLASMIDS("Plasmids", false, null), CIRCULAR(
            "Circular", false, null), BACKBONE("Backbone", false, null), PROMOTERS("Promoters",
            false, null), ORIGIN_OF_REPLICATION("Origin of Replication", false, null), HOMOZYGOSITY(
            "Homozygosity", false, null), ECOTYPE("Ecotype", false, null), HARVEST_DATE(
            "Harvest Data", false, FieldType.DATE), GENERATION("Generation", true, null), PLANT_TYPE(
            "Plant Type", true, null), PARENTS("Parents", false, null), PLASMID_NAME(
            "Plasmid Name", true, null), PLASMID_ALIAS("Plasmid Alias", false, null), PLASMID_KEYWORDS(
            "Keywords", false, null), PLASMID_SUMMARY("Summary", true, null), PLASMID_NOTES(
            "Notes", false, null), PLASMID_REFERENCES("References", false, null), PLASMID_LINKS(
            "Links", false, null), PLASMID_STATUS("Status", true, null), PLASMID_BACKBONE(
            "Backbone", false, null), PLASMID_PROMOTERS("Promoters", false, null), PLASMID_ORIGIN_OF_REPLICATION(
            "Origin of Replication", false, null), PLASMID_SEQ_FILENAME("Sequence Filename", false,
            FieldType.FILE_INPUT), PLASMID_ATT_FILENAME("Attachments Filename", false,
            FieldType.FILE_INPUT), PLASMID_SELECTION_MARKERS("Selection Markers", false,
            FieldType.AUTO_COMPLETE), STRAIN_NAME("Name", true, null), STRAIN_ALIAS("Alias", false,
            null), STRAIN_KEYWORDS("Keywords", false, null), STRAIN_SUMMARY("Summary", true, null), STRAIN_NOTES(
            "Notes", false, null), STRAIN_REFERENCES("References", false, null), STRAIN_LINKS(
            "Links", false, null), STRAIN_STATUS("Status", true, null), STRAIN_SELECTION_MARKERS(
            "Selection Markers", false, FieldType.AUTO_COMPLETE), STRAIN_PARENTAL_STRAIN(
            "Parental Strain", false, null), STRAIN_GEN_PHEN("Genotype or Phenotype", false, null), STRAIN_PLASMIDS(
            "Plasmids", false, null), STRAIN_SEQ_FILENAME("Sequence Filename", false,
            FieldType.FILE_INPUT), STRAIN_ATT_FILENAME("Attachments Filename", false,
            FieldType.FILE_INPUT);

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
package org.jbei.ice.client.bulkimport.sheet;

public enum Header {
    PI("Principal Investigator", true, ""), FUNDING_SOURCE("Funding Source", false, ""), IP(
            "Intellectual Property", false, ""), BIOSAFETY("BioSafety Level", true, ""), NAME(
            "Name", true, ""), ALIAS("Alias", false, ""), KEYWORDS("Keywords", false, ""), SUMMARY(
            "Summary", true, ""), NOTES("Notes", false, ""), REFERENCES("References", false, ""), LINKS(
            "Links", false, ""), STATUS("Status", true, ""), SEQ_FILENAME("Sequence Filename",
            false, ""), ATT_FILENAME("Attachments Filename", false, ""), SELECTION_MARKERS(
            "Selection Markers", false, ""), PARENTAL_STRAIN("Parental Strain", false, ""), GEN_PHEN(
            "Genotype or Phenotype", false, ""), PLASMIDS("Plasmids", false, ""), CIRCULAR(
            "Circular", false, ""), BACKBONE("Backbone", false, ""), PROMOTERS("Promoters", false,
            ""), ORIGIN_OF_REPLICATION("Origin of Replication", false, ""), HOMOZYGOSITY(
            "Homozygosity", false, ""), ECOTYPE("Ecotype", false, ""), HARVEST_DATE("Harvest Data",
            false, ""), GENERATION("Generation", true, ""), PLANT_TYPE("Plant Type", true, ""), PARENTS(
            "Parents", false, ""), PLASMID_NAME("Plasmid Name", true, ""), PLASMID_ALIAS(
            "Plasmid Alias", false, ""), PLASMID_KEYWORDS("Keywords", false, ""), PLASMID_SUMMARY(
            "Summary", true, ""), PLASMID_NOTES("Notes", false, ""), PLASMID_REFERENCES(
            "References", false, ""), PLASMID_LINKS("Links", false, ""), PLASMID_STATUS("Status",
            true, ""), PLASMID_BACKBONE("Backbone", false, ""), PLASMID_PROMOTERS("Promoters",
            false, ""), PLASMID_ORIGIN_OF_REPLICATION("Origin of Replication", false, ""), PLASMID_SEQ_FILENAME(
            "Sequence Filename", false, ""), PLASMID_ATT_FILENAME("Attachments Filename", false, ""), PLASMID_SELECTION_MARKERS(
            "Selection Markers", false, ""), STRAIN_NAME("Name", true, ""), STRAIN_ALIAS("Alias",
            false, ""), STRAIN_KEYWORDS("Keywords", false, ""), STRAIN_SUMMARY("Summary", true, ""), STRAIN_NOTES(
            "Notes", false, ""), STRAIN_REFERENCES("References", false, ""), STRAIN_LINKS("Links",
            false, ""), STRAIN_STATUS("Status", true, ""), STRAIN_SELECTION_MARKERS(
            "Selection Markers", false, ""), STRAIN_PARENTAL_STRAIN("Parental Strain", false, ""), STRAIN_GEN_PHEN(
            "Genotype or Phenotype", false, ""), STRAIN_PLASMIDS("Plasmids", false, ""), STRAIN_SEQ_FILENAME(
            "Sequence Filename", false, ""), STRAIN_ATT_FILENAME("Attachments Filename", false, "");

    private String label;
    private boolean required;
    private String description;

    Header(String label, boolean required, String description) {
        this.label = label;
        this.required = required;
        this.description = description;
    }

    public boolean isRequired() {
        return this.required;
    }

    @Override
    public String toString() {
        return this.label;
    }

    public String getDescription() {
        return this.description;
    }

    /**
     * @return true for headers that have fields that
     *         are autocomplete. false otherwise
     */
    public boolean hasAutoComplete() {
        switch (this) {
        case BIOSAFETY:
        case SELECTION_MARKERS:
        case STRAIN_SELECTION_MARKERS:
        case PLASMID_SELECTION_MARKERS:
            return true;

        default:
            return false;
        }

    }
}
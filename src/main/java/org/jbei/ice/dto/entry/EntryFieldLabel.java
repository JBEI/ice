package org.jbei.ice.dto.entry;

import org.jbei.ice.storage.IDataTransferModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the complete (default) list of fields for parts.
 * // todo store input type along with labels
 *
 * @author Hector Plahar
 */
public enum EntryFieldLabel implements IDataTransferModel {

    PI("Principal Investigator", true, null),
    PI_EMAIL("Principal Investigator Email", false, null),
    PART_NUMBER("Part Number", false, null),
    FUNDING_SOURCE("Funding Source", false, null),
    IP("Intellectual Property", false, null),
    BIO_SAFETY_LEVEL("BioSafety Level", true, null),
    NAME("Name", true, null),
    ALIAS("Alias", false, null),
    KEYWORDS("Keywords", false, null),
    BACKBONE("Backbone", false, null),
    SUMMARY("Summary", true, null),
    NOTES("Notes", false, null),
    REFERENCES("References", false, null),
    EXTERNAL_URL("External URL", false, null),
    LINKS("Links", false, null),
    STATUS("Status", true, null),
    CREATOR("Creator", true, null),
    CREATOR_EMAIL("Creator Email", true, null),
    SEQ_FILENAME("Sequence File", false, null),
    ATT_FILENAME("Attachment File", false, null),
    SEQ_TRACE_FILES("Sequence Trace File(s)", false, null),
    SELECTION_MARKERS("Selection Markers", true, null),
    HOST("Host", false, null),
    GENOTYPE_OR_PHENOTYPE("Genotype or Phenotype", false, null),
    CIRCULAR("Circular", false, FieldInputType.BOOLEAN),
    PROMOTERS("Promoters", false, null),
    REPLICATES_IN("Replicates In", false, null),
    ORIGIN_OF_REPLICATION("Origin of Replication", false, null),
    HOMOZYGOSITY("Homozygosity", false, null),
    ECOTYPE("Ecotype", false, null),
    HARVEST_DATE("Harvest Date", false, null),
    GENERATION("Generation", false, null),
    SENT_TO_ABRC("Sent to ABRC?", false, FieldInputType.BOOLEAN),
    PLANT_TYPE("Plant Type", false, null),
    PARENTS("Parents", false, null),
    EXISTING_PART_NUMBER("Existing Part Number", false, null),
    ORGANISM("Organism", false, null),
    FULL_NAME("Full Name", false, null),
    GENE_NAME("Gene Name", false, null),
    UPLOADED_FROM("Uploaded From", false, null);

    private final String label;
    private final boolean required;
    private final FieldInputType fieldType;

    EntryFieldLabel(String label, boolean required, FieldInputType type) {
        this.label = label;
        this.required = required;
        this.fieldType = type;
    }

    /**
     * @return list of entry field labels for a "generic" part
     * This is a subset of the field labels for other entry types
     */
    public static List<EntryFieldLabel> getPartLabels() {
        List<EntryFieldLabel> labels = new ArrayList<>();
        labels.add(NAME);
        labels.add(ALIAS);
        labels.add(PI);
        labels.add(FUNDING_SOURCE);
        labels.add(STATUS);
        labels.add(BIO_SAFETY_LEVEL);
        labels.add(CREATOR);
        labels.add(KEYWORDS);
        labels.add(EXTERNAL_URL);
        labels.add(SUMMARY);
        labels.add(REFERENCES);
        labels.add(IP);

        return labels;
    }

    /**
     * @return List of entry field labels
     */
    public static List<EntryFieldLabel> getPlasmidLabels() {
        List<EntryFieldLabel> labels = new ArrayList<>(getPartLabels());
        labels.add(BACKBONE);
        labels.add(CIRCULAR);
        labels.add(ORIGIN_OF_REPLICATION);
        labels.add(SELECTION_MARKERS);
        labels.add(PROMOTERS);
        labels.add(REPLICATES_IN);

        return labels;
    }

    public static List<EntryFieldLabel> getStrainLabels() {
        List<EntryFieldLabel> labels = new ArrayList<>(getPartLabels());
        labels.add(SELECTION_MARKERS);
        labels.add(GENOTYPE_OR_PHENOTYPE);
        labels.add(HOST);

        return labels;
    }

    public static List<EntryFieldLabel> getSeedLabels() {
        List<EntryFieldLabel> labels = new ArrayList<>(getPartLabels());
        labels.add(SENT_TO_ABRC);
        labels.add(PLANT_TYPE);
        labels.add(GENERATION);
        labels.add(HARVEST_DATE);
        labels.add(HOMOZYGOSITY);
        labels.add(ECOTYPE);
        labels.add(SELECTION_MARKERS);

        return labels;
    }

    public static List<EntryFieldLabel> getProteinFields() {
        List<EntryFieldLabel> labels = new ArrayList<>(getPartLabels());
        labels.add(ORGANISM);
        labels.add(FULL_NAME);
        labels.add(GENE_NAME);
        labels.add(UPLOADED_FROM);
        return labels;
    }

    public static EntryFieldLabel fromString(String label) {
        for (EntryFieldLabel field : EntryFieldLabel.values()) {
            if (field.label.equalsIgnoreCase(label))
                return field;
        }
        return null;
    }

    public static List<CustomField> getDefaultOptions(EntryFieldLabel label) {
        List<CustomField> options = new ArrayList<>();
        switch (label) {
            case STATUS:
                options.add(new CustomField("Complete"));
                options.add(new CustomField("In Progress"));
                options.add(new CustomField("Abandoned"));
                options.add(new CustomField("Planned"));
                break;

            case PLANT_TYPE:
                options.add(new CustomField("EMS", "EMS"));
                options.add(new CustomField("OVER_EXPRESSION", "OVER_EXPRESSION"));
                options.add(new CustomField("RNAI", "RNAi"));
                options.add(new CustomField("REPORTER", "Reporter"));
                options.add(new CustomField("T_DNA", "T-DNA"));
                options.add(new CustomField("OTHER", "Other"));
                break;

            case GENERATION:
                options.add(new CustomField("UNKNOWN"));
                options.add(new CustomField("F1"));
                options.add(new CustomField("F2"));
                options.add(new CustomField("F3"));
                options.add(new CustomField("M0"));
                options.add(new CustomField("M1"));
                options.add(new CustomField("M2"));
                options.add(new CustomField("T0"));
                options.add(new CustomField("T1"));
                options.add(new CustomField("T2"));
                options.add(new CustomField("T3"));
                options.add(new CustomField("T4"));
                options.add(new CustomField("T5"));
                break;

            case BIO_SAFETY_LEVEL:
                options.add(new CustomField("1", "Level 1"));
                options.add(new CustomField("2", "Level 2"));
                options.add(new CustomField("-1", "Restricted"));
                break;
        }

        return options;
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

    public FieldInputType getFieldType() {
        return fieldType;
    }
}

package org.jbei.ice.dto.entry;

import org.jbei.ice.storage.IDataTransferModel;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Label for an entry field. Represents the default "built-in fields".
 * Has three components defined as follows:
 * \t display: the text to show when label is to be displayed
 * \t required: whether this label is required or not (todo : should probably be in the context specific entry type)
 * \t type: input type for field. e.g. whether it is a text field
 *
 * @author Hector Plahar
 */
public enum EntryFieldLabel implements IDataTransferModel {

    PI("Principal Investigator", true, FieldInputType.USER_WITH_EMAIL, "principalInvestigator", ""),
    PI_EMAIL("Principal Investigator Email", false, null, "", ""),
    PART_NUMBER("Part Number", false, null, "partId", ""),
    FUNDING_SOURCE("Funding Source", false, FieldInputType.LONG_TEXT, "", ""),
    IP("Intellectual Property", false, FieldInputType.TEXTAREA, "", ""),
    BIO_SAFETY_LEVEL("BioSafety Level", true, FieldInputType.SELECT, "", ""),
    NAME("Name", true, FieldInputType.TEXT, "name", ""),
    ALIAS("Alias", false, FieldInputType.TEXT, "alias", ""),
    KEYWORDS("Keywords", false, FieldInputType.LONG_TEXT, "keywords", ""),
    BACKBONE("Backbone", false, FieldInputType.TEXT, "", ""),
    SUMMARY("Summary", true, FieldInputType.TEXTAREA, "shortDescription", ""),
    NOTES("Notes", false, FieldInputType.TEXTAREA, "longDescription", ""),
    REFERENCES("References", false, FieldInputType.LONG_TEXT, "", ""),
    EXTERNAL_URL("External URL", false, FieldInputType.MULTI_TEXT, "", ""),
    LINKS("Links", false, FieldInputType.TEXT, "", ""),
    STATUS("Status", true, FieldInputType.SELECT, "status", ""),
    CREATOR("Creator", true, FieldInputType.USER_WITH_EMAIL, "creator", ""),
    CREATOR_EMAIL("Creator Email", true, null, "", ""),
    SEQ_FILENAME("Sequence File", false, null, "", ""),
    ATT_FILENAME("Attachment File", false, null, "", ""),
    SEQ_TRACE_FILES("Sequence Trace File(s)", false, null, "", ""),
    SELECTION_MARKERS("Selection Markers", true, FieldInputType.MULTI_TEXT, "", ""),
    HOST("Host", false, FieldInputType.TEXT, "", ""),
    GENOTYPE_OR_PHENOTYPE("Genotype or Phenotype", false, FieldInputType.TEXTAREA, "", ""),
    CIRCULAR("Circular", false, FieldInputType.BOOLEAN, "", ""),
    PROMOTERS("Promoters", false, FieldInputType.TEXT, "promoters", "plasmidData"),
    REPLICATES_IN("Replicates In", false, FieldInputType.TEXT, "", ""),
    ORIGIN_OF_REPLICATION("Origin of Replication", false, FieldInputType.TEXT, "", ""),
    HOMOZYGOSITY("Homozygosity", false, FieldInputType.LONG_TEXT, "", ""),
    ECOTYPE("Ecotype", false, FieldInputType.LONG_TEXT, "", ""),
    HARVEST_DATE("Harvest Date", false, FieldInputType.DATE, "", ""),
    GENERATION("Generation", false, FieldInputType.SELECT, "", ""),
    SENT_TO_ABRC("Sent to ABRC?", false, FieldInputType.BOOLEAN, "", ""),
    PLANT_TYPE("Plant Type", false, FieldInputType.SELECT, "", ""),
    PARENTS("Parents", false, null, "", ""),
    EXISTING_PART_NUMBER("Existing Part Number", false, null, "", ""),
    ORGANISM("Organism", false, FieldInputType.LONG_TEXT, "", ""),
    FULL_NAME("Full Name", false, FieldInputType.LONG_TEXT, "", ""),
    GENE_NAME("Gene Name", false, FieldInputType.LONG_TEXT, "", ""),
    UPLOADED_FROM("Uploaded From", false, FieldInputType.LONG_TEXT, "", "");

    private final String display;
    private final boolean required;
    private final FieldInputType fieldType;
    private final String field;
    private final String subField;

    EntryFieldLabel(String display, boolean required, FieldInputType type, String field, String subField) {
        this.display = display;
        this.required = required;
        this.fieldType = type;
        this.field = field;
        this.subField = subField;
    }

    /**
     * @return list of entry field labels for a "generic" part
     * This is a subset of the field labels for other entry types
     */
    public static List<EntryFieldLabel> getPartLabels() {
        List<EntryFieldLabel> labels = new LinkedList<>();
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
        labels.add(NOTES);
        labels.add(LINKS);

        return labels;
    }

    /**
     * @return List of labels specifically for entries of type <code>PLASMID</code>
     */
    public static List<EntryFieldLabel> getPlasmidLabels() {
        List<EntryFieldLabel> labels = new LinkedList<>(getPartLabels());
        labels.add(BACKBONE);
        labels.add(CIRCULAR);
        labels.add(ORIGIN_OF_REPLICATION);
        labels.add(SELECTION_MARKERS);
        labels.add(PROMOTERS);
        labels.add(REPLICATES_IN);

        return labels;
    }

    /**
     * @return List of labels specifically for entries of type <code>STRAIN</code>
     */
    public static List<EntryFieldLabel> getStrainLabels() {
        List<EntryFieldLabel> labels = new LinkedList<>(getPartLabels());
        labels.add(SELECTION_MARKERS);
        labels.add(GENOTYPE_OR_PHENOTYPE);
        labels.add(HOST);

        return labels;
    }

    /**
     * @return List of labels specifically for entries of type <code>SEED</code>
     */
    public static List<EntryFieldLabel> getSeedLabels() {
        List<EntryFieldLabel> labels = new LinkedList<>(getPartLabels());
        labels.add(SENT_TO_ABRC);
        labels.add(PLANT_TYPE);
        labels.add(GENERATION);
        labels.add(HARVEST_DATE);
        labels.add(HOMOZYGOSITY);
        labels.add(ECOTYPE);
        labels.add(SELECTION_MARKERS);

        return labels;
    }

    /**
     * @return List of labels specifically for entries of type <code>PROTEIN</code>
     */
    public static List<EntryFieldLabel> getProteinFields() {
        List<EntryFieldLabel> labels = new LinkedList<>(getPartLabels());
        labels.add(ORGANISM);
        labels.add(FULL_NAME);
        labels.add(GENE_NAME);
        labels.add(UPLOADED_FROM);
        return labels;
    }

    public static EntryFieldLabel fromString(String label) {
        for (EntryFieldLabel field : EntryFieldLabel.values()) {
            if (field.display.equalsIgnoreCase(label))
                return field;
        }
        return null;
    }

    public static List<CustomField> getDefaultOptions(EntryFieldLabel label) {
        List<CustomField> options = new ArrayList<>();
        switch (label) {
            case STATUS -> {
                options.add(new CustomField("Complete"));
                options.add(new CustomField("In Progress"));
                options.add(new CustomField("Abandoned"));
                options.add(new CustomField("Planned"));
            }
            case PLANT_TYPE -> {
                options.add(new CustomField("EMS", "EMS"));
                options.add(new CustomField("OVER_EXPRESSION", "OVER_EXPRESSION"));
                options.add(new CustomField("RNAI", "RNAi"));
                options.add(new CustomField("REPORTER", "Reporter"));
                options.add(new CustomField("T_DNA", "T-DNA"));
                options.add(new CustomField("OTHER", "Other"));
            }
            case GENERATION -> {
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
            }
            case BIO_SAFETY_LEVEL -> {
                options.add(new CustomField("1", "Level 1"));
                options.add(new CustomField("2", "Level 2"));
                options.add(new CustomField("-1", "Restricted"));
            }
        }

        return options;
    }

    @Override
    public String toString() {
        return this.name();
    }

    public String getDisplay() {
        return this.display;
    }

    public boolean isRequired() {
        return this.required;
    }

    public FieldInputType getFieldType() {
        return fieldType;
    }

    public String getField() {
        return field;
    }

    public String getSubField() {
        return subField;
    }
}

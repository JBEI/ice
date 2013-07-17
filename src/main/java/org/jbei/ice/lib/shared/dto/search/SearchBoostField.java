package org.jbei.ice.lib.shared.dto.search;

import java.util.HashSet;

import org.jbei.ice.lib.shared.dto.IDTOModel;

/**
 * Search fields that have user boost support (shown in the user preference section)
 *
 * @author Hector Plahar
 */
public enum SearchBoostField implements IDTOModel {

    OWNER("owner"),
    OWNER_EMAIL("ownerEmail"),
    CREATOR("creator"),
    NAME("names.name"),
    ALIAS("alias"),
    KEYWORDS("keywords"),
    SUMMARY("shortDescription"),
    NOTES("longDescription"),
    CREATOR_EMAIL("creatorEmail"),

    INTELLECTUAL_PROPERTY("intellectualProperty"),
    REFERENCES("references"),
    PART_ID("partNumbers.partNumber"),
    LINK("links.link"),
    //    LINK_URL("links.url"),
    SELECTION_MARKER("selectionMarkers.name"),
    FUNDING_SOURCE("entryFundingSources.fundingSource.fundingSource"),
    PRINCIPAL_INVESTIGATOR("entryFundingSources.fundingSource.principalInvestigator"),

    // strain fields
    STRAIN_PLASMIDS("plasmids"),
    GENOTYPE_OR_PHENOTYPE("genotypePhenotype"),
    PARENT_STRAIN("host"),

    // plasmid fields
    BACKBONE("backbone"),
    PROMOTERS("promoters"),
    REPLICATES_IN("replicatesIn"),
    ORIGIN_OF_REPLICATION("originOfReplication"),

    // seed fields
    ECOTYPE("ecotype"),
    GENERATION("generation"),
    SEED_PARENTS("parents"),
    PLANT_TYPE("plantType");

    private String field; // actual field value. Should correspond to value in the SearchFieldFactory

    private SearchBoostField(String field) {
        this.field = field;
    }

    private SearchBoostField() {
    }

    public String getField() {
        return this.field;
    }

    public static HashSet<String> getFields() {
        HashSet<String> fields = new HashSet<String>();
        for (SearchBoostField boostField : SearchBoostField.values()) {
            fields.add(boostField.getField());
        }
        return fields;
    }

    /**
     * @param field index field
     * @return boost field associated with index field
     */
    public static SearchBoostField boostFieldForField(String field) {
        for (SearchBoostField boostField : SearchBoostField.values()) {
            if (field.equalsIgnoreCase(boostField.getField()))
                return boostField;
        }
        return null;
    }
}

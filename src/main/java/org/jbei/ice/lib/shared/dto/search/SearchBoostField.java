package org.jbei.ice.lib.shared.dto.search;

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
    NAME("name", 3f),
    ALIAS("alias", true),
    KEYWORDS("keywords", 1.8f),
    SUMMARY("shortDescription", true),
    NOTES("longDescription"),
    CREATOR_EMAIL("creatorEmail"),

    INTELLECTUAL_PROPERTY("intellectualProperty"),
    REFERENCES("references"),
    PART_ID("partNumber", 1.7f),
    LINK("links.link"),

    SELECTION_MARKER("selectionMarkers.name"),
    FUNDING_SOURCE("fundingSource"),
    PRINCIPAL_INVESTIGATOR("principalInvestigator"),

    // strain fields
    STRAIN_PLASMIDS("plasmids"),
    GENOTYPE_OR_PHENOTYPE("genotypePhenotype"),
    PARENT_STRAIN("host"),

    // plasmid fields
    BACKBONE("backbone", true),
    PROMOTERS("promoters"),
    REPLICATES_IN("replicatesIn"),
    ORIGIN_OF_REPLICATION("originOfReplication"),

    // seed fields
    ECOTYPE("ecotype"),
    GENERATION("generation"),
    SEED_PARENTS("parents"),
    PLANT_TYPE("plantType");

    private String field; // actual field value. Should correspond to value in the SearchFieldFactory
    private float defaultBoost;
    private boolean userBoostable;

    private SearchBoostField(String field) {
        this.field = field;
        this.defaultBoost = 1.0f;
        userBoostable = false;
    }

    private SearchBoostField(String field, float defaultBoost) {
        this.field = field;
        this.defaultBoost = defaultBoost;
        this.userBoostable = true;
    }

    private SearchBoostField(String field, boolean userBoostable) {
        this(field);
        this.userBoostable = userBoostable;
    }

    private SearchBoostField() {
    }

    public String getField() {
        return this.field;
    }

    public float getDefaultBoost() {
        return this.defaultBoost;
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

    public boolean isUserBoostable() {
        return userBoostable;
    }
}

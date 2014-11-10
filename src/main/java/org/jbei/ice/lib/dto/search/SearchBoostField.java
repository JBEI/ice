package org.jbei.ice.lib.dto.search;

/**
 * Search fields that have user boost support (shown in the user preference section)
 *
 * @author Hector Plahar
 */
public enum SearchBoostField {

    NAME("name"),
    ALIAS("alias"),
    KEYWORDS("keywords"),
    SUMMARY("shortDescription"),
    SELECTION_MARKER("selectionMarkers.name");

    private String field; // actual field value. Should correspond to value in the SearchFieldFactory

    private SearchBoostField(String field) {
        this.field = field;
    }

    public String getField() {
        return this.field;
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

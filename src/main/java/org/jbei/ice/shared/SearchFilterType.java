package org.jbei.ice.shared;

import org.jbei.ice.client.common.FilterOperand;
import org.jbei.ice.client.common.search.SearchFilterOperandFactory;

/**
 * Fields represented in the search filter pull down.
 * Users can search by each field type. Each type is
 * associated with an operator and optionally, operand.
 * These are retrieved with a factory
 * 
 * @author Hector Plahar
 */

// TODO : some of these are restricted to certain types of other filters
// TODO : constraints?
public enum SearchFilterType {

    CHOOSE("Choose One", "", false), NAME_OR_ALIAS("Name or Alias", "name", true), PART_NUMBER(
            "Part ID", "id", false), TYPE("Type", "type", false), STATUS("Status", "status", false), OWNER(
            "Owner", "owner", true), CREATOR("Creator", "creator", false), KEYWORDS("Keywords",
            "keywords", false), DESCRIPTION("Description (Summary/Notes/References)",
            "description", false), HAS_ATTACHMENT("Has Attachment", "has_attach", false), HAS_SEQUENCE(
            "Has Sequence", "", false), HAS_SAMPLE("Has Sample", "", false), BIO_SAFETY_LEVEL(
            "Bio Safety Level", "", false), INTELLECTUAL_PROPERTY("Intellectual Property", "ip",
            false), PRINCIPAL_INVESTIGATOR("Principal Investigator", "pi", false), FUNDING_SOURCE(
            "Funding Source", "", false), SELECTION_MARKER(
            "Selection Marker (Strains and Plasmids only)", "", false), BACKBONE(
            "Backbone (Plasmids only)", "", false), PROMOTERS("Promoters (Plasmids Only)", "",
            false), ORIGIN("Origin of Replication (Plasmids Only)", "", false), HOST(
            "Host (Strains Only)", "", false), STRAIN_PLASMIDS("Strain Plasmids (Strains only)",
            "", false), GEN_PHEN("Genotype/Phenotype (Strains only)", "", false), PACKAGE_FORMAT(
            "Package Format (Parts Only)", "", false), RECORD_ID("Record ID", "", false);

    private String displayName;
    private boolean composite;
    private String shortName;

    SearchFilterType(String displayName, String shortName, boolean isComposite,
            SearchFilterType... constraints) {
        this.displayName = displayName;
        this.composite = isComposite;
        this.shortName = shortName;
    }

    public String displayName() {
        return this.displayName;
    }

    @Override
    public String toString() {
        return this.displayName;
    }

    public boolean isComposite() {
        return this.composite;
    }

    public String getShortName() {
        return this.shortName;
    }

    public static SearchFilterType filterValueOf(String value) {
        try {
            return SearchFilterType.valueOf(value);
        } catch (IllegalArgumentException iae) {
            for (SearchFilterType type : SearchFilterType.values()) {
                if (value.equals(type.shortName) && !type.getShortName().isEmpty())
                    return type;
            }

            return null;
        }
    }

    /**
     * Any new search filter type that is added in here
     * needs to have an operand composite defined in the factory
     * 
     * @return The composite of operands for this search filter type
     * @see SearchFilterOperandFactory
     */
    public FilterOperand getOperatorAndOperands() {
        return SearchFilterOperandFactory.getOperand(this);
    }
}

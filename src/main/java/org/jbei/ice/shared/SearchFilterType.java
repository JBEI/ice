package org.jbei.ice.shared;

import java.util.ArrayList;
import java.util.Arrays;

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

    NAME_OR_ALIAS("Name or Alias", "name", true), PART_NUMBER("Part ID", "id", false), TYPE("Type",
            "type", false), STATUS("Status", "status", false), OWNER("Owner", "owner", true), CREATOR(
            "Creator", "creator", false), KEYWORDS("Keywords", "keywords", false), DESCRIPTION(
            "Description (Summary/Notes/References)", "description", false), HAS_ATTACHMENT(
            "Has Attachment", "has_attach", false), HAS_SEQUENCE("Has Sequence", "has_seq", false), HAS_SAMPLE(
            "Has Sample", "has_sample", false), BIO_SAFETY_LEVEL("Bio Safety Level",
            "safety_level", false), INTELLECTUAL_PROPERTY("Intellectual Property", "ip", false), PRINCIPAL_INVESTIGATOR(
            "Principal Investigator", "pi", false), FUNDING_SOURCE("Funding Source", "funding",
            false), SELECTION_MARKER("Selection Marker (Strains and Plasmids only)", "marker",
            false), BACKBONE("Backbone (Plasmids only)", "backbone", false), PROMOTERS(
            "Promoters (Plasmids Only)", "promoters", false), ORIGIN(
            "Origin of Replication (Plasmids Only)", "origin", false), HOST("Host (Strains Only)",
            "host", false), STRAIN_PLASMIDS("Strain Plasmids (Strains only)", "plasmids", false), GEN_PHEN(
            "Genotype/Phenotype (Strains only)", "gen_phen", false), PACKAGE_FORMAT(
            "Package Format (Parts Only)", "format", false), RECORD_ID("Record ID", "rid", false), BLAST(
            "Blast", "blast", false);

    private String displayName;
    private boolean composite;
    private String shortName;
    private ArrayList<SearchFilterType> constraints;

    SearchFilterType(String displayName, String shortName, boolean isComposite,
            SearchFilterType... constraints) {
        this.displayName = displayName;
        this.composite = isComposite;
        this.shortName = shortName;
        this.constraints = new ArrayList<SearchFilterType>();
        if (constraints != null)
            this.constraints.addAll(Arrays.asList(constraints));
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

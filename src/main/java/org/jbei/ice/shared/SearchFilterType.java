package org.jbei.ice.shared;

import org.jbei.ice.shared.dto.EntryType;

/**
 * Fields represented in the search filter pull down.
 * Users can search by each field type. Each type is
 * associated with an operator and optionally, operand.
 * These are retrieved with a factory
 *
 * @author Hector Plahar
 */
public enum SearchFilterType {

    IDENTIFIER("Identifier", "identifier"),     // name, alias, part_number, rid, part_id
    HAS_ATTACHMENT("Has Attachment", "has_attach"),
    HAS_SEQUENCE("Has Sequence", "has_seq"),
    HAS_SAMPLE("Has Sample", "has_sample"),
    DESCRIPTION("Description", "description"),  // keywords, intellectual property, summary, notes, references
    STATUS("Status", "status"),
    OWNER("Owner", "owner"),
    CREATOR("Creator", "creator"),
    BIO_SAFETY_LEVEL("Bio Safety Level", "safety_level"),
    PRINCIPAL_INVESTIGATOR("Principal Investigator", "pi"),
    FUNDING_SOURCE("Funding Source", "funding"),
    SELECTION_MARKER("Selection Marker", "marker"),
    BACKBONE("Backbone", "backbone"),
    PROMOTERS("Promoters", "promoters"),
    ORIGIN("Origin of Replication", "origin"),
    HOST("Host", "host", EntryType.STRAIN),
    STRAIN_PLASMIDS("Strain Plasmids", "plasmids"),
    GEN_PHEN("Genotype/Phenotype", "gen_phen"),
    PACKAGE_FORMAT("Package Format", "format"),
    BLAST("Blast", "blast");

    private String displayName;
    private String shortName;
    private EntryType[] entryType;

    SearchFilterType(String displayName, String shortName, EntryType... entryType) {
        this.displayName = displayName;
        this.shortName = shortName;
        this.entryType = entryType;
    }

    public String displayName() {
        return this.displayName;
    }

    @Override
    public String toString() {
        return this.displayName;
    }

    public String getShortName() {
        return this.shortName;
    }

    public EntryType[] getEntryRestrictions() {
        return this.entryType;
    }

    public static SearchFilterType filterValueOf(String value) {
        if (value == null)
            return null;

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
}

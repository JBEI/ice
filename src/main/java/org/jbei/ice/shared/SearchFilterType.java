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

    IDENTIFIER("Identifier", "identifier", EntryType.values()),
    HAS_ATTACHMENT("Has Attachment", "has_attach", EntryType.values()),
    HAS_SEQUENCE("Has Sequence", "has_seq", EntryType.values()),
    HAS_SAMPLE("Has Sample", "has_sample", EntryType.values()),
    DESCRIPTION("Description", "description", EntryType.values()),
    STATUS("Status", "status", EntryType.values()),
    OWNER("Owner", "owner", EntryType.values()),
    CREATOR("Creator", "creator", EntryType.values()),
    BIO_SAFETY_LEVEL("Bio Safety Level", "safety_level", EntryType.values()),
    PRINCIPAL_INVESTIGATOR("Principal Investigator", "pi", EntryType.values()),
    FUNDING_SOURCE("Funding Source", "funding", EntryType.values()),

    SELECTION_MARKER("Selection Marker", "marker", EntryType.STRAIN, EntryType.PLASMID),
    BACKBONE("Backbone", "backbone", EntryType.PLASMID),
    PROMOTERS("Promoters", "promoters", EntryType.PLASMID),
    ORIGIN("Origin of Replication", "origin", EntryType.PLASMID),
    HOST("Host", "host", EntryType.STRAIN),
    STRAIN_PLASMIDS("Strain Plasmids", "plasmids", EntryType.STRAIN),
    GEN_PHEN("Genotype/Phenotype", "gen_phen", EntryType.STRAIN),
    //    PACKAGE_FORMAT("Package Format", "format", EntryType.PART),
    BLAST("Blast", "blast", EntryType.values());

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

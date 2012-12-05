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

    IDENTIFIER("Identifier", "id", EntryType.values()),
    HAS_ATTACHMENT("Has Attachment", "ha", EntryType.values()),
    HAS_SEQUENCE("Has Sequence", "hse", EntryType.values()),
    HAS_SAMPLE("Has Sample", "hsa", EntryType.values()),
    DESCRIPTION("Description", "des", EntryType.values()),
    STATUS("Status", "st", EntryType.values()),
    OWNER("Owner", "o", EntryType.values()),
    CREATOR("Creator", "c", EntryType.values()),
    BIO_SAFETY_LEVEL("Bio Safety Level", "sl", EntryType.values()),
    PRINCIPAL_INVESTIGATOR("Principal Investigator", "pi", EntryType.values()),
    FUNDING_SOURCE("Funding Source", "f", EntryType.values()),

    SELECTION_MARKER("Selection Marker", "sm", EntryType.STRAIN, EntryType.PLASMID),
    BACKBONE("Backbone", "b", EntryType.PLASMID),
    PROMOTERS("Promoters", "p", EntryType.PLASMID),
    ORIGIN("Origin of Replication", "or", EntryType.PLASMID),
    HOST("Host", "h", EntryType.STRAIN),
    STRAIN_PLASMIDS("Strain Plasmids", "spl", EntryType.STRAIN),
    GEN_PHEN("Genotype/Phenotype", "gp", EntryType.STRAIN),
    //    PACKAGE_FORMAT("Package Format", "format", EntryType.PART),
    BLAST("Blast", "bl", EntryType.values());

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

    public static SearchFilterType stringToSearchType(String value) {
        for (SearchFilterType type : SearchFilterType.values()) {
            if (type.getShortName().equals(type))
                return type;
        }

        return null;
    }
}

package org.jbei.ice.shared;

/**
 * Fields represented in the search filter pull down.
 * Users can search by each field type. Each type is
 * associated with an operator and optionally, operand.
 * These are retrieved with a factory
 *
 * @author Hector Plahar
 */
public enum SearchFilterType {

    IDENTIFIER("Identifier", "identifier"),
    NAME("Name", "name"),
    ALIAS("Alias", "alias"),
    PART_ID("Part ID", "part_id"),

    NAME_OR_ALIAS("Name or Alias", "name"),
    PART_NUMBER("Part ID", "id"),
    TYPE("Type", "type"),
    STATUS("Status", "status"),
    OWNER("Owner", "owner"),
    CREATOR("Creator", "creator"),
    KEYWORDS("Keywords", "keywords"),
    DESCRIPTION("Description", "description"),
    HAS_ATTACHMENT("Has Attachment", "has_attach"),
    HAS_SEQUENCE("Has Sequence", "has_seq"),
    HAS_SAMPLE("Has Sample", "has_sample"),
    BIO_SAFETY_LEVEL("Bio Safety Level", "safety_level"),
    INTELLECTUAL_PROPERTY("Intellectual Property", "ip"),
    PRINCIPAL_INVESTIGATOR("Principal Investigator", "pi"),
    FUNDING_SOURCE("Funding Source", "funding"),
    SELECTION_MARKER("Selection Marker", "marker"),
    BACKBONE("Backbone", "backbone"),
    PROMOTERS("Promoters", "promoters"),
    ORIGIN("Origin of Replication (Plasmids Only)", "origin"),
    HOST("Host", "host"),
    STRAIN_PLASMIDS("Strain Plasmids", "plasmids"),
    GEN_PHEN("Genotype/Phenotype", "gen_phen"),
    PACKAGE_FORMAT("Package Format", "format"),
    RECORD_ID("Record ID", "rid"),
    BLAST("Blast", "blast");

    private String displayName;
    private String shortName;

    SearchFilterType(String displayName, String shortName) {
        this.displayName = displayName;
        this.shortName = shortName;
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

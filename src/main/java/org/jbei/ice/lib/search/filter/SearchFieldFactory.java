package org.jbei.ice.lib.search.filter;

import org.jbei.ice.lib.entry.model.*;
import org.jbei.ice.shared.dto.entry.EntryType;

import java.util.HashSet;

/**
 * @author Hector Plahar
 */
public class SearchFieldFactory {

    private static HashSet<String> commonFields = new HashSet<>();
    private static HashSet<String> strainFields = new HashSet<>();
    private static HashSet<String> plasmidFields = new HashSet<>();
    private static HashSet<String> seedFields = new HashSet<>();

    static {
        commonFields.add("owner");
        commonFields.add("ownerEmail");
        commonFields.add("creator");
        commonFields.add("names.name");
        commonFields.add("alias");
        commonFields.add("creatorEmail");
        commonFields.add("keywords");
        commonFields.add("shortDescription");
        commonFields.add("longDescription");
        commonFields.add("intellectualProperty");
        commonFields.add("references");
        commonFields.add("partNumbers.partNumber");
        commonFields.add("links.link");
        commonFields.add("links.url");
        commonFields.add("selectionMarkers.name");
        commonFields.add("entryFundingSources.fundingSource.fundingSource");
        commonFields.add("entryFundingSources.fundingSource.principalInvestigator");

        // strain fields
        strainFields.add("plasmids");
        strainFields.add("genotypePhenotype");
        strainFields.add("host");

        // plasmid fields
        plasmidFields.add("backbone");
        plasmidFields.add("promoters");
        plasmidFields.add("originOfReplication");

        // seed fields
        seedFields.add("ecotype");
        seedFields.add("generation");
        seedFields.add("parents");
        seedFields.add("plantType");
    }

    public static HashSet<String> getCommonFields() {
        return commonFields;
    }

    public static HashSet<String> entryFields(EntryType type) {
        switch (type) {
            case STRAIN:
                return strainFields;

            case PLASMID:
                return plasmidFields;

            case ARABIDOPSIS:
                return seedFields;

            default:
                return commonFields;
        }
    }

    public static Class<?> entryClass(EntryType type) {
        switch (type) {
            case STRAIN:
                return Strain.class;

            case PLASMID:
                return Plasmid.class;

            case ARABIDOPSIS:
                return ArabidopsisSeed.class;

            case PART:
                return Part.class;

            default:
                return Entry.class;
        }
    }
}

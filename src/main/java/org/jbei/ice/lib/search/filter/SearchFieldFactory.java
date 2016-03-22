package org.jbei.ice.lib.search.filter;

import org.jbei.ice.lib.dto.entry.EntryField;
import org.jbei.ice.lib.dto.entry.EntryType;
import org.jbei.ice.storage.model.ArabidopsisSeed;
import org.jbei.ice.storage.model.Part;
import org.jbei.ice.storage.model.Plasmid;
import org.jbei.ice.storage.model.Strain;

import java.util.HashSet;
import java.util.List;

/**
 * Maintains information about the particular fields for the entry types (not exactly a factory in that regard)
 * and also the class types for hibernate search
 *
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
        commonFields.add("name");
        commonFields.add("alias");
        commonFields.add("creatorEmail");
        commonFields.add("keywords");
        commonFields.add("shortDescription");
        commonFields.add("longDescription");
        commonFields.add("intellectualProperty");
        commonFields.add("references");
        commonFields.add("partNumber");
        commonFields.add("links.link");
        commonFields.add("links.url");
        commonFields.add("selectionMarkers.name");
        commonFields.add("parameters.value");
        commonFields.add("fundingSource");
        commonFields.add("principalInvestigator");

        // strain fields
        strainFields.add("plasmids");
        strainFields.add("genotypePhenotype");
        strainFields.add("host");

        // plasmid fields
        plasmidFields.add("backbone");
        plasmidFields.add("promoters");
        plasmidFields.add("replicatesIn");
        plasmidFields.add("originOfReplication");

        // seed fields
        seedFields.add("ecotype");
        seedFields.add("generation");
        seedFields.add("parents");
        seedFields.add("plantType");
    }

    public static String[] getCommonFields() {
        return commonFields.toArray(new String[commonFields.size()]);
    }

    public static HashSet<String> entryFields(List<EntryType> types) {
        HashSet<String> fields = new HashSet<>();

        for (EntryType type : types) {
            switch (type) {
                case STRAIN:
                    fields.addAll(strainFields);
                    break;

                case PLASMID:
                    fields.addAll(plasmidFields);
                    break;

                case ARABIDOPSIS:
                    fields.addAll(seedFields);
                    break;
            }
        }

        fields.addAll(commonFields);
        return fields;
    }

    public static Class<?>[] classesForTypes(List<EntryType> types) {
        if (types == null || types.isEmpty())
            return new Class<?>[0];

        Class<?>[] classes = new Class<?>[types.size()];

        for (int i = 0; i < types.size(); i += 1) {
            switch (types.get(i)) {
                case STRAIN:
                    classes[i] = Strain.class;
                    break;

                case PLASMID:
                    classes[i] = Plasmid.class;
                    break;

                case ARABIDOPSIS:
                    classes[i] = ArabidopsisSeed.class;
                    break;

                case PART:
                    classes[i] = Part.class;
                    break;
            }
        }
        return classes;
    }

    /**
     * Determines the search field for the specified entry field
     *
     * @param entryField entry field
     * @return corresponding lucene search field for defined entry field; empty string if no field is found
     */
    public static String searchFieldForEntryField(EntryField entryField) {
        if (entryField == null)
            return "";

        switch (entryField) {
            default:
                return "";

            case NAME:
                return "name";

            case ALIAS:
                return "alias";

            case EXISTING_PART_NUMBER:
            case PART_NUMBER:
                return "partNumber";
        }
    }
}

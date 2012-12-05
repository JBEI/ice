package org.jbei.ice.lib.search.filter;

import org.jbei.ice.lib.entry.model.ArabidopsisSeed;
import org.jbei.ice.lib.entry.model.Entry;
import org.jbei.ice.lib.entry.model.Part;
import org.jbei.ice.lib.entry.model.Plasmid;
import org.jbei.ice.lib.entry.model.Strain;
import org.jbei.ice.shared.dto.EntryType;

/**
 * @author Hector Plahar
 */
public class SearchFieldFactory {

    public static String[] entryFields(EntryType type) {
        String[] fields;

        switch (type) {
            case STRAIN:
                fields = new String[]{"owner", "creator", "names.name", "alias", "creator", "keywords",
                        "shortDescription", "references", "longDescription", "intellectualProperty", "host", "plasmids",
                        "genotypePhenotype"
                };
                break;

            case PLASMID:
                fields = new String[]{"owner", "creator", "names.name", "alias", "creator", "keywords",
                        "shortDescription", "references", "longDescription", "intellectualProperty", "backbone"
                };
                break;

            case ARABIDOPSIS:
                fields = new String[]{"owner", "creator", "names.name", "alias", "creator", "keywords",
                        "shortDescription", "references", "longDescription", "intellectualProperty"
                };
                break;

            case PART:
                fields = new String[]{"owner", "creator", "names.name", "alias", "creator", "keywords",
                        "shortDescription", "references", "longDescription", "intellectualProperty"
                };
                break;

            default:
                fields = new String[]{};
        }

        return fields;
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

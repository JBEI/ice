package org.jbei.ice.lib.entry;


import org.jbei.ice.lib.dto.entry.EntryType;
import org.jbei.ice.lib.entry.model.ArabidopsisSeed;
import org.jbei.ice.lib.entry.model.Entry;
import org.jbei.ice.lib.entry.model.Part;
import org.jbei.ice.lib.entry.model.Plasmid;
import org.jbei.ice.lib.entry.model.Strain;

/**
 * Utility class for operating on entries
 *
 * @author Hector Plahar
 */
public class EntryUtil {

    public static Entry createEntryFromType(EntryType type, String name, String email) {
        Entry entry;

        switch (type) {
            case PLASMID:
                entry = new Plasmid();
                break;
            case STRAIN:
                entry = new Strain();
                break;
            case PART:
                entry = new Part();
                break;
            case ARABIDOPSIS:
                entry = new ArabidopsisSeed();
                break;

            default:
                return null;
        }

        entry.setOwner(name);
        entry.setOwnerEmail(email);
        entry.setCreator(name);
        entry.setCreatorEmail(email);
        return entry;
    }
}

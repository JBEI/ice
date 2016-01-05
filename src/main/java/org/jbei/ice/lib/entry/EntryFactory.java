package org.jbei.ice.lib.entry;

import org.jbei.ice.lib.dto.entry.EntryType;
import org.jbei.ice.storage.model.*;

/**
 * Factory class for creating new entry object depending on type
 *
 * @author Hector Plahar
 */
public class EntryFactory {

    public static Entry buildEntry(EntryType type) {
        switch (type) {
            case PLASMID:
                return new Plasmid();

            case STRAIN:
                return new Strain();

            case ARABIDOPSIS:
                return new ArabidopsisSeed();

            default:
                return new Part();
        }
    }
}

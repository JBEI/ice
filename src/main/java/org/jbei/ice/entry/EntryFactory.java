package org.jbei.ice.entry;

import org.jbei.ice.dto.entry.EntryType;
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

            case SEED:
                return new ArabidopsisSeed();

            case PROTEIN:
                return new Protein();

            default:
                return new Part();
        }
    }
}

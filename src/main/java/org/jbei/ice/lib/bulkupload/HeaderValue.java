package org.jbei.ice.lib.bulkupload;

import org.jbei.ice.lib.dto.bulkupload.EntryField;

/**
 * Value of header
 *
 * @author Hector Plahar
 */
public class HeaderValue {
    private boolean isSubType;
    private EntryField entryField;

    public HeaderValue(boolean subType, EntryField entryField) {
        this.isSubType = subType;
        this.entryField = entryField;
    }

    public boolean isSubType() {
        return isSubType;
    }

    public EntryField getEntryField() {
        return entryField;
    }
}

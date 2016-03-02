package org.jbei.ice.lib.bulkupload;

import org.jbei.ice.lib.dto.entry.EntryField;

/**
 * @author Hector Plahar
 */
public class EntryHeaderValue implements HeaderValue {

    private boolean isSubType;
    private EntryField entryField;

    public EntryHeaderValue(boolean subType, EntryField entryField) {
        this.isSubType = subType;
        this.entryField = entryField;
    }

    public boolean isSubType() {
        return isSubType;
    }

    public EntryField getEntryField() {
        return entryField;
    }

    @Override
    public boolean isSampleField() {
        return false;
    }
}

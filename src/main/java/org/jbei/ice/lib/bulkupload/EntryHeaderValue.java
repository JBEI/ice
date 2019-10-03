package org.jbei.ice.lib.bulkupload;

import org.jbei.ice.lib.dto.entry.EntryField;

/**
 * @author Hector Plahar
 */
public class EntryHeaderValue implements HeaderValue {

    private boolean isSubType;
    private boolean isCustom;
    private EntryField entryField;
    private String label;

    EntryHeaderValue(EntryField entryField, boolean subType, boolean isCustom, String value) {
        this.isSubType = subType;
        this.entryField = entryField;
        this.isCustom = isCustom;
        this.label = value;
    }

    EntryHeaderValue(EntryField entryField) {
        this.entryField = entryField;
    }

    boolean isSubType() {
        return isSubType;
    }

    EntryField getEntryField() {
        return entryField;
    }

    @Override
    public boolean isSampleField() {
        return false;
    }

    @Override
    public boolean isCustomField() {
        return isCustom;
    }

    public void setCustom(boolean custom) {
        isCustom = custom;
    }

    public String getLabel() {
        return label;
    }
}

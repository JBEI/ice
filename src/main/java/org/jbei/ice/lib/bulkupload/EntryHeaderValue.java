package org.jbei.ice.lib.bulkupload;

import org.jbei.ice.lib.dto.entry.EntryFieldLabel;

/**
 * @author Hector Plahar
 */
public class EntryHeaderValue implements HeaderValue {

    private boolean isSubType;
    private boolean isCustom;
    private EntryFieldLabel entryFieldLabel;
    private String label;

    EntryHeaderValue(EntryFieldLabel entryFieldLabel, boolean subType, boolean isCustom, String value) {
        this.isSubType = subType;
        this.entryFieldLabel = entryFieldLabel;
        this.isCustom = isCustom;
        this.label = value;
    }

    EntryHeaderValue(EntryFieldLabel entryFieldLabel) {
        this.entryFieldLabel = entryFieldLabel;
    }

    boolean isSubType() {
        return isSubType;
    }

    EntryFieldLabel getEntryFieldLabel() {
        return entryFieldLabel;
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

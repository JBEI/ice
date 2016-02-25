package org.jbei.ice.lib.dto.bulkupload;

import org.jbei.ice.storage.IDataTransferModel;

/**
 * DTO for preferences
 *
 * @author Hector Plahar
 */
public class PreferenceInfo implements IDataTransferModel {

    private String key;
    private String value;
    private boolean isAdd;

    public PreferenceInfo() {
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public boolean isAdd() {
        return isAdd;
    }

    public void setAdd(boolean add) {
        isAdd = add;
    }

    @Override
    public String toString() {
        return "(add=" + isAdd + ", key:" + key + ", value:" + value + ")";
    }
}

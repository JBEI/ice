package org.jbei.ice.client.bulkupload.model;

import org.jbei.ice.lib.shared.dto.bulkupload.EntryField;

// wrapper around entered data to enable sending more info 
// from the sheet. e.g. file input has the uploaded id
// and the display name

public class SheetCellData {

    private EntryField type;
    private String id;     // used for file id
    private String value;

    public SheetCellData(EntryField type, String id, String value) {
        this.type = type;
        this.id = id;
        this.value = value;
    }

    public SheetCellData() {
    }

    public EntryField getTypeHeader() {
        return type;
    }

    public String getId() {
        return id;
    }

    public String getValue() {
        return value;
    }

    public void setType(EntryField type) {
        this.type = type;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "(type=" + type + ", id=" + id + ", value=" + value + ")";
    }
}

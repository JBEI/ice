package org.jbei.ice.client.bulkimport.model;

import org.jbei.ice.client.bulkimport.sheet.Header;

// wrapper around entered data to enable sending more info 
// from the sheet. e.g. file input has the uploaded id
// and the display name

public class SheetCellData {

    private Header type;
    private String id;
    private String value;

    public SheetCellData(Header type, String id, String value) {
        this.type = type;
        this.id = id;
        this.value = value;
    }

    public SheetCellData() {
    }

    public Header getTypeHeader() {
        return type;
    }

    public String getId() {
        return id;
    }

    public String getValue() {
        return value;
    }

    public void setType(Header type) {
        this.type = type;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setValue(String value) {
        this.value = value;
    }
}

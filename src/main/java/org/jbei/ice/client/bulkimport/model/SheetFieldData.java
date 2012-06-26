package org.jbei.ice.client.bulkimport.model;

import org.jbei.ice.client.bulkimport.sheet.Header;

// wrapper around entered data to enable sending more info 
// from the sheet. e.g. file input has the uploaded id
// and the display name

public class SheetFieldData {

    private final Header type;
    private final String id;
    private final String value;

    public SheetFieldData(Header type, String id, String value) {
        this.type = type;
        this.id = id;
        this.value = value;
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
}

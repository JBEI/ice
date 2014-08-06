package org.jbei.ice.lib.dto.entry;

import org.jbei.ice.lib.dao.IDataTransferModel;

/**
 * User created fields for entry
 *
 * @author Hector Plahar
 */
public class CustomField implements IDataTransferModel {

    private String name;
    private String value;

    public CustomField() {
    }

    public CustomField(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}

package org.jbei.ice.client.admin.setting;

import org.jbei.ice.lib.shared.dto.ConfigurationKey;

/**
 * @author Hector Plahar
 */
public class RowData {

    private ConfigurationKey key;
    private int row;
    private String value;

    public ConfigurationKey getKey() {
        return key;
    }

    public void setKey(ConfigurationKey key) {
        this.key = key;
    }

    public int getRow() {
        return row;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}

package org.jbei.ice.client.profile.preferences;

import org.jbei.ice.shared.dto.user.PreferenceKey;

/**
 * @author Hector Plahar
 */
public class RowData {

    private PreferenceKey key;
    private int row;
    private String value;

    public PreferenceKey getKey() {
        return key;
    }

    public void setKey(PreferenceKey key) {
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

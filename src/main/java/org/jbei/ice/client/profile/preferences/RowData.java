package org.jbei.ice.client.profile.preferences;

import org.jbei.ice.shared.dto.search.SearchBoostField;
import org.jbei.ice.shared.dto.user.PreferenceKey;

/**
 * Wrapper around entry defaults and search settings
 *
 * @author Hector Plahar
 */
public class RowData {

    private PreferenceKey key;
    private SearchBoostField field;
    private int row;
    private String value;
    private int section; // section in the panel that should be updated

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

    public SearchBoostField getField() {
        return field;
    }

    public void setField(SearchBoostField field) {
        this.field = field;
    }

    public int getSection() {
        return section;
    }

    public void setSection(int section) {
        this.section = section;
    }
}
